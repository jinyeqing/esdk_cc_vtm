package com.huawei.vtm.authentic;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.huawei.meeting.ConfPrew;
import com.huawei.meeting.Conference;
import com.huawei.meeting.func.ConferenceMgr;
import com.huawei.vtm.MobileVTM;
import com.huawei.vtm.NotifyID;
import com.huawei.vtm.NotifyMsg;
import com.huawei.vtm.common.AccountInfo;
import com.huawei.vtm.common.Constants;
import com.huawei.vtm.common.Constants.CALLMODE;
import com.huawei.vtm.common.Constants.CC_STATE;
import com.huawei.vtm.common.Constants.CC_STATE_EVENT;
import com.huawei.vtm.common.Constants.IAS_REQUEST_PATH;
import com.huawei.vtm.common.Constants.MCC_REQUEST_PATH;
import com.huawei.vtm.service.HandReceiverUtil;
import com.huawei.vtm.service.VTMApp;
import com.huawei.vtm.utils.LogUtils;
import com.huawei.vtm.utils.SHA256_BASE64;
import com.huawei.vtm.utils.Tools;

public class Authentic
{
	private static final String TAG = "Authentic";

	/**
	 * 登录
	 * 
	 * @param userIdStr
	 * @param userPwdStr
	 * @param userTypeStr
	 * @return <b>true</b> 接口调用成功;<b>false</b> 接口调用失败,检查ip地址和端口号格式是否正确.
	 */
	public static boolean login(String userIdStr, String userPwdStr, String userTypeStr)
	{
		if (!Tools.checkIP() || !Tools.checkPort())
		{
			return false;
		}
		CommonPostAsync loginAnsyc = new CommonPostAsync(MCC_REQUEST_PATH.LOGIN_PATH, new String[] { "userId", "userPwd", "userType" });
		loginAnsyc.setOnPostExecuteListener(new PostExecuteListener()
		{
			@Override
			public void onPostExecuteListener(Object result)
			{
				LogUtils.i(TAG, "login result:" + result.toString());
				NotifyMsg notifyMsg = new NotifyMsg(NotifyID.TERMINAL_LOGIN_EVENT);
				if (Constants.RESPONSE_ERROR.equals(result.toString())) // 网络响应错误
				{
					notifyMsg.setRecode(Constants.RESPONSE_ERROR_CODE);
					notifyMsg.setMsg(Constants.RESPONSE_ERROR);
				} else
				{
					try
					{
						JSONObject jRetcode = new JSONObject(result.toString());
						String retcode = jRetcode.getString(Constants.NOTIFY_RETCODE);

						notifyMsg.setRecode(retcode);
						notifyMsg.setMsg(jRetcode.getString(Constants.NOTIFY_MSG));

						if (Constants.RETCODE_SUCCESS.equals(retcode))
						{
							JSONObject jResult = jRetcode.getJSONObject("result");

							String token = jResult.getString("token");
							String sessionId = jResult.getString("sessionId");

							token = SHA256_BASE64.execute(token.getBytes(Constants.CHARSET_UTF_8), token.getBytes(Constants.CHARSET_UTF_8).length);

							AccountInfo.getInstance().setLogin(true);
							AccountInfo.getInstance().setToken(token);
							AccountInfo.getInstance().setSessionId(sessionId);

							VTMApp.getInstances().startHeart();

							HandReceiverUtil.onMCCLoginSuccess();
						}
					} catch (JSONException e)
					{
						LogUtils.e(TAG, "get login result exception:" + e.getMessage());
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e)
					{
						LogUtils.e(TAG, "get login result exception:" + e.getMessage());
						e.printStackTrace();
					} catch (UnsupportedEncodingException e)
					{
						LogUtils.e(TAG, "get login result exception:" + e.getMessage());
						e.printStackTrace();
					}
				}
				VTMApp.getInstances().sendBroadcast(notifyMsg);
			}
		});
		loginAnsyc.execute(userIdStr, userPwdStr, userTypeStr);
		return true;
	}

