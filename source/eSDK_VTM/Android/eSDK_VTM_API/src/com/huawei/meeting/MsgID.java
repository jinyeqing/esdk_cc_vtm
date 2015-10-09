package com.huawei.meeting;

public class MsgID
{
    /** 该消息类型主要是提供数据通道能力给外部使用，一般是给集成商提供VTA和VTM之间的一些信令交互所需的通道 **/
    public static final int CONF_COMMON_SIG_MSG = 8;
    
    /** VTA通过该消息告知VTM被保持或静音 **/
    public static final int CONF_IS_HOLD_MSG = 18;
    
    /** VTA通过该消息告知VTM侧取消被保持或静音 **/
    public static final int CONF_IS_UNHOLD_MSG = 19;
    
    /** 在通话建立过程中，VTA发送该消息给VTM，告知VTA侧的会话信息，并请求VTM告知VTM侧的会话信息 **/
    public static final int CONF_NEGOTIATE_MSG = 25;
    
    /** 质检员加入 **/
    public static final int CONF_MONITOR_JOIN_MSG = 26;
    
    /** 在VTM收到VTA的25类型的消息后，所需应答的消息，告知VTA本侧的会话相关消息 **/
    public static final int CONF_NEGOTIATE_RESPOND_MSG = 27;
    
    /** VTA通知VTM要呼叫保持/暂停视频 **/
    public static final int CONF_HOLD_MSG = 29;
    
    /** VTA通知VTM要恢复呼叫保持/暂停视频 **/
    public static final int CONF_RESUME_HOLD_MSG = 30;
    
    /** VTM通知VTA呼叫保持/暂停视频成功 **/
    public static final int CONF_HOLD_RESPOND_SUCCESS_MSG = 31;
    
    /** VTM通知VTA恢复呼叫保持/暂停视频成功 **/
    public static final int CONF_RESUME_HOLD_RESPOND_SUCCESS_MSG = 32;
    
    /** VTM通知VTA呼叫保持/暂停视频失败 **/
    public static final int CONF_HOLD_RESPOND_FAILED_MSG = 33;
   
    /** VTM通知VTA恢复呼叫保持/暂停视频失败 **/
    public static final int CONF_RESUME_HOLD_RESPOND_FAILED_MSG = 34;
    
    /** 
     * VTA通过该消息告知VTM是否已经开始服务，0表示暂停服务，1表示开始服务。
     * 在通话成功或呼叫转移失败后，会发送该消息告知VTM开始服务 
     **/
    public static final int MESSAGE_CMD_IS_SERVICE_BEGINS = 35;
    
    /** VTA通过该消息告知VTM准备进行呼叫转移，并请求VTM释放视频 **/
    public static final int MESSAGE_CMD_CALL_TRANSFER = 36;
    
    /** 质检员加入响应消息 **/
    public static final int CONF_MONITOR_JOIN_RESPOND = 38;
    
    /** 质检员切换监听/插入消息 **/
    public static final int CONF_MONITOR_SWITCH_MSG = 39;
    
    /** 质检员切换监听/插入消息响应 **/
    public static final int CONF_MONITOR_SWITCH_RESPOND = 40;
    
    /** 质检结束 **/
    public static final int CONF_QUALITY_END_MSG = 42;
    
    /** 质检员插入成功 **/
    public static final int CONF_MONITOR_JOIN_SUCCESS_MSG = 43;
    
    /** 发送带宽数据及期望的视频参数通知给VTA **/
    public static final int CONF_BANDWIDTH_INFO_MSG = 45;
    
    /** VTM端会收到VTA通过会议服务器发过来的三方呼叫请求 **/
    public static final int MESSAGE_CMD_TRIPARTITE_REQUEST = 50;
    
    /** VTM端允许进入三方，向VTA确认并请求三方协商详细信息 **/
    public static final int MESSAGE_CMD_TRIPARTITE_ACK = 51;
    
    /** 给VTA，告知三方通话处理成功 **/
    public static final int MESSAGE_CMD_TRIPARTITE_OK = 52;
    
    /** VTA响应VTM端允许进入三方的消息 **/
    public static final int MESSAGE_CMD_TRIPARTITE_CALLDATA = 53;
   
    /** VTM端不允许进入三方/告知VTA，结束三方建立流程 **/
    public static final int MESSAGE_CMD_TRIPARTITE_FINISH = 54;
    
    /** 告知对方三方处理取消，切回两方 **/
    public static final int MESSAGE_CMD_TRIPARTITE_CANCELED = 55;
    
}
