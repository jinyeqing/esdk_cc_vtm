package com.huawei.vtm.call;

import tupsdk.TupCall;
import tupsdk.TupCallParam;

public class CallSession 
{

    /**
     * Callmanager对象句柄
     */
    private CallManager callManager = null;

    // 是否是第三方业务  ，  非语音呼叫和语音留言的其他业务，例如设置前转，登记免打扰业务
    private boolean is3rdPartyServer = false;

    private boolean isCaller = false;
    /**
     * 主叫号码 一定不带域名
     */
//    private String callerNumber = null;
    /**
     * 主叫的DisplayName  From头域
     */
    private String callerDisplayName = null;
    /**
     * from ip地址
     */
    private String fromAddr = null;
    /**
     * to ip地址
     */
    private String toAddr = null;
    /**
     * 主被叫控制，  默认为普通呼叫
     */
    private CallControl callControl = CallControl.NORMAL;
    /**
     * 历史号码
     */
    private String historyNumber = null;
    /**
     * refreFlag
     */
    private int refreFlag;
    /**
     * 是否是语音留言
     */
    private boolean isVoiceMail = false;
    private String phoneContext;


    private TupCall tupCall ;


    /**
     * 用户主动挂断
     */
    private boolean userHangup = false;

    public CallSession(CallManager callManager, String callID,
            String sessionID, String callerNumber, String calleeNumber)
    {
        this.callManager = callManager;
    }

    public TupCall getTupCall()
    {
        return tupCall;
    }

    public void setTupCall(TupCall tupCall)
    {
        this.tupCall = tupCall;
    }


    public String getPhoneContext()
    {
        return phoneContext;
    }

    public void setPhoneContext(String phoneContext)
    {
        this.phoneContext = phoneContext;
    }

    /**
     * @param callManager the callManager to set
     */
    public void setCallManager(CallManager callManager)
    {
        this.callManager = callManager;
    }

    public CallSession(CallManager callManager)
    {
        this.callManager = callManager;
    }



    public CallSession(TupCall call)
    {
        this.tupCall = call;
    }


    public boolean isIs3rdPartyServer()
    {
        return is3rdPartyServer;
    }

    public void setIs3rdPartyServer(boolean is3rdPartyServer)
    {
        this.is3rdPartyServer = is3rdPartyServer;
    }

    /**
     * 被叫号码
     * @return
     */
    public String getCalleeNumber()
    {
        if(tupCall.isCaller())
        {
            return tupCall.getToNumber();
        }
        else
        {
    //        return ContactLogic.getIns().getMyContact().getBinderNumber();
            return null;
        }
    }

    /**
     * 主叫号码
     * @return
     */
    public String getCallerNumber()
    {
        if(tupCall.isCaller())
        {
  //          return ContactLogic.getIns().getMyContact().getBinderNumber();
            return null;
        }
        else
        {
            return tupCall.getFromNumber();
        }
    }

    public String getCallID()
    {
        return tupCall.getCallId()+"";
    }

    /**
     * 获得会话ID
     *
     * @return
     * @author cWX69332
     */
    public String getSessionId()
    {
        return tupCall.getCallId()+"";
    }


    /**
     * @return the callerDisplayName
     */
    public String getCallerDisplayName()
    {
        return tupCall.getFromDisplayName();
    }

    /**
     * @return the paiNumber
     */
    public String getPaiNumber()
    {
    	if(tupCall.isCaller())
    	{
    		return tupCall.getToNumber();
    	}
    	else
    	{
    		return tupCall.getFromNumber();
    	}
    }

    /**
     * @return the tellNumber
     */
    public String getTellNumber()
    {
        return tupCall.getTelNumTel();
    }


    /**
     * @return the paiDisplayName
     */
    public String getPaiDisplayName()
    {
        if(tupCall != null)
        {
            return tupCall.getFromDisplayName();
        }
        return "";
    }



    /**
     * @return the callControl
     */
    public CallControl getCallControl()
    {
        if(tupCall.getRmtCtrl() == 1){
          return  CallControl.CALLEE;
        }
        return   CallControl.NORMAL;
    }


    /**
     * @return the isVoiceMail
     */
    public boolean isVoiceMail()
    {
        return isVoiceMail;
    }

    /**
     * @param isVoiceMail the isVoiceMail to set
     */
    public void setVoiceMail(boolean isVoiceMail)
    {
        this.isVoiceMail = isVoiceMail;
    }