	/**
	 * 心跳
	 * 
	 * @return <b>true</b> 接口调用成功;<b>false</b> 接口调用失败,检查ip地址和端口号格式是否正确.
	 */
	public static boolean alive()
	{
		if (!Tools.checkIP() || !Tools.checkPort())
		{
			return false;
		}
		CommonGetAsync aliveAsync = new CommonGetAsync(MCC_REQUEST_PATH.ALIVE_PATH);
		aliveAsync.setOnPostExecuteListener(new PostExecuteListener()
		{
			@Override
			public void onPostExecuteListener(Object result)
			{
				LogUtils.i(TAG, "alive result:" + result.toString());
				NotifyMsg notifyMsg = new NotifyMsg(NotifyID.TERMINAL_ALIVE_EVENT);
				if (Constants.RESPONSE_ERROR.equals(result.toString())) // 网络响应错误
				{
					notifyMsg.setRecode(Constants.RESPONSE_ERROR_CODE);
					notifyMsg.setMsg(Constants.RESPONSE_ERROR);
					AccountInfo.getInstance().setLogin(false);
					VTMApp.getInstances().stopHeart();
				} else
				{
					try
					{
						JSONObject jRetcode = new JSONObject(result.toString());
						String retcode = jRetcode.getString(Constants.NOTIFY_RETCODE);

						notifyMsg.setRecode(retcode);
						notifyMsg.setMsg(jRetcode.getString(Constants.NOTIFY_MSG));
						if (!Constants.RETCODE_SUCCESS.equals(retcode))
						{
							AccountInfo.getInstance().setLogin(false);
							VTMApp.getInstances().stopHeart();
						}
					} catch (JSONException e)
					{
						LogUtils.e(TAG, "alive result exception:" + e.getMessage());
						e.printStackTrace();
					}
				}
				VTMApp.getInstances().sendBroadcast(notifyMsg);
			}
		});
		aliveAsync.execute();
		return true;
	}

	/**
	 * 获取终端信息
	 * 
	 * @return <b>true</b> 接口调用成功;<b>false</b> 接口调用失败,检查ip地址和端口号格式是否正确.
	 */
	public static boolean getTerminalInfo()
	{
		if (!Tools.checkIP() || !Tools.checkPort())
		{
			return false;
		}
		CommonGetAsync terminalAsync = new CommonGetAsync(MCC_REQUEST_PATH.TERMINAL_PATH);
		terminalAsync.setOnPostExecuteListener(new PostExecuteListener()
		{
			@Override
			public void onPostExecuteListener(Object result)
			{
				LogUtils.i(TAG, "getTerminalInfo result:" + result.toString());
				NotifyMsg notifyMsg = new NotifyMsg(NotifyID.TERMINAL_GETTERMINALINFO_EVENT);
				if (Constants.RESPONSE_ERROR.equals(result.toString())) // 网络响应错误
				{
					notifyMsg.setRecode(Constants.RESPONSE_ERROR_CODE);
					notifyMsg.setMsg(Constants.RESPONSE_ERROR);
				} else
				{
					try
					{
						JSONObject jRetcode = new JSONObject(result.toString());
						String retcode = jRetcode.getString(Constants.NOTIFY_RETCODE);

						notifyMsg.setRecode(retcode);
						notifyMsg.setMsg(jRetcode.getString(Constants.NOTIFY_MSG));

						if (Constants.RETCODE_SUCCESS.equals(retcode))
						{
							JSONObject jResult = jRetcode.getJSONObject("result");

							String phoneNo = jResult.getString("phoneNo");
							String password = jResult.getString("password");
							String uapIp = jResult.getString("uapIp");
							String uapPort = jResult.getString("uapPort");

							AccountInfo.getInstance().setVtmNo(phoneNo);
							AccountInfo.getInstance().setPhoneNo(phoneNo);
							AccountInfo.getInstance().setPassword(password);
							AccountInfo.getInstance().setSipServerIp(uapIp);
							AccountInfo.getInstance().setSipServerPort(uapPort);

							HandReceiverUtil.onGetTerminalInfoSuccess();
						}
					} catch (JSONException e)
					{
						LogUtils.e(TAG, "get Terminal info result exception:" + e.getMessage());
						e.printStackTrace();
					}
				}
				VTMApp.getInstances().sendBroadcast(notifyMsg);
			}
		});
		terminalAsync.execute();
		return true;
	}

