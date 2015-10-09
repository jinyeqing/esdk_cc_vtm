package com.huawei.vtm.call;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tupsdk.TupAudioQuality;
import tupsdk.TupAudioStatistic;
import tupsdk.TupCall;
import tupsdk.TupCallCfgAudioVideo;
import tupsdk.TupCallCfgSIP;
import tupsdk.TupCallLocalQos;
import tupsdk.TupCallManager;
import tupsdk.TupCallNotify;
import tupsdk.TupCallParam;
import tupsdk.TupCallQos;
import tupsdk.TupComFunc;
import tupsdk.TupDevice;
import tupsdk.TupMsgWaitInfo;
import tupsdk.TupRegisterResult;
import tupsdk.TupVideoQuality;
import tupsdk.TupVideoStatistic;
import android.os.Environment;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.huawei.meeting.func.ConferenceMgr;
import com.huawei.vtm.NotifyID;
import com.huawei.vtm.NotifyMsg;
import com.huawei.vtm.StringUtils;
import com.huawei.vtm.call.data.VOIPConfigParamsData;
import com.huawei.vtm.call.data.VideoCaps;
import com.huawei.vtm.call.data.VoiceQuality;
import com.huawei.vtm.common.AccountInfo;
import com.huawei.vtm.common.Constants;
import com.huawei.vtm.common.Constants.CALLMODE;
import com.huawei.vtm.service.HandReceiverUtil;
import com.huawei.vtm.service.VTMApp;
import com.huawei.vtm.utils.LogUtils;
import com.huawei.vtm.utils.Tools;

public final class CallManager implements TupCallNotify {

	private boolean initFlag = false;

	private static CallManager instance;

	TupCallManager tupManager;

	public static final String TAG = "[TUPC30]";

	private static final int audioMin = 10500;

	private static final int audioMax = 10519;

	private static final int videoMin = 10580;

	private static final int videoMax = 10599;

	private static final int FORCE_OPEN = 1;
	private TupCallCfgAudioVideo tpCllCfgAdVd = null;

	/** 硬解码默认码率 */
	public static final int MAX_DATARATE_HARDCODEC = 512;

	/** 软解码默认码率 */
	public static final int MAX_DATARATE_SOFTCODEC = 256;

	/**
	 * 在登录时协商一次后，后面不应该再去修改，否则会入不了会 硬编码能力下最大支持512，软编码能力下最大支持256
	 */
	private int maxBw = MAX_DATARATE_SOFTCODEC;

	/**
	 * 在登录时协商一次后，后面不应该再去修改，否则会入不了会 可选，解码器处理的图像格式。
	 * 1：SQCIF格式；2：QCIF格式；3：CIF格式；4：4CIF格式；5：16CIF格式；
	 * 6：QQVGA格式；7：QVGA格式；8：VGA格式；9：720P。
	 */
	private int decodeFrameSize = 3;

	private int minDataRate = 1;

	/**
	 * 在登录时协商一次后，后面不应该再去修改，否则会入不了会 硬编码能力下最大支持512，软编码能力下最大支持256
	 */
	private int maxDataRate = MAX_DATARATE_SOFTCODEC;

	/**
	 * 缓存Call集合， 可以扩展到多路通话，目前只有一路
	 */
	private Map<Integer, CallSession> calls = null;

	/**
	 * 是否在等待注销的 ACK
	 */
	private boolean isWaitingUnRegisterAck = false;

	private List<String> unInterruptSessionIds = new ArrayList<String>();

	/** 显示本地视频的 SurfaceView **/
	private SurfaceView svLocalSurfaceView;

	/** 用于装载本地视频的 ViewGroup **/
	private ViewGroup mLocalContainer;

	/**
	 * VOIP 注册状态
	 */
	public enum State {
		UNREGISTE, // 未注册
		REGISTING, // 注册过程中 包括注册失败 刷新注册
		REGISTED, // 注册成功
	}

	public void saveUninterruptIds(String sessionId) {
		unInterruptSessionIds.add(sessionId);

	}

	public synchronized static CallManager getInstance() {
		if (instance == null) {
			instance = new CallManager();
		}
		return instance;
	}

	/**
	 * VOIP 参数对象
	 */
	private VOIPConfigParamsData voipConfig = new VOIPConfigParamsData();

	private SIPRegister register = new SIPRegister();

	private CallSession currentCallSession = null;

	private CallManager() {
		calls = new ConcurrentHashMap<Integer, CallSession>();

		tupManager = new TupCallManager(this, VTMApp.getInstances()
				.getApplication());
		loadSo();

		// codecParams = new VideoCaps.CodecParams();
		tpCllCfgAdVd = new TupCallCfgAudioVideo();
	}

