package com.huawei.vtm.common;

import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.huawei.vtm.common.Constants.VIDEO_SHOW_MODE;
import com.huawei.vtm.utils.AES128_CBC_HEX_GLY;
import com.huawei.vtm.utils.LogUtils;
import com.huawei.vtm.utils.SHA256_BASE64;

/**
 * 
 * 用于配置类信息的存储
 * 
 * @author lWX169831
 * 
 */
public class SystemSetting
{
    private static SystemSetting ins;

    private boolean isHttps = true;

    /** 服务器地址 **/
    private String serverIp = "";

    /** 端口号 **/
    private int serverPort = 8080;

    /** 视频参数 **/
    private VideoParam videoParam;

    /** 视频显示模式 **/
    private int videoShowMode = VIDEO_SHOW_MODE.FULL;

    /** 屏幕密度 **/
    private float xdpi;

    private byte[] identifyCode = "huawei.123".getBytes(Charset
            .forName(Constants.CHARSET_UTF_8));

    private SystemSetting()
    {
    }

    public synchronized static SystemSetting getInstance()
    {
        if (ins == null)
        {
            ins = new SystemSetting();
        }
        return ins;
    }

    public boolean isHttps()
    {
        return isHttps;
    }

    public void setHttps(boolean isHttps)
    {
        this.isHttps = isHttps;
    }

    public void initServer(String ip, int port)
    {
        setServerIp(ip);
        setServerPort(port);
    }

    public String getServerIp()
    {
        return serverIp;
    }

    private void setServerIp(String serverIp)
    {
        this.serverIp = serverIp;
    }

    public int getServerPort()
    {
        return serverPort;
    }

    private void setServerPort(int serverPort)
    {
        this.serverPort = serverPort;
    }

    public VideoParam getVideoParam()
    {
        if (videoParam == null)
        {
            videoParam = new VideoParam();
        }
        return videoParam;
    }

    public void setVideoParam(VideoParam videoParam)
    {
        this.videoParam = videoParam;
    }

    public int getVideoShowMode()
    {
        return videoShowMode;
    }

    public void setVideoShowMode(int videoShowMode)
    {
        this.videoShowMode = videoShowMode;
    }

    public void setXdpi(float xdpi)
    {
        this.xdpi = xdpi;
    }

    public float getXdpi()
    {
        return xdpi;
    }

    public boolean setIdentifyCode(String identifyCode)
    {
        String[] strs = identifyCode.split(";");
        if (strs == null || strs.length != 2)
        {
            return false;
        }
        try
        {
            byte[] iv = strs[0].getBytes(Charset
                    .forName(Constants.CHARSET_UTF_8));
            byte[] key = Constants.CBC_KEY.getBytes(Charset
                    .forName(Constants.CHARSET_UTF_8));
            String ciphertext = strs[1];
            byte[] token = AES128_CBC_HEX_GLY.decode(ciphertext, key, 0, iv, 0);
            if (token == null || token.length == 0)
            {
                return false;
            }

            // 加密 IdentifyCode
            // this.identifyCode = SHA256_BASE64.execute(token, token.length);
            this.identifyCode = token;

            return true;
        }
        catch (NoSuchAlgorithmException e)
        {
            LogUtils.e("setIdentifyCode | NoSuchAlgorithmException error : "
                    + e.getMessage());
            return false;
        }
        catch (InvalidKeyException e)
        {
            LogUtils.e("setIdentifyCode | InvalidKeyException error : "
                    + e.getMessage());
            return false;
        }
        catch (BadPaddingException e)
        {
            LogUtils.e("setIdentifyCode | BadPaddingException error : "
                    + e.getMessage());
            return false;
        }
        catch (IllegalBlockSizeException e)
        {
            LogUtils.e("setIdentifyCode | IllegalBlockSizeException error : "
                    + e.getMessage());
            return false;
        }
        catch (InvalidAlgorithmParameterException e)
        {
            LogUtils.e("setIdentifyCode | InvalidAlgorithmParameterException error : "
                    + e.getMessage());
            return false;
        }
        catch (NoSuchPaddingException e)
        {
            LogUtils.e("setIdentifyCode | NoSuchPaddingException error : "
                    + e.getMessage());
            return false;
        }
    }

    public String getIdentifyCode() throws NoSuchAlgorithmException
    {
        return SHA256_BASE64.execute(identifyCode, identifyCode.length);
    }

}
