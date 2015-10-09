package com.huawei.vtm.service;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.DisplayMetrics;

import com.huawei.AudioDeviceAndroidService;
import com.huawei.vtm.NotifyMsg;
import com.huawei.vtm.NotifyMsg.NOTIFY;
import com.huawei.vtm.authentic.Authentic;
import com.huawei.vtm.common.AccountInfo;
import com.huawei.vtm.common.SystemSetting;
import com.huawei.vtm.utils.LogUtils;
import com.huawei.vtm.utils.Tools;

public class VTMApp
{

    private Application app;

    private static VTMApp ins = new VTMApp();

    private IntentFilter filter = new IntentFilter();

    private MyBroadcastReceiver receiver;

    private VTMApp()
    {
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
    }

    public static VTMApp getInstances()
    {
        return ins;
    }

    public void initApp(Application application)
    {
        if (application != null)
        {
            app = application;
            LogUtils.d("VTMApp", app.toString());

            DisplayMetrics dm = application.getResources().getDisplayMetrics();
            float xdpi = dm.xdpi;
            SystemSetting.getInstance().setXdpi(xdpi);

            Tools.unZipFile();
        }
    }

    public Application getApplication()
    {
        return app;
    }

    public void startAudioDeviceService()
    {
        Intent intent = new Intent(app, AudioDeviceAndroidService.class);
        app.startService(intent);
    }

    public void stopAudioDeviceService()
    {
        Intent service = new Intent(app, AudioDeviceAndroidService.class);
        app.stopService(service);
    }

    public void sendBroadcast(Intent intent)
    {
        app.sendBroadcast(intent);
    }

    public void sendBroadcast(NotifyMsg notifyMsg)
    {
        Intent intent = new Intent(notifyMsg.getAction());
        intent.putExtra(NOTIFY.KEY_NAME, notifyMsg);
        app.sendBroadcast(intent);
    }

    public void startHeart()
    {
        if (app != null)
        {
            receiver = new MyBroadcastReceiver();
            app.registerReceiver(receiver, filter);
        }
    }

    public void stopHeart()
    {
        if (app != null && receiver != null)
        {
            app.unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private static class MyBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            LogUtils.d("VTMApp", "action | " + action);

            // Android 系统时间变化通知(次/min)，用于心跳请求
            if (Intent.ACTION_TIME_TICK.equals(action))
            {
                if (AccountInfo.getInstance().isLogin())
                {
                    Authentic.alive();
                }
            }
        }
    }

}