    /**
     * Function: TODO
     *
     * @param operationCode
     * @return int
     * @author luotianjia 00186254/huawei
     */
    public boolean voiceMailOperation(int operationCode)
    {
        return reDial(operationCode);
    }


    public boolean requestHangup()
    {
        int ret = tupCall.pnotificationHoldcall();
        
//        Logger.beginInfo(EspaceService.TAG).p(TupHelper.TAG).p("requestHangup : ").p(ret).end();
        return 0 == ret ;
    }

    public boolean cancelHangup()
    {
        int ret = tupCall.pnotificationUnholdcall();
        
 //       Logger.beginInfo(EspaceService.TAG).p(TupHelper.TAG).p("cancelHangup : ").p(ret).end();
        return 0 == ret;
    }


    /**
     * 挂机
     *
     * @param isBusy true:因忙碌拒绝接听(直接回复486，表示忙碌),
     *               false:根据服务器配置回复486或603，默认值为603
     * @return
     */
    public void hangUp(boolean isBusy)
    {
        setUserHangup(true);
//        Logger.beginInfo(EspaceService.TAG).p(TupHelper.TAG).p("end_Call ").p(getTupCall().getCallId()).end();
        this.getTupCall().endCall();
    }

    /**
     * 切换前后摄像头
     *
     * @return
     * @author cWX69332 //  add by chenke
     *//*
    public String switchCamera(int cameraIndex, int remoteRenderIndex)
    {
        Log.d("ken", "cameraIndex =" + cameraIndex + ", remoteRenderIndex="
                + remoteRenderIndex + ", callID=" + callID);
        String strRet = callManager.setVideoCaps(cameraIndex,
                remoteRenderIndex, callID);
        return strRet;
    }*/
    public boolean isSdp()
    {
        return tupCall.getHaveSDP() == 1;
    }

    public void setSdp(boolean isSdp)
    {
//        this.isSdp = isSdp;
    }

    public int getRefreFlag()
    {
        return refreFlag;
    }

    public void setRefreFlag(int refreFlag)
    {
        this.refreFlag = refreFlag;
    }

    /**
     * @return the historyNumber
     */
    public String getHistoryNumber()
    {
        return getTupCall().getFwdFromNum();
    }

    /**
     * @param historyNumber the historyNumber to set
     */
    public void setHistoryNumber(String historyNumber)
    {
        this.historyNumber = historyNumber;
    }

    public String getFromAddr()
    {
        return fromAddr;
    }

    public void setFromAddr(String fromAddr)
    {
        this.fromAddr = fromAddr;
    }

    public String getToAddr()
    {
        return toAddr;
    }

    public void setToAddr(String toAddr)
    {
        this.toAddr = toAddr;
    }


    public boolean isCaller()
    {
        return isCaller;
    }

    public void setCaller(boolean isCaller)
    {
        this.isCaller = isCaller;
    }



    public boolean isUserHangup()
    {
        return userHangup;
    }

    public void setUserHangup(boolean userHangup)
    {
        this.userHangup = userHangup;
    }



    /*
      * <p>Title: toString</p>
      * <p>Description: </p>
      * @return
      * @see java.lang.Object#toString()
      */
    @Override
    public String toString()
    {
//        return "CallSession [callManager=" + callManager + ", callID=" + callID
//                + ", sessionID=" + sessionID + ", releaseReason="
//                + releaseReason + ", callerNumber=" + callerNumber
//                + ", callerDisplayName=" + callerDisplayName
//                + ", calleeNumber=" + calleeNumber + ", fromDisplayNumber="
//                + fromDisplayNumber + ", toDisplayNumber=" + toDisplayNumber
//                + ", callControl=" + callControl + ", isSdp=" + isSdp
//                + ", isMediaXCall=" + isMediaXCall + ", paiNumber=" + paiNumber
//                + ", paiDisplayName=" + paiDisplayName + ", phoneUser="
//                + phoneUser + ", historyNumber=" + historyNumber
//                + ", refreFlag=" + refreFlag + "]";
        return "CallSession";
    }

    /**
     * Function: 主被叫控制权限
     *
     * @author luotianjia 00186254/huawei
     *         Date:  2012-12-6 下午4:48:26
     */
    public static enum CallControl
    {
        /**
         * 主叫控制
         */
        CALLER,
        /**
         * 被叫控制
         */
        CALLEE,
        /**
         * 普通呼叫
         */
        NORMAL
    }