	/**
	 * 注销
	 * 
	 * @return <b>true</b> 接口调用成功;<b>false</b> 接口调用失败,检查ip地址和端口号格式是否正确.
	 */
	public static boolean logout()
	{
		if (!Tools.checkIP() || !Tools.checkPort())
		{
			return false;
		}
		CommonPostAsync logoutAsync = new CommonPostAsync(MCC_REQUEST_PATH.LOGOUT_PATH, new String[] {});
		logoutAsync.setOnPostExecuteListener(new PostExecuteListener()
		{
			@Override
			public void onPostExecuteListener(Object result)
			{
				LogUtils.i(TAG, "logout result:" + result.toString());
				NotifyMsg notifyMsg = new NotifyMsg(NotifyID.TERMINAL_LOGOUT_EVENT);
				if (Constants.RESPONSE_ERROR.equals(result.toString())) // 网络响应错误
				{
					notifyMsg.setRecode(Constants.RESPONSE_ERROR_CODE);
					notifyMsg.setMsg(Constants.RESPONSE_ERROR);
				} else
				{
					try
					{
						JSONObject jRetcode = new JSONObject(result.toString());
						String retcode = jRetcode.getString(Constants.NOTIFY_RETCODE);

						notifyMsg.setRecode(retcode);
						notifyMsg.setMsg(jRetcode.getString(Constants.NOTIFY_MSG));

						if (Constants.RETCODE_SUCCESS.equals(retcode))
						{
							AccountInfo.getInstance().clear();

							VTMApp.getInstances().stopHeart();
						}
					} catch (JSONException e)
					{
						LogUtils.e(TAG, "logout result exception:" + e.getMessage());
						e.printStackTrace();
					}
				}
				VTMApp.getInstances().sendBroadcast(notifyMsg);
			}
		});
		logoutAsync.execute();
		return true;
	}

	/**
	 * 获取终端配置信息
	 * 
	 * @return <b>true</b> 接口调用成功;<b>false</b> 接口调用失败,检查ip地址和端口号格式是否正确.
	 */
	public static boolean getClientConfig()
	{
		if (!Tools.checkIP() || !Tools.checkPort())
		{
			return false;
		}
		CommonGetAsync clientAsync = new CommonGetAsync(MCC_REQUEST_PATH.CLIENT_PATH);
		clientAsync.setOnPostExecuteListener(new PostExecuteListener()
		{
			@Override
			public void onPostExecuteListener(Object result)
			{
				LogUtils.i(TAG, "client result:" + result.toString());
				NotifyMsg notifyMsg = new NotifyMsg(NotifyID.TERMINAL_GETCLIENTCONFIG_EVENT);
				if (Constants.RESPONSE_ERROR.equals(result.toString())) // 网络响应错误
				{
					notifyMsg.setRecode(Constants.RESPONSE_ERROR_CODE);
					notifyMsg.setMsg(Constants.RESPONSE_ERROR);
				} else
				{
					try
					{
						JSONObject jRetcode = new JSONObject(result.toString());
						String retcode = jRetcode.getString(Constants.NOTIFY_RETCODE);

						notifyMsg.setRecode(retcode);
						notifyMsg.setMsg(jRetcode.getString(Constants.NOTIFY_MSG));

						if (Constants.RETCODE_SUCCESS.equals(retcode))
						{
							JSONObject jResult = jRetcode.getJSONObject("result");

							String sipServerIp = jResult.getString("SipServerIp");
							String sipServerPort = jResult.getString("SipServerPort");
							String meetingServerSite = jResult.getString("MeetingServerSite");
							String meetingServerIp = jResult.getString("MeetingServerIp");

							AccountInfo.getInstance().setSipServerIp(sipServerIp);
							AccountInfo.getInstance().setSipServerPort(sipServerPort);
							AccountInfo.getInstance().setMeetingServerSite(meetingServerSite);
							AccountInfo.getInstance().setMeetingServerIp(meetingServerIp);

							HandReceiverUtil.onGetClientConfigSuccess();
						}
					} catch (JSONException e)
					{
						LogUtils.e(TAG, "get client info exception:" + e.getMessage());
						e.printStackTrace();
					}
				}
				VTMApp.getInstances().sendBroadcast(notifyMsg);
			}
		});
		clientAsync.execute();
		return true;
	}

