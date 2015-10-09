package com.huawei.meeting.func;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.huawei.meeting.ConfDefines;
import com.huawei.meeting.ConfExtendMsg;
import com.huawei.meeting.ConfExtendUserDataMsg;
import com.huawei.meeting.ConfExtendUserInfoMsg;
import com.huawei.meeting.ConfExtendVideoDeviceInfoMsg;
import com.huawei.meeting.ConfExtendVideoParamMsg;
import com.huawei.meeting.ConfGLView;
import com.huawei.meeting.ConfInfo;
import com.huawei.meeting.ConfInstance;
import com.huawei.meeting.ConfInstance.AudioParam;
import com.huawei.meeting.ConfMsg;
import com.huawei.meeting.ConfOper;
import com.huawei.meeting.ConfPrew;
import com.huawei.meeting.ConfResult;
import com.huawei.meeting.Conference;
import com.huawei.meeting.IConferenceUI;
import com.huawei.meeting.MsgID;
import com.huawei.videoengine.ViERenderer;
import com.huawei.vtm.NotifyID;
import com.huawei.vtm.NotifyMsg;
import com.huawei.vtm.common.AccountInfo;
import com.huawei.vtm.common.CameraInfo;
import com.huawei.vtm.common.Constants;
import com.huawei.vtm.common.Constants.CALLMODE;
import com.huawei.vtm.common.SystemSetting;
import com.huawei.vtm.common.UserInfo;
import com.huawei.vtm.common.VideoParam;
import com.huawei.vtm.service.HandReceiverUtil;
import com.huawei.vtm.service.VTMApp;
import com.huawei.vtm.utils.LogUtils;
import com.huawei.vtm.utils.Tools;

/**
 * 处理会议相关操作的类
 */
public class ConferenceMgr implements IConferenceUI {

	private static final String TAG = "ConferenceMgr";

	/** 共享状态-停止 **/
	public static final int CONF_SHARED_STOP = 0;

	/** 共享状态-开始 **/
	public static final int CONF_SHARED_START = 1;

	private static ConferenceMgr instance;

	private ConfInstance conf;

	public synchronized static ConferenceMgr getInstance() {
		if (instance == null) {
			instance = new ConferenceMgr();
		}

		return instance;
	}

	private ConferenceMgr() {
	}

	/** 会议预览摄像头开启标识 **/
	private boolean captureFlag = false;
	/**转移开启本地摄像头控制**/
	private boolean turnCaptureFlag = false;
	/**转移匿名呼叫时显示视频控制**/
	private boolean turnOpenLocalFlag = false;

	public boolean isCaptureFlag() {
		return captureFlag;
	}

	public void setCaptureFlag(boolean captureFlag) {
		this.captureFlag = captureFlag;
	}

	/** 控制心跳的Timer **/
	private Timer mytimer;

	/** 控制心跳的Handler **/
	private Handler mheartBeatHandler;

	private WorkThread confThread;

	private Semaphore confThreadStartSemaphore;

	/** 用于释放会议的Timer **/
	private Timer releaseConfTimer;

	private Handler mconfHandler;

	/** 主线程 ID **/
	private long mMainThreadID;

	/** 会议句柄 **/
	private int confHandle = 0;

	/** 用于存储个用户的视频能力 **/
	private Map<String, List<VideoParam>> videoParamsMap = new HashMap<String, List<VideoParam>>();

	/** 显示本地视频的 SurfaceView **/
	private SurfaceView svLocalSurfaceView;
	
	
	private SurfaceView localSurfaceView;//0827

	/** 显示对端视频的 SurfaceView **/
	private SurfaceView remoteSurfaceView;

	/** 用于装载本地视频的 ViewGroup **/
	private ViewGroup mLocalContainer;

	/** 用于装载远端视频的 ViewGroup **/
	private ViewGroup mRemoteContainer;

	/** 显示远端共享屏幕的 SurfaceView **/
	private ConfGLView desktopSurfaceView;

	/** 显示远端共享文档的 SurfaceView **/
	private ConfGLView docSurfaceView;

	/** 用于装载远端共享屏幕的 ViewGroup **/
	private ViewGroup mDesktopViewContainer;

	/** 用于装载远端共享文档的 ViewGroup **/
	private ViewGroup mDocViewContainer;

	/** 当前共享文档的数量 **/
	private int dscurrentDocCount = 0;

	/** 当前共享文档的ID **/
	private int dscurrentDocID = 0;

	/** 当前共享文档的页码 **/
	private int dscurrentPageID = 0;

	public void initConf() {
		mytimer = new Timer();
		mytimer.schedule(new TimerTask() {
			@Override
			public void run() {
				Message m = new Message();
				m.what = 0;
				mheartBeatHandler.sendMessage(m);
			}
		}, 200, 100);

		mheartBeatHandler = new Handler() {
			public void handleMessage(Message msg) {
				// super.handleMessage(msg);
				heartBeat();
			}
		};

		mMainThreadID = Looper.getMainLooper().getThread().getId();

		confThreadStartSemaphore = new Semaphore(0);
		confThread = new WorkThread();
		confThread.start();
		confThreadStartSemaphore.acquireUninterruptibly();

		mconfHandler = confThread.getHandler();
	}

	/**
	 * 设置视频视图SurfaceView的装载容器ViewGroup
	 * 
	 * @param context
	 *            上下文
	 * @param localView
	 *            显示本地视频的ViewGroup
	 * @param remoteView
	 *            显示远端视频的ViewGroup
	 */
	public void setVideoContainer(Context context, ViewGroup localView,
			ViewGroup remoteView) {
		if (!AccountInfo.getInstance().isAnonymous()) {
			if (null == mLocalContainer) {
				mLocalContainer = localView;
			}
			if (null == svLocalSurfaceView) {
				svLocalSurfaceView = ViERenderer.CreateLocalRenderer(context);
			}
			if (svLocalSurfaceView != null) {
				mLocalContainer.removeView(svLocalSurfaceView);
				mLocalContainer.addView(svLocalSurfaceView);
			}
		}
		
		//0827
		if(null == localSurfaceView){
			localSurfaceView = ViERenderer.CreateRenderer(context, true);
		}

		if (null == mRemoteContainer) {
			mRemoteContainer = remoteView;
		}

		if (null == remoteSurfaceView) {
			remoteSurfaceView = ViERenderer.CreateRenderer(context, true);
		}

		if (remoteSurfaceView != null) {
			mRemoteContainer.removeView(remoteSurfaceView);
			mRemoteContainer.addView(remoteSurfaceView);
		}
	}

	// 0814
	public void setConfContainer(Context context, ViewGroup localView) {
		if (null == mLocalContainer) {
			mLocalContainer = localView;
		}

		if (null == svLocalSurfaceView) {
			svLocalSurfaceView = ViERenderer.CreateLocalRenderer(context);
			if (svLocalSurfaceView != null) {
				// 向导属于会议部分，打开日志
				String logFile = Environment.getExternalStorageDirectory()
						.toString() + File.separator + Constants.VTM_LOG_FILE;

				File dirFile = new File(logFile);
				if (!(dirFile.exists()) && !(dirFile.isDirectory())) {
					if (dirFile.mkdir()) {
						LogUtils.d("Conference", "mkdir " + dirFile.getPath());
					}
				}
				Conference.getInstance().setLogLevel(3, 3);
				Conference.getInstance().setPath(logFile, logFile);
				Conference.getInstance().initSDK(false, 4);

				LogUtils.d("ConferenceMgr setConfContainer deviceInfo::");

//				int deviceId = 1;//前置摄像头
//				List<TupDevice> tupDevices = CallManager.getInstance()
//						.getDeviceVideoInfo();
//				for (TupDevice tupDevice : tupDevices) {
//					deviceId = tupDevice.getIndex();
//					String deviceName = tupDevice.getStrName();
//					LogUtils.d("tupdevice deviceId:" + deviceId
//							+ " ,deviceName:" + deviceName);
//					if (deviceName.contains("front")) {
//						break;
//					}
//				}
//				int i = ConfPrew.getInstance().confSetCallback();
//				LogUtils.d("ConfPrew  confSetCallback  result:" + i);
				int handle = ViERenderer.getIndexOfSurface(svLocalSurfaceView);
				int fl = ConfPrew.getInstance().videoWizStartcapture(1,
						320, 240, 15, handle);
				LogUtils.d("ConfPrew  videoWizStartcapture result:" + fl);
				if (fl == 0) {
					captureFlag = true;
					setCaptureFlag(true);
				}
			}

		}

		if (svLocalSurfaceView != null) {
			mLocalContainer.removeView(svLocalSurfaceView);
			mLocalContainer.addView(svLocalSurfaceView);

		}
	}

	/**
	 * 设置显示共享的容器
	 * 
	 * @param context
	 * @param sharedView
	 * @param sharedType
	 *            数据共享类型 参考 {@link ConfConstants.SHARED_TYPE}
	 */
	public void setSharedViewContainer(Context context, ViewGroup sharedView,
			int sharedType) {
		if (ConfDefines.IID_COMPONENT_AS == sharedType) {
			mDesktopViewContainer = sharedView;
			mDesktopViewContainer.removeAllViews();
			desktopSurfaceView = new ConfGLView(context);
			desktopSurfaceView.setConf(conf);
			desktopSurfaceView.setViewType(sharedType);
			mDesktopViewContainer.addView(desktopSurfaceView);
			desktopSurfaceView.onResume();
			desktopSurfaceView.setVisibility(View.VISIBLE);
		} else if (ConfDefines.IID_COMPONENT_DS == sharedType) {
			mDocViewContainer = sharedView;
			mDocViewContainer.removeAllViews();
			docSurfaceView = new ConfGLView(context);
			docSurfaceView.setConf(conf);
			docSurfaceView.setViewType(sharedType);
			mDocViewContainer.addView(docSurfaceView);
			docSurfaceView.onResume();
			docSurfaceView.setVisibility(View.VISIBLE);
		} else {
			LogUtils.d(TAG, "setSharedViewContainer | sharedType = "
					+ sharedType + " not support type");
		}
	}

	/**
	 * 释放共享容器
	 */
	public void releaseShareView() {
		releaseDesktopShareView();
		releaseDocShareView();
	}

	/**
	 * 释放桌面共享容器
	 */
	public void releaseDesktopShareView() {
		if (desktopSurfaceView != null && mDesktopViewContainer != null) {
			desktopSurfaceView.onPause();
			mDesktopViewContainer.removeView(desktopSurfaceView);
			mDesktopViewContainer.removeAllViews();
			mDesktopViewContainer.invalidate();
			desktopSurfaceView = null;
		}
	}

	/**
	 * 释放文档共享容器
	 */
	public void releaseDocShareView() {
		if (docSurfaceView != null && mDocViewContainer != null) {
			docSurfaceView.onPause();
			mDocViewContainer.removeView(docSurfaceView);
			mDocViewContainer.removeAllViews();
			mDocViewContainer.invalidate();
			docSurfaceView = null;
		}

		dscurrentDocCount = 0;
		dscurrentDocID = 0;
		dscurrentPageID = 0;
	}

	/**
	 * 更新视频显示
	 * 
	 * @param userid
	 * @param deviceid
	 */
	public void updateVideoView(int userid, String deviceid) {
		LogUtils.d(TAG, "updateVideoView");
		ViewGroup viewGroup = null;
		SurfaceView surfaceView = null;
		// int IndexWnd = 0;
		if (String.valueOf(userid).equals(
				AccountInfo.getInstance().getSelfUserInfo().getUserId())) {
			viewGroup = mLocalContainer;
			if (svLocalSurfaceView != null) {
				surfaceView = svLocalSurfaceView;
			}
		} else {
			viewGroup = mRemoteContainer;
			if (remoteSurfaceView != null) {
				surfaceView = remoteSurfaceView;
			}
		}
		if (surfaceView == null) {
			return;
		}
		LogUtils.d(TAG, "updateVideoView | videoDetach succeed");
		viewGroup.removeAllViews();
		LogUtils.d(TAG, "updateVideoView | videoAttach succeed");
		viewGroup.addView(surfaceView);
	}

