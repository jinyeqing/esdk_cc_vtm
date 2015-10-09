package com.huawei.esdk.vtm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huawei.vtm.MobileVTM;
import com.huawei.vtm.NotifyID;
import com.huawei.vtm.NotifyMsg;
import com.huawei.vtm.NotifyMsg.NOTIFY;
import com.huawei.vtm.utils.LogUtils;

public class ConfShareActivity extends Activity
{
    private Button leaveConfBtn;

    private ImageButton backConfBtn;

    private ImageButton muteBtn;

    private TextView logTv;

    private RelativeLayout fileSharedLayout;

    private RelativeLayout desktopSharedLayout;

    private boolean isMute = false;

    private AudioManager audioManager;

    private String TAG = ConfShareActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_conf_share);

        LogUtils.i(TAG, "onCreate");

        IntentFilter filter = new IntentFilter();
        filter.addAction(NotifyID.CONFERENCE_JOIN_EVENT);
        filter.addAction(NotifyID.CONFERENCE_TERMINATE_EVENT);
        filter.addAction(NotifyID.DATA_SHARE_START_EVENT);
        filter.addAction(NotifyID.TERMINAL_CALLING_RELEASE_EVENT);
        filter.addAction(NotifyID.TERMINAL_ALIVE_EVENT);
        registerReceiver(receiver, filter);

        initComp();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
        
        MobileVTM.getInstance().setDesktopContainer(getBaseContext(),
                desktopSharedLayout); // 接收共享数据，设置显示容器
        MobileVTM.getInstance().setFileContainer(getBaseContext(),
                fileSharedLayout); // 接收共享数据，设置显示容器
    }

    int index = 1;
    
    private void initComp()
    {
        fileSharedLayout = (RelativeLayout) findViewById(R.id.fileSharedLayout); // sharedView
        desktopSharedLayout = (RelativeLayout) findViewById(R.id.desktopSharedLayout); // sharedView

        leaveConfBtn = (Button) findViewById(R.id.leaveConf);
        backConfBtn = (ImageButton) findViewById(R.id.backConf);
        muteBtn = (ImageButton) findViewById(R.id.mute);

        logTv = (TextView) findViewById(R.id.logtext);
        logTv.setMovementMethod(ScrollingMovementMethod.getInstance());

        leaveConfBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addLog("release call(leave conf)!");
                MobileVTM.getInstance().releaseCall(); // 释放通话，结束会议
            }
        });
        backConfBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
        muteBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (MobileVTM.getInstance().mute(!isMute)) // 静音设置
                {
                    isMute = !isMute;
                    if (isMute)
                    {
                        addLog("mute!");
                        muteBtn.setImageResource(R.drawable.icon_mute_on);
                    }
                    else
                    {
                        addLog("cancle mute!");
                        muteBtn.setImageResource(R.drawable.icon_mute_off);
                    }
                }
            }
        });
    }

    private void addLog(String logText)
    {
        logTv.append(logText);
        logTv.append("\n");
        
        
        LogUtils.i(TAG, logText);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            LogUtils.d(TAG, "action = " + action);
            if (NotifyID.CONFERENCE_JOIN_EVENT.equals(action))
            {
                NotifyMsg notifyMsg = (NotifyMsg) intent
                        .getSerializableExtra(NOTIFY.KEY_NAME);
                String recode = notifyMsg.getRecode();
                if ("0".equals(recode))
                {
                    addLog("jion conf!");
//                    int currentvolume = (int) (((float) (audioManager
//                            .getStreamVolume(AudioManager.STREAM_MUSIC)) / (float) (audioManager
//                            .getStreamMaxVolume(AudioManager.STREAM_MUSIC))) * (float) 100);
//
//                    MobileVTM.getInstance().setAudioVolume(currentvolume); // 设置扬声器音量
//
//                    LogUtils.d(TAG, "current volume = "
//                            + MobileVTM.getInstance().getAudioVolume()); // 获取扬声器音量
                }
                else
                {
                    addLog("jion conf error:" + recode + "!");
                }
            }
            else if (NotifyID.CONFERENCE_TERMINATE_EVENT.equals(action))
            {
                addLog("leaved conf!");
                finish();
            }
            else if (NotifyID.TERMINAL_CALLING_RELEASE_EVENT.equals(action))
            {
                addLog("call colsed!");
                finish();
            }
            else if (NotifyID.DATA_SHARE_START_EVENT.equals(action))
            {
                NotifyMsg notifyMsg = (NotifyMsg) intent
                        .getSerializableExtra(NOTIFY.KEY_NAME);
                String recode = notifyMsg.getRecode();
                String msg = notifyMsg.getMsg();

                String sharedType;
                String sharedState;
                if ("1".equals(recode))
                {
                    sharedType = "Document sharing";
                }
                else if ("2".equals(recode))
                {
                    sharedType = "Application sharing";
                }
                else
                {
                    sharedType = "";
                }

                if ("1".equals(msg))
                {
                    sharedState = "begin !";
                    if ("1".equals(recode))
                    {
                        MobileVTM.getInstance().setFileContainer(
                                getBaseContext(), fileSharedLayout); // 接收共享数据，设置显示容器
                    }
                    else if ("2".equals(recode))
                    {
                        MobileVTM.getInstance().setDesktopContainer(
                                getBaseContext(), desktopSharedLayout); // 接收共享数据，设置显示容器
                    }
                }
                else
                {
                    sharedState = "end !";
                }
                addLog(sharedType + " - " + sharedState);

            }
            else if (NotifyID.TERMINAL_ALIVE_EVENT.equals(action))
            {
                NotifyMsg notifyMsg = (NotifyMsg) intent
                        .getSerializableExtra(NOTIFY.KEY_NAME);
                String recode = notifyMsg.getRecode();
                if (!"0000".equals(recode))
                {
                    addLog("alive failed!");
                    MobileVTM.getInstance().releaseCall();
                }
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        LogUtils.d(TAG, "current volume = "
                + MobileVTM.getInstance().getAudioVolume());
        return super.onKeyDown(keyCode, event);
    };

    @Override
    protected void onDestroy()
    {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

}