	public SIPRegister getRegister() {
		return register;
	}

	public static byte[] tupRsaPublicEncrypt(String strSrcData,
			String strKeyPath) {
		TupComFunc comFunc = new TupComFunc();
		return comFunc.rsaPublicEncrypt(strSrcData, strKeyPath);
	}

	public VOIPConfigParamsData getVoipConfig() {
		return voipConfig;
	}

	/**
	 * Function: 发送注册消息
	 */
	public void register(boolean delay) {
		boolean is3GConnect = StringUtils.is3GConnect();
		boolean is3GAbility = getVoipConfig().isNotWIFIAbility();
		if (is3GConnect && !is3GAbility) {
			LogUtils.d("VTMService",
					"No Permission to use VOIP in 3G, register discard !  "
							+ "Please check with the  WIFI connection");
			return;
		}
		registerVoip();
	}

	/**
	 * 发起SIP注册 (UI 发起注册，需要自动重新config， 本地IP地址可能切换)
	 */
	public void registerVoip() {
		// register.isConfigReady = false;// 重新CONFIG;
		register.registerVoip();
	}

	/**
	 * Function: 注销VOIP 对外接口
	 * 
	 * @author luotianjia 00186254/huawei
	 */
	public void unRegister() {
		unRegistVoip();
	}

	/**
	 * 注销SIP注册
	 */
	public void unRegistVoip() {
		// stopRegister();
		register.unRegisterVOIP();
	}

	/**
	 * 获取当前SIP注册状态
	 * 
	 * @return
	 */
	public State getStatus() {
		if (tupManager.getRegState() == TupCallParam.CALL_E_REG_STATE.CALL_E_REG_STATE_REGISTERED) {
			return State.REGISTED;
		}

		return State.UNREGISTE;
	}

	/**
	 * Function: 加载静态的 SO 库
	 * 
	 * @author luotianjia 00186254/huawei
	 * @return void
	 */
	private void loadSo() {

		// 加载公共API
		// System.loadLibrary("comFunc");
		// 加载SIP信令相关的so
		tupManager.loadLib();

		// 加载云盘 HTTP 相关的so
		System.loadLibrary("tup_tupcore");
		System.loadLibrary("tup_httptrans");
		System.loadLibrary("tup_httpofflinefile");

		// 配置 HME 的媒体接口
		tupManager.setAndroidObjects();
		// 配置TUP 日志

		String logFile = Environment.getExternalStorageDirectory().toString()
				+ File.separator + Constants.VTM_LOG_FILE;

		File dirFile = new File(logFile);
		if (!(dirFile.exists()) && !(dirFile.isDirectory())) {
			if (dirFile.mkdir()) {
				LogUtils.d("CALL", "mkdir " + dirFile.getPath());
			}
		}

		tupManager.logStart(3, 5 * 1000, 1, logFile);

		// 加载会议组件库
		System.loadLibrary("TupConf");
	}

	@Override
	public void onCallComing(TupCall call) {
		LogUtils.d("CallManager onCallComing");
		if (tupManager.getRegState() != TupCallParam.CALL_E_REG_STATE.CALL_E_REG_STATE_REGISTERED) {
			call.endCall();
			return;
		}
		CallSession callSession = new CallSession(call);
		callSession.setCallManager(this);
		calls.put(callSession.getTupCall().getCallId(), callSession);
	}

	@Override
	public void onRegisterResult(TupRegisterResult result) {

		System.out.println(" onRegisterResult");

		if (tupManager.getRegState() == TupCallParam.CALL_E_REG_STATE.CALL_E_REG_STATE_REGISTERED) {

			NotifyMsg notifyMsg = new NotifyMsg(
					NotifyID.TERMINAL_VOIP_REGISTER_EVENT);
			notifyMsg.setRecode("0");
			notifyMsg.setMsg("");
			VTMApp.getInstances().sendBroadcast(notifyMsg);
			System.out
					.println("ggg TupHelper - sendBroadcast : TERMINAL_VOIP_REGISTER_EVENT");
		}
	}

	@Override
	public void onSessionModified(TupCall call) {
		LogUtils.d("CallManager onSessionModified");
		int focus = call.getIsFocus();
		String confID = call.getServerConfID();
		int orientType = call.getOrientType();

		if (call.getIsFocus() == 1) {
			CallSession session = calls.get(call.getCallId());
		}
		// 非会议
		else {
			if (call.getReinvieType() == 1 || call.getReinvieType() == 2) {
				CallSession session = calls.get(call.getCallId());
			}

		}
	}