	/**
	 * 请求会场资源
	 * 
	 * @param caller
	 *            软终端号码
	 * @return <b>true</b> 接口调用成功;<b>false</b> 接口调用失败,检查ip地址和端口号格式是否正确.
	 */
	public static boolean assignConfForCaller(String caller)
	{
		if (!Tools.checkIP() || !Tools.checkPort())
		{
			return false;
		}
		CommonGetAsync confCallerAsync = new CommonGetAsync(MCC_REQUEST_PATH.CONF_CALLER_PATH);
		confCallerAsync.setOnPostExecuteListener(new PostExecuteListener()
		{
			@Override
			public void onPostExecuteListener(Object result)
			{
				LogUtils.i(TAG, "assignConfForCaller result:" + result.toString());
				NotifyMsg notifyMsg = new NotifyMsg(NotifyID.TERMINAL_ASSIGNCONFFORCALLER_EVENT);
				if (Constants.RESPONSE_ERROR.equals(result.toString())) // 网络响应错误
				{
					/*
					 * 获取会场资源错误，继续判断通话是否关闭，如果通话关闭， 则不用再继续请求，发送广播通知用户获取会场资源失败，
					 * 否则重新发送请求获取会场资源
					 */
					if (AccountInfo.getInstance().getCallMode() != CALLMODE.CALL_CLOSED && Tools.isEmpty(AccountInfo.getInstance().getConfId()))
					{ // 获取会场资源错误，判断通话未关闭，并且还未获取到会议Id，继续请求
						Authentic.assignConfForCaller(AccountInfo.getInstance().getVtmNo());
					} else
					{ // 获取会场资源错误，判断通话关闭，发送广播通知失败
						notifyMsg.setRecode(Constants.RESPONSE_ERROR_CODE);
						notifyMsg.setMsg(Constants.RESPONSE_ERROR);
						VTMApp.getInstances().sendBroadcast(notifyMsg);
					}
					return;
				}
				try
				{
					/*
					 * 判断是否成功获取会场资源，如果成功发送广播通知用户，否则继续判断通话是否关闭，
					 * 如果通话关闭，则不用再继续请求，发送广播通知用户获取会场资源失败， 否则重新发送请求获取会场资源
					 */
					JSONObject jRetcode = new JSONObject(result.toString());
					String retcode = jRetcode.getString(Constants.NOTIFY_RETCODE);

					notifyMsg.setRecode(retcode);
					notifyMsg.setMsg(jRetcode.getString(Constants.NOTIFY_MSG));

					if (Constants.RETCODE_SUCCESS.equals(retcode))
					{ // 成功获取会场资源,发送广播通知用户
						JSONObject jObject = jRetcode.getJSONObject("result");

						String confId = jObject.getString("confId");
						String authKey = jObject.getString("authKey");

						AccountInfo.getInstance().setConfId(confId);
						AccountInfo.getInstance().setAuthKey(authKey);
						LogUtils.d(TAG, "get conf resource for caller success, and send broadcast");

						HandReceiverUtil.onAssignConfForCallerSuccess();

						VTMApp.getInstances().sendBroadcast(notifyMsg);
						return;
					}

					LogUtils.e(TAG, "assignConfForCaller Fail");

					if (AccountInfo.getInstance().getCallMode() != CALLMODE.CALL_CLOSED && Tools.isEmpty(AccountInfo.getInstance().getConfId()))
					{ // 获取会场资源错误，判断通话未关闭，并且还未获取到会议Id，继续请求
						Authentic.assignConfForCaller(AccountInfo.getInstance().getVtmNo());
					} else
					{ // 获取会场资源失败，判断通话关闭，发送广播通知失败
						VTMApp.getInstances().sendBroadcast(notifyMsg);
					}
				} catch (JSONException e)
				{
					LogUtils.e(TAG, "assignConfForCaller get exception" + e.getMessage());
					e.printStackTrace();
				}
			}
		});
		confCallerAsync.execute("?caller=" + caller);
		return true;
	}

	/**
	 * 通知入会
	 * 
	 * @param userId
	 *            用户标识（软终端号码）
	 * @param confId
	 *            会议Id
	 * @return <b>true</b> 接口调用成功;<b>false</b> 接口调用失败,检查ip地址和端口号格式是否正确.
	 */
	public static boolean joinedConfNotify(String userId, String confId)
	{
		if (!Tools.checkIP() || !Tools.checkPort())
		{
			return false;
		}
		CommonGetAsync joinedAsync = new CommonGetAsync(MCC_REQUEST_PATH.NOTIFY_JOINED_CONF_PATH);
		joinedAsync.setOnPostExecuteListener(new PostExecuteListener()
		{
			@Override
			public void onPostExecuteListener(Object result)
			{
				LogUtils.i(TAG, "notifyJoinedConf result:" + result.toString());
			}
		});
		joinedAsync.execute("?user=" + userId + "&" + "confId=" + confId);
		return true;
	}

	/**
	 * 通知离会
	 * 
	 * @param userId
	 *            用户标识（软终端号码）
	 * @param confId
	 *            会议Id
	 * @return <b>true</b> 接口调用成功;<b>false</b> 接口调用失败,检查ip地址和端口号格式是否正确.
	 */
	public static boolean leavedConfNotify(String userId, String confId)
	{
		if (!Tools.checkIP() || !Tools.checkPort())
		{
			return false;
		}
		CommonGetAsync leavedAsync = new CommonGetAsync(MCC_REQUEST_PATH.NOTIFY_LEAVED_CONF_PATH);
		leavedAsync.setOnPostExecuteListener(new PostExecuteListener()
		{
			@Override
			public void onPostExecuteListener(Object result)
			{
				LogUtils.i(TAG, "notifyLeavedConf result:" + result.toString());
			}
		});
		leavedAsync.execute("?user=" + userId + "&" + "confId=" + confId);
		return true;
	}

