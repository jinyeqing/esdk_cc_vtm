package com.huawei.vtm.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.huawei.vtm.common.Constants.CALLMODE;

/**
 * 
 * 用于存储当前账号的相关信息
 * 
 * @author lWX169831
 * 
 */
public final class AccountInfo
{
    private static AccountInfo ins;
    
    //0818
    private String vtaDeviceId;
    
    public String getVtaDeviceId() {
		return vtaDeviceId;
	}

	public void setVtaDeviceId(String vtaDeviceId) {
		this.vtaDeviceId = vtaDeviceId;
	}

	/**
     * 默认的呼叫id，必须是0. 如果非0，表示当前呼叫成功过
     */
    public static final int DEFAULT_CALLID = 0;

    private int callMode = CALLMODE.CALL_NOTSTART;

    private int currentCallID;

    /** 是否已登录 **/
    private boolean isLogin = false;

    /** token **/
    private String token = "";

    /** sessionId **/
    private String sessionId = "";

    /** Vtm编号 **/
    private String vtmNo = "";

    /** SIP终端号码 **/
    private String phoneNo = "";

    /** SIP终端密码 **/
    private String password = "";

    /** 媒体网关服务器IP地址 UAP IP **/
    private String sipServerIp = "";

    /** 媒体网关服务器端口号 UAP Port **/
    private String sipServerPort = "";
    
    /** 媒体网关服务器是否加密 */
    private boolean sipIsEncoded = false;

    /** 会议Site编号 **/
    private String meetingServerSite = "";

    /** 会议MS IP **/
    private String meetingServerIp = "";

    /** 会议Key **/
    private String confKey = "";

    /** 会议ID **/
    private String confId = "";

    /** 会议鉴权码 **/
    private String authKey = "";

    /** 是否显示第三方视频,0:不显示,1:显示 **/
    private int triCallType;

    private UserInfo attachUser;

    private Map<String, UserInfo> userMap = new HashMap<String, UserInfo>();

    private List<String> userids = new ArrayList<String>();
    
    private boolean isAnonymous = false;
    
    /** 匿名呼叫时的被叫号码 **/
    private String calledNumber;
    
    /** 是否轮循获取匿名呼叫的呼叫状态 **/
    private boolean isGetIasEvent = true;
    
    /** 匿名呼叫请求响应的cookie **/
    private String cookie = "" ;
    
    /** 预留字段，呼叫信息 **/
    private String callInfo = "" ;
    
    /**匿名呼叫设置的号码**/
    private String anonymousNum = "";
    
    public String getAnonymousNum() {
		return anonymousNum;
	}

	public void setAnonymousNum(String anonymousNum) {
		this.anonymousNum = anonymousNum;
	}

	private List<String[]> ipItemList = new ArrayList<String[]>();
    
    private AccountInfo()
    {

    }

    public synchronized static AccountInfo getInstance()
    {
        if (ins == null)
        {
            ins = new AccountInfo();
        }
        return ins;
    }

    public void clear()
    {
        releaseIns();
    }
    
    private synchronized static void releaseIns()
    {
        ins = null;
    }

    public void clearConf()
    {
        setConfKey("");
        setConfId("");
        setAuthKey("");
        userMap.clear();
        userids.clear();
        setTriCallType(0);
    }

    public int getCallMode()
    {
        return callMode;
    }

    public void setCallMode(int callMode)
    {
        this.callMode = callMode;
    }

    public void setLogin(boolean isLogin)
    {
        this.isLogin = isLogin;
    }