	@Override
	public void onCallStartResult(TupCall call) {
		LogUtils.d("CallManager onCallStartResult");
	}

	@Override
	public void onCallGoing(TupCall call) {
		LogUtils.d("CallManager onCallGoing");

		HandReceiverUtil.onCallGoing();
	}

	@Override
	public void onCallRingBack(TupCall call) {
		LogUtils.d("CallManager onCallRingBack");
	}

	@Override
	public void onCallConnected(TupCall call) {
		HandReceiverUtil.onTalking();
		LogUtils.d("CallManager onCallConnected");

		AccountInfo.getInstance().setCallMode(CALLMODE.CALL_TALKING);
		AccountInfo.getInstance().setCurrentCallID(call.getCallId());

		NotifyMsg notifyMsg = new NotifyMsg(NotifyID.TERMINAL_TALKING_EVENT);
		notifyMsg.setRecode(String.valueOf(call.getCallId()));
		notifyMsg.setMsg(call.getRemoteAddr());
		VTMApp.getInstances().sendBroadcast(notifyMsg);

	}

	@Override
	public void onCallEnded(TupCall call) {
		LogUtils.d("CallManager onCallEnded");
		AccountInfo.getInstance().setCallMode(CALLMODE.CALL_CLOSED);
		AccountInfo.getInstance().setCurrentCallID(AccountInfo.DEFAULT_CALLID);

		if (!("").equals(AccountInfo.getInstance().getConfId())) {
			// 这里是把releaseCall()底层调用异步线程的VoIP.getInstance().releaseCall();换成这里执行endCall了。
			ConferenceMgr.getInstance().toleaveConf();
			// call.endCall();
		}

		HandReceiverUtil.onCallEnd();
		NotifyMsg notifyMsg = new NotifyMsg(
				NotifyID.TERMINAL_CALLING_RELEASE_EVENT);
		VTMApp.getInstances().sendBroadcast(notifyMsg);
	}

	@Override
	public void onCallDestroy(TupCall call) {
		LogUtils.d("CallManager onCallDestroy");
	}

	@Override
	public void onCallRTPCreated(TupCall call) {
		LogUtils.d("CallManager onCallRTPCreated");
	}

	@Override
	public void onCallAddVideo(TupCall call) {
		LogUtils.d("CallManager onCallAddVideo");
	}

	@Override
	public void onCallDelVideo(TupCall call) {
		LogUtils.d("CallManager onCallDelVideo");
	}

	@Override
	public void onCallViedoResult(TupCall call) {
		LogUtils.d("CallManager onCallViedoResult");
	}

	@Override
	public void onCallRefreshView(TupCall call) {
		LogUtils.d("CallManager onCallRefreshView");

		NotifyMsg notifyMsg = new NotifyMsg(
				NotifyID.TERMINAL_CALLING_REFRESH_VIEW_EVENT);

		VTMApp.getInstances().sendBroadcast(notifyMsg);

	}

	@Override
	public void onCallHoldSuccess(TupCall call) {
		LogUtils.d("CallManager onCallHoldSuccess");
	}

	@Override
	public void onCallHoldFailed(TupCall call) {
		LogUtils.d("CallManager onCallHoldFailed");
	}

	@Override
	public void onCallUnHoldSuccess(TupCall call) {
		LogUtils.d("CallManager onCallUnHoldSuccess");
	}

	@Override
	public void onCallUnHoldFailed(TupCall call) {
		LogUtils.d("CallManager onCallUnHoldFailed");
	}

	@Override
	public void onCallBldTransferSuccess(TupCall call) {
		LogUtils.d("CallManager onCallBldTransferSuccess");
	}

	@Override
	public void onCallBldTransferRecvSucRsp(TupCall call) {
		LogUtils.d("CallManager onCallBldTransferRecvSucRsp");
	}

	@Override
	public void onCallBldTransferFailed(TupCall call) {
		LogUtils.d("CallManager onCallBldTransferFailed");
	}

	@Override
	public void onMobileRouteChange(TupCall call) {
		LogUtils.d("CallManager onMobileRouteChange");
	}

	@Override
	public void onAudioEndFile(int handler) {
		LogUtils.d("CallManager onAudioEndFile");
	}

	@Override
	public void onNetQualityChange(TupAudioQuality audioQuality) {
		LogUtils.d("CallManager onNetQualityChange");
		VoiceQuality level = new VoiceQuality();
		level.convertFrom(String.valueOf(audioQuality.getAudioNetLevel()));
	}

