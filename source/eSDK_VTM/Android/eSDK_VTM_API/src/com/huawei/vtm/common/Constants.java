package com.huawei.vtm.common;

import com.huawei.vtm.service.VTMApp;

public class Constants
{
    
    public static final String VERSION = "V100R005C00B105";
    
    public static final String VTM_LOG = "VTMLOG";

    public static final String VTM_LOG_FILE = "VTMLOG";

    public static final String CONF_FILE = "conf";

    public static final String CONF_TEMP_FILE = "temp";

    public static final String VTM_LOG_FILE_NAME = "VTM.log";

    public static final String VTM_CONF_LOG_FILE_NAME = "VTM_CONF_TEMP.log";

    public static final String USERTYPE_TERMINAL = "terminal"; // 机具终端

    public static final String USERTYPE_MOBILE = "mobile"; // 移动终端

    public static final String HTTP = "http://";

    public static final String HTTPS = "https://";

    public static final String RESPONSE_ERROR = "response error";

    public static final String RESPONSE_ERROR_CODE = "-1";

    public static final String NOTIFY_RETCODE = "retcode";

    public static final String NOTIFY_MSG = "msg";

    public static final String RETCODE_SUCCESS = "0000";

    public static final String CHARSET_GBK = "GBK";

    public static final String CHARSET_UTF_8 = "UTF-8";

    public static final String NO_EVENT = "no_event";

    // public static final String IDENTIFYCODE = "huawei.123";
    // public static final String IDENTIFYCODE = "Delano.123";
    // public static final String IDENTIFYCODE = "ebola";

    public static final String CBC_KEY = "_Wc1689Abc*";

    public static final int TIMEOUT = 10 * 1000;

    public static final String ANNORESPATH = VTMApp.getInstances()
            .getApplication().getFilesDir()
            + "/AnnoRes";

    /**
     * 匿名呼叫类型
     * @author lWX169831
     * 
     */
    public static interface MEDIA_TYPE
    {
        public static final String WEBPHONE = "MEDIA_TYPE_WEBPHONE";
    }

    /**
     * 向MCC发送请求的路径
     * 
     * @author lWX169831
     */
    public static interface MCC_REQUEST_PATH
    {
        /** 登录 **/
        public static final String LOGIN_PATH = "/MCC/rest/100001020/0/sa/user/login";

        /** 登出 **/
        public static final String LOGOUT_PATH = "/MCC/rest/100001020/1/sa/user/logout";

        /** 心跳 **/
        public static final String ALIVE_PATH = "/MCC/rest/100001020/1/sa/user/alive";

        /** 获取终端信息 **/
        public static final String TERMINAL_PATH = "/MCC/rest/100001020/2/config/terminal";

        /** 获取终端配置信息 **/
        public static final String CLIENT_PATH = "/MCC/rest/100001020/2/config/client";

        /** 请求会场资源 **/
        public static final String CONF_CALLER_PATH = "/MCC/rest/100001020/2/ms/conf/assign/caller";

        /** 通知入会 **/
        public static final String NOTIFY_JOINED_CONF_PATH = "/MCC/rest/100001020/2/ms/notify/joined";

        /** 通知离会 **/
        public static final String NOTIFY_LEAVED_CONF_PATH = "/MCC/rest/100001020/2/ms/notify/leaved";

    }

    public static interface IAS_REQUEST_PATH
    {
        /** 请求匿名呼叫 **/
        public static final String REQUEST_CONNECT_PATH = "/IAS/rest/100002000/2/call/resource/request";

        /** 通知呼叫状态变更 **/
        public static final String NOTIFY_SESSION_STATE_PATH = "/IAS/rest/100002000/2/call/state/notify";

        /** 获取呼叫状态事件 **/
        public static final String GET_SESSION_STATE_EVENT_PATH = "/IAS/rest/100002000/2/call/state/getevent";
        
        /**获取排队信息**/
        public static final String GET_CALL_QUEUE_INFO_PATH = "/IAS/rest/100002000/2/call/queue";
    }

    public static interface CALLMODE
    {
        /** 初始状态 **/
        public static final int CALL_NOTSTART = 0;

        /** 通话中 **/
        public static final int CALL_TALKING = 1;

        /** 通话已结束 **/
        public static final int CALL_CLOSED = 2;

    }

    public static interface CC_STATE
    {
        /** 主动入会 **/
        public static final String AUDIOTALKING = "AUDIOTALKING";

        /** 已接通 **/
        public static final String TALKING = "TALKING";

        /** 已释放 **/
        public static final String RELEALSE = "RELEASE";
    }

    public static interface CC_STATE_EVENT
    {
        /** 呼叫进入排队事件 **/
        public static final String QUEUINGEVENT = "AnonymousQueuingEvent";

        /** 呼叫释放事件 **/
        public static final String RELEASEEVENT = "AnonymousReleaseEvent";

        /** 呼叫预占用柜员成功事件 **/
        public static final String OCCUPYAGENTEVENT = "AnonymousOccupyAgentEvent";

        /** 呼叫入会信息事件 **/
        public static final String MEETINGINFOEVENT = "AnonymousMeetingInfoEvent";
    }

    public static interface VIDEO_SHOW_MODE
    {
        /** 表示布满窗口 **/
        public static final int FULL = 0;

        /** 表示按视频的比例进行显示，其余部分以黑色填充 **/
        public static final int PRORATE = 1;

        /** 表示按窗口大小进行裁剪 **/
        public static final int CLIP = 2;

    }

}