	/**
	 * 释放会议
	 */
	public void releaseConf() {
		mMainThreadID = 0;
		if (mytimer != null) {
			mytimer.cancel();
			mytimer = null;
		}
		if (confThreadStartSemaphore != null) {
			confThreadStartSemaphore.release();
			confThreadStartSemaphore = null;
		}
		if (confThread != null) {
			confThread.getHandler().getLooper().quit();
			confThread.interrupt();
			confThread = null;
		}
		if (releaseConfTimer != null) {
			releaseConfTimer.cancel();
			releaseConfTimer = null;
		}

		if (svLocalSurfaceView != null && mLocalContainer != null) {
			ViERenderer.setSurfaceNull(svLocalSurfaceView);
			mLocalContainer.removeAllViews();
			svLocalSurfaceView = null;
			mLocalContainer = null;
		}

		if (remoteSurfaceView != null && mRemoteContainer != null) {
			ViERenderer.setSurfaceNull(remoteSurfaceView);
			mRemoteContainer.removeAllViews();
			remoteSurfaceView = null;
			mRemoteContainer = null;
		}

		releaseShareView();

		// VoIP.getInstance().releaseCall();
	}

	/**
	 * 获取会议句柄
	 * 
	 * @return
	 */
	public int getConfHandle() {
		return confHandle;
	}

	/**
	 * 创建并加入会议
	 * 
	 * @param siteID
	 * @param svrIP
	 * @param confID
	 * @param confKey
	 * @param hostKey
	 * @param nUserID
	 * @param userName
	 * @return
	 */
	public boolean joinConf(String siteID, String svrIP, int confID,
			String confKey, String hostKey, int nUserID, String userName) {
		newConf(siteID, svrIP, confID, confKey, hostKey, nUserID, userName);

		// setIPMap();

		return joinConf();
	}

	/**
	 * 离开会议，释放资源
	 */
	public void toleaveConf() {
		clearVideoParamsMap();

		if (conf != null) {
			leaveConf();

			// 定义Handler
			final Handler tmpHandler = new Handler(Looper.getMainLooper()) {
				public void handleMessage(Message msg) {
					if (captureFlag) {
						int closeCapRes = ConfPrew.getInstance()
								.videoWizCloseCapture(1);
						LogUtils.d("ConfPrew videoWizCloseCapture result:"
								+ closeCapRes);
						if (closeCapRes == 0) {
							captureFlag = false;
							setCaptureFlag(false);
						}
					}
					// super.handleMessage(msg);
					exitConf();
					releaseConf();

					HandReceiverUtil.onTerminateConf();

					NotifyMsg notifyMsg = new NotifyMsg(
							NotifyID.CONFERENCE_TERMINATE_EVENT);
					VTMApp.getInstances().sendBroadcast(notifyMsg);
				}
			};

			// 定义计时器
			releaseConfTimer = new Timer();
			MyTimerTask task = new MyTimerTask();
			task.setHandler(tmpHandler);
			releaseConfTimer.schedule(task, 500);
		}

	}

	private static class MyTimerTask extends TimerTask {
		private Handler handler;

		public void setHandler(Handler handler) {
			this.handler = handler;
		}

		@Override
		public void run() {
			Message msg = new Message();
			handler.sendMessage(msg);
		}
	}

	/**
	 * 创建会议
	 * 
	 * @param siteID
	 * @param svrIP
	 * @param confID
	 * @param confKey
	 * @param hostKey
	 * @param nUserID
	 * @param userName
	 */
	public void newConf(String siteID, String svrIP, int confID,
			String confKey, String hostKey, int nUserID, String userName) {
		if(!AccountInfo.getInstance().isAnonymous()){
			String logFile = Environment.getExternalStorageDirectory().toString()
					 + File.separator + Constants.VTM_LOG_FILE;
					
			 File dirFile = new File(logFile);
			 if (!(dirFile.exists()) && !(dirFile.isDirectory())) {
			 if (dirFile.mkdir()) {
			 LogUtils.d("Conference", "mkdir " + dirFile.getPath());
			 }
			 }
			
			 Conference.getInstance().setLogLevel(3, 3);
			 Conference.getInstance().setPath(logFile, logFile);
			 Conference.getInstance().initSDK(false, 4);
		}
		conf = new ConfInstance();
		conf.setConfUI(this);
		ConfInfo cinfo = new ConfInfo();
		cinfo.setConfId(confID);
		cinfo.setConfKey(confKey);

		if (AccountInfo.getInstance().isAnonymous()) {
			cinfo.setConfOption(1048577); // 0x00100000, //视频一直打开Capture的模式
		} else {
			cinfo.setConfOption(1);
		}

		cinfo.setHostKey("111111");
		cinfo.setUserId(nUserID);
		cinfo.setUserName(userName);
		cinfo.setUserType(8);

		cinfo.setSiteId(siteID);
		cinfo.setSvrIp(svrIP);
		cinfo.setSiteUrl("");
		cinfo.setUserUri("");
		boolean flag = conf.confNew(cinfo);
		LogUtils.d("new conf result: " + flag);
	}

	/**
	 * 加入会议
	 * 
	 * @return
	 */
	public boolean joinConf() {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.CONF_OPER_JOIN;
			mconfHandler.sendMessage(msg);

			return true;
		}

		int nRet = conf.confJoin();
		LogUtils.d("Conference", "JoinConf |  nRet = " + nRet);