	@Override
	public void onStatisticNetinfo(TupAudioStatistic audioStatistic) {
		LogUtils.d("CallManager onStatisticNetinfo");
		CallSession callSession = null;

		if (audioStatistic.getCallId() != 0) {
			callSession = calls.get(audioStatistic.getCallId());
		}
		// 还什么都没做呢。。
	}

	@Override
	public void onStatisticMos(int callId, int mos) {
		LogUtils.d("CallManager onStatisticMos");
		CallSession callSession = null;

		float value = mos / 1000f;

		LogUtils.i(TAG, "onNotifyMos |sessionId:" + callId + "mos:" + value);
		NotifyMsg notifyMsg = new NotifyMsg(
				NotifyID.MEDIA_NTF_STATISTIC_MOS_EVENT);
		notifyMsg.setMsg(value + "");
		VTMApp.getInstances().sendBroadcast(notifyMsg);

		if (value <= 0) {
			return;
		}

		callSession = calls.get(callId);
		// ......

	}

	@Override
	public void onVideoOperation(TupCall call) {
		LogUtils.d("CallManager onVideoOperation");
	}

	@Override
	public void onVideoStatisticNetinfo(TupVideoStatistic videoStatistic) {
		LogUtils.d("CallManager onVideoStatisticNetinfo");
	}

	@Override
	public void onVideoQuality(TupVideoQuality videoQuality) {
		LogUtils.d("CallManager onVideoQuality");
	}

	@Override
	public void onVideoFramesizeChange(TupCall call) {
		LogUtils.d("CallManager onVideoFramesizeChange");
	}

	@Override
	public void onSessionCodec(TupCall call) {
		LogUtils.d("CallManager onSessionCodec");
		CallSession callSession = null;

		if (call.getCallId() != 0) {
			callSession = calls.get(call.getCallId());
		}

		if (callSession != null) {
			if (callSession.isVoiceMail()) {
				return;
			}
		}
	}

	@Override
	public void onSipaccountWmi(List<TupMsgWaitInfo> tupWaitMsgInfos) {
		LogUtils.d("CallManager onSipaccountWmi");
	}

	@Override
	public void onImsForwardResult(List<String> tempHistoryNums) {
		LogUtils.d("CallManager onImsForwardResult");
		if (tempHistoryNums != null && tempHistoryNums.size() > 0) {
			String historyNumber = tempHistoryNums
					.get(tempHistoryNums.size() - 1);
		}
	}

	@Override
	public void onCallUpateRemoteinfo(TupCall call) {
		LogUtils.d("CallManager onCallUpateRemoteinfo");
	}

	@Override
	public void onSetIptServiceSuc(int serviceCallType) {
		LogUtils.d("CallManager onSetIptServiceSuc");
	}

	@Override
	public void onSetIptServiceFal(int serviceCallType) {
		LogUtils.d("CallManager onSetIptServiceFal");
	}

	@Override
	public void onVoicemailSubSuc() {
		LogUtils.d("CallManager onVoicemailSubSuc");
	}

	@Override
	public void onVoicemailSubFal() {
		LogUtils.d("CallManager onVoicemailSubFal");
	}

	/**
	 * 播放语音留言
	 * 
	 * @param shortNumber
	 *            号码
	 * @param domain
	 *            域名
	 * @return CallSession
	 */
	public CallSession playVoiceMail(String shortNumber, String domain) {

		CallSession callSession = makeCall(shortNumber, domain, false);
		if (callSession != null) {
			callSession.setVoiceMail(true);
		}
		return callSession;
	}

	/**
	 * 发起呼叫
	 * 
	 * @param number
	 *            呼叫号码
	 * @param isVideo
	 *            是否是视频呼叫 , 普通呼叫传 false
	 * @return CallSession
	 */
	public CallSession makeCall(String number, String domain, boolean isVideo) {
		TupCall call = tupMakeCall(number, isVideo);
		if (call != null) {
			CallSession callSession = new CallSession(this);
			callSession.setTupCall(call);
			calls.put(callSession.getTupCall().getCallId(), callSession);

			currentCallSession = callSession;

			return callSession;
		}
		return null;
	}

	/**
	 * 发起语音呼叫
	 * 
	 * @param toNumber
	 * @Param isVideo 是否带视频
	 * @return
	 */
	private TupCall tupMakeCall(String toNumber, boolean isVideo) {
		if (isVideo) {
			return tupManager.makeVideoCall(toNumber);
		}
		return tupManager.makeCall(toNumber);
	}