	/**
	 * 请求匿名呼叫
	 * 
	 * @param accessCode
	 *            接入码，正整数
	 * @param mediaType
	 *            媒体类型，参考 {@link Constants.MEDIA_TYPE}
	 * @param callInfo
	 *            呼叫随路数据，预留字段
	 * @return <b>true</b> 接口调用成功;<b>false</b> 接口调用失败,检查ip地址和端口号格式是否正确.
	 */
	public static boolean requestConnect(String accessCode, String mediaType, String callInfo)
	{
		if (!Tools.checkIP() || !Tools.checkPort())
		{
			return false;
		}
		CommonPostAsync requsetAsync = new CommonPostAsync(IAS_REQUEST_PATH.REQUEST_CONNECT_PATH, new String[] { "accessCode", "mediaType", "callInfo" });
		requsetAsync.setOnPostExecuteListener(new PostExecuteListener()
		{
			@Override
			public void onPostExecuteListener(Object result)
			{
				LogUtils.i(TAG, "requestConnect result:" + result.toString());
				NotifyMsg notifyMsg = new NotifyMsg(NotifyID.ANONY_REQUEST_CONNECT_EVENT);
				if (Constants.RESPONSE_ERROR.equals(result.toString())) // 网络响应错误
				{
					notifyMsg.setRecode(Constants.RESPONSE_ERROR_CODE);
					notifyMsg.setMsg(Constants.RESPONSE_ERROR);
				} 
				else
				{
					try
					{
						JSONObject jRetcode = new JSONObject(result.toString());
						String retcode = jRetcode.getString(Constants.NOTIFY_RETCODE);
						String msg = jRetcode.getString(Constants.NOTIFY_MSG);

						notifyMsg.setRecode(retcode);
						notifyMsg.setMsg(msg);

						if (Constants.RETCODE_SUCCESS.equals(retcode))
						{
							JSONObject jResult = jRetcode.getJSONObject("result");

							String sessionId = jResult.getString("sessionId");
							AccountInfo.getInstance().setSessionId(sessionId);

							HandReceiverUtil.onAnonyRequestSuccess();
						}
					} catch (JSONException e)
					{
						LogUtils.e(TAG, "requestConnect result exception:" + e.toString());
						e.printStackTrace();
					}
				}
				VTMApp.getInstances().sendBroadcast(notifyMsg);
			}
		});
		requsetAsync.execute(accessCode, mediaType, callInfo);
		return true;
	}

	/**
	 * 通知会话状态变更
	 * 
	 * @param state
	 *            可以为: {@link CC_STATE.AUDIOTALKING}， {@link CC_STATE.TALKING} ，
	 *            {@link CC_STATE.RELEALSE}
	 * @return <b>true</b> 接口调用成功;<b>false</b> 接口调用失败,检查ip地址和端口号格式是否正确.
	 */
	public static boolean notifySessionState(final String state)
	{
		if (!Tools.checkIP() || !Tools.checkPort())
		{
			return false;
		}
		CommonPostAsync notifyStateAsync = new CommonPostAsync(IAS_REQUEST_PATH.NOTIFY_SESSION_STATE_PATH, new String[] { "state" });
		notifyStateAsync.setOnPostExecuteListener(new PostExecuteListener()
		{
			@Override
			public void onPostExecuteListener(Object result)
			{
				LogUtils.i(TAG, "notifySessionState | state = " + state + " | result:" + result.toString());
				NotifyMsg notifyMsg = new NotifyMsg(NotifyID.ANONY_NOTIFY_SESSION_STATE_EVENT);
				notifyMsg.setMsg(state);
				if (Constants.RESPONSE_ERROR.equals(result.toString())) // 网络响应错误
				{
					notifyMsg.setRecode(Constants.RESPONSE_ERROR_CODE);
					// notifyMsg.setMsg(Constants.RESPONSE_ERROR);
				} else
				{
					try
					{
						JSONObject jRetcode = new JSONObject(result.toString());
						String retcode = jRetcode.getString(Constants.NOTIFY_RETCODE);
						// String msg =
						// jRetcode.getString(Constants.NOTIFY_MSG);

						notifyMsg.setRecode(retcode);
						// notifyMsg.setMsg(msg);
					} catch (JSONException e)
					{
						LogUtils.e(TAG, "notifySessionState result exception:" + e.toString());
						e.printStackTrace();
					}
				}
				// 响应很久才返回（甚至出现挂断返回的情况，此时会5002,5002错误码此时不能改变状态）
				if (!"5002".equals(notifyMsg.getRecode()))
				{
					HandReceiverUtil.onNotifySessionStateFailed(notifyMsg.getRecode());
				}

				VTMApp.getInstances().sendBroadcast(notifyMsg);
			}
		});
		notifyStateAsync.execute("?state=" + state);
		return true;
	}

