package com.huawei.vtm.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 会议成员对象
 * 
 * @author lWX169831
 * 
 */
public class UserInfo
{
    /** 用户Id（软终端号码） **/
    private String userId;

    private int deviceNum = 1;

    private List<CameraInfo> cameraInfos = new ArrayList<CameraInfo>();

    public UserInfo()
    {

    }

    public UserInfo(String userId)
    {
        this.userId = userId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public int getDeviceNum()
    {
        return deviceNum;
    }

    public void setDeviceNum(int deviceNum)
    {
        this.deviceNum = deviceNum;
    }

    public String getCurrDeviceId()
    {
        if (cameraInfos != null && cameraInfos.size() > 0)
        {
            return cameraInfos.get(0).getDeviceID();
        }
        return null;
    }

    public List<CameraInfo> getCameraInfos()
    {
        return cameraInfos;
    }

    public void addCameraInfo(CameraInfo cameraInfo)
    {
        cameraInfos.add(cameraInfo);
    }

    public void removeCameraInfo(CameraInfo cameraInfo)
    {
        cameraInfos.remove(cameraInfo);
    }

}