	/**
	 * SDK封装的视频窗口操作
	 * 
	 * @param type
	 *            0为远端窗口，1为本地窗口
	 * @param index
	 *            视频index
	 * @param callId
	 *            会话号
	 */
	public void videoWindowAction(int type, int index, String callId) {
		// 修改为如果有callid调 update方法，没有callid调create方法
		if (callId == null || "".equals(callId)) {
			createVideoWindow(type, index);
		} else {
			updateVideoWindow(type, index, callId);
		}
	}

	/**
	 * 设置摄像头参数
	 * 
	 * @param index
	 */
	public void setVideoIndex(int index) {
		int reg = tupManager.mediaSetVideoIndex(index);
		LogUtils.d("CallManager setVideoIndex result:" + reg);
	}

	/**
	 * 创建视频窗口
	 * 
	 * @param type
	 * @param index
	 */
	public void createVideoWindow(int type, int index) {
		int reg = tupManager.createVideoWindow(type, index);
		LogUtils.d("createVideoWindow type = " + type + " ,result:" + reg);
	}

	/**
	 * 有callid时需调这个方法
	 */
	public void updateVideoWindow(int type, int index, String callIdStr) {
		int callId = StringUtils.stringToInt(callIdStr);
		int reg = tupManager.updateVideoWindow(type, index, callId);
		LogUtils.d("updateVideoWindow type = " + type + " ,result:" + reg);
	}

	/**
	 * 设置音频路由
	 * 
	 * @param rote
	 */
	public void setAudioRoute(int rote) {
		tupManager.setMobileAudioRoute(rote);
	}

	/**
	 * 获取设备视频能力
	 * 
	 * @return
	 */
	public List<TupDevice> getDeviceVideoInfo() {
		List<TupDevice> list = tupManager
				.tupGetDevices(TupCallParam.CALL_E_DEVICE_TYPE.CALL_E_CALL_DEVICE_VIDEO);
		int size = 0;
		if (list != null) {
			size = list.size();
		}
		// Logger.beginInfo(EspaceService.TAG).p(TAG).p("tup_GetDevices = ").p(size).end();
		return list;
	}

	/**
	 * 配置视频方向参数
	 * 
	 * @param caps
	 * @return
	 */
	public int setOrientParams(VideoCaps caps) {
		VideoCaps.OrientParams params = caps.getOrientParams();
		int callId = StringUtils.stringToInt(caps.getSessionId(), -1);
		return tupManager.setMboileVideoOrient(callId, params.cameraIndex,
				params.orient, params.orientPortrait, params.orientLandscape,
				params.orientSeascape);
	}

	/**
	 * 获取当前路由
	 * 
	 * @return
	 */
	public int getAudioRoute() {
		int rote = tupManager.getMobileAudioRoute();
		return rote;
	}

	/**
	 * 开始录音
	 * 
	 * @param path
	 */
	public void startRecord(String path) {
		tupManager.mediaStartRecord(0, path, 0);
	}

	/**
	 * 结束录音
	 */
	public void stopRecord() {
		tupManager.mediaStopRecord(0);
	}

	/**
	 * 获取mic的音量
	 * 
	 * @return
	 */
	public int getMircoVol() {
		int reg = tupManager.mediaGetMicLevel();
		return reg;
	}

	/**
	 * 开始播放
	 */
	public int startPlay(String path, int loop) {
		if (StringUtils.isStringEmpty(path)) {
			return -1;
		}
		if (path.toLowerCase(Locale.ENGLISH).endsWith("pcm")) {
			tupManager
					.setAudioPlayfileAdditioninfo(TupCallParam.CALL_E_FILE_FORMAT.CALL_FILE_FORMAT_PCM);
		} else if (path.toLowerCase(Locale.ENGLISH).endsWith("amr")) {
			tupManager
					.setAudioPlayfileAdditioninfo(TupCallParam.CALL_E_FILE_FORMAT.CALL_FILE_FORMAT_AMR);
		} else {
			tupManager
					.setAudioPlayfileAdditioninfo(TupCallParam.CALL_E_FILE_FORMAT.CALL_FILE_FORMAT_WAV);
		}
		int ret = tupManager.mediaStartplay(loop, path);
		// Logger.beginInfo(EspaceService.TAG).p(TAG).p("media_Startplay = ").p(ret).end();
		return ret;
	}

	/**
	 * 停止播放
	 */
	public int stopPlay(int handler) {
		int ret = tupManager.mediaStopplay(handler);
		// Logger.beginInfo(EspaceService.TAG).p(TAG).p("media_stoprecord = ").p(ret).p("  handler = ").p(handler).end();
		return ret;
	}

	/**
	 * TUP 全局配置
	 */
	public void tupConfig() {
		// configCall();
		configMedia();
		configSip();

	}

