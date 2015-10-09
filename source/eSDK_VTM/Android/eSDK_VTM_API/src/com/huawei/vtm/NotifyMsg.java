package com.huawei.vtm;

import java.io.Serializable;

/**
 * 广播通知携带消息类
 * @author lWX169831
 *
 */
public class NotifyMsg implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 5852695241803490766L;
    
    /**
     * 广播通知携带消息的Intent的key-name
     * @author lWX169831
     *
     */
    public static interface NOTIFY
    {
        /** Intent key-name **/
        public static final String KEY_NAME = "notifyMsg";
    }

    private String action;
    private String recode;
    private String msg;

    public NotifyMsg()
    {
    }

    public NotifyMsg(String action)
    {
        this.action = action;
    }
    
    /**
     * 获取广播通知的action
     * <br>
     * 
     * @return 返回广播通知的action
     * 
     * @attention 无
     * @par 示例
     * @code
     * @endcode
     * @see 
     * @since V100R003
     */
    public String getAction()
    {
        return action;
    }

    /**
     * 设置广播通知的action
     * <br>
     * @param action 广播通知的action
     * @attention 无
     * @par 示例
     * @code
     * @endcode
     * @see 
     * @since V100R003
     * {@hide}
     */
    public void setAction(String action)
    {
        this.action = action;
    }

    /**
     * 获取广播通知的返回码
     * <br>
     * 
     * @return 返回广播通知的返回码
     * @attention 无
     * @par 示例
     * @code
     * @endcode
     * @see 
     * @since V100R003
     */
    public String getRecode()
    {
        return recode;
    }

    /**
     * 设置广播通知的返回码
     * <br>
     * 
     * @param recode 广播通知的message
     * 
     * @attention 无
     * @par 示例
     * @code
     * @endcode
     * @see 
     * @since V100R003
     */
    public void setRecode(String recode)
    {
        this.recode = recode;
    }

    /**
     * 获取广播通知的message
     * <br>
     * 
     * @return 返回广播通知的message
     * 
     * @attention 无
     * @par 示例
     * @code
     * @endcode
     * @see 
     * @since V100R003
     */
    public String getMsg()
    {
        return msg;
    }

    /**
     * 设置广播通知的message
     * <br>
     * 
     * @param recode 广播通知的message
     * 
     * @attention 无
     * @par 示例
     * @code
     * @endcode
     * @see 
     * @since V100R003
     */
    public void setMsg(String msg)
    {
        this.msg = msg;
    }

}
