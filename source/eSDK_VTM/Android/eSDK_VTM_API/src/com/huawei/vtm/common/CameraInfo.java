package com.huawei.vtm.common;

public class CameraInfo{
	
	private int index;
	
	private String deviceID;
	
	private String deviceName;
	
	public CameraInfo() {
		
	}
	
	public CameraInfo(String deviceID, String deviceName) {
	    this.deviceID = deviceID;
	    this.deviceName = deviceName;
	}
	
	public CameraInfo(int index, String deviceID, String deviceName) {
		this.index = index;
		this.deviceID = deviceID;
		this.deviceName = deviceName;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public String getDeviceID() {
		return deviceID;
	}
	
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	
	public String getDeviceName() {
		return deviceName;
	}
	
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	
}