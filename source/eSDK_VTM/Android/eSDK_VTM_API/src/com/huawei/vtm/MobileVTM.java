package com.huawei.vtm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.ViewGroup;

import com.huawei.meeting.ConfDefines;
import com.huawei.meeting.MsgID;
import com.huawei.meeting.func.ConferenceMgr;
import com.huawei.vtm.authentic.Authentic;
import com.huawei.vtm.call.CallManager;
import com.huawei.vtm.call.CallSession;
import com.huawei.vtm.common.AccountInfo;
import com.huawei.vtm.common.Constants;
import com.huawei.vtm.common.Constants.CALLMODE;
import com.huawei.vtm.common.Constants.CC_STATE;
import com.huawei.vtm.common.Constants.VIDEO_SHOW_MODE;
import com.huawei.vtm.common.SystemSetting;
import com.huawei.vtm.common.UserInfo;
import com.huawei.vtm.common.VideoParam;
import com.huawei.vtm.service.VTMApp;
import com.huawei.vtm.utils.LogUtils;
import com.huawei.vtm.utils.Tools;

/**
 * API接口调用类
 * 
 * @author lWX169831
 * 
 */
public class MobileVTM {

	private String TAG = "MobileVTM";

	private static MobileVTM instance;

	/**
	 * 获取MobileVTM的单实例. <br>
	 * 
	 * @return 返回MobileVTM的实例.
	 * 
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public synchronized static MobileVTM getInstance() {
		if (null == instance) {
			instance = new MobileVTM();
		}

		return instance;
	}

	private MobileVTM() {
		LogUtils.d(TAG, "version = " + Constants.VERSION);
	}

	/**
	 * 初始化SDK服务. <br>
	 * 
	 * @param app
	 *            该工程的Application,用于启动服务.
	 * @return <li>0 初始化成功. <br><li>-1 初始化VoIP失败. <br><li>-2 初始化Meeting失败.
	 * 
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public int initSDK(Application app) {
		LogUtils.d(TAG, "initSDK | app = " + app);
		int retCode = 0;
		VTMApp.getInstances().initApp(app);

		CallManager callManager = CallManager.getInstance();
		callManager.tupInit();

		// 这是原来C20的初始化方式
		// retCode = VoIP.getInstance().init(app, true) ? 0 : -1;

		return retCode;
	}

	/**
	 * 停止SDK相关服务. <br>
	 * 
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public void stopSDK() {
		LogUtils.d(TAG, "stopSDK");

		CallManager callManager = CallManager.getInstance();
		if (callManager != null) {
			callManager.tupUninit();
		}
		// stopAudioDeviceService();

		// C20 stop SDK
		// VoIP.getInstance().unInit();
		// VTMApp.getInstances().stopAudioDeviceService();
	}

	/**
	 * 设置服务器地址设置. <br>
	 * 对服务器ip地址和端口号进行设置.
	 * 
	 * @param ip
	 *            服务器ip地址.
	 * @param port
	 *            服务器端口号.
	 * @param isHttps
	 *            是否是Https协议,<b>true</b>为Https,<b>false</b>为Http.
	 * 
	 * @return <b>true</b> 接口调用成功;<b>false</b> 接口调用失败,检查ip地址和端口号格式是否正确.
	 * 
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean setHostAddr(String ip, int port, boolean isHttps) {
		LogUtils.d(TAG, "setHostAddr | " + "ip = " + ip + ", port = " + port);
		SystemSetting.getInstance().initServer(ip, port);
		SystemSetting.getInstance().setHttps(isHttps);

		if (!Tools.checkIP() || !Tools.checkPort()) {
			return false;
		}
		return true;
	}

	/**
	 * 登录. <br>
	 * 登录MCC服务器.
	 * 
	 * @param userId
	 *            用户Id.
	 * @param userPwd
	 *            用户密码.
	 * 
	 * @return <b>true</b> 接口调用成功;<b>false</b> 接口调用失败,检查ip地址和端口号格式是否正确.
	 * 
	 * @attention 登录之前需要先对服务器地址进行设置.
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean login(String userId, String userPwd) {
		/*
		 * 用户类型，只允许以下两种类型： <br><li>terminal：机具终端. <br><li>user：用户（柜员、管理员、质检员）.
		 * <br><li>mobile：移动终端，该版本默认为 mobile.
		 */