		return (nRet == 0);
	}

	/**
	 * 结束会议
	 */
	public void exitConf() {
		conf.confRelease();

		confHandle = 0;

		LogUtils.d("Conference", "ExitConf");
	}

	/**
	 * 离开会议
	 * 
	 * @return
	 */
	public boolean leaveConf() {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.CONF_OPER_LEAVE;
			mconfHandler.sendMessage(msg);

			return true;
		}

		int nRet = conf.confLeave();

		LogUtils.d("Conference", "leaveConf | nRet = " + nRet);

		return (nRet == 0);
	}

	/**
	 * 锁定会议
	 * 
	 * @return
	 */
	public boolean lockConf() {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.CONF_OPER_LOCK;
			mconfHandler.sendMessage(msg);

			return true;
		}

		int nRet = conf.confLock();

		return (nRet == 0);
	}

	/**
	 * 浼氬満闈欓煶璁剧疆
	 * 
	 * @param bMute
	 * @return
	 */
	public boolean muteConf(boolean bMute) {
		int nRet = conf.confMute(bMute);
		return (nRet == 0);
	}

	/**
	 * 会场静音设置
	 * 
	 * @return
	 */
	public boolean unLockConf() {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.CONF_OPER_UNLOCK;
			mconfHandler.sendMessage(msg);
			return true;
		}

		int nRet = conf.confUnLock();
		return (nRet == 0);
	}

	/**
	 * 加载组件
	 * 
	 * @return
	 */
	public boolean loadComponent() {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.CONF_OPER_LOAD_COMPONENT;
			mconfHandler.sendMessage(msg);
			return true;
		}

		int intparam = ConfDefines.IID_COMPONENT_BASE
				| ConfDefines.IID_COMPONENT_DS | ConfDefines.IID_COMPONENT_AS
				| ConfDefines.IID_COMPONENT_AUDIO
				| ConfDefines.IID_COMPONENT_VIDEO
				// | ConfDefines.IID_COMPONENT_RECORD
				| ConfDefines.IID_COMPONENT_CHAT
				| ConfDefines.IID_COMPONENT_POLLING
				// | ConfDefines.IID_COMPONENT_MS
				| ConfDefines.IID_COMPONENT_FT | ConfDefines.IID_COMPONENT_WB;

		int nRet = conf.confLoadComponent(intparam);
		LogUtils.d("Conference", "LoadComponent |  nRet = " + nRet);
		return (nRet == 0);
	}

	/**
	 * 心跳
	 */
	public void heartBeat() {
		conf.confHeartBeat();
	}

	/**
	 * 终止会议
	 * 
	 * @return
	 */
	public boolean terminateConf() {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.CONF_OPER_TERMINATE;
			mconfHandler.sendMessage(msg);
			return true;
		}

		int nRet = conf.confTerminate();
		LogUtils.d("Conference", "TerminateConf | nRet = " + nRet);

		return (nRet == 0);
	}

	/**
	 * 踢人
	 * 
	 * @param nUserID
	 * @return
	 */
	public boolean kickout(int nUserID) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.CONF_OPER_KICKOUT;
			msg.arg1 = nUserID;
			mconfHandler.sendMessage(msg);
			return true;
		}
		int nRet = conf.confUserKickout(nUserID);
		return (nRet == 0);
	}

	/**
	 * 设置角色
	 * 
	 * @param nUserID
	 * @param nRole
	 * @return
	 */
	public boolean setRole(int nUserID, int nRole) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.CONF_OPER_SET_ROLE;
			msg.arg1 = nUserID;
			msg.arg2 = nRole;
			mconfHandler.sendMessage(msg);
			return true;
		}
		int nRet = conf.confUserSetRole(nUserID, nRole);
		return (nRet == 0);
	}

	/**
	 * 设置音频参数
	 * 
	 * @return
	 */
	public boolean setAudioParam() {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.AUDIO_OPER_SET_AUDIOPARAM;
			mconfHandler.sendMessage(msg);
			return true;
		}

		// String AudioParamXml =
		// "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
		// "<MSG type = \"AudioParamXml\">" + "<version>1</version> "
		// + "<mix_method>2</mix_method>" + "<frame_len>16000</frame_len>" +
		// "<sample_rate>8000</sample_rate>" +
		// "<audio_codec_fmt>0</audio_codec_fmt>"
		// + "<EC>2</EC>" + "<AGC>1</AGC>" + "<NR>4</NR>" +
		// "<log_level>2</log_level>" + "<log_size>1</log_size>" + "</MSG>";

		AudioParam paramAudioParam = new AudioParam();
		paramAudioParam.mixMethod = 2;
		paramAudioParam.frameLen = 16000;
		paramAudioParam.sampleRate = 8000;
		paramAudioParam.audioCodecFmt = 0;
		paramAudioParam.ec = 2;
		paramAudioParam.agc = 1;
		paramAudioParam.nr = 4;
		paramAudioParam.logLevel = 2;
		paramAudioParam.logSize = 1;

		int nRet = conf.setAudioParam(paramAudioParam);
		return (nRet == 0);
	}

	/**
	 * 打开麦克风
	 * 
	 * @param micID
	 * @return
	 */
	public boolean openMic(int micID) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.AUDIO_OPER_OPEN_MIC;
			mconfHandler.sendMessage(msg);
			return true;
		}

		int nRet = conf.audioOpenMic(micID);

		return (nRet == 0);
	}

	/**
	 * 音频输入设备静音设置
	 * 
	 * @param isMmute
	 *            0:静音; 1:取消静音
	 * @return
	 */
	public boolean muteMic(int isMmute) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.AUDIO_OPER_MUTE_MIC;
			msg.arg1 = isMmute; // 0:闈欓煶; 1:鍙栨秷闈欓煶
			mconfHandler.sendMessage(msg);
			return true;
		}

		int nRet = conf.audioMuteMic();

		return (nRet == 0);
	}

	/**
	 * 关闭音频输入设备
	 * 
	 * @return
	 */
	public boolean closeMic() {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.AUDIO_OPER_CLOSE_MIC;
			mconfHandler.sendMessage(msg);
			return true;
		}
		int nRet = conf.audioCloseMic();
		return (nRet == 0);
	}

	/**
	 * 打开音频输出设备
	 * 
	 * @param speakerID
	 * @return
	 */
	public boolean openSpeaker(int speakerID) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.AUDIO_OPER_OPEN_SPEAKER;
			msg.arg1 = speakerID;
			mconfHandler.sendMessage(msg);

			return true;
		}
		LogUtils.d(TAG, "speakerID = " + speakerID);

		int nRet = conf.audioOpenSpeaker(speakerID);

		return (nRet == 0);
	}

	/**
	 * 音频输出设备静音设置
	 * 
	 * @param isMmute
	 *            0:静音; 1:取消静音
	 * @return
	 */
	public boolean muteSpeaker(int isMmute) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.AUDIO_OPER_MUTE_SPEAKER;
			msg.arg1 = isMmute; // 0:闈欓煶; 1:鍙栨秷闈欓煶
			mconfHandler.sendMessage(msg);
			return true;
		}
		int nRet = conf.audioMuteSpeaker();

		return (nRet == 0);
	}

	/**
	 * 关闭音频输出设备
	 * 
	 * @return
	 */
	public boolean closeSpeaker() {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.AUDIO_OPER_CLOSE_SPEAKER;
			// msg.arg1 = 1;
			mconfHandler.sendMessage(msg);
			return true;
		}

		int nRet = conf.audioCloseSpeaker();
		return (nRet == 0);
	}

	/**
	 * 获取本地视频设备数量
	 * 
	 * @return
	 */
	public boolean getVideoDeviceNum() {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.VIDEO_OPER_GETDEVICE_NUM;
			mconfHandler.sendMessage(msg);
			return true;
		}

		int nRet = conf.videoGetDeviceCount();
		return (nRet == 0);
	}

	/**
	 * 设置视频编码端的最大宽和高。 Android对于不同的型号有不同的编码要求，如果需要请设置，不设置的话，采用默认值640*480
	 * 
	 * @return
	 */
	public boolean setEncodeMaxResolution(int xResolution, int yResolution) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.VIDEO_OPER_SETENCODE_MAXRESOLUTION;
			msg.arg1 = xResolution;
			msg.arg2 = yResolution;
			mconfHandler.sendMessage(msg);
			return true;
		}
		// 设置设备能编码的最大分辨率（针对发送方），加载成功之后立即进行设置
		int nRet = conf.videoSetEncodeMaxResolution(xResolution, yResolution);
		LogUtils.d("maxXY nRet=" + nRet + " ,x=" + xResolution + " ,y="
				+ yResolution);
		return (nRet == 0);
	}

	/**
	 * 获取本地视频设备信息
	 * 
	 * @return
	 */
	public boolean getVideoDeviceInfo() {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.VIDEO_OPER_GETDEVICE_INFO;
			mconfHandler.sendMessage(msg);
			return true;
		}

		int nRet = conf.videoGetDeviceInfo();
		return (nRet == 0);
	}

	/**
	 * 设置视频参数
	 * 
	 * @param nUserID
	 * @param deviceID
	 * @return
	 */
	public boolean videoSetParam(int nUserID, String deviceID) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.VIDEO_OPER_SETPARAM;
			msg.arg1 = nUserID;
			msg.obj = deviceID;
			mconfHandler.sendMessage(msg);

			return true;
		}

		VideoParam videoParam = SystemSetting.getInstance().getVideoParam();
		int xRes = videoParam.getxRes();
		int yRes = videoParam.getyRes();
		int nFrame = videoParam.getnFrame();
		// int nBitRate = videoParam.getnBitRate();
		// int nRawtype = videoParam.getnRawtype();
		LogUtils.d("SystemSetting videoParam :xRes=" + xRes + ", yRes=" + yRes
				+ ", nFrame" + nFrame);
		int nRet = conf.videoSetParam(Long.parseLong(deviceID), xRes, yRes,
				nFrame);
		LogUtils.d("videoSetParam result:" + nRet);
		return (nRet == 0);
	}

	/**
	 * 打开视频
	 * 
	 * @param nUserID
	 * @param deviceID
	 * @return
	 */
	public boolean videoOpen(int nUserID, String deviceID) {
		LogUtils.d(TAG, "videoOpen | userId = " + nUserID + ", deviceId = "
				+ deviceID);
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.VIDEO_OPER_OPEN;
			msg.arg1 = nUserID;
			msg.obj = deviceID;
			mconfHandler.sendMessage(msg);

			return true;
		}
		int nRet = conf.videoOpen(Long.parseLong(deviceID));
		LogUtils.d("videoOpen RESULT:" + nRet);
		return (nRet == 0);
	}

	/**
	 * 关闭视频
	 * 
	 * @param nUserID
	 * @param deviceID
	 * @return
	 */
	public boolean videoClose(int nUserID, String deviceID) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.VIDEO_OPER_CLOSE;
			msg.arg1 = nUserID;
			msg.obj = deviceID;
			mconfHandler.sendMessage(msg);

			return true;
		}

		int nRet = conf.videoClose(Long.parseLong(deviceID), false); // 0707
		return (nRet == 0);
	}

	/**
	 * 暂停视频
	 * 
	 * @param nUserID
	 * @param deviceID
	 * @return
	 */
	public boolean videoPause(int nUserID, String deviceID) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.VIDEO_OPER_PAUSE;
			msg.arg1 = nUserID;
			msg.obj = deviceID;
			mconfHandler.sendMessage(msg);

			return true;
		}
		int nRet = conf.videoPause(nUserID, Long.parseLong(deviceID));

		return (nRet == 0);
	}

	/**
	 * 继续播放视频
	 * 
	 * @param nUserID
	 * @param deviceID
	 * @return
	 */
	public boolean videoResume(int nUserID, String deviceID) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.VIDEO_OPER_RESUME;
			msg.arg1 = nUserID;
			msg.obj = deviceID;
			mconfHandler.sendMessage(msg);

			return true;
		}

		int nRet = conf.videoResume(nUserID, Long.parseLong(deviceID));
		return (nRet == 0);
	}

	/**
	 * 得到视频设备能力个数
	 * 
	 * @param nUserID
	 * @param deviceID
	 * @return
	 */
	public boolean videGetDevicecApbilityNum(int nUserID, String deviceID) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.VIDEO_OPER_GETDEVICECAPBILITY_NUM;
			msg.arg1 = nUserID;
			msg.obj = deviceID;
			mconfHandler.sendMessage(msg);
			return true;
		}

		String VideoGetCapbiltityNum = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<MSG type = \"VideoGetCapbilityNum\">"
				+ "<version>1</version>"
				+ "<userid>%d</userid> "
				+ "<deviceid>%s</deviceid>" + "</MSG>";

		VideoGetCapbiltityNum = String.format(VideoGetCapbiltityNum, nUserID,
				deviceID);

		int nRet = Conference.getInstance().confHandleMsg(conf.getConfHandle(),
				ConfOper.VIDEO_OPER_GETDEVICECAPBILITY_NUM,
				VideoGetCapbiltityNum, null);
		LogUtils.d("videGetDevicecApbilityNum result: " + nRet);
		return (nRet == 0);
	}

	/**
	 * 得到视频设备能力信息
	 * 
	 * @param nUserID
	 * @param deviceID
	 * @return
	 */
	public boolean videGetDevicecApbilityInfo(int nUserID, String deviceID) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.VIDEO_OPER_GETDEVICECAPBILITY_INFO;
			msg.arg1 = nUserID;
			msg.obj = deviceID;
			mconfHandler.sendMessage(msg);
			return true;
		}

		String VideoGetCapbiltityInfo = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<MSG type = \"VideoGetCapbilityNum\">"
				+ "<version>1</version>"
				+ "<userid>%d</userid> "
				+ "<deviceid>%s</deviceid>" + "</MSG>";
		VideoGetCapbiltityInfo = String.format(VideoGetCapbiltityInfo, nUserID,
				deviceID);

		int nRet = Conference.getInstance().confHandleMsg(conf.getConfHandle(),
				ConfOper.VIDEO_OPER_GETDEVICECAPBILITY_INFO,
				VideoGetCapbiltityInfo, null);
		LogUtils.d("videGetDevicecApbilityInfo result: " + nRet + " ,deviceID:"
				+ deviceID);
		return (nRet == 0);
	}

	/**
	 * 获取视频参数
	 * 
	 * @param nUserID
	 * @param deviceID
	 * @return
	 */
	public boolean videoGetParam(int nUserID, String deviceID) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.VIDEO_OPER_GETPARAM;
			msg.arg1 = nUserID;
			msg.obj = deviceID;
			mconfHandler.sendMessage(msg);

			return true;
		}
		int nRet = conf.videoGetParam(nUserID, Long.parseLong(deviceID));

		return (nRet == 0);
	}

	/**
	 * 视频旋转
	 * 
	 * @param nUserID
	 * @param deviceID
	 * @return
	 */
	public boolean videoSetCaptureRotate(int nUserID, String deviceID) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.VIDEO_OPER_SET_CAPTURE_ROTATE;
			msg.arg1 = nUserID;
			msg.obj = deviceID;
			mconfHandler.sendMessage(msg);

			return true;
		}

		int nRet = conf.videoSetCaptureRotate(nUserID,
				Long.parseLong(deviceID), 180);
		return (nRet == 0);
	}

	/**
	 * 设置视频依附于页面
	 * 
	 * @param IndexWnd
	 * @param userid
	 * @param deviceid
	 * @return
	 */
	public boolean videoAttach(int IndexWnd, int userid, String deviceid) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.VIDEO_OPER_ATTACH;
			msg.arg1 = IndexWnd;
			msg.arg2 = userid;
			msg.obj = deviceid;
			mconfHandler.sendMessage(msg);
			return true;
		}

		int videoShowMode = SystemSetting.getInstance().getVideoShowMode();
		int nRet = conf.videoAttach(userid, Long.parseLong(deviceid), IndexWnd,
				1, videoShowMode);
		return (nRet == 0);
	}

	/**
	 * 设置视频分离页面
	 * 
	 * @param IndexWnd
	 * @param userid
	 * @param deviceid
	 * @return
	 */
	public boolean videoDetach(int IndexWnd, int userid, String deviceid) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.VIDEO_OPER_DETACH;
			msg.arg1 = IndexWnd;
			msg.arg2 = userid;
			msg.obj = deviceid;
			mconfHandler.sendMessage(msg);

			return true;
		}

		int nRet = conf.videoDetach(userid, Long.parseLong(deviceid), IndexWnd,
				false);
		return (nRet == 0);
	}

	/**
	 * 本地视频截图
	 * 
	 * @param userid
	 * @param deviceid
	 * @return
	 */