    /**
     * Function: TODO
     * @param isVideo
     * @return String
     * @author luotianjia 00186254/huawei
     */
    public boolean answer(boolean isVideo)
    {
  //      Logger.beginInfo(EspaceService.TAG).p(TupHelper.TAG).p("accept_Call").end();
        tupCall.acceptCall(isVideo? TupCallParam.CALL_E_CALL_TYPE.TUP_CALLTYPE_VIDEO:TupCallParam.CALL_E_CALL_TYPE.TUP_CALLTYPE_AUDIO);
        return true;
    }


    /**
     * 呼叫保持
     *
     * @return
     */
    public boolean holding()
    {
  //      Logger.beginInfo(EspaceService.TAG).p(TupHelper.TAG).p("hold_Call").end();
        tupCall.holdCall();
        return true;
    }


    /**
     * 呼叫保持后恢复通话
     *
     * @return
     */
    public boolean resume()
    {
 //       Logger.beginInfo(EspaceService.TAG).p(TupHelper.TAG).p("unhold_Call").end();
        tupCall.unholdCall();
        return true;
    }


    /**
     * 通话中送号
     * 二次拨号 DTMF
     * @param code
     * @return
     */
    public boolean reDial(int code)
    {
        int reg = tupCall.sendDTMF(code);
  //      Logger.beginInfo(EspaceService.TAG).p(TupHelper.TAG).p("send_DTMF ").p(code).p("  reg = ").p(reg).end();
        return reg == 0;
    }

    /**
     * Function: 点对点通话升级到视频通话
     *
     * @return int
     * @author luotianjia 00186254/huawei
     */
    public boolean addVideo()
    {
  //      Logger.beginInfo(EspaceService.TAG).p(TupHelper.TAG).p("addVideo").end();
        tupCall.addVideo();
        return true;
    }


    /**
     * Function: 关闭视频通话，回到语音通话
     * @author luotianjia 00186254/huawei
     */
    public boolean removeVideo()
    {
   //     Logger.beginInfo(EspaceService.TAG).p(TupHelper.TAG).p("del_video").end();
        tupCall.delVideo();
        return true;
    }



    /**
     * Function: 收到语音通话增加视频通话的变更消息，同意增加视频
     * @return String
     * @author luotianjia 00186254/huawei
     */
    public boolean agreeVideoUpdate()
    {
 //      Logger.beginInfo(EspaceService.TAG).p(TupHelper.TAG).p("reply_add_video").end();
       tupCall.replyAddVideo(1);
       return true;
    }

    /**
     * 不同意视频升级
     *
     * @return
     */
    public boolean disagreeVideoUpdate()
    {
  //      Logger.beginInfo(EspaceService.TAG).p(TupHelper.TAG).p("reply_add_video").end();
        tupCall.replyAddVideo(0);
        return true;
    }



    /**
     * 静音
     * @param type 类型 -1：扬声器和麦克风， 0：麦克风， 1：扬声器
     * @param mute
     * @return
     */
    public boolean mute(int type, boolean mute)
    {
        int result = -1;
        if (type == 0)
        {
  //          Logger.beginInfo(EspaceService.TAG).p(TupHelper.TAG).p("media_mute_mic ").p(mute).end();
            result = tupCall.mediaMuteMic(mute ? 1 : 0);
        }
        else
        {
 //           Logger.beginInfo(EspaceService.TAG).p(TupHelper.TAG).p("media_mute_speak ").p(mute).end();
            result = tupCall.mediaMuteSpeak(mute ? 1 : 0);
        }
        return  (result == 0);
    }



    /**
     * 呼叫偏转
     * @param to
     */
    public void transferTo(String to)
    {
  //      Logger.beginInfo(EspaceService.TAG).p(TupHelper.TAG).p("divert_call ").p(to).end();
        tupCall.divertCall(to);
    }


    /**
     * Function: 重协商
     *
     * @return String
     * @author luotianjia 00186254/huawei
     */
    public int reInvite()
    {
        int ret=  tupCall.reinvite();
 //       Logger.beginInfo(EspaceService.TAG).p(TupHelper.TAG).p("reinvite ").end();
        return ret;
    }
}