	/**
	 * 获取会话状态事件
	 * 
	 * @return <b>true</b> 接口调用成功;<b>false</b> 接口调用失败,检查ip地址和端口号格式是否正确.
	 */
	public static boolean getSessionStateEvent()
	{
		if (!Tools.checkIP() || !Tools.checkPort())
		{
			return false;
		}
		CommonGetAsync getStateAsync = new CommonGetAsync(IAS_REQUEST_PATH.GET_SESSION_STATE_EVENT_PATH);
		getStateAsync.setOnPostExecuteListener(new PostExecuteListener()
		{
			@Override
			public void onPostExecuteListener(Object result)
			{
				LogUtils.i(TAG, "getSessionStateEvent result:" + result.toString());
				NotifyMsg notifyMsg = new NotifyMsg(NotifyID.ANONY_GET_SESSION_STATE_EVENT);
				if (Constants.RESPONSE_ERROR.equals(result.toString())) // 网络响应错误
				{
					notifyMsg.setRecode(Constants.RESPONSE_ERROR_CODE);
					notifyMsg.setMsg(Constants.RESPONSE_ERROR);

					 AccountInfo.getInstance().setIsGetIasEvent(false);//如果超时便不再轮询
				} else
				{
					try
					{
						JSONObject jRetcode = new JSONObject(result.toString());
						String retcode = jRetcode.getString(Constants.NOTIFY_RETCODE);
						String msg = jRetcode.getString(Constants.NOTIFY_MSG);

						notifyMsg.setRecode(retcode);
						notifyMsg.setMsg(msg);

						if (Constants.RETCODE_SUCCESS.equals(retcode))
						{
							final JSONObject jResult = jRetcode.getJSONObject("result");
							String eventName = "";
							// if (null != jResult
							// && !Tools.isEmpty(jResult.toString()))
							if (null != jResult && jResult.has("eventName"))
							{
								eventName = jResult.getString("eventName");
							}

							if (CC_STATE_EVENT.QUEUINGEVENT.equals(eventName))
							{
								parseAnonymousQueuingEvent();
								if (AccountInfo.getInstance().isGetIasEvent())
								{
									getSessionStateEvent();
								}
								return;
							} else if (CC_STATE_EVENT.RELEASEEVENT.equals(eventName))
							{
								AccountInfo.getInstance().setIsGetIasEvent(false);
								LogUtils.d(TAG, "getSessionStateEvent AnonymousReleaseEvent set isGetIasEvent false!");
								parseAnonymousReleaseEvent(jResult);
								return;
							} else if (CC_STATE_EVENT.OCCUPYAGENTEVENT.equals(eventName))
							{
								parseAnonymousOccupyAgentEvent(jResult);
								if (AccountInfo.getInstance().isGetIasEvent())
								{
									getSessionStateEvent();
								}
								return;
							} else if (CC_STATE_EVENT.MEETINGINFOEVENT.equals(eventName))
							{
								AccountInfo.getInstance().setIsGetIasEvent(false);
								LogUtils.d(TAG, "getSessionStateEvent AnonymousMeetingInfoEvent set isGetIasEvent false!");
								parseAnonymousMeetingInfoEvent(jResult);
								return;
							} else
							{
								notifyMsg.setMsg(Constants.NO_EVENT);
							}
						}
					} catch (JSONException e)
					{
						LogUtils.e(TAG, "getSessionStateEvent result exception:" + e.toString());
						e.printStackTrace();
					}
				}
				VTMApp.getInstances().sendBroadcast(notifyMsg);

				if (AccountInfo.getInstance().isGetIasEvent())
				{
					getSessionStateEvent();
				}
			}
		});
		getStateAsync.execute();
		return true;
	}

	/**
	 * 呼叫进入排队事件通知 解析
	 */
	private static void parseAnonymousQueuingEvent()
	{
		LogUtils.i(TAG, "AnonymousQueuingEvent");
		NotifyMsg notifyMsg = new NotifyMsg(NotifyID.ANONY_QUEUING_EVENT);
		VTMApp.getInstances().sendBroadcast(notifyMsg);
//		getCallQueueInfo();
	}
	