	private void configMedia() {

		tpCllCfgAdVd.setAudioPortRange(audioMin, audioMax);
		tpCllCfgAdVd.setVideoPortRange(videoMin, videoMax);
		// audioCode ， 区分 3G 和WIFI
		tpCllCfgAdVd.setAudioCodec(getVoipConfig().getAudioCode());

		// 下面先注释掉0619

		/*
		 * MyOtherInfo info = ContactLogic.getIns().getMyOtherInfo(); // ILBC
		 * mode tpCllCfgAdVd.setAudioIlbcmode(info.getiLBCMode() == 20 ? 20 :
		 * 30); // ANR tpCllCfgAdVd.setAudioAnr(info.getANR());
		 */
		// aec
		tpCllCfgAdVd.setAudioAec(1);
		// agc
		// tpCllCfgAdVd.setAudioAgc(ContactLogic.getIns().getAbility().isAgcEnable()
		// ? 1 : 0);
		// Dscp
		tpCllCfgAdVd.setDscpAudio(getVoipConfig().getAudioDSCP());
		tpCllCfgAdVd.setDscpVideo(getVoipConfig().getVideoDSCP());
		// net level
		tpCllCfgAdVd.setAudioNetatelevel(getVoipConfig().getNetate() == 1);
		// opus 采样率
		tpCllCfgAdVd.setAudioClockrate(getVoipConfig().getOpusSamplingFreq());
		tpCllCfgAdVd.setForceIdrInfo(FORCE_OPEN);

		// 摄像头旋转
		// 可选，设置视频捕获（逆时针旋转）的角度。
		// 仅Android/iOS平台有效。
		// 0：0度；1：90度；2：180度；3：270度；
		// {0,1,2,3}
		tpCllCfgAdVd.setVideoCaptureRotation(/* caps.getCameraRotation() */0);
		tpCllCfgAdVd.setVideoDisplayType(VideoCaps.DISPLAY_TYPE_EDGE);
		tpCllCfgAdVd.setVideoCoderQuality(/* caps.getQuality() */15);
		tpCllCfgAdVd.setVideoKeyframeinterval(/* caps.getKeyInterval() */10);
		tpCllCfgAdVd
				.setAudioDtmfMode(TupCallParam.CALL_E_DTMF_MODE.CALL_E_DTMF_MODE_CONST2833);
		// tpCllCfgAdVd.setVideoErrorcorrecting(info.getFecEnable() == 1);

		// 视频编解码，最好MAA通过插件参数下发 , 如果没有下发，不配置，TUP有默认值 , 和PC一致
		// if (!StringUtils.isStringEmpty(info.getVideoCodec()))
		// {
		// tpCllCfgAdVd.setVideoCodec("106,34");
		// }

		// 以下参数只配置一次，否则会导致融合会议无法入会
		VOIPConfigParamsData data = getVoipConfig();
		if (data.isHardCodec()) {
			// 硬编码能力下最大支持512，软编码能力下最大支持256
			maxBw = MAX_DATARATE_HARDCODEC;
			maxDataRate = MAX_DATARATE_HARDCODEC;
			// 硬编码能力下最大支持4CIF，软编码能力下最大支持CIF
			// decodeFrameSize = UCResource.VideoFrameSize._4CIF;
			decodeFrameSize = 4;
		}

		// configCodecParams(false); 融合会议的0619

		// Logger.beginDebug(EspaceService.TAG).p(TAG).p("audioCode = ")
		// .p(tpCllCfgAdVd.getAudioCodec()).end();

		tupManager.setCfgAudioAndVideo(tpCllCfgAdVd);
		// 先只配置默认值
		tupManager.setMboileVideoOrient(0, 1, 1, 0, 0, 0);

	}

