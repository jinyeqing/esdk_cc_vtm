package com.huawei.vtm;

/**
 * 广播通知ID类
 * 
 * @author lWX169831
 * 
 */
public class NotifyID
{
    /* ************************ 登录登出相关 begin ****************************** */
    /**
     * 登录MCC服务器通知 
     * <br><li><b>recode</b>：<b>-1</b> 网络异常，<b>0000</b> 成功，其他为MCC错误码
     * <br><li><b>msg</b>: recode描述
     */
    public static final String TERMINAL_LOGIN_EVENT = "mcc_login_event";

    /**
     * 登出MCC服务器通知 
     * <br><li><b>recode</b>：<b>-1</b> 网络异常，<b>0000</b> 成功，其他为MCC错误码
     * <br><li><b>msg</b>: recode描述
     */
    public static final String TERMINAL_LOGOUT_EVENT = "mcc_logout_event";
    
    /**
     * MCC心跳通知 
     * <br><li><b>recode</b>：<b>-1</b> 网络异常，<b>0000</b> 成功，其他为MCC错误码
     * <br><li><b>msg</b>: recode描述
     */
    public static final String TERMINAL_ALIVE_EVENT = "mcc_alive_event";

    /**
     * 获取终端信息通知 
     * <br><li><b>recode</b>：<b>-1</b> 网络异常，<b>0000</b> 成功，其他为MCC错误码
     * <br><li><b>msg</b>: recode描述
     */
    public static final String TERMINAL_GETTERMINALINFO_EVENT = "mcc_get_terminal_info_event";

    /**
     * 获取终端配置信息通知 
     * <br><li><b>recode</b>：<b>-1</b> 网络异常，<b>0000</b> 成功，其他为MCC错误码
     * <br><li><b>msg</b>: recode描述
     */
    public static final String TERMINAL_GETCLIENTCONFIG_EVENT = "mcc_get_client_config_event";
    
    /**
     * VOIP注册通知 
     * <br><li><b>recode</b>：<b>0</b> 成功，<b>-1</b> 失败.
     * <br><li><b>msg</b>: recode = 0 时为null；recode = -1 时为FAST错误码
     */
    public static final String TERMINAL_VOIP_REGISTER_EVENT = "voip_register_event";

    /**
     * VOIP反注册通知 
     * <br><li><b>recode</b>：<b>0</b> 成功，<b>-1</b> 失败.
     * <br><li><b>msg</b>: recode = 0 时为null；recode = -1 时为FAST错误码
     */
    public static final String TERMINAL_VOIP_DEREGISTER_EVENT = "voip_deregister_event";

    /* ************************ 登录登出相关 end ****************************** */
    
    /**
     * 获取会场资源通知 
     * <br><li><b>recode</b>：<b>-1</b> 网络异常，<b>0000</b> 成功，其他为MCC错误码
     * <br><li><b>msg</b>: recode描述
     */
    public static final String TERMINAL_ASSIGNCONFFORCALLER_EVENT = "assign_conf_for_caller_event";

    /**
     * 加入会议通知 
     * <br><li><b>recode</b>： <b>0</b> 加入会议成功，其他为Meeting错误码，
     */
    public static final String CONFERENCE_JOIN_EVENT = "conference_join_event";

    /** 会议终止通知 **/
    public static final String CONFERENCE_TERMINATE_EVENT = "conference_terminate_event";

    /** 会议断线通知 **/
    public static final String NETWORK_ERROR_EVENT = "conf_network_error_event";

    /** 会议重连成功通知 **/
    public static final String RECONNECT_EVENT = "conf_reconnect_event";
    
    /**
     * 用户加入会议通知
     * <br><li><b>msg</b>: 用户Id
     */
    public static final String CONF_USER_ENTER_EVENT = "conf_user_enter_event";

    /**
     * 用户收到其他人(VTA)发过来的信令通知
     * <br><li><b>msg</b>: 文本消息内容
     */
    public static final String MSG_ARRIVED_EVENT = "conf_msg_receive_event";
    
    /**
     * 用户离开会议
     * <br><li><b>msg</b>: 用户Id
     */
    public static final String CONF_USER_LEAVE_EVENT = "conf_user_leave_event";
    
//    /**
//     * VTA通过会议服务器发过来的三方呼叫请求,VTM判断是否允许进入三方
//     */
//    public static final String TRIPARTITE_REQUEST_EVENT = "tripartite_request_event";
    
    /**
     * 已进入三方通话
     */
    public static final String TERMINAL_THREEPARTY_TALKING_EVENT = "conf_threeparty_talking_event";
    
    /**
     * 视频变化通知 (包括自己或其它人的设备) 
     * <br><li><b>recode</b>: <b>0</b> 关闭，<b>1</b> 打开，<b>2</b> Resume，<b>4</b> Pause 
     * <br><li><b>msg</b>: <b>0</b> 本地，<b>1</b> 远端
     */
    public static final String COMPT_VIDEO_SWITCH_EVENT = "conf_compt_video_switch_event";
    