//	public boolean videoSnapShot(final long userid, final String deviceid) {
//		if (isMainThread()) {
//			Message msg = new Message();
//			msg.what = ConfOper.VIDEO_OPER_SNAPSHOT;
//			msg.arg1 = (int) userid;
//			msg.obj = deviceid;
//			mconfHandler.sendMessage(msg);
//			return true;
//		}
//
//		class MyAsync extends AsyncTask<String, Integer, Boolean> {
//
//			@Override
//			protected Boolean doInBackground(String... params) {
//
//				VideoParam videoParam = SystemSetting.getInstance()
//						.getVideoParam();
//				int xRes = videoParam.getxRes();
//				int yRes = videoParam.getyRes();
//				if (xRes == 0 && yRes == 0) {
//					xRes = 176;
//					yRes = 144;
//				}
//				LogUtils.d("videoSnapShot --userid=" + userid + " ,deviceid="
//						+ deviceid + " ,x:" + xRes + " ,y:" + yRes);
//				int nRet = conf.videoSnapShot(userid, Long.parseLong(deviceid),
//						xRes, yRes); // 0707设的c20的默认值
//				LogUtils.d("videoSnapShot result:" + nRet);
//				return (nRet == 0);
//			}
//		}
//		MyAsync myTask = new MyAsync();
//		myTask.execute();
//		return true;
//
//	}

	/**
	 * 视频截图 截取远端视频
	 * 
	 * @param userid
	 * @param deviceid
	 * @return
	 */
	public boolean videoRenderSnapShot(final long userid,
			final long deviceid, final String filename) {
		if (isMainThread()) {
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					int nRet = conf.videoRenderSnapShot(userid, deviceid,
							filename);
					LogUtils.d("videoRenderSnapShot userid:" + userid
							+ " ,deviceid" + deviceid + " ,result:" + nRet);
				}
			});
			t.start();
			return true;
		} else {
			int nRet = conf.videoRenderSnapShot(userid, deviceid, filename);
			LogUtils.d("videoRenderSnapShot userid:" + userid + " ,deviceid"
					+ deviceid + " ,result:" + nRet);
			return (nRet == 0);
		}

	}

	/**
	 * 发送信令消息给VTA
	 * 
	 * @return
	 */
	public boolean sendData(int nUserID, int msgID, byte[] optContext) {
		if (optContext == null) {
			optContext = "".getBytes(Charset.forName(Constants.CHARSET_GBK));
		}
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.CONF_OPER_SEND_DATA; // CONF_OPER_LOAD_COMPONENT;
														// CONF_OPER_SEND_DATA
			msg.arg1 = nUserID;
			msg.obj = optContext;
			Bundle data = new Bundle();
			data.putInt("msgID", msgID);
			data.putByteArray("optContext", optContext);
			msg.setData(data);
			mconfHandler.sendMessage(msg);

			return true;
		}

		// userid 鐢ㄦ埛UserID;intparam:娑堟伅绫诲瀷 锛圛N锛夌敤鎴峰畾涔夌殑娑堟伅ID,鏀寔鑼冨洿[0,85]
		String loadcomponent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<MSG type = \"SendData\">"
				+ "<version>1</version>"
				+ "<userid>%d</userid> " + "<intparam>%d</intparam>" + "</MSG>";

		loadcomponent = String.format(loadcomponent, nUserID, msgID);

		int nRet = Conference.getInstance().confHandleMsg(conf.getConfHandle(),
				ConfOper.CONF_OPER_SEND_DATA, loadcomponent, optContext);// 0707
		LogUtils.d("sendData result :" + nRet);
		if(turnCaptureFlag){
			openLocalVideo();
			turnCaptureFlag = false;
		}
		return (nRet == 0);
	}

	/**
	 * 通知某他人打开或是关闭视频
	 * 
	 * @param userid
	 * @param deviceid
	 * @param isOpen
	 *            1:open; 2:close
	 * @return
	 */
	public boolean videoNotifyOpen(int userid, String deviceid, int isOpen) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.VIDEO_OPER_NOTIFY;
			msg.arg1 = isOpen; // 1:open; 2:close
			msg.arg2 = userid;
			msg.obj = deviceid;
			mconfHandler.sendMessage(msg);

			return true;
		}

		int nRet = conf.videoNotifyOpen(userid, Long.parseLong(deviceid), 176,
				144, 10);// 0707用的C20默认值
		return (nRet == 0);
	}

	/**
	 * 发送消息
	 * 
	 * @param nDstID
	 * @param strMsg
	 * @return
	 */
	public boolean chatSendMsg(int nDstID, String strMsg) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.CHAT_OPER_SEND;
			msg.arg1 = nDstID;
			msg.obj = strMsg;
			mconfHandler.sendMessage(msg);

			return true;
		}

		int nRet = conf.chatSendMsg(1, 0, strMsg);// 0707C20默认值
		return (nRet == 0);
	}

	/**
	 * 文档共享 —— 设置当前页码
	 * 
	 * @param nDocID
	 * @param nPageID
	 * @return
	 */
	public boolean dsSetcurrentpage(int nDocID, int nPageID) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.DS_OPER_SET_CURRENTPAGE;
			msg.arg1 = nDocID;
			msg.arg2 = nPageID;
			mconfHandler.sendMessage(msg);

			return true;
		}
		int nRet = conf.dsSetCurrentPage(nDocID, nPageID);

		return (nRet == 0);
	}

	/**
	 * 打开共享文档
	 * 
	 * @param strFileName
	 * @return
	 */
	public boolean dsOpen(String strFileName) {
		int nRet = conf.dsOpen(strFileName);

		return (nRet == 0);
	}

	/**
	 * 关闭一个文档
	 * 
	 * @param strFileName
	 * @return
	 */
	public boolean dsClose(int nDocID) {
		int nRet = conf.dsClose(nDocID);
		return (nRet == 0);
	}

	/**
	 * 新建一个空白文档
	 * 
	 * @return
	 */
	public boolean wbNewdoc() {
		return true;
	}

	/**
	 * 删除一个白板文档
	 * 
	 * @param nDocID
	 * @return
	 */
	public boolean wbDeldoc(int nDocID) {
		int nRet = conf.wbDelDoc(nDocID);
		return (nRet == 0);
	}

	/**
	 * 在指定的白板文档中新建白板页，如果新建成功，新建的页面将被作为该文档的最后一页
	 * 
	 * @param nDocID
	 * @return
	 */
	public boolean wbNewpage(int nDocID) {
		return true;
	}

	/**
	 * 设置当前页面
	 * 
	 * @param nDocID
	 * @param nPageID
	 * @return
	 */
	public boolean wbSetcurrentpage(int nDocID, int nPageID) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.WB_OPER_SET_CURRENTPAGE;
			msg.arg1 = nDocID;
			msg.arg2 = nPageID;
			mconfHandler.sendMessage(msg);

			return true;
		}

		int nRet = conf.wbSetCurrentPage(nDocID, nPageID);

		return (nRet == 0);
	}

	/**
	 * 屏幕共享加入某个通道
	 * 
	 * @return
	 */
	public boolean asAttach() {
		int nRet = conf.asAttach();
		return (nRet == 0);
	}

	/**
	 * 屏幕共享退出某个通道
	 * 
	 * @return
	 */
	public boolean asDetach() {
		int nRet = conf.asDetach();
		return (nRet == 0);
	}

	/**
	 * 设置屏幕共享参数
	 * 
	 * @param value
	 *            1:开启；0：关闭
	 * @return
	 */
	public boolean asSetParam(int value) {
		if (isMainThread()) {
			Message msg = new Message();
			msg.what = ConfOper.AS_OPER_SET_PARAM;
			msg.arg1 = value;
			mconfHandler.sendMessage(msg);

			return true;
		}

		int nRet = conf.asSetParam(ConfDefines.AS_PROP_SAMPLING, value);// 0707C20默认
		LogUtils.d(TAG, "as_set_param end | value = " + value + ", nRet = "
				+ nRet);

		return (nRet == 0);
	}

	/**
	 * 本地屏幕共享开始
	 * 
	 * @return
	 */
	public boolean asStart() {
		int nRet = conf.asStart();
		return (nRet == 0);
	}

	/**
	 * 本地屏幕共享停止
	 * 
	 * @return
	 */
	public boolean asStop() {
		int nRet = conf.asStop();
		return (nRet == 0);
	}

	/**
	 * 设置屏幕共享权限
	 * 
	 * @param dwUserID
	 * @param dwAction
	 * @return
	 */
	public boolean asSetOwner(int dwUserID, int dwAction) {
		int nRet = conf.asSetOwner(dwUserID, dwAction);
		return (nRet == 0);
	}

	/**
	 * 设置屏幕共享区域
	 * 
	 * @param startX
	 * @param startY
	 * @param width
	 * @param height
	 * @return
	 */
	public boolean asSetShareRange(int startX, int startY, int width, int height) {
		String setShareRangeXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<MSG type = \"as_set_param\">"
				+ "<version>1</version>"
				+ "<ulPorpType>"
				// + ConfDefines.AS_PROP_RANGE 0709
				+ ConfDefines.AS_PROP_SHARING_RECT
				+ "</ulPorpType>"
				+ "<lParam1>"
				+ startX
				+ "</lParam1> "
				+ "<lParam2>"
				+ startY
				+ "</lParam2> "
				+ "<lParam3>"
				+ width
				+ "</lParam3> "
				+ "<lParam4>" + height + "</lParam4> " + "</MSG>";
		int iRet = Conference.getInstance().confHandleMsg(confHandle,
				ConfOper.AS_OPER_SET_PARAM, setShareRangeXml, null); // 0707没该方法

		LogUtils.d(TAG, "as_set_shareRange result is: " + iRet);
		return (0 == iRet);
	}

	/**
	 * 电话会场静音，用于静音整个电话会场
	 * 
	 * @param bmute
	 *            1:静音，0:取消静音
	 * @return
	 */
	public boolean phoneMute(int bmute) {
		int nRet = 0;
		if (bmute == 1) {
			nRet = conf.phoneConfMute(true);
		} else if (bmute == 0) {
			nRet = conf.phoneConfMute(false);
		}

		return (nRet == 0);
	}

	/**
	 * 电话会议锁定，锁定后的电话会议不能再邀请电话（该接口以及废弃）
	 * 
	 * @param block
	 *            1为锁定，0为解锁
	 * @return
	 */
	public boolean phoneLock(int block) {
		int nRet = 0;
		if (block == 1) {
			nRet = conf.phoneConfLock(true);
		} else if (block == 0) {
			nRet = conf.phoneConfLock(false);
		}

		return (nRet == 0);
	}

	/**
	 * 呼叫电话用户
	 * 
	 * @param phonenum
	 * @param pinnum
	 * @param username
	 * @param bHost
	 * @return
	 */
	public boolean phoneCall(String phonenum, int pinnum, String username,
			boolean bHost) {
		int nRet = conf.phoneCallOut(phonenum, pinnum, username, bHost);
		return (nRet == 0);
	}

	/**
	 * 电话静音，对单独一个电话用户进行静音
	 * 
	 * @param nrecordid
	 * @param bmute
	 * @return
	 */
	public boolean phoneCallMute(int nrecordid, boolean bmute) {
		int nRet = conf.phoneCallMute(nrecordid, bmute);
		return (nRet == 0);
	}

	/**
	 * 踢出电话用户
	 * 
	 * @param nrecordid
	 * @return
	 */
	public boolean phoneCallKick(int nrecordid) {
		int nRet = conf.phoneCallKillOff(nrecordid);
		return (nRet == 0);
	}

	public int annotRegCustomerType(int compid) {
		return conf.annotRegCustomerType(compid);

	}

	//
	public int annotInitResource(String path, int ciid) {
		return conf.annotInitResource(path, ciid);
	}

	public boolean setCaptureRotate(int nUserID, String nDeviceID, int rotate) {

		int nRet = conf.videoSetCaptureRotate(nUserID,
				Long.parseLong(nDeviceID), rotate);
		return (nRet == 0);
	}

	public boolean setIPMap() {
		List<String[]> IpItemList = AccountInfo.getInstance().getIpItemList();
		if (IpItemList == null) {
			return false;
		}
		StringBuffer msgContent = new StringBuffer();
		msgContent
				.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
						+ "<MSG type = \"AndroidIpMapMsg\">");
		msgContent.append("<count>" + IpItemList.size() + "</count>");

		String ipItemStr;
		for (int i = 0; i < IpItemList.size(); i++) {
			String[] IpItem = IpItemList.get(i);
			String interIp = "";
			String outerIp = "";
			for (int j = 0; IpItem != null && j < IpItem.length; j++) {
				if (j == 0) {
					interIp = IpItem[j];
				} else if (j == 1) {
					outerIp = IpItem[j];
				}
			}
			ipItemStr = "<IpItem>" + "<inter_ip>%s</inter_ip>"
					+ "<outer_ip>%s</outer_ip>" + "</IpItem>";
			ipItemStr = String.format(ipItemStr, interIp, outerIp);
			msgContent.append(ipItemStr);
		}
		msgContent.append("</MSG>");

		// String msgContent =
		// "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
		// + "<MSG type = \"%s\">"
		// + "<count>%d</count>"
		// + "<IpItem>"
		// + "<inter_ip>%s</inter_ip>"
		// + "<outer_ip>%s</outer_ip>"
		// + "</IpItem>" + "</MSG>";

		// int nRet = conf.confHandleMsg(confHandle,
		// ConfOper.CONF_OPER_SET_IPMAP, msgContent.toString(), null);
		//
		// LogUtils.i(TAG, ConfOper.CONF_OPER_SET_IPMAP + " | msgContent = " +
		// msgContent);
		//
		// return (nRet == 0);
		return true;
	}

	/**
	 * 会议回调消息通知
	 */
	@Override
	public void confMsgNotify(ConfMsg msg, ConfExtendMsg extendMsg) {
		long nValue1 = msg.getnValue1();
		long nValue2 = msg.getnValue2();
		int msgType = msg.getMsgType();

		LogUtils.i(TAG, "msgType = " + msgType + " , nValue1 = " + nValue1
				+ " , nValue2 = " + nValue2);

		NotifyMsg notifyMsg = new NotifyMsg();
		// if (null != data)
		// {
		// LogUtils.i(TAG, "msgType = " + msgType + " | data = " + new
		// String(data, Charset.forName(Constants.CHARSET_GBK)));
		// }
		switch (msgType) {
		case ConfMsg.CONF_MSG_ON_CONFERENCE_JOIN:
			LogUtils.d("CONF_MSG_ON_CONFERENCE_JOIN");
			clearVideoParamsMap();

			notifyMsg.setAction(NotifyID.CONFERENCE_JOIN_EVENT);
			notifyMsg.setRecode(String.valueOf(nValue1));

			AccountInfo.getInstance().addUser(
					new UserInfo(AccountInfo.getInstance().getVtmNo()));
			if (nValue1 == ConfResult.TC_OK) {
				loadComponent();
			}

			HandReceiverUtil.onJoinConf(String.valueOf(nValue1));

			VTMApp.getInstances().sendBroadcast(notifyMsg);

			break;
		case ConfMsg.CONF_MSG_USER_ON_LEAVE_IND: {
			ConfExtendUserInfoMsg infoMsg = (ConfExtendUserInfoMsg) extendMsg;
			String userId = infoMsg.getUserid() + "";

			AccountInfo.getInstance().removeUser(userId);

			notifyMsg.setAction(NotifyID.CONF_USER_LEAVE_EVENT);
			notifyMsg.setMsg(userId);
			VTMApp.getInstances().sendBroadcast(notifyMsg);
		}
			break;
		case ConfMsg.CONF_MSG_USER_ON_ENTER_IND: {
			ConfExtendUserInfoMsg infoMsg = (ConfExtendUserInfoMsg) extendMsg;
			String userId = infoMsg.getUserid() + "";
			AccountInfo.getInstance().addUser(new UserInfo(userId));

			notifyMsg.setAction(NotifyID.CONF_USER_ENTER_EVENT);
			notifyMsg.setMsg(userId);
			VTMApp.getInstances().sendBroadcast(notifyMsg);
		}
			break;
		case ConfMsg.CONF_MSG_USER_ON_MESSAGE_IND: // 用户收到消息
			ConfExtendUserDataMsg rawData = (ConfExtendUserDataMsg) extendMsg;
			String fromId = rawData.getFromuserid() + "";
			byte[] data = rawData.getUserData();
			msgType = (int) rawData.getMsgtype();
			handleUserMsg(fromId, msgType, data);

			break;
		case ConfMsg.CONF_MSG_ON_DISCONNECT: // 网络故障（导致会议终止）
			notifyMsg.setAction(NotifyID.NETWORK_ERROR_EVENT);
			VTMApp.getInstances().sendBroadcast(notifyMsg);
			break;
		case ConfMsg.CONF_MSG_ON_RECONNECT:
			notifyMsg.setAction(NotifyID.RECONNECT_EVENT);
			VTMApp.getInstances().sendBroadcast(notifyMsg);
			break;
		case ConfMsg.CONF_MSG_ON_COMPONENT_LOAD:
			switch ((int) nValue2) {
			case ConfDefines.IID_COMPONENT_VIDEO:
				LogUtils.d(TAG, "loadComponent video");
				// Android对于不同的型号有不同的编码要求，如果需要请设置，不设置的话，采用默认值640*480
				setEncodeMaxResolution(352, 288);
				getVideoDeviceNum();
				getVideoDeviceInfo();
				break;
			case ConfDefines.IID_COMPONENT_DS:

				annotRegCustomerType(ConfDefines.IID_COMPONENT_DS);
				annotInitResource(Constants.ANNORESPATH,
						ConfDefines.IID_COMPONENT_DS);

				LogUtils.d(TAG, "loadComponent DS");
				break;
			case ConfDefines.IID_COMPONENT_AS:
				LogUtils.d(TAG, "loadComponent AS");
				break;
			default:
				break;
			}
			break;
		case ConfMsg.COMPT_MSG_VIDEO_ON_GETDEVICE_NUM:
		case ConfMsg.COMPT_MSG_VIDEO_ON_GETDEVICE_INFO:
		case ConfMsg.COMPT_MSG_VIDEO_ON_DEVICE_INFO:
		case ConfMsg.COMPT_MSG_VIDEO_ON_DEVICECAPBILITY_NUM:
		case ConfMsg.COMPT_MSG_VIDEO_ON_DEVICECAPBILITY_INFO:
		case ConfMsg.COMPT_MSG_VIDEO_ON_SWITCH:
		case ConfMsg.COMPT_MSG_VIDEO_ON_FIRST_KEYFRAME:
			confMsgNotifyVideo(msg, extendMsg);
			break;
		case ConfMsg.COMPT_MSG_AS_ON_SCREEN_DATA:
			// LogUtils.d("COMPT_MSG_AS_ON_SCREEN_DATA");
			// confMsgNotifyAs(msgType,nValue1,nValue2);
			// break;
		case ConfMsg.COMPT_MSG_AS_ON_SHARING_SESSION:
		case ConfMsg.COMPT_MSG_AS_ON_SCREEN_SIZE:
		case ConfMsg.COMPT_MSG_AS_ON_SHARING_STATE:
			confMsgNotifyAs(msg, extendMsg);
			// confMsgNotifyAs(msgType,nValue1,nValue2);
			break;
		case ConfMsg.COMPT_MSG_DS_ANDROID_DOC_COUNT:
		case ConfMsg.COMPT_MSG_DS_ON_DOC_NEW:
		case ConfMsg.COMPT_MSG_DS_ON_DOC_DEL:
		case ConfMsg.COMPT_MSG_DS_ON_PAGE_NEW:
		case ConfMsg.COMPT_MSG_DS_ON_PAGE_DEL:
		case ConfMsg.COMPT_MSG_DS_ON_CURRENT_PAGE:
		case ConfMsg.COMPT_MSG_DS_ON_DRAW_DATA_NOTIFY:
		case ConfMsg.COMPT_MSG_DS_PAGE_DATA_DOWNLOAD:
		case ConfMsg.COMPT_MSG_DS_ON_CURRENT_PAGE_IND:
			confMsgNotifyDs(msg, extendMsg);
			break;
		default:
			break;
		}
	}

	/**
	 * 处理会议中其他用户通过会议通道发送过来的普通消息
	 * 
	 * @param MsgContent
	 *            发送的消息外层包装 的xml内容
	 * @param data
	 *            具体的消息内容
	 */
	private void handleUserMsg(String fromUserId, int msgType, byte[] data) {
		NotifyMsg notifyMsg = new NotifyMsg();
		switch (msgType) {
		case MsgID.CONF_NEGOTIATE_MSG:// 信令协商消息
		{
			AccountInfo.getInstance().addUser(new UserInfo(fromUserId));

			if (data != null) {
				String dataStr = new String(data,
						Charset.forName(Constants.CHARSET_GBK));
				try {
					JSONObject jObject = new JSONObject(dataStr);
					JSONObject jObjectDataSend = createSendMsg(jObject);
					sendData(
							Tools.parseInt(fromUserId),
							MsgID.CONF_NEGOTIATE_RESPOND_MSG,
							jObjectDataSend.toString().getBytes(
									Charset.forName(Constants.CHARSET_GBK)));
				} catch (JSONException e) {
					e.printStackTrace();
					LogUtils.e(TAG, e.getMessage());
				}
			}
		}
			break;
		case MsgID.CONF_COMMON_SIG_MSG:// 普通的供上层使用的文本信令消息
		{
			notifyMsg.setAction(NotifyID.MSG_ARRIVED_EVENT);
			String sigMsg = "";

			if (null != data) {
				sigMsg = new String(data,
						Charset.forName(Constants.CHARSET_GBK));
			}
			notifyMsg.setMsg(sigMsg);
			VTMApp.getInstances().sendBroadcast(notifyMsg);
		}
			break;

		case MsgID.CONF_HOLD_MSG: // VTA通知VTM要呼叫保持/暂停视频
		case MsgID.CONF_RESUME_HOLD_MSG: // VTA通知VTM要恢复呼叫保持/暂停视频
		{
			handBeHoldMsg(msgType, fromUserId);
		}
			break;
		case MsgID.CONF_IS_HOLD_MSG:// 被保持或静音成功
		{
			notifyMsg.setAction(NotifyID.VEDIO_BEEN_PAUSED_EVENT);
			VTMApp.getInstances().sendBroadcast(notifyMsg);
		}
			break;
		case MsgID.CONF_IS_UNHOLD_MSG:// 取消被保持或静音成功
		{
			notifyMsg.setAction(NotifyID.VEDIO_BEEN_RESUMED_EVENT);
			VTMApp.getInstances().sendBroadcast(notifyMsg);
		}
			break;

		case MsgID.MESSAGE_CMD_IS_SERVICE_BEGINS:// 服务开启或暂停通知
		{
			// TODO
		}
			break;
		case MsgID.MESSAGE_CMD_CALL_TRANSFER:// 呼叫转移
		{
			handCallTransfer();
		}
			break;
		case MsgID.MESSAGE_CMD_TRIPARTITE_REQUEST: // 三方通话请求
		case MsgID.MESSAGE_CMD_TRIPARTITE_CALLDATA: // 三方通话协商
		case MsgID.MESSAGE_CMD_TRIPARTITE_CANCELED: // 第三方要退出会议
		{
			handTripartiteMsg(msgType, fromUserId, data);
		}
			break;
		case MsgID.CONF_MONITOR_JOIN_MSG: // 质检员加入
		{
			sendRespondData(Tools.parseInt(fromUserId),
					MsgID.CONF_MONITOR_JOIN_RESPOND);
		}
			break;
		case MsgID.CONF_MONITOR_SWITCH_MSG: // 质检员切换监听/插入消息
		{
			if (AccountInfo.getInstance().isShowThirdVideo()) {
				// detachThridVideo(fromUserId);
				AccountInfo.getInstance().setTriCallType(0);
				videoSwitch(fromUserId);
			}
			notifyMsg.setAction(NotifyID.INSERTED_SWITCH_EVENT);
			VTMApp.getInstances().sendBroadcast(notifyMsg);

			sendRespondData(Tools.parseInt(fromUserId),
					MsgID.CONF_MONITOR_SWITCH_RESPOND);
		}
			break;
		case MsgID.CONF_QUALITY_END_MSG: // 质检结束
		{
			if (AccountInfo.getInstance().isShowThirdVideo()) {
				detachThridVideo(fromUserId);
			}

			notifyMsg.setAction(NotifyID.INSERTED_END_EVENT);
			VTMApp.getInstances().sendBroadcast(notifyMsg);
		}
			break;
		case MsgID.CONF_MONITOR_JOIN_SUCCESS_MSG: // 质检员插入成功
		{
			AccountInfo.getInstance().setTriCallType(1);
			notifyMsg.setAction(NotifyID.INSERTED_BEGIN_EVENT);
			VTMApp.getInstances().sendBroadcast(notifyMsg);
		}
			break;
		default:
			break;
		}
	}

	/**
	 * 澶勭悊鍛煎彨琚繚鎸佸拰鎭㈠鍥炶皟閫氱煡
	 * 
	 * @param msgType
	 * @param parser
	 */
	private void handBeHoldMsg(int msgType, String fromuserid) {
		int nUserID = Tools.parseInt(fromuserid);
		UserInfo userInfo = AccountInfo.getInstance().getUserInfo(fromuserid);

		int msgID;
		if (MsgID.CONF_HOLD_MSG == msgType) // VTA閫氱煡VTM瑕佸懠鍙繚鎸�鏆傚仠瑙嗛
		{
			if (videoPause(nUserID, userInfo.getCurrDeviceId())) {
				msgID = MsgID.CONF_HOLD_RESPOND_SUCCESS_MSG;
			} else {
				msgID = MsgID.CONF_HOLD_RESPOND_FAILED_MSG;
			}
		} else
		// MsgID.CONF_RESUME_HOLD_MSG: // VTA閫氱煡VTM瑕佹仮澶嶅懠鍙繚鎸�鏆傚仠瑙嗛
		{
			if (videoResume(nUserID, userInfo.getCurrDeviceId())) {
				msgID = MsgID.CONF_RESUME_HOLD_RESPOND_SUCCESS_MSG;
			} else {
				msgID = MsgID.CONF_RESUME_HOLD_RESPOND_FAILED_MSG;
			}
		}

		sendRespondData(nUserID, msgID);
	}

	/**
	 * 澶勭悊鍛煎彨琚浆绉荤浉鍏冲洖璋冮�鐭�
	 */
	private void handCallTransfer() {
		UserInfo userInfo = AccountInfo.getInstance().getSelfUserInfo();
		int nUserID = Tools.parseInt(userInfo.getUserId());
		String deviceID = userInfo.getCurrDeviceId();
		if (videoClose(nUserID, deviceID)) {
			int indexOfSurface = ViERenderer
					.getIndexOfSurface(svLocalSurfaceView);
			LogUtils.d("handCallTransfer videoDetach " + indexOfSurface);
			videoDetach(indexOfSurface, nUserID, deviceID);
			turnCaptureFlag = true;
			turnOpenLocalFlag = true;
		}
	}

	/**
	 * 澶勭悊涓夋柟閫氳瘽鐩稿叧鍥炶皟閫氱煡
	 * 
	 * @param msgType
	 * @param parser
	 * @param data
	 */
	private void handTripartiteMsg(int msgType, String fromuserid, byte[] data) {
		int fromUserId = Tools.parseInt(fromuserid);

		switch (msgType) {
		// VTM绔細鏀跺埌VTA閫氳繃浼氳鏈嶅姟鍣ㄥ彂杩囨潵鐨勪笁鏂瑰懠鍙姹傦紝鍒ゆ柇鏄惁鍏佽杩涘叆涓夋柟
		case MsgID.MESSAGE_CMD_TRIPARTITE_REQUEST: {
			// 鍏佽 MsgID.MESSAGE_CMD_TRIPARTITE_ACK
			// 涓嶅厑璁�MsgID.MESSAGE_CMD_TRIPARTITE_FINISH
			// NotifyMsg notifyMsg = new NotifyMsg();
			// notifyMsg.setAction(NotifyID.CONF_MSG_TRIPARTITE_REQUEST);
			// VTMApp.getInstances().sendBroadcast(notifyMsg);

			sendRespondData(fromUserId, MsgID.MESSAGE_CMD_TRIPARTITE_ACK);
		}
			break;
		// VTA鍝嶅簲VTM绔厑璁歌繘鍏ヤ笁鏂圭殑娑堟伅锛岃В鏋愬崗鍟嗘秷鎭�
		case MsgID.MESSAGE_CMD_TRIPARTITE_CALLDATA: {
			if (data != null) {
				String dataStr = new String(data,
						Charset.forName(Constants.CHARSET_GBK));
				try {
					JSONObject jObject = new JSONObject(dataStr);
					int triCallType = jObject.getInt("tripartite_call_type");
					AccountInfo.getInstance().setTriCallType(triCallType);
					sendRespondData(fromUserId, MsgID.MESSAGE_CMD_TRIPARTITE_OK);

					NotifyMsg notifyMsg = new NotifyMsg();
					notifyMsg
							.setAction(NotifyID.TERMINAL_THREEPARTY_TALKING_EVENT);
					VTMApp.getInstances().sendBroadcast(notifyMsg);
				} catch (JSONException e) {
					e.printStackTrace();
					LogUtils.e(TAG, e.getMessage());
					sendRespondData(fromUserId,
							MsgID.MESSAGE_CMD_TRIPARTITE_FINISH);
				}
			}
		}
			break;
		// 涓夋柟閫氳瘽鍒囧洖涓ゆ柟
		case MsgID.MESSAGE_CMD_TRIPARTITE_CANCELED: {
			detachThridVideo(String.valueOf(fromUserId));
		}
			break;
		default:
			break;
		}
	}

	/**
	 * 解除第三方视频页面绑定
	 * 
	 * @param fromUserId
	 */
	private void detachThridVideo(String fromUserId) {
		AccountInfo.getInstance().setTriCallType(0);

		UserInfo attachUser = AccountInfo.getInstance().getUserInfo(fromUserId);
		String attachDeviceid = attachUser.getCurrDeviceId();
		LogUtils.d(TAG, "attachUserid = " + fromUserId);

		if (mRemoteContainer != null && remoteSurfaceView != null) {
			int indexOfSurface = ViERenderer
					.getIndexOfSurface(remoteSurfaceView);
			if (videoAttach(indexOfSurface, Tools.parseInt(fromUserId),
					attachDeviceid)) {
				mRemoteContainer.removeAllViews();
				mRemoteContainer.addView(remoteSurfaceView);

				AccountInfo.getInstance().setAttachUser(attachUser);
			}
		}
	}

	/**
	 * 切换视频显示
	 * 
	 * @param attachUserId
	 *            需要显示视频的userId
	 */
	public boolean videoSwitch(String attachUserId) {
		if (null != mRemoteContainer && null != remoteSurfaceView) {
			UserInfo detachInfo = AccountInfo.getInstance().getAttachUser();
			UserInfo attachInfo = AccountInfo.getInstance().getUserInfo(
					attachUserId);

			int indexOfSurface = ViERenderer
					.getIndexOfSurface(remoteSurfaceView);

			int detachUserid = Tools.parseInt(detachInfo.getUserId());
			String detachDeviceid = detachInfo.getCurrDeviceId();
			LogUtils.d(TAG, "detachUserid = " + detachUserid
					+ ", detachDeviceid = " + detachDeviceid);
			if (videoDetach(indexOfSurface, detachUserid, detachDeviceid)) {
				mRemoteContainer.removeAllViews();

				int attachUserid = Tools.parseInt(attachInfo.getUserId());
				String attachDeviceid = attachInfo.getCurrDeviceId();
				LogUtils.d(TAG, "attachUserid = " + attachUserid
						+ ", attachDeviceid = " + attachUserid);
				if (videoAttach(indexOfSurface, attachUserid, attachDeviceid)) {
					mRemoteContainer.addView(remoteSurfaceView);

					AccountInfo.getInstance().setAttachUser(attachInfo);

					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 切换视频显示模式
	 * 
	 * @param videoShowMode
	 *            参考{@link Constants.VIDEO_SHOW_MODE}
	 * @return
	 */
	public boolean videoShowModeSwitch(int videoShowMode) {
		SystemSetting.getInstance().setVideoShowMode(videoShowMode);

		if (CALLMODE.CALL_TALKING != AccountInfo.getInstance().getCallMode()
				|| Tools.isEmpty(AccountInfo.getInstance().getConfId())) {
			return false;
		}

		if (null != mRemoteContainer && null != remoteSurfaceView) {
			UserInfo userInfo = AccountInfo.getInstance().getAttachUser();

			int indexOfSurface = ViERenderer
					.getIndexOfSurface(remoteSurfaceView);

			int userid = Tools.parseInt(userInfo.getUserId());
			String deviceid = userInfo.getCurrDeviceId();
			LogUtils.d(TAG, "userid = " + userid + ", ueviceid = " + deviceid);
			if (videoDetach(indexOfSurface, userid, deviceid)) {
				mRemoteContainer.removeAllViews();
				if (videoAttach(indexOfSurface, userid, deviceid)) {
					mRemoteContainer.addView(remoteSurfaceView);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 携带自己的userID 响应VTA的消息 <br>
	 * 
	 * @param nUserID
	 *            VTA的userID
	 * @param msgID
	 *            消息ID
	 */
	public void sendRespondData(int nUserID, int msgID) {
		JSONObject jObject = new JSONObject();
		try {
			jObject.put("userID", AccountInfo.getInstance().getSelfUserInfo()
					.getUserId());
			byte[] optContext = jObject.toString().getBytes(
					Charset.forName(Constants.CHARSET_GBK));

			sendData(nUserID, msgID, optContext);
		} catch (JSONException e) {
			e.printStackTrace();
			LogUtils.e(TAG, e.toString());
		}
	}

	// 视频相关回调通知
	private void confMsgNotifyVideo(ConfMsg msg, ConfExtendMsg extendMsg) {
		int msgType = msg.getMsgType();
		int nValue1 = msg.getnValue1();
		int nValue2 = (int) msg.getnValue2();
		switch (msgType) {
		case ConfMsg.COMPT_MSG_VIDEO_ON_GETDEVICE_NUM: {
			UserInfo userInfo = AccountInfo.getInstance().getSelfUserInfo();
			userInfo.setDeviceNum(nValue1);
			AccountInfo.getInstance().addUser(userInfo);
		}
			break;
		case ConfMsg.COMPT_MSG_VIDEO_ON_GETDEVICE_INFO: // 取得自己的设备信息
		{
			LogUtils.d("COMPT_MSG_VIDEO_ON_GETDEVICE_INFO");

			ConfExtendVideoDeviceInfoMsg rawData = (ConfExtendVideoDeviceInfoMsg) extendMsg;
			String deviceId = rawData.getDeviceId() + "";
			String deviceName = rawData.getDeviceName();

			CameraInfo selfCameraInfo = new CameraInfo(nValue1, deviceId,
					deviceName);

			UserInfo userInfo = AccountInfo.getInstance().getSelfUserInfo();

			userInfo.addCameraInfo(selfCameraInfo);

			AccountInfo.getInstance().addUser(userInfo);

			int nUserID = Tools.parseInt(userInfo.getUserId());
			LogUtils.d("COMPT_MSG_VIDEO_ON_GETDEVICE_INFO nUserID:" + nUserID
					+ " ,deviceId:" + deviceId + " ,deviceName:" + deviceName);
			videGetDevicecApbilityNum(nUserID, deviceId);
			videGetDevicecApbilityInfo(nUserID, deviceId);

			openLocalVideo();
		}
			break;
		case ConfMsg.COMPT_MSG_VIDEO_ON_DEVICECAPBILITY_NUM: // 获取视频设备能力个数
		{
			// LogUtils.i(TAG, "devicecapbility_num | " + "msgType = " + msgType
			// + " , nValue1 = " + nValue1 + " , nValue2 = " + nValue2 +
			// " ,MsgContent = "
			// + MsgContent);
		}
			break;
		case ConfMsg.COMPT_MSG_VIDEO_ON_DEVICECAPBILITY_INFO: // 获取视频设备能力信息
		{
			// LogUtils.i(TAG, "devicecapbility_info | " + "msgType = " +
			// msgType + " , nValue1 = " + nValue1 + " , nValue2 = " + nValue2 +
			// " ,MsgContent = "
			// + MsgContent);
			LogUtils.d("COMPT_MSG_VIDEO_ON_DEVICECAPBILITY_INFO");
			ConfExtendVideoParamMsg rawData = (ConfExtendVideoParamMsg) extendMsg;
			String deviceID = rawData.getDeviceId() + "";
			int nFrame = rawData.getFramerate();
			VideoParam videoParam = new VideoParam();
			videoParam.setxRes(rawData.getXresolution());
			videoParam.setyRes(rawData.getYresolution());
			// videoParam.setnFrame(nFrame > 15 ? 15 : nFrame);
			videoParam.setnFrame(nFrame);
			videoParam.setnBitRate(rawData.getBitrate());
			// videoParam.setnRawtype(0); // 0707不对应
			LogUtils.d("COMPT_MSG_VIDEO_ON_DEVICECAPBILITY_INFO ---"
					+ videoParam.toString() + ", deviceID = " + deviceID);
			addVideoParamsMap(deviceID, videoParam);
		}
			break;
		case ConfMsg.COMPT_MSG_VIDEO_ON_DEVICE_INFO: // 设备添加或是删除:(包括自己和别人)
		{
			ConfExtendVideoDeviceInfoMsg fromMsg = (ConfExtendVideoDeviceInfoMsg) extendMsg;
			// VideoDeviceInfo toMsg = new VideoDeviceInfo(
			// fromMsg.getUserId(), fromMsg.getDeviceId(),
			// fromMsg.getDeviceName());
			// toMsg.setOrientation(fromMsg.getDeviceStatus());
			LogUtils.d("deviceID:" + fromMsg.getDeviceId() + ", orientation = "
					+ fromMsg.getDeviceStatus());
			if (nValue1 == 1) // 0:删除 1:添加
			{
				String userid = fromMsg.getUserId() + "";
				UserInfo selfUserInfo = AccountInfo.getInstance()
						.getSelfUserInfo();
				if (!selfUserInfo.getUserId().equals(userid)) {
					String deviceid = fromMsg.getDeviceId() + "";
					String deviceName = fromMsg.getDeviceName();
					CameraInfo fromCameraInfo = new CameraInfo(deviceid,
							deviceName);
					UserInfo userInfo = AccountInfo.getInstance().getUserInfo(
							userid);
					userInfo.addCameraInfo(fromCameraInfo);
				}
			}
		}
			break;
		case ConfMsg.COMPT_MSG_VIDEO_ON_FIRST_KEYFRAME: // 返回解码后的第一个关键帧
		{
			// 视频第一帧是为了解决视频显示时候黑屏闪烁问题的，不过后面没用到，先留着
			NotifyMsg notifyMsg = new NotifyMsg(
					NotifyID.COMPT_VIDEO_FIRST_KEYFRAME_EVENT);
			notifyMsg.setRecode(String.valueOf(nValue1));
			notifyMsg.setMsg(String.valueOf(nValue2));
			VTMApp.getInstances().sendBroadcast(notifyMsg);
		}
			break;

		case ConfMsg.COMPT_MSG_VIDEO_ON_SWITCH: // 视频状态相关:1:打开 0:关闭 2:Resume
			ConfExtendVideoParamMsg paramMsg = (ConfExtendVideoParamMsg) extendMsg;
			String deviceid = "" + paramMsg.getDeviceId();

			if (nValue1 == 1 || nValue1 == 2) // 1:打开 2:Resume
			{
				if (nValue2 == Tools.parseInt(AccountInfo.getInstance()
						.getVtmNo())) // 本地
				{
					LogUtils.d("COMPT_MSG_VIDEO_ON_SWITCH vtm nValue1:"
							+ nValue1);
					if (mLocalContainer != null && svLocalSurfaceView != null) {
						if (nValue1 == 1) {
							if (!AccountInfo.getInstance().isAnonymous()) {
								mLocalContainer.removeAllViews();
								mLocalContainer.addView(svLocalSurfaceView);
								LogUtils.d("COMPT_MSG_VIDEO_ON_SWITCH vtm open");
							}
							if(turnOpenLocalFlag){
								mLocalContainer.removeAllViews();
								mLocalContainer.addView(svLocalSurfaceView);
								turnOpenLocalFlag = false;
							}
							int indexOflocalSurfaceView = ViERenderer
									.getIndexOfSurface(localSurfaceView);
									int userid = Integer.parseInt(AccountInfo.getInstance()
											.getSelfUserInfo().getUserId());
							boolean attachRet =  videoAttach(indexOflocalSurfaceView, userid, deviceid);
							LogUtils.d("COMPT_MSG_VIDEO_ON_SWITCH attachRet:"+attachRet+" ,userid:"+userid+" ,deviceid:"+deviceid
									+" ,indexOflocalSurfaceView:"+indexOflocalSurfaceView);
						} else if (nValue1 == 2) {
							mLocalContainer.removeAllViews();
							mLocalContainer.addView(svLocalSurfaceView);
							LogUtils.d("COMPT_MSG_VIDEO_ON_SWITCH vtm resume");
						}
					}
				} else
				// 远端VTA
				{
					VTAOpenVideo((int) nValue2, deviceid);
					AccountInfo.getInstance().setVtaDeviceId(deviceid);
				}
			} else if (nValue1 == 0) // 关闭
			{
				if (nValue2 == Tools.parseInt(AccountInfo.getInstance()
						.getVtmNo())) // 本地
				{
					if (mLocalContainer != null && svLocalSurfaceView != null) {
						ViERenderer.setSurfaceNull(svLocalSurfaceView);
						mLocalContainer.removeAllViews();
					}
				} else if (nValue2 == Tools.parseInt(AccountInfo.getInstance()
						.getVTAUserInfos().get(0).getUserId()))
				// 远端VTA
				{
					// 把窗口置空,必须的
					if (mRemoteContainer != null && remoteSurfaceView != null) {
						int indexOfSurface = ViERenderer
								.getIndexOfSurface(remoteSurfaceView);
						if (videoDetach(indexOfSurface, (int) nValue2, deviceid)) {
							ViERenderer.setSurfaceNull(remoteSurfaceView);
							mRemoteContainer.removeAllViews();
						}
					}
				} else {
					LogUtils.i(TAG, "[video], tripartite video closed ");
				}
			}
			NotifyMsg notifyMsg = new NotifyMsg(
					NotifyID.COMPT_VIDEO_SWITCH_EVENT);
			notifyMsg.setRecode(String.valueOf(nValue1));

			if (nValue2 == Tools.parseInt(AccountInfo.getInstance().getVtmNo())) // 鏈湴
			{
				notifyMsg.setMsg(String.valueOf(0)); // msg: 0 本地，1 远端
			} else {
				notifyMsg.setMsg(String.valueOf(1)); // msg: 0 本地，1 远端
			}
			VTMApp.getInstances().sendBroadcast(notifyMsg);
			break;
		default:
			break;
		}

	}

	/**
	 * 远端 VTA视频打开
	 * 
	 * @param userID
	 * @param deviceid
	 */
	private void VTAOpenVideo(int userID, String deviceid) {
		LogUtils.d(TAG, "VTAOpenVideo | userID = " + userID + ", deviceid = "
				+ deviceid);
		UserInfo userInfo = AccountInfo.getInstance().getUserInfo(
				String.valueOf(userID));
		List<CameraInfo> cameraInfos = userInfo.getCameraInfos();
		CameraInfo cameraInfo1;
		// 获取指定视频设备编号的视频设备信息
		for (int i = 0; i < cameraInfos.size(); i++) {
			cameraInfo1 = cameraInfos.get(i);
			if (deviceid.equals(cameraInfo1.getDeviceID())) {
				cameraInfos.remove(cameraInfo1);
				cameraInfos.add(0, cameraInfo1);
				break;
			}
		}

		// 判断用户ID为是否为VTA端被叫用户（即非第三方用户）
		if (userID == Tools.parseInt(AccountInfo.getInstance()
				.getVTAUserInfos().get(0).getUserId())) {
			if (mRemoteContainer != null && remoteSurfaceView != null) {
				int indexOfSurface = ViERenderer
						.getIndexOfSurface(remoteSurfaceView);
				if (videoAttach(indexOfSurface, (int) userID, deviceid)) {
					mRemoteContainer.removeAllViews();
					mRemoteContainer.addView(remoteSurfaceView);

					UserInfo attachUser = AccountInfo.getInstance()
							.getUserInfo(String.valueOf(userID));

					AccountInfo.getInstance().setAttachUser(attachUser);

				}
			} else {
				LogUtils.i(TAG,
						"[video], COMPT_MSG_VIDEO_ON_SWITCH,mLlRemoteSurface is null\n");
			}
		} else {
			LogUtils.i(TAG, "[video], tripartite video opened ");
		}
	}

	private void openLocalVideo() {
		// 打开自己摄像头
		UserInfo selfInfo = AccountInfo.getInstance().getSelfUserInfo();
		List<CameraInfo> selfCameraInfos = selfInfo.getCameraInfos();
		if (selfInfo.getDeviceNum() != selfCameraInfos.size()) {
			return;
		}
		int nSelfUserID = Tools.parseInt(selfInfo.getUserId());
		String selfDeviceID;
		CameraInfo cameraInfo = null;
		for (int i = 0; i < selfCameraInfos.size(); i++) {
			cameraInfo = selfCameraInfos.get(i);
			if (cameraInfo.getDeviceName().contains("front")) {
				break;
			}

		}
		if (cameraInfo != null) {
			selfCameraInfos.remove(cameraInfo);
			selfCameraInfos.add(0, cameraInfo);

			selfDeviceID = cameraInfo.getDeviceID();
			// videoSetParam(nSelfUserID, selfDeviceID);
			LogUtils.d("STRAT videoOpen");
			videoOpen(nSelfUserID, selfDeviceID);
		}
	}

	/**
	 * 屏幕共享通知
	 */
	// private void confMsgNotifyAs(int msgType, long nValue1, long nValue2)
	// {
	// // sharedType = SHARED_TYPE.CONF_SHARED_AS; // 设置共享类型为屏幕共享
	// switch (msgType)
	// {
	// case ConfMsg.COMPT_MSG_AS_ON_SHARING_SESSION:
	// if (nValue1 == ConfDefines.AS_SESSION_OWNER)
	// {
	// if (nValue2 == ConfDefines.AS_ACTION_ADD
	// || nValue2 == ConfDefines.AS_ACTION_MODIFY)
	// {
	// NotifyMsg notifyMsg = new NotifyMsg(
	// NotifyID.DATA_SHARE_START_EVENT);
	// notifyMsg.setRecode(String
	// .valueOf(ConfDefines.IID_COMPONENT_AS));
	// // .valueOf(SHARED_TYPE.CONF_SHARED_AS)); 0709
	// notifyMsg.setMsg(String.valueOf(CONF_SHARED_START));
	// VTMApp.getInstances().sendBroadcast(notifyMsg);
	// }
	// }
	// updateDesktopSharedView();
	// break;
	// case ConfMsg.COMPT_MSG_AS_ON_SHARING_STATE:
	// if (nValue2 == ConfDefines.AS_STATE_NULL)
	// {
	// releaseDesktopShareView();
	//
	// NotifyMsg notifyMsg = new NotifyMsg(
	// NotifyID.DATA_SHARE_START_EVENT);
	// notifyMsg.setRecode(String
	// .valueOf(ConfDefines.IID_COMPONENT_AS));
	// // .valueOf(SHARED_TYPE.CONF_SHARED_AS));0709
	// notifyMsg.setMsg(String.valueOf(CONF_SHARED_STOP));
	// VTMApp.getInstances().sendBroadcast(notifyMsg);
	// }
	// updateDesktopSharedView();
	// break;
	// default:
	// if (msgType == ConfMsg.COMPT_MSG_AS_ON_SCREEN_SIZE
	// || msgType == ConfMsg.COMPT_MSG_AS_ON_SCREEN_DATA)
	// {
	// updateDesktopSharedView();
	// }
	// break;
	// }
	// }
	//

	private void confMsgNotifyAs(ConfMsg msg, ConfExtendMsg extendMsg) {
		int msgType = msg.getMsgType();
		int nValue1 = msg.getnValue1();
		int nValue2 = (int) msg.getnValue2();

		switch (msgType) {
		case ConfMsg.COMPT_MSG_AS_ON_SHARING_SESSION:
			LogUtils.d("COMPT_MSG_AS_ON_SHARING_SESSION");
			if (nValue1 == ConfDefines.AS_SESSION_OWNER) {
				if (nValue2 == ConfDefines.AS_ACTION_ADD
						|| nValue2 == ConfDefines.AS_ACTION_MODIFY) {
					NotifyMsg notifyMsg = new NotifyMsg(
							NotifyID.DATA_SHARE_START_EVENT);
					notifyMsg.setRecode(String
							.valueOf(ConfDefines.IID_COMPONENT_AS));
					notifyMsg.setMsg(String.valueOf(CONF_SHARED_START));
					VTMApp.getInstances().sendBroadcast(notifyMsg);
				}
			}
			updateDesktopSharedView();
			break;
		case ConfMsg.COMPT_MSG_AS_ON_SHARING_STATE:
			LogUtils.d("COMPT_MSG_AS_ON_SHARING_STATE");
			if (nValue2 == ConfDefines.AS_STATE_NULL) {
				releaseDesktopShareView();

				NotifyMsg notifyMsg = new NotifyMsg(
						NotifyID.DATA_SHARE_START_EVENT);
				notifyMsg.setRecode(String
						.valueOf(ConfDefines.IID_COMPONENT_AS));
				notifyMsg.setMsg(String.valueOf(CONF_SHARED_STOP));
				VTMApp.getInstances().sendBroadcast(notifyMsg);
			}
			updateDesktopSharedView();
			break;
		default:
			if (msgType == ConfMsg.COMPT_MSG_AS_ON_SCREEN_SIZE
					|| msgType == ConfMsg.COMPT_MSG_AS_ON_SCREEN_DATA) {
				updateDesktopSharedView();
			}
			break;
		}

	}

	/**
	 * 文档共享通知
	 */
	private void confMsgNotifyDs(ConfMsg msg, ConfExtendMsg extendMsg) {
		int msgType = msg.getMsgType();
		int nValue1 = msg.getnValue1();
		int nValue2 = (int) msg.getnValue2();

		switch (msgType) {
		case ConfMsg.COMPT_MSG_DS_ON_DOC_NEW:
			LogUtils.d("confMsgNotifyDs: COMPT_MSG_DS_ON_DOC_NEW");
			if (dscurrentDocCount == 0) // 新建一个文档共享时，当前共享文档数量为零，即文档共享开始
			{
				NotifyMsg notifyMsg = new NotifyMsg(
						NotifyID.DATA_SHARE_START_EVENT);
				notifyMsg.setRecode(String
						.valueOf(ConfDefines.IID_COMPONENT_DS));
				notifyMsg.setMsg(String.valueOf(CONF_SHARED_START));
				VTMApp.getInstances().sendBroadcast(notifyMsg);
			}
			updateDocSharedView();
			break;
		case ConfMsg.COMPT_MSG_DS_PAGE_DATA_DOWNLOAD: // 文档页面数据已经下载通知
			LogUtils.d("confMsgNotifyDs: COMPT_MSG_DS_PAGE_DATA_DOWNLOAD");
			if ((nValue1 == dscurrentDocID)
					&& ((int) nValue2 == dscurrentPageID)) {
				updateDocSharedView();
			}
			break;
		case ConfMsg.COMPT_MSG_DS_ON_CURRENT_PAGE_IND: // 同步翻页预先通知
			LogUtils.d("confMsgNotifyDs: COMPT_MSG_DS_ON_CURRENT_PAGE_IND");
			if (nValue1 != 0) // nValue1：文档ID
			{
				dscurrentDocID = nValue1;
				dscurrentPageID = (int) nValue2;

				dsSetcurrentpage(nValue1, (int) nValue2);
			}
			updateDocSharedView();
			break;
		case ConfMsg.COMPT_MSG_DS_ANDROID_DOC_COUNT:
			LogUtils.d("confMsgNotifyDs: COMPT_MSG_DS_ANDROID_DOC_COUNT");
			dscurrentDocCount = nValue1;
			if (nValue1 == 0) // 共享文档数量为零，即文档共享停止
			{
				releaseDocShareView();

				NotifyMsg notifyMsg = new NotifyMsg(
						NotifyID.DATA_SHARE_START_EVENT);
				notifyMsg.setRecode(String
						.valueOf(ConfDefines.IID_COMPONENT_DS));
				notifyMsg.setMsg(String.valueOf(CONF_SHARED_STOP));
				VTMApp.getInstances().sendBroadcast(notifyMsg);
			}
			break;
		default:
			if (msgType == ConfMsg.COMPT_MSG_DS_ON_DOC_DEL
					|| msgType == ConfMsg.COMPT_MSG_DS_ON_PAGE_NEW
					|| msgType == ConfMsg.COMPT_MSG_DS_ON_CURRENT_PAGE
					|| msgType == ConfMsg.COMPT_MSG_DS_ON_DRAW_DATA_NOTIFY) {
				updateDocSharedView();
				if (msgType == ConfMsg.COMPT_MSG_DS_ON_DRAW_DATA_NOTIFY) {
					updateDesktopSharedView();
				}
			}
			break;
		}

	}

	/**
	 * 刷新共享屏幕页面
	 */
	private void updateDesktopSharedView() {
		if (desktopSurfaceView != null) {
			desktopSurfaceView.update();
		}
	}

	/**
	 * 刷新共享文档页面
	 */
	private void updateDocSharedView() {
		if (docSurfaceView != null) {
			docSurfaceView.update();
		}
	}

	/**
	 * 判断是否是主线程
	 */
	private boolean isMainThread() {
		return Thread.currentThread().getId() == mMainThreadID;
	}

	/**
	 * 协商回调消息解析
	 * 
	 * @param sourceMsgObj
	 * @return
	 * @throws JSONException
	 */
	private JSONObject createSendMsg(JSONObject sourceMsgObj)
			throws JSONException {

		// String str = " { \"callmode\" : 1 "
		// + " ,\"baudRate\" : \"1152\" "
		// + " ,\"video_device_id\" : \"659171019\" "
		// + " ,\"video_mode\": \"Meeting\" "
		// + " ,\"calltype\" : \"normal\" "
		// + " ,\"vtm_resource\" : \"true\" "
		// + " ,\"tripartite_call_type\" : \"0\" "
		// +
		// ",\"video_session_id\" : \""+confId+"\",\"addr_call_mode\" : \"audioandvideo\""
		// +
		// ", \"Address\" : [{ \"resId\" : \""+phoneNo+"\",\"ResType\" : \"phone\"}"
		// + ",{ \"resId\" : \""+phoneNo+"\",\"ResType\" : \"collaboration\"}"
		// + ",{ \"resId\" : \""+phoneNo+"\",\"ResType\" : \"message\"}"
		// + ",{ \"resId\" : \""+phoneNo+"\",\"ResType\" : \"file\"}"
		// + ",{ \"resId\" : \""+agentNo+"\",\"ResType\" : \"agent\"}"
		// + ",{ \"resId\" : \""+phoneNo+"\",\"ResType\" : \"video\"}]}";

		JSONObject msgToSend = new JSONObject();
		String baudRate = sourceMsgObj.getString("baudRate");
		String callId = sourceMsgObj.getString("callId");
		int callmode = sourceMsgObj.getInt("callmode");
		String calltype = sourceMsgObj.getString("calltype");
		String video_mode = sourceMsgObj.getString("video_mode");
		String addr_call_mode = sourceMsgObj.getString("addr_call_mode"); // 0710

		JSONArray addressObjToSend = new JSONArray();

		JSONObject addressItemPhone = new JSONObject();
		addressItemPhone.put("ResType", "phone");
		addressItemPhone.put("resId", AccountInfo.getInstance().getVtmNo());

		JSONObject addressItemAgent = new JSONObject();
		addressItemAgent.put("ResType", "agent");
		addressItemAgent.put("resId", "");

		JSONObject addressItemCollaboration = new JSONObject();
		addressItemCollaboration.put("ResType", "collaboration");
		addressItemCollaboration.put("resId", AccountInfo.getInstance()
				.getVtmNo());

		JSONObject addressItemMessage = new JSONObject();
		addressItemMessage.put("ResType", "message");
		addressItemMessage.put("resId", AccountInfo.getInstance().getVtmNo());

		JSONObject addressItemFile = new JSONObject();
		addressItemFile.put("ResType", "file");
		addressItemFile.put("resId", AccountInfo.getInstance().getVtmNo());

		JSONObject addressItemVideo = new JSONObject();
		addressItemVideo.put("ResType", "video");
		addressItemVideo.put("resId", AccountInfo.getInstance().getVtmNo());

		JSONObject addressItemWorkNo = new JSONObject();
		addressItemWorkNo.put("ResType", "workNo");
		if (AccountInfo.getInstance().isAnonymous()) {
			addressItemWorkNo.put("resId", "anonymousCard");
		} else {
			addressItemWorkNo
					.put("resId", AccountInfo.getInstance().getVtmNo());
		}

		addressObjToSend.put(0, addressItemPhone);
		addressObjToSend.put(1, addressItemAgent);
		addressObjToSend.put(2, addressItemCollaboration);
		addressObjToSend.put(3, addressItemMessage);
		addressObjToSend.put(4, addressItemFile);
		addressObjToSend.put(5, addressItemVideo);
		addressObjToSend.put(6, addressItemWorkNo);

		msgToSend.put("Address", addressObjToSend);
		msgToSend.put("baudRate", baudRate);
		msgToSend.put("callId", callId);
		msgToSend.put("callmode", callmode);
		msgToSend.put("calltype", calltype);
		msgToSend.put("video_mode", video_mode);
		msgToSend.put("addr_call_mode", addr_call_mode);// 0710
		msgToSend
				.put("video_session_id", AccountInfo.getInstance().getConfId());
		return msgToSend;
	}

	/**
	 * Handle 消息处理
	 * 
	 * @param msg
	 */
	private void handleMsg(Message msg) {
		switch (msg.what) {
		case ConfOper.CONF_OPER_JOIN:
			joinConf();
			break;
		case ConfOper.CONF_OPER_LEAVE:
			leaveConf();
			break;
		case ConfOper.CONF_OPER_TERMINATE:
			terminateConf();
			break;
		case ConfOper.CONF_OPER_LOCK:
			lockConf();
			break;
		case ConfOper.CONF_OPER_UNLOCK:
			unLockConf();
			break;
		case ConfOper.CONF_OPER_KICKOUT: {
			int nUserID = msg.arg1;
			kickout(nUserID);
		}
			break;
		case ConfOper.VIDEO_OPER_SETENCODE_MAXRESOLUTION: {
			int xResolution = msg.arg1;
			int yResolution = msg.arg2;
			setEncodeMaxResolution(xResolution, yResolution);
		}
			break;
		case ConfOper.CONF_OPER_SET_ROLE: {
			int nUserID = msg.arg1;
			int nRole = msg.arg2;
			setRole(nUserID, nRole);
		}
			break;
		case ConfOper.CONF_OPER_REQUEST_ROLE:
			break;
		case ConfOper.CONF_OPER_SEND_DATA:
			Bundle data = msg.getData();
			sendData(msg.arg1, data.getInt("msgID"),
					data.getByteArray("optContext"));
			break;
		case ConfOper.CONF_OPER_LOAD_COMPONENT:
			loadComponent();
			break;
		case ConfOper.VIDEO_OPER_OPEN:
			videoOpen(msg.arg1, (String) msg.obj);
			break;
		case ConfOper.VIDEO_OPER_CLOSE:
			videoClose(msg.arg1, (String) msg.obj);
			break;
		case ConfOper.VIDEO_OPER_PAUSE:
			videoPause(msg.arg1, (String) msg.obj);
			break;
		case ConfOper.VIDEO_OPER_RESUME:
			videoResume(msg.arg1, (String) msg.obj);
			break;
		case ConfOper.VIDEO_OPER_ATTACH:
			videoAttach(msg.arg1, msg.arg2, (String) msg.obj);
			break;
		case ConfOper.VIDEO_OPER_DETACH:
			videoDetach(msg.arg1, msg.arg2, (String) msg.obj);
			break;
		case ConfOper.VIDEO_OPER_SET_CAPTURE_ROTATE:
			videoSetCaptureRotate(msg.arg1, (String) msg.obj);
			break;
		case ConfOper.VIDEO_OPER_SETPARAM:
			videoSetParam(msg.arg1, (String) msg.obj);
			break;
		case ConfOper.VIDEO_OPER_GETDEVICECAPBILITY_INFO:
			videGetDevicecApbilityNum(msg.arg1, (String) msg.obj);
			break;
		case ConfOper.VIDEO_OPER_GETDEVICECAPBILITY_NUM:
			videGetDevicecApbilityInfo(msg.arg1, (String) msg.obj);
			break;
		case ConfOper.VIDEO_OPER_GETPARAM:
			videoGetParam(msg.arg1, (String) msg.obj);
			break;
		case ConfOper.VIDEO_OPER_GETDEVICE_NUM:
			getVideoDeviceNum();
			break;
		case ConfOper.VIDEO_OPER_GETDEVICE_INFO:
			getVideoDeviceInfo();
			break;
		case ConfOper.VIDEO_OPER_NOTIFY: {
			videoNotifyOpen(msg.arg2, (String) msg.obj, msg.arg1);
		}
			break;
//		case ConfOper.VIDEO_OPER_SNAPSHOT:
//			videoSnapShot(msg.arg1, (String) msg.obj);
//			break;
//		case ConfOper.VIDEO_OPER_RENDER_SNAPSHOT:
//			videoRenderSnapShot(msg.arg1, msg.arg2, (String) msg.obj);
//			break;
		case ConfOper.AUDIO_OPER_SET_AUDIOPARAM:
			setAudioParam();
			break;
		case ConfOper.AUDIO_OPER_OPEN_MIC:
			openMic(0);
			break;
		case ConfOper.AUDIO_OPER_CLOSE_MIC:
			closeMic();
			break;
		case ConfOper.AUDIO_OPER_MUTE_MIC: {
			muteMic(msg.arg1);
		}
			break;
		case ConfOper.AUDIO_OPER_OPEN_SPEAKER: {
			int speakerID = msg.arg1;
			openSpeaker(speakerID);
		}
			break;
		case ConfOper.AUDIO_OPER_CLOSE_SPEAKER:
			closeSpeaker();
			break;
		case ConfOper.AUDIO_OPER_MUTE_SPEAKER: {
			muteSpeaker(msg.arg1);
		}
			break;
		case ConfOper.DS_OPER_SET_CURRENTPAGE:
			dsSetcurrentpage(msg.arg1, msg.arg2);
			break;
		case ConfOper.CHAT_OPER_SEND: {
			String str = msg.obj.toString();
			chatSendMsg(msg.arg1, str);
		}
			break;
		case ConfOper.AS_OPER_SET_PARAM: {
			int value = msg.arg1;
			asSetParam(value);
		}
			break;
		default:
			break;
		}
	}

	/**
	 * 该类主要是确保操作是在主线程中
	 */
	private class WorkThread extends Thread {
		public Handler mHandler;

		public void run() {
			Looper.prepare();

			mHandler = new Handler() {
				public void handleMessage(Message msg) {
					handleMsg(msg);
				}
			};
			confThreadStartSemaphore.release();

			Looper.loop();
		}

		public Handler getHandler() {
			return mHandler;
		}
	}

	/**
	 * 添加视频设备参数
	 * 
	 * @param key
	 *            视频设备ID
	 * @param param
	 *            视频参数
	 */
	private void addVideoParamsMap(String key, VideoParam param) {
		List<VideoParam> params = videoParamsMap.get(key);
		if (null == params || params.size() == 0) {
			params = new ArrayList<VideoParam>();
		}
		params.add(param);
		videoParamsMap.put(key, params);
	}

	/**
	 * 获取当前视频设备能力参数
	 * 
	 * @return
	 */
	public List<VideoParam> getCurrDevicecAbility() {
		LogUtils.d("getCurrDevicecAbility deviceId:"
				+ AccountInfo.getInstance().getCurrDeviceId() + " ,maps:"
				+ videoParamsMap.size());
		return videoParamsMap.get(AccountInfo.getInstance().getCurrDeviceId());
	}

	/**
	 * 清除视频设备能力参数缓存
	 */
	private void clearVideoParamsMap() {
		if (null != videoParamsMap) {
			videoParamsMap.clear();
		}
	}

}