	private void configSip() {

		TupCallCfgSIP tupCallCfgSIP = new TupCallCfgSIP();

		// ip port
		tupCallCfgSIP.setServerRegPrimary(getVoipConfig().getServerIp(),
				StringUtils.stringToInt(getVoipConfig().getServerPort()));
		tupCallCfgSIP.setServerProxyPrimary(getVoipConfig().getServerIp(),
				StringUtils.stringToInt(getVoipConfig().getServerPort()));
		// localip
		// tupCallCfgSIP.setNetAddress(getVoipConfig().getLocalIpAddress());
		String svnIp = StringUtils.getIpAddress();
		tupCallCfgSIP.setNetAddress(svnIp);
		LogUtils.d("id::::::::" + svnIp);

		// 本地端口 TODO

		// 刷新注册的时间
		tupCallCfgSIP.setSipRegistTimeout(getVoipConfig().getRegExpires());
		// 刷新订阅的时间
		tupCallCfgSIP.setSipSubscribeTimeout(getVoipConfig()
				.getSessionExpires());
		// 会话
		tupCallCfgSIP.setSipSessionTimerEnable(true);
		// 会话超时
		tupCallCfgSIP.setSipSessionTime(90);// 0805改为90s
		LogUtils.d("setSipSessionTime : 90s");

		// 设置备服务器
		// tupCallCfgSIP.setServerProxyBackup(getVoipConfig().getBackUpServerIp(),
		// StringUtils.stringToInt(getVoipConfig().getBackUpServerPort()));
		// tupCallCfgSIP.setServerRegBackup(getVoipConfig().getBackUpServerIp(),
		// StringUtils.stringToInt(getVoipConfig().getBackUpServerPort()));

		// 设置 DSCP
		tupCallCfgSIP.setDscpEnable(true);
		// tupCallCfgSIP.setDscpSip(getVoipConfig().getSipDSCP());
		// 设置 tup 再注册的时间间隔， 注册失败后 间隔再注册
		tupCallCfgSIP.setSipReregisterTimeout(10);

		// 证书与SVN等先注释掉0619

		/*
		 * // TLS 开关
		 * tupCallCfgSIP.setSipTransMode(ContactLogic.getIns().getMyOtherInfo
		 * ().isSipTLS() ? 1 : 0);// 打开 // TLS 证书路径， 绝对路径 String filesDir =
		 * VTMApp.getApp().getFilesDir().getAbsolutePath(); // String pemPath =
		 * filesDir+"/" + TLS.CACERT;// 根证书 // File file = new File(pemPath); //
		 * Logger
		 * .beginInfo(EspaceService.TAG).p(TAG).p("cert exit?").p(file.exists
		 * ()).end();
		 * 
		 * 
		 * //设置SVN 开关
		 * 
		 * boolean isSVNenable = CommonVariables.getIns().isEnableSVN();
		 * 
		 * tupCallCfgSIP.setSipTransMode(isSVNenable?TupCallParam.
		 * CALL_E_TRANSPORTMODE.CALL_E_TRANSPORTMODE_SVN:
		 * tupCallCfgSIP.getSipTransMode()); // QOS
		 * tupCallCfgSIP.setDscpSip(getVoipConfig().getSipDSCP());
		 * 
		 * // userAgent
		 * tupCallCfgSIP.setEnvUseagent(getVoipConfig().getUserAgent());
		 * 
		 * tupCallCfgSIP.setSipTlsRootcertpath(pemPath);
		 * 
		 * // 设置 close code
		 * 
		 * tupCallCfgSIP.setSipRejectType(getRejectType()); // 设置组网环境
		 * 
		 * if(CommonVariables.getIns().isUCTwo()) {
		 * tupCallCfgSIP.setEnvSolution(1); } else {
		 * tupCallCfgSIP.setEnvSolution(0); }
		 * 
		 * 
		 * // 订阅语音留言事件
		 * 
		 * tupCallCfgSIP.setMobileVvmRight(ContactLogic.getIns().getAbility().
		 * isVoiceMailAbility());
		 * 
		 * Logger.beginInfo(EspaceService.TAG).p(TAG).p("SBC:").p(getVoipConfig()
		 * .getServerIp()).end();
		 */
		if (AccountInfo.getInstance().isAnonymous()) {
			String calledNum = AccountInfo.getInstance().getPhoneNo();
			String localIpAddress = Tools.getLocalIpAddress();
			String port = AccountInfo.getInstance().getSipServerPort();
			String anonymousNum = calledNum + "@" + localIpAddress + ":" + port;
			LogUtils.d("anonymousNum = " + anonymousNum);
			tupCallCfgSIP.setAnonymousNum(anonymousNum);
			tupManager.setCfgSIP(tupCallCfgSIP);
			AccountInfo.getInstance().setAnonymousNum(anonymousNum);
			// makeAnonymousCall(anonymousNum);

		} else {
			tupManager.setCfgSIP(tupCallCfgSIP);
		}

	}

