package com.huawei.esdk.vtm;

import android.app.Application;

import com.huawei.vtm.MobileVTM;

public class VTMApp extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		MobileVTM.getInstance().initSDK(this); // 初始化SDK，启动服务
		
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		
		MobileVTM.getInstance().stopSDK(); // 停止SDK服务
	}

}
