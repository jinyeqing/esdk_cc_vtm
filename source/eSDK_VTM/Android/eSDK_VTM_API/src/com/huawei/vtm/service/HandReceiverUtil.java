package com.huawei.vtm.service;

import com.huawei.meeting.func.ConferenceMgr;
import com.huawei.vtm.NotifyID;
import com.huawei.vtm.NotifyMsg;
import com.huawei.vtm.authentic.Authentic;
import com.huawei.vtm.call.CallManager;
import com.huawei.vtm.common.AccountInfo;
import com.huawei.vtm.common.Constants;
import com.huawei.vtm.common.Constants.CC_STATE;
import com.huawei.vtm.utils.LogUtils;
import com.huawei.vtm.utils.Tools;

public class HandReceiverUtil
{

	/**
	 * 登录 MCC 成功
	 */
	public static void onMCCLoginSuccess()
	{
		Authentic.getTerminalInfo();
	}

	/**
	 * 获取终端信息成功
	 */
	public static void onGetTerminalInfoSuccess()
	{
		Authentic.getClientConfig();
	}

	/**
	 * 获取终端配置信息成功
	 */
	public static void onGetClientConfigSuccess()
	{
		// VoIP.getInstance().initVoip(AccountInfo.getInstance().isAnonymous());

		String phoneNo = AccountInfo.getInstance().getPhoneNo();
		String password = AccountInfo.getInstance().getPassword();
		String sipServerIp = AccountInfo.getInstance().getSipServerIp();
		String sipServerPort = AccountInfo.getInstance().getSipServerPort();

		CallManager.getInstance().getVoipConfig().resetData(phoneNo, password, sipServerIp, sipServerPort);
		CallManager.getInstance().register(true);
	}

	/**
	 * 获取获取会场资源成功
	 */
	public static void onAssignConfForCallerSuccess()
	{
		String siteId = AccountInfo.getInstance().getMeetingServerSite();
		String serverIp = AccountInfo.getInstance().getMeetingServerIp();
		int confId = Tools.parseInt(AccountInfo.getInstance().getConfId());
		String confKey = AccountInfo.getInstance().getAuthKey();
		int userId = Tools.parseInt(AccountInfo.getInstance().getVtmNo());
		String userName = AccountInfo.getInstance().getVtmNo();
		String hostKey = "";

		ConferenceMgr.getInstance().initConf();
		ConferenceMgr.getInstance().joinConf(siteId, serverIp, confId, confKey, hostKey, userId, userName);
	}

	/**
	 * 对方接听成功，通话开始
	 */
	public static void onTalking()
	{
		LogUtils.d("------------------------------------onTalking");
		if (AccountInfo.getInstance().isAnonymous())
		{
			Authentic.notifySessionState(CC_STATE.AUDIOTALKING);
		} else
		{
			Authentic.assignConfForCaller(AccountInfo.getInstance().getVtmNo());
		}
	}

	/**
	 * 加入会议
	 */
	public static void onJoinConf(String recode)
	{
		if (AccountInfo.getInstance().isAnonymous())
		{
			Authentic.notifySessionState(CC_STATE.TALKING);
		} else
		{
			if ("0".equals(recode))
			{
				String userId = AccountInfo.getInstance().getVtmNo();
				String confId = AccountInfo.getInstance().getConfId();
				Authentic.joinedConfNotify(userId, confId);
			}
		}
	}

	/**
	 * 会议终止
	 */
	public static void onTerminateConf()
	{
		if (AccountInfo.getInstance().isAnonymous())
		{
			AccountInfo.getInstance().setIsGetIasEvent(false);
			LogUtils.d("HandReceiverUtil", "onTerminateConf set isGetIasEvent false!");
			AccountInfo.getInstance().clear();
		} else
		{
			String userId = AccountInfo.getInstance().getVtmNo();
			String confId = AccountInfo.getInstance().getConfId();
			Authentic.leavedConfNotify(userId, confId);
			AccountInfo.getInstance().clearConf();
		}
	}

	/**
	 * 请求匿名呼叫成功
	 */
	public static void onAnonyRequestSuccess()
	{
		Authentic.getSessionStateEvent();
	}

	/**
	 * 预占用柜员成功
	 */
	public static void onAnonyOccupyAgentSuccess()
	{

		String sipServerIp = AccountInfo.getInstance().getSipServerIp();
		String sipServerPort = AccountInfo.getInstance().getSipServerPort();
		CallManager.getInstance().getVoipConfig().resetData("", "", sipServerIp, sipServerPort);	
		CallManager.getInstance().tupConfig();
		if(AccountInfo.getInstance().getAnonymousNum()!=""){
			onAnonyVoipConfigEnd();
		}
	}

	/**
	 * 匿名呼叫 voip初始化完成
	 */
	public static void onAnonyVoipConfigEnd()
	{
		CallManager.getInstance().makeAnonymousCall(AccountInfo.getInstance().getCalledNumber());
		
		

		
	}

	/**
	 * 成功获取到匿名呼叫入会信息
	 */
	public static void onAnonyMeetingInfoSuccess()
	{
		String siteId = AccountInfo.getInstance().getMeetingServerSite();
		String serverIp = AccountInfo.getInstance().getMeetingServerIp();
		int confId = Tools.parseInt(AccountInfo.getInstance().getConfId());
		String confKey = AccountInfo.getInstance().getAuthKey();
		int userId = Tools.parseInt(AccountInfo.getInstance().getVtmNo());
		String userName = AccountInfo.getInstance().getVtmNo();
		String hostKey = "";

		ConferenceMgr.getInstance().initConf();
		ConferenceMgr.getInstance().joinConf(siteId, serverIp, confId, confKey, hostKey, userId, userName);
	}

	/**
	 * 呼叫结束
	 */
	public static void onCallEnd()
	{
		if (AccountInfo.getInstance().isAnonymous())
		{
			AccountInfo.getInstance().setIsGetIasEvent(false);
			LogUtils.d("HandReceiverUtil", "onCallEnd set isGetIasEvent false!");
			AccountInfo.getInstance().clear();
		}
	}

	/**
	 * 通知会话状态变更事件
	 */
	public static void onNotifySessionStateFailed(String recode)
	{
		if (!Constants.RETCODE_SUCCESS.equals(recode))
		{
			AccountInfo.getInstance().setIsGetIasEvent(false);
			LogUtils.d("HandReceiverUtil", "onNotifySessionStateFailed set isGetIasEvent false!");
		}
	}

	public static void onCallGoing() {
		// TODO Auto-generated method stub
		//LogUtils.i(TAG, "AnonymousMeetingInfoEvent " + event.toString());
		NotifyMsg notifyMsg = new NotifyMsg(NotifyID.ANONY_CALLED_EVENT);


		VTMApp.getInstances().sendBroadcast(notifyMsg);
	}

}