	public static void getCallQueueInfo() {

		CommonGetAsync getCallQueueInfoAsync = new CommonGetAsync(IAS_REQUEST_PATH.GET_CALL_QUEUE_INFO_PATH);
		getCallQueueInfoAsync.setOnPostExecuteListener(new PostExecuteListener()
		{
			@Override
			public void onPostExecuteListener(Object result)
			{
				LogUtils.i(TAG, "getCallQueueInfo result:" + result.toString());
				NotifyMsg notifyMsg = new NotifyMsg(NotifyID.QUERY_CALL_QUEUE_EVENT);
				if (Constants.RESPONSE_ERROR.equals(result.toString())) // 网络响应错误
				{
					notifyMsg.setRecode(Constants.RESPONSE_ERROR_CODE);
					notifyMsg.setMsg(Constants.RESPONSE_ERROR);
				} else
				{
					try
					{
						JSONObject jRetcode = new JSONObject(result.toString());
						String retcode = jRetcode.getString(Constants.NOTIFY_RETCODE);
						String msg = jRetcode.getString("msg");
						notifyMsg.setRecode(retcode);
						if (Constants.RETCODE_SUCCESS.equals(retcode))
						{
							JSONObject jResult = jRetcode.getJSONObject("result");
							
//							{
//								"result": {"queueInfo": "{"skillId":36,"onlineAgentNum":1,"position":1,"totalWaitTime":31
//							,"currentDeviceWaitTime":31,"configMaxcWaitTime":600,"longestWaitTime":31,"skillGroupLongestWaitTime":31
//							,"estimateWaitTime":15,"skillGroupMaxEstimateWaitTime":15}"},
//								"retcode": "0000",
//								"msg": "SUCCESS"
//								}

							if(jResult.get("queueInfo")!=""){
								String queueInfos =  (String) jResult.get("queueInfo");
								notifyMsg.setMsg(queueInfos);
							}else{
								notifyMsg.setMsg("");
							}
						}
					} catch (JSONException e)
					{
						LogUtils.e(TAG, "getCallQueueInfo result exception:" + e.getMessage());
						e.printStackTrace();
					}
				}
				VTMApp.getInstances().sendBroadcast(notifyMsg);
			}
		});
		getCallQueueInfoAsync.execute();
		
	}
	
	
	

	/**
	 * 呼叫释放事件通知 解析
	 * 
	 * @param event
	 * @throws JSONException
	 */
	private static void parseAnonymousReleaseEvent(JSONObject event) throws JSONException
	{
		LogUtils.i(TAG, "AnonymousReleaseEvent | " + event.toString());
		if(ConferenceMgr.getInstance().isCaptureFlag()){
			int closeCapRes = ConfPrew.getInstance().videoWizCloseCapture(1);
            LogUtils.d("ConfPrew videoWizCloseCapture result:"+closeCapRes);
            if(closeCapRes==0){
            	ConferenceMgr.getInstance().setCaptureFlag(false);
            	Conference.getInstance().confRelease(0);
            	ConferenceMgr.getInstance().releaseConf();
            }
		}
		MobileVTM.getInstance().releaseCall();

		NotifyMsg notifyMsg = new NotifyMsg(NotifyID.ANONY_RELEASE_EVENT);
		String reason = event.getString("reason");
		notifyMsg.setRecode(reason);
		VTMApp.getInstances().sendBroadcast(notifyMsg);
	}

	/**
	 * 呼叫预占用柜员成功事件通知 解析
	 * 
	 * @param event
	 * @throws JSONException
	 */
	private static void parseAnonymousOccupyAgentEvent(JSONObject event) throws JSONException
	{
		LogUtils.i(TAG, "AnonymousOccupyAgentEvent " + event.toString());
		NotifyMsg notifyMsg = new NotifyMsg(NotifyID.ANONY_OCCUPY_AGENT_EVENT);

		String calledNumber = event.getString("calledNumber"); // 被叫号码
		String sipServerIp = event.getString("sipServerIp");
		String sipServerPort = event.getString("sipServerPort");
		String vtmNo = event.getString("vtmNo");
		String anonymousCard = event.getString("anonymousCard");

		if (event.has("sipIsEncoded"))
		{
			AccountInfo.getInstance().setSipIsEncoded(Boolean.valueOf(event.getString("sipIsEncoded")));
		} else if (event.has("sipTlsEnable"))
		{
			AccountInfo.getInstance().setSipIsEncoded(Boolean.valueOf(event.getString("sipTlsEnable")));
		}

		AccountInfo.getInstance().setCalledNumber(calledNumber);
		AccountInfo.getInstance().setSipServerIp(sipServerIp);
		AccountInfo.getInstance().setSipServerPort(sipServerPort);
		AccountInfo.getInstance().setVtmNo(vtmNo);
		AccountInfo.getInstance().setPhoneNo(anonymousCard);

		VTMApp.getInstances().sendBroadcast(notifyMsg);

		HandReceiverUtil.onAnonyOccupyAgentSuccess();
	}