    /**
     * 语音质量信息通知
     * mos 浮点型 
     * 四舍五入 5-1 优，良，中，差，劣 
     */
    public static final String MEDIA_NTF_STATISTIC_MOS_EVENT  = "conf_notify_mos_event";
    
    /**
     * 远端视频保持/静音
     */
    public static final String VEDIO_BEEN_PAUSED_EVENT = "cond_video_been_paused_event";
    
    /**
     * 远端视频取消保持/静音
     */
    public static final String VEDIO_BEEN_RESUMED_EVENT = "conf_video_been_resumed_event";
    
    /**
     * 质检员加入
     */
    public static final String INSPECTOR_JOIN_EVENT = "conf_inspector_join_event";
    
    /**
     * 质检结束
     */
    public static final String INSERTED_END_EVENT = "conf_inserted_end_event";
    
    /**
     * 质检员插入成功
     */
    public static final String INSERTED_BEGIN_EVENT  = "conf_inserted_begin_event";

    /**
     * 质检员切换监听/插入
     */
    public static final String INSERTED_SWITCH_EVENT = "conf_inserted_switch_event";
    
    /**
     * 收到解码后的第一个关键帧的通知
     * <br><li><b>recode</b>: userId 
     * <br><li><b>msg</b>: deviceId
     */
    public static final String COMPT_VIDEO_FIRST_KEYFRAME_EVENT = "conf_compt_video_first_keyframe_event";

    
    /**
     * VIOP通话开始通知 
     * <br><li><b>recode</b>：callId 
     * <br><li><b>msg</b>: 对端的uri
     */
    public static final String TERMINAL_TALKING_EVENT = "voip_talking_event";

    /**
     * VIOP呼叫结束通知 
     * <br><li><b>recode</b>：返回码 
     * <br><li><b>msg</b>: 通话结束的原因
     * <b>cancelled</b>：呼叫被取消, <b>no-answer</b>：对端没有应答, <b>not-found</b>：没有找到对端,
     * <b>busy</b>：对端忙, <b>forbidden</b>：禁止呼叫, <b>network-busy</b>：网络忙,
     * <b>network-failure</b>：网络失败, <b>temp-unvailable</b>: 对方当前不可用.
     */
    public static final String TERMINAL_CALLING_RELEASE_EVENT = "voip_calling_release_event";
    
    
    public static final String TERMINAL_CALLING_REFRESH_VIEW_EVENT = "voip_calling_refresh_view_event";
    
    
    
    /**
     * 接收到远端共享数据
     * <br><li><b>recode</b>： 共享类型：<b>1</b> 文档共享，<b>2</b> 屏幕共享，<b>512</b> 白板.
     * <br><li><b>msg</b>： 共享状态：<b>1</b> 开始，<b>0</b> 停止.
     */
    public static final String DATA_SHARE_START_EVENT = "conf_data_share_start_event";
    
    //************************** 匿名呼叫相关 ************************************************/

    /**
     * 请求匿名呼叫回调通知
     * <br><li><b>recode</b>：<b>-1</b> 网络异常，<b>0000</b> 成功，其他为IAS错误码
     * <br><li><b>msg</b>: recode描述.
     */
    public static final String ANONY_REQUEST_CONNECT_EVENT = "anony_request_connect_event";
    
    /**
     * 排队信息上报应用事件
     */
    public static final String QUERY_CALL_QUEUE_EVENT = "anony_query_call_queue_event";
    
    
    /**
     * 通知会话状态变更通知
     * <br><li><b>recode</b>：<b>-1</b> 网络异常，<b>0000</b> 成功，其他为IAS错误码
     * <br><li><b>msg</b>: 通知的状态类型.
     */
    public static final String ANONY_NOTIFY_SESSION_STATE_EVENT = "anony_notify_session_state_event";
    
    /**
     * 获取会话状态事件通知
     * <br><li><b>recode</b>：<b>-1</b> 网络异常，<b>0000</b> 成功，其他为IAS错误码
     * <br><li><b>msg</b>: recode描述.
     */
    public static final String ANONY_GET_SESSION_STATE_EVENT = "anony_get_session_state_event";
    
    /**
     * 呼叫进入排队事件通知
     */
    public static final String ANONY_QUEUING_EVENT = "anony_queuing_event";
    
    
    /**
     * 呼叫请求被释放事件通知
     * <br><li><b>recode</b>: 呼叫释放原因码(IAS).
     */
    public static final String ANONY_CALLED_EVENT = "anony_called_event";
    
    /**
     * 呼叫请求被释放事件通知
     * <br><li><b>recode</b>: 呼叫释放原因码(IAS).
     */
    public static final String ANONY_RELEASE_EVENT = "anony_release_event";
    
    /**
     * 呼叫预占用柜员成功事件通知
     */
    public static final String ANONY_OCCUPY_AGENT_EVENT = "anony_occupy_agent_event";
    
    /**
     * 呼叫入会信息事件通知
     */
    public static final String ANONY_MEETING_INFO_EVENT = "anony_meeting_info_event";
    
    /**
     * 匿名呼叫VOIP配置完成事件通知
     */
    public static final String ANONY_CONFIG_VOIP_END_EVENT = "anony_config_voip_end_event";
    
}