	private void configCall() {
		// TupCallCfgMedia c = new TupCallCfgMedia();
		// //CALL_D_CFG_ID.CALL_D_CFG_ID_MEDIA_VIDEOSESSION_PREVIEWTYPE
		// c.setMediaVideoPreview(2);
		// tupManager.setCfgMedia(c);
		/*
		 * TupCallCfgMedia c = new TupCallCfgMedia();
		 * 
		 * // mos上报阀值 c.setMediaMosThreshold((int)(1000 *
		 * ContactLogic.getIns().getMyOtherInfo(). getMosthreshold())); // mos
		 * 上报间隔 c. setMediaMosTime (ContactLogic.getIns().getMyOtherInfo
		 * ().getVqminterval());
		 * 
		 * // 设置 SRTP加密模式 int mode = ContactLogic.getIns(
		 * ).getMyOtherInfo().getSrtpMode();//(0-不加密 1-自动模式 2-强制加密)
		 * c.setMediaSrtpMode(mode); // 设置 自动模式下的 优先级
		 * 
		 * String value =ContactLogic.getIns().getMyOtherInfo
		 * ().getPriorityRtp();
		 * 
		 * int rtpPriority = 1; if("2".equals(value)) { rtpPriority = 2; }
		 * c.setMediaRtpPriority(rtpPriority);// 1 rtp 优先 ， 2 srtp优先。 默认设置为1
		 * 
		 * 
		 * String codec = ContactLogic.getIns().getMyOtherInfo
		 * ().getUmVoiceCodecs(); int freq = 8000; if (!"G729".equals(codec) &&
		 * !"AMR-WB".equals(codec)) { codec = "G729"; }
		 * 
		 * if ("AMR-WB".equals(codec)) { freq = 16000; } //
		 * H264视频协商时，是否判断打包模式：取值范围false关闭，true开启 c.setMediaUsepackmode(true);
		 * c.setMediaRecordfileInfo(0, 0, freq, 0, codec);
		 * 
		 * // QOS 上报能力 c.setMediaMosSendInfoSwitch(ContactLogic
		 * .getIns().getAbility() .isQosAbility());
		 * 
		 * tupManager.setCfgMedia(c); // Logger.beginDebug
		 * (EspaceService.TAG).p(TAG).p(c).end();
		 */
	}

	public void tupInit() {
		if (!initFlag) {
			LogUtils.d(TAG, "call_Init enter");
			tupManager.callInit();
			tupManager.registerReceiver();
			initFlag = true;
			LogUtils.d(TAG, "call_Init end");
		}

	}

	/**
	 * 去初始化 TUP组件
	 * 
	 * @l00186254
	 */
	public void tupUninit() {
	}

	public class SIPRegister {
		// /**
		// * 媒体配置完成状态， 这里要注意断网后 IP变化，需要重新 config .
		// * 但是一次连续的VOIP注册，（重复注册知道注册成功） 不需要重新config
		// */
		// // 引入了SIP主备注册，
		// public boolean isConfigReady = false;

		// /**
		// * 注册记数 SIP 主备注册用到
		// */
		// private int registerCount = 0;

		// private boolean isInit = false;

		public SIPRegister() {
		}

		/**
		 * Function: VOIP注册
		 * 
		 * @author luotianjia 00186254/huawei
		 * @return void
		 */
		void registerVoip() {

			LogUtils.i("tupConfig-------------");
			tupConfig();

			LogUtils.i("callRegister------------");

			tupManager.callRegister(getVoipConfig().getVoipNumber(),
					getVoipConfig().getVoipNumber(), getVoipConfig()
							.getVoipPassword());
			LogUtils.i("registerVoip end------------");
		}

		/**
		 * Function: VOIP注销
		 * 
		 * @author luotianjia 00186254/huawei
		 */
		void unRegisterVOIP() {
			if (tupManager.getRegState() != 0) {
				tupManager
						.setRegState(TupCallParam.CALL_E_REG_STATE.CALL_E_REG_STATE_DEREGISTERING);
				tupManager.callDeregister();
			}
		}
	}

	public void releaseCall() {
		// TODO Auto-generated method stub
		if (currentCallSession != null) {
			currentCallSession.hangUp(false);
			currentCallSession = null;
		}

	}

	public int makeAnonymousCall(String number) {

		int callId = tupManager.startAnonmousCall(number);
		if (0 != callId) {
			TupCall tupCall = new TupCall(callId, 0);
			tupCall.setCaller(true);
			tupCall.setNormalCall(true);
			tupCall.setToNumber(number);
			CallSession callSession = new CallSession(this);
			callSession.setTupCall(tupCall);
			calls.put(callId, callSession);
			currentCallSession = callSession;
			// callMap.put(Integer.valueOf(callId), tupCall);
		}
		return callId;
	}



	@Override
	public void onCallDialoginfo(int arg0, String arg1, String arg2, String arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNotifyLocalQosinfo(TupCallLocalQos arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNotifyQosinfo(TupCallQos arg0) {
		// TODO Auto-generated method stub

	}

	public boolean mute(int type, boolean isMute) {
		if (currentCallSession != null) {
			return currentCallSession.mute(type, isMute);
		}
		return false;
	}

}