		// <- begin 2014-11-4 添加NAT穿透，修改UserType "terminal" 为 "mobile" ->
		String userType = Constants.USERTYPE_TERMINAL;
		// String userType = Constants.USERTYPE_MOBILE;
		// <- 2014-11-4 end ->

		LogUtils.d(TAG, "login | " + "userId = " + userId);
		return Authentic.login(userId, userPwd, userType);
	}

	/**
	 * 登出. <br>
	 * 登出MCC服务器.
	 * 
	 * @return <b>true</b> 接口调用成功;<b>false</b> 接口调用失败,检查ip地址和端口号格式是否正确.
	 * 
	 * @attention 该接口的调用需要以已登录MCC服务器为前提.
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean logout() {
		LogUtils.d(TAG, "logout");
		// VoIP.getInstance().logout(VoIP.getInstance().getRegId());
		CallManager.getInstance().unRegister();
		return Authentic.logout();
	}

	/**
	 * 
	 * 获取登录状态. <br>
	 * 
	 * @return 返回<b>true</b>已登录,<b>false</b>未登录.
	 * 
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean isLogin() {
		LogUtils.d(TAG, "isLogin");
		return AccountInfo.getInstance().isLogin();
	}

	/**
	 * 
	 * 设置视频参数. <br>
	 * 设置视频本地视频传输到远端的参数.
	 * 
	 * @param videoParam
	 *            视频参数，要求必须包含x、y分辨率，帧率等属性
	 * @return
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean setVideoParam(VideoParam videoParam) {
		// return false;
		// 返回 <b>0</b> 设置成功, <b>1</b> 获取用户信息错误, <b>2</b> 参数错误, <b>11</b>
		// 状态错误(当前不在通话中 ).
		if (CALLMODE.CALL_TALKING != AccountInfo.getInstance().getCallMode()) {
			return false;
		}

		if (null == videoParam) {
			return false;
		}

		LogUtils.d(TAG,
				"setVideoParams | VideoParam: xRes = " + videoParam.getxRes()
						+ ", yRes = " + videoParam.getyRes() + ", nFrame = "
						+ videoParam.getnFrame());
		SystemSetting.getInstance().setVideoParam(videoParam);

		int nUserID;
		try {
			nUserID = Tools.parseInt(AccountInfo.getInstance()
					.getSelfUserInfo().getUserId());
		} catch (NumberFormatException e) {
			LogUtils.e(TAG, e.toString());
			return false;
		}
		String deviceID = AccountInfo.getInstance().getCurrDeviceId();

		if (deviceID == null) {
			return false;
		}
		boolean ret = ConferenceMgr.getInstance().videoSetParam(nUserID,
				deviceID);

		if (ret) {
			ConferenceMgr.getInstance().updateVideoView(nUserID, deviceID);
		}

		return ret;
	}

	/**
	 * 本地视频旋转
	 * 
	 * @param rotate
	 *            旋转角度
	 * @return true:接口调用成功;false:接口调用失败
	 */
	public boolean setVideoRotate(int rotate) {
		if (CALLMODE.CALL_TALKING != AccountInfo.getInstance().getCallMode()) {
			return false;
		}

		int nUserID;
		try {
			nUserID = Tools.parseInt(AccountInfo.getInstance()
					.getSelfUserInfo().getUserId());
		} catch (NumberFormatException e) {
			LogUtils.e(TAG, e.toString());
			return false;
		}
		String deviceID = AccountInfo.getInstance().getCurrDeviceId();

		if (deviceID == null) {
			return false;
		}

		boolean ret = ConferenceMgr.getInstance().setCaptureRotate(nUserID,
				deviceID, rotate);

		return ret;
	}

	/**
	 * 设置远端视频参数 <br>
	 * 根据VTM端带宽，调节远端视频传过来的质量
	 * 
	 * @param param
	 *            视频参数，要求必须包含带宽(单位:kbps)，x、y分辨率，帧率等属性。
	 * 
	 * @return
	 * 
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean setRemoteVideoParam(VideoParam param) {
		// 返回 <b>0</b> 设置成功, <b>1</b> 获取用户信息错误, <b>2</b> 参数错误, <b>11</b>
		// 状态错误(当前不在通话中 ).
		if (CALLMODE.CALL_TALKING != AccountInfo.getInstance().getCallMode()) {
			return false;
		}
		List<UserInfo> infos = AccountInfo.getInstance().getVTAUserInfos();
		if (!infos.isEmpty()) {
			if (null == param) {
				return false;
			}
			int nUserID = Tools.parseInt(infos.get(0).getUserId());
			int msgID = MsgID.CONF_BANDWIDTH_INFO_MSG;

			JSONObject jObject = new JSONObject();
			try {
				jObject.put("BW", param.getnBandwidth() + "kbps");
				jObject.put("X", param.getxRes());
				jObject.put("Y", param.getyRes());
				jObject.put("FRate", param.getnFrame());
			} catch (JSONException e) {
				LogUtils.e(TAG, e.toString());
				return false;
			}

			byte[] optContext = jObject.toString().getBytes(
					Charset.forName(Constants.CHARSET_GBK));

			LogUtils.d(TAG, "sendMsg | userId = " + nUserID + ", msgID = "
					+ msgID + ", optContext = " + jObject.toString());

			boolean ret = ConferenceMgr.getInstance().sendData(nUserID, msgID,
					optContext);

			return ret;
		}
		return false;
	}

	/**
	 * 
	 * 设置视频视图SurfaceView的装载容器ViewGroup. <br>
	 * 设置会议中视频显示容器.
	 * 
	 * @param context
	 *            上下文.
	 * @param localView
	 *            显示本地视频的ViewGroup.
	 * @param remoteView
	 *            显示远端视频的ViewGroup.
	 * 
	 * @attention 该接口需要在会议被拉起之前调用,才能确保视频正常显示.
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public void setVideoContainer(Context context, ViewGroup localView,
			ViewGroup remoteView) {
		LogUtils.d(TAG, "setVideoContainer | context = " + context
				+ "，localView = " + localView + "，remoteView" + remoteView);
		ConferenceMgr.getInstance().setVideoContainer(context, localView,
				remoteView);
	}

	/**
	 * 
	 * 接收远端屏幕共享数据. <br>
	 * 
	 * @param context
	 *            上下文.
	 * @param sharedView
	 *            显示屏幕共享数据的ViewGroup.
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public void setDesktopContainer(Context context, ViewGroup sharedView) {
		LogUtils.d(TAG, "recvConferenceShare | context = " + context
				+ "，sharedView = " + sharedView);
		ConferenceMgr.getInstance().setSharedViewContainer(context, sharedView,
		// SHARED_TYPE.CONF_SHARED_AS);0709
				ConfDefines.IID_COMPONENT_AS);
	}

	/**
	 * 
	 * 接收远端文档共享数据. <br>
	 * 
	 * @param context
	 *            上下文.
	 * @param sharedView
	 *            显示文档共享数据的ViewGroup.
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public void setFileContainer(Context context, ViewGroup sharedView) {
		LogUtils.d(TAG, "recvConferenceShare | context = " + context
				+ "，sharedView = " + sharedView);
		ConferenceMgr.getInstance().setSharedViewContainer(context, sharedView,
		// SHARED_TYPE.CONF_SHARED_DS);0709
				ConfDefines.IID_COMPONENT_DS);
	}

	/**
	 * 建立通话. <br>
	 * 发起VOIP呼叫,拉起会议.
	 * 
	 * @param calledNum
	 *            对方号码.
	 * @param callType
	 *            通话类型 音频 {@link FastVoIPConstant.AUDIOTYPE}; 视频
	 *            {@link FastVoIPConstant.VIDEOTYPE}.
	 * @param callInfo
	 *            呼叫随路数据，预留字段
	 * 
	 * @return 返回通话id,错误返回-1.
	 * 
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public int makeCall(String calledNum, String callType, String callInfo) {
		LogUtils.d(TAG, "makeCall | " + "calledNum = " + calledNum
				+ ", callType = " + callType + ", callInfo = " + callInfo);

		// AccountInfo.getInstance().setCallInfo(callInfo);

		CallSession callSession = CallManager.getInstance().makeCall(calledNum,
				"", false);
		if (callSession != null) {
			return 0;
		} else {
			return -1;
		}

		// return VoIP.getInstance().call(calledNum, callType);
	}

	/**
	 * 发起匿名呼叫请求. <br>
	 * 
	 * @param accessCode
	 *            接入码
	 * @param mediaType
	 *            媒体类型 参考 {@link Constants.MEDIA_TYPE}
	 * @param callInfo
	 *            呼叫随路数据，预留字段
	 * 
	 * @return <b>true</b> 接口调用成功;<b>false</b> 接口调用失败,检查ip地址和端口号格式是否正确.
	 * 
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean anonymousCall(String accessCode, String mediaType,
			String callInfo) {
		LogUtils.d(TAG, "makeAnonymousCall | " + "accessCode = " + accessCode
				+ ", mediaType = " + mediaType + ", callInfo = " + callInfo);

		AccountInfo.getInstance().setCallInfo(callInfo);
		AccountInfo.getInstance().setIsGetIasEvent(true);
		LogUtils.d(TAG, "anonymousCall set isGetIasEvent true!");
		AccountInfo.getInstance().setAnonymous(true);
		return Authentic.requestConnect(accessCode, mediaType, callInfo);
	}

	/**
	 * 暂停本地视频. <br>
	 * 会议中暂停本地视频.
	 * 
	 * @return 返回<b>true</b>成功,<b>false</b>失败.
	 * 
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean videoPause() {
		LogUtils.d(TAG, "VideoPause");
		UserInfo userInfo = AccountInfo.getInstance().getSelfUserInfo();
		if (userInfo != null) {
			int nUserID = Tools.parseInt(userInfo.getUserId());
			String deviceID = userInfo.getCurrDeviceId();
			if (deviceID != null) {
				return ConferenceMgr.getInstance()
						.videoPause(nUserID, deviceID);
			}
		}
		return false;
	}

	/**
	 * 恢复已暂停的本地视频. <br>
	 * 本地视频被暂停后恢复视频的播放.
	 * 
	 * @return 返回<b>true</b>成功,<b>false</b>失败.
	 * 
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean videoResume() {
		LogUtils.d(TAG, "VideoResume");
		UserInfo userInfo = AccountInfo.getInstance().getSelfUserInfo();
		if (userInfo != null) {
			int nUserID = Tools.parseInt(userInfo.getUserId());
			String deviceID = userInfo.getCurrDeviceId();
			if (deviceID != null) {
				return ConferenceMgr.getInstance().videoResume(nUserID,
						deviceID);
			}
		}
		return false;
	}

	/**
	 * 设置扬声器音量. <br>
	 * 会议中设置扬声器的音量.
	 * 
	 * @param audioVolume
	 *            取值范围为[0, 100].
	 * @return 返回<b>true</b>设置成功,<b>false</b>设置失败.
	 * 
	 * @attention 音频流为AudioManager.STREAM_MUSIC.
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean setAudioVolume(int audioVolume) {
		LogUtils.d(TAG, "setAudioVolume | audioVolume = " + audioVolume);
		// return VoIP.getInstance().setAudioVolume(audioVolume);
		return true;
	}

	/**
	 * 获取扬声器音量. <br>
	 * 会议中获取扬声器的音量.
	 * 
	 * @return 返回扬声器音量值,范围为[0,100].
	 * 
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public int getAudioVolume() {
		LogUtils.d(TAG, "getAudioVolume");
		// return VoIP.getInstance().getAudioVolume();
		return 1;
	}

	/**
	 * 麦克风静音设置. <br>
	 * 会议中对进行麦克风静音和取消静音设置.
	 * 
	 * @param isMute
	 *            如果为 <b>true</b>,则设置静音;反之为<b>false</b>,则取消静音设置.
	 * @return 返回<b>true</b>设置成功,<b>false</b>设置失败.
	 * 
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean mute(boolean isMute) {
		LogUtils.d(TAG, "mute | isMute = " + isMute);
		// return VoIP.getInstance().mute(0, isMute);
		return CallManager.getInstance().mute(0, isMute);
	}

	/**
	 * 释放通话. <br>
	 * 释放VOIP呼叫和会议,调用该方法可离开会议.
	 * 
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public void releaseCall() {
		LogUtils.d(TAG, "releaseCall");
		if (AccountInfo.getInstance().isAnonymous()) {
			AccountInfo.getInstance().setIsGetIasEvent(false);
			LogUtils.d(TAG, "releaseCall set isGetIasEvent false!");
			// VoIP.getInstance().stop();
			Authentic.notifySessionState(CC_STATE.RELEALSE);
		}

		ConferenceMgr.getInstance().toleaveConf();

		CallManager.getInstance().releaseCall();

		AccountInfo.getInstance().setCallMode(CALLMODE.CALL_CLOSED);
	}

	/**
	 * 
	 * 获取视频设备能力 <br>
	 * 获取当前视频设备参数列表
	 * 
	 * @return
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public List<VideoParam> getVideoParams() {
		LogUtils.d(TAG, "getCurrDevicecAbility");
		return ConferenceMgr.getInstance().getCurrDevicecAbility();
	}

	/**
	 * 
	 * 发送信令消息
	 * 
	 * @param content
	 *            消息内容
	 * @return
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean sendMsg(String message) {
		List<UserInfo> infos = AccountInfo.getInstance().getVTAUserInfos();
		if (!infos.isEmpty()) {
			if (null == message) {
				message = "";
			}
			int nUserID = Integer.parseInt(infos.get(0).getUserId());
			int msgID = MsgID.CONF_COMMON_SIG_MSG;

			LogUtils.d(TAG, "sendMsg | userId = " + nUserID + ", msgID = "
					+ msgID + ", optContext = " + message);

			byte[] optContext = message.getBytes(Charset
					.forName(Constants.CHARSET_GBK));

			return ConferenceMgr.getInstance().sendData(nUserID, msgID,
					optContext);
		}
		return false;
	}

	/**
	 * 是否允许三方进入会议 <br>
	 * 
	 * @param msgID
	 *            消息ID。参考MsgID，允许进入：MsgID.MESSAGE_CMD_TRIPARTITE_ACK；不允许进入：MsgID
	 *            . MESSAGE_CMD_TRIPARTITE_FINISH
	 * 
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public void setIsJoinTripartite(int msgID) {
		List<UserInfo> infos = AccountInfo.getInstance().getVTAUserInfos();
		if (!infos.isEmpty()) {
			int nUserID = Tools.parseInt(infos.get(0).getUserId());

			LogUtils.d(TAG, "sendMsg | userId = " + nUserID + ", msgID = "
					+ msgID);

			ConferenceMgr.getInstance().sendRespondData(nUserID, msgID);
		}
	}

	/**
	 * 切换视频显示 <br>
	 * 
	 * @param attachUserId
	 *            需要显示视频的userId
	 * @return
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean videoSwitch(String attachUserId) {
		if (null == attachUserId
				|| attachUserId.equals("")
				|| !AccountInfo.getInstance().getUserMap()
						.containsKey(attachUserId)) {
			return false;
		}
		if (attachUserId.equals(AccountInfo.getInstance().getSelfUserInfo()
				.getUserId())) {
			return false;
		}

		return ConferenceMgr.getInstance().videoSwitch(attachUserId);
	}

	/**
	 * 会议前或会议中设置视频显示模式 <br>
	 * 
	 * @param videoShowMode
	 *            视频的显示模式，参考{@link Constants.VIDEO_SHOW_MODE}.<br>
	 *            {@link VIDEO_SHOW_MODE.FULL}:布满窗口;
	 *            {@link VIDEO_SHOW_MODE.PRORATE} :按视频的比例进行显示， 其余部分以黑色填充;
	 *            {@link VIDEO_SHOW_MODE.CLIP}:按窗口大小进行裁剪.
	 * @return
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean setVideoShowMode(int videoShowMode) {
		return ConferenceMgr.getInstance().videoShowModeSwitch(videoShowMode);
	}

	/**
	 * 设置Token值 <br>
	 * 
	 * @param token
	 * @return
	 * @attention 需要在发起匿名呼叫之前设置，否则无效
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean setToken(String token) {
		return SystemSetting.getInstance().setIdentifyCode(token);
	}

	/**
	 * 设置自己共享屏幕的权限
	 * 
	 * @param dwAction
	 *            权限，参考ConfConstants.AS_ACTION，释放：AS_ACTION .AS_ACTION_DELETE;
	 *            拥有：AS_ACTION.AS_ACTION_ADD
	 * @return
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean asSetOwner(int dwAction) {
		LogUtils.d(TAG, "asSetOwner | dwAction = " + dwAction);

		int nUserID = Integer.parseInt(AccountInfo.getInstance()
				.getSelfUserInfo().getUserId());
		return ConferenceMgr.getInstance().asSetOwner(nUserID,
		// AS_ACTION.AS_ACTION_ADD);0709
				ConfDefines.AS_ACTION_ADD);
	}

	/**
	 * VTM开始共享屏幕
	 * 
	 * @return
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean asStart() {
		LogUtils.d(TAG, "asStart");
		return ConferenceMgr.getInstance().asStart();
	}

	/**
	 * VTM停止共享屏幕
	 * 
	 * @return
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean asStop() {
		LogUtils.d(TAG, "asStop");
		return ConferenceMgr.getInstance().asStop();
	}

	/**
	 * 是否设置共享分辨率为之前一般
	 * 
	 * @param value
	 *            1:开启；0：关闭
	 * @return
	 * @attention 无
	 * @par 示例
	 * @code
	 * @endcode
	 * @see
	 * @since V100R005
	 */
	public boolean asSetResolveHalf() {
		return ConferenceMgr.getInstance().asSetParam(2);
	}

	public boolean asSetResolveQuarter() {
		LogUtils.d(TAG, "asSetResolveQuarter");
		return ConferenceMgr.getInstance().asSetParam(4);
	}

	/**
	 * 入会后屏幕设置屏幕共享的矩形区域的宽度和高度，以像素点为单位。共享的矩形的左上角目前只支持（0，0）
	 * 
	 * @param width
	 *            需要共享的宽度
	 * @param height
	 *            需要共享的高度
	 * @return 是否设置成功
	 */
	public boolean asSetShareRange(int width, int height) {
		return ConferenceMgr.getInstance().asSetShareRange(0, 0, width, height);
	}


	public boolean videoRenderSnapShot(String filename) {
//		long userid = Long.parseLong(AccountInfo.getInstance()
//				.getVTAUserInfos().get(0).getUserId());
//		String deviceid = AccountInfo.getInstance().getVtaDeviceId();
		long userid = Long.parseLong(AccountInfo.getInstance()
				.getSelfUserInfo().getUserId());
		String deviceid = AccountInfo.getInstance().getCurrDeviceId();
		String fname = Environment.getExternalStorageDirectory().toString()
				+ File.separator + Constants.VTM_LOG_FILE + File.separator
				+ filename + ".jpg";
		return ConferenceMgr.getInstance().videoRenderSnapShot(userid,
				Long.parseLong(deviceid), fname);
	}


	public void setConfContainer(Context context, ViewGroup localView) {
		LogUtils.d(TAG, "setConfContainer | context = " + context
				+ "，localView = " + localView);
		ConferenceMgr.getInstance().setConfContainer(context, localView);
	}


	public void getCallQueueInfo() {
		Authentic.getCallQueueInfo();
	}

	// 0817添加pingIAS的接口
	public String getPingIAS() {
		try {
			result = new PingIAS().execute().get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return result;
	}

	private class PingIAS extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			String s = "";
			s = Ping();
			return s;
		}
	}

	String result = "";

	public String Ping() {

		String iasIp = SystemSetting.getInstance().getServerIp();
		Process p;
		try {
			// ping -c 3 -w 15 中 ，-c 是指ping的次数 2是指ping 2次 ，-w 15
			// 以秒为单位指定超时间隔，是指超时时间为15秒
			p = Runtime.getRuntime().exec("ping -c 3 -w 15 " + iasIp);
			int status = p.waitFor();
			InputStream input = p.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(input));
			StringBuffer buffer = new StringBuffer();
			String line = "";
			while ((line = in.readLine()) != null) {
				buffer.append(line);
			}
			// System.out.println("Return ============" + buffer.toString());
			int start = buffer.indexOf(":") + 1;
			String res = buffer.substring(start);
			if (status == 0) {
				result = "success," + res;
				LogUtils.d("MobileVTM ping result:" + result);
			} else {
				result = "faild,status=" + status;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return result;
	}

}