    public boolean isLogin()
    {
        return isLogin;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public String getToken()
    {
        return token;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public String getVtmNo()
    {
        return vtmNo;
    }

    public void setVtmNo(String vtmNo)
    {
        this.vtmNo = vtmNo;
    }

    public String getPhoneNo()
    {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo)
    {
        this.phoneNo = phoneNo;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getSipServerIp()
    {
        return sipServerIp;
    }

    public void setSipServerIp(String sipServerIp)
    {
        this.sipServerIp = sipServerIp;
    }

    public String getSipServerPort()
    {
        return sipServerPort;
    }

    public void setSipServerPort(String sipServerPort)
    {
        this.sipServerPort = sipServerPort;
    }

    public boolean isSipIsEncoded() {
		return sipIsEncoded;
	}

	public void setSipIsEncoded(boolean sipIsEncoded) {
		this.sipIsEncoded = sipIsEncoded;
	}

	public String getMeetingServerSite()
    {
        return meetingServerSite;
    }

    public void setMeetingServerSite(String meetingServerSite)
    {
        this.meetingServerSite = meetingServerSite;
    }

    public String getMeetingServerIp()
    {
        return meetingServerIp;
    }

    public void setMeetingServerIp(String meetingServerIp)
    {
        this.meetingServerIp = meetingServerIp;
    }

    public String getConfKey()
    {
        return confKey;
    }

    public void setConfKey(String confKey)
    {
        this.confKey = confKey;
    }

    public String getConfId()
    {
        return confId;
    }

    public void setConfId(String confId)
    {
        this.confId = confId;
    }

    public String getAuthKey()
    {
        return authKey;
    }

    public void setAuthKey(String authKey)
    {
        this.authKey = authKey;
    }

    public int getCurrentCallID()
    {
        return currentCallID;
    }

    public void setCurrentCallID(int currentCallID)
    {
        this.currentCallID = currentCallID;
    }

    public String getCurrDeviceId()
    {
        return getSelfUserInfo().getCurrDeviceId();
    }

    public boolean isShowThirdVideo()
    {
        return (1 == triCallType);
    }

    public void setTriCallType(int triCallType)
    {
        this.triCallType = triCallType;
    }

    public UserInfo getAttachUser()
    {
        return attachUser;
    }

    public void setAttachUser(UserInfo attachUser)
    {
        this.attachUser = attachUser;
    }

    public UserInfo getSelfUserInfo()
    {
        return userMap.get(getVtmNo());
    }

    public List<UserInfo> getVTAUserInfos()
    {
        List<UserInfo> userInfos = new ArrayList<UserInfo>();

        for (UserInfo userInfo : getUserInfos())
        {
            if (!userInfo.getUserId().equals(getVtmNo()))
            {
                userInfos.add(userInfo);
            }
        }

        return userInfos;
    }

    public List<UserInfo> getUserInfos()
    {
        List<UserInfo> userInfos = new ArrayList<UserInfo>();
        UserInfo userInfo;
        for (String userId2 : userids)
        {
            userInfo = userMap.get(userId2);
            userInfos.add(userInfo);
        }
        return userInfos;
    }

    public UserInfo getUserInfo(String userId)
    {
        return userMap.get(userId);
    }

    public Map<String, UserInfo> getUserMap()
    {
        return userMap;
    }

    public Map<String, UserInfo> addUser(UserInfo userInfo)
    {
        String userId = userInfo.getUserId();
        if (!userMap.containsKey(userId))
        {
            userids.add(userId);
        }

        userMap.put(userInfo.getUserId(), userInfo);
        return userMap;
    }

    public void removeUser(String userId)
    {
        userMap.remove(userId);
        userids.remove(userId);
    }

    public boolean isAnonymous()
    {
        return isAnonymous;
    }
    
    public void setAnonymous(boolean isAnonymous)
    {
        this.isAnonymous = isAnonymous;
    }
    
    public String getCalledNumber()
    {
        return calledNumber;
    }
    
    public void setCalledNumber(String calledNumber)
    {
        this.calledNumber = calledNumber;
    }
    
    public boolean isGetIasEvent()
    {
        return isGetIasEvent;
    }
    
    public void setIsGetIasEvent(boolean isGetIasEvent)
    {
        this.isGetIasEvent = isGetIasEvent;
    }
    
    public String getCookie()
    {
        return cookie;
    }
    
    public void setCookie(String cookie)
    {
        this.cookie = cookie;
    }
    
    public String getallInfo()
    {
        return callInfo;
    }
    
    public void setCallInfo(String callInfo)
    {
        this.callInfo = callInfo;
    }
    
    public void addIpItem(String[] IpItem)
    {
        ipItemList.add(IpItem);
    }
    
    public List<String[]> getIpItemList()
    {
        return ipItemList;
    }
    
}

