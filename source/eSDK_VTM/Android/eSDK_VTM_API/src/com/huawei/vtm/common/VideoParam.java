package com.huawei.vtm.common;

public class VideoParam
{
    /** 视频x分辨率（宽） **/
    private int xRes ;
    
    /** 视频y分辨率（高） **/
    private int yRes ;

    /** 视频帧率 **/
    private int nFrame ;
    
    /** 比特率 **/
    private int nBitRate ;
    
    /** 视频格式 **/
    private int nRawtype ;
    
    /** 带宽**/
    private int nBandwidth ;
    
    public VideoParam()
    {
    }

    public VideoParam(int xRes, int yRes, int nFrame)
    {
        this.xRes = xRes;
        this.yRes = yRes;
        this.nFrame = nFrame;
    }

    public int getxRes()
    {
        return xRes;
    }

    public void setxRes(int xRes)
    {
        this.xRes = xRes;
    }

    public int getyRes()
    {
        return yRes;
    }

    public void setyRes(int yRes)
    {
        this.yRes = yRes;
    }

    public int getnFrame()
    {
        return nFrame;
    }

    public void setnFrame(int nFrame)
    {
        this.nFrame = nFrame;
    }

    public int getnBitRate()
    {
        return nBitRate;
    }

    public void setnBitRate(int nBitRate)
    {
        this.nBitRate = nBitRate;
    }

    public int getnRawtype()
    {
        return nRawtype;
    }

    public void setnRawtype(int nRawtype)
    {
        this.nRawtype = nRawtype;
    }
    
    public int getnBandwidth()
    {
        return nBandwidth;
    }
    
    public void setnBandwidth(int nBandwidth)
    {
        this.nBandwidth = nBandwidth;
    }
    
    @Override
    public String toString()
    {
        return xRes + " × " + yRes;
    }
    
}