	/**
	 * 呼叫入会信息事件通知 解析
	 * 
	 * @param event
	 * @throws JSONException
	 */
	private static void parseAnonymousMeetingInfoEvent(JSONObject event) throws JSONException
	{
		LogUtils.i(TAG, "AnonymousMeetingInfoEvent " + event.toString());
		NotifyMsg notifyMsg = new NotifyMsg(NotifyID.ANONY_MEETING_INFO_EVENT);

		String confId = event.getString("confId");
		String authKey = event.getString("authKey");
		String siteId = event.getString("siteId");

		String meetingServer = getMeetingServer(event.getString("meetingServer"));

		AccountInfo.getInstance().setConfId(confId);
		AccountInfo.getInstance().setAuthKey(authKey);
		AccountInfo.getInstance().setMeetingServerSite(siteId);
		AccountInfo.getInstance().setMeetingServerIp(meetingServer);

		VTMApp.getInstances().sendBroadcast(notifyMsg);

		HandReceiverUtil.onAnonyMeetingInfoSuccess();
	}

	private static void parseMeetingServer(String meetingServerString)
	{
		LogUtils.d(TAG, "parseMeetingServer | " + "meetingServerString = " + meetingServerString);

		// "meetingServer":"192.168.1.114,10.174.5.114:1124|192.168.1.107,10.174.5.107:1124"
		String[] serverGroups = meetingServerString.split("\\|");// "192.168.1.114,10.174.5.114:1124",
																	// "192.168.1.107,10.174.5.107:1124"

		LogUtils.d(TAG, "parseMeetingServer | " + "meetingServerGroups.length = " + serverGroups.length);

		String[] ipItem;

		for (int i = 0; serverGroups != null && i < serverGroups.length; i++)
		{
			String ipItemsStr = serverGroups[i]; // "192.168.1.114,10.174.5.114:1124"
			if (!Tools.isEmpty(ipItemsStr))
			{
				// "192.168.1.114", "10.174.5.114:1124"
				ipItem = ipItemsStr.split(",");
				AccountInfo.getInstance().addIpItem(ipItem);
			}
		}
	}

	private static String getMeetingServer(String meetingServerString)
	{
		LogUtils.d(TAG, "getMeetingServer | " + "meetingServerString = " + meetingServerString);

		// "meetingServer":"192.168.1.114,10.174.5.114:1124|192.168.1.107,10.174.5.107:1124"
		parseMeetingServer(meetingServerString);

		String[] meetingServerGroups = meetingServerString.split("\\|");
		LogUtils.d(TAG, "getMeetingServer | " + "meetingServerGroups.length = " + meetingServerGroups.length);

		String meetingServer = meetingServerString;
		if (meetingServerGroups != null && meetingServerGroups.length == 1)
		{
			String[] svrs = meetingServerString.split(",");
			if (svrs != null && svrs.length >= 2)
			{
				meetingServer = svrs[1];
			} else
			{
				// meetingServer = meetingServerString.split(",")[0];
				LogUtils.d(TAG, "MeetingServer --  | " + "meetingServer = " + meetingServer);
				meetingServer = meetingServer.split(":")[0];
			}
		} else
		{
			List<String[]> IpItemList = AccountInfo.getInstance().getIpItemList();
			StringBuffer buffer = new StringBuffer();
			if (IpItemList != null)
			{
				for (int i = 0; i < IpItemList.size(); i++)
				{
					String[] IpItem = IpItemList.get(i);
					String outerIp = "";
					if (IpItem != null && IpItem.length == 2)
					{
						outerIp = IpItem[1];
						buffer.append(outerIp);
						buffer.append("|");
					}
				}
			}
			if (buffer.length() > 0)
			{
				meetingServer = buffer.substring(0, buffer.length() - 1);
			}
		}
		LogUtils.d(TAG, "MeetingServer | " + "meetingServer : " + meetingServer + "\n");
		return meetingServer;
	}

}
