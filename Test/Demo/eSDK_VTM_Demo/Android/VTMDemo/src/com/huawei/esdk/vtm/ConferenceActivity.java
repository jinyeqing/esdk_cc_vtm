package com.huawei.esdk.vtm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huawei.vtm.MobileVTM;
import com.huawei.vtm.NotifyID;
import com.huawei.vtm.NotifyMsg;
import com.huawei.vtm.NotifyMsg.NOTIFY;
import com.huawei.vtm.common.AccountInfo;
import com.huawei.vtm.common.Constants.MEDIA_TYPE;
import com.huawei.vtm.common.SystemSetting;
import com.huawei.vtm.common.UserInfo;
import com.huawei.vtm.common.VideoParam;
import com.huawei.vtm.utils.LogUtils;
import com.huawei.vtm.utils.Tools;

public class ConferenceActivity extends Activity
{
    private Button leaveConfBtn;

    private Button makeCallBtn;
    
    private Button screenshotBtn;

    private ImageButton pauseBtn;

    private ImageButton muteBtn;

    private ImageButton dataBtn;

    private Button setVideoParamBtn;

    private Button showmodeBtn;

    private Button ratoteVideoBtn;

    private TextView logTv;
    
    private TextView tipMsg;
    private LinearLayout tip;
    private ImageView netView;

    private EditText msgTestEd;

    private Button msgTestBtn;

    private ImageButton videoHideBtn;

    private LinearLayout mLlRemoteSurface;

    private LinearLayout mLlLocalSurface;

    private RelativeLayout mRemoteRl;

    private RelativeLayout mlocalRl;

    private LinearLayout userlistLl;

    private LinearLayout bottomBarll;

    private boolean isPause = false;

    private boolean isMute = false;

    private boolean isDocShare = false;

    private boolean isAppShare = false;

    private boolean isClicked = false;

    private boolean isShow = false;

    private AudioManager audioManager;

    private VideoParam videoParam;

    private int index;

    private int showMode;

    private int angleId;

    private String callNum;

    private String TAG = ConferenceActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_conference);

        IntentFilter filter = new IntentFilter();
        filter.addAction(NotifyID.TERMINAL_ASSIGNCONFFORCALLER_EVENT);
        filter.addAction(NotifyID.CONFERENCE_JOIN_EVENT);
        filter.addAction(NotifyID.CONF_USER_ENTER_EVENT);
        filter.addAction(NotifyID.CONFERENCE_TERMINATE_EVENT);
        filter.addAction(NotifyID.CONF_USER_LEAVE_EVENT);
        filter.addAction(NotifyID.DATA_SHARE_START_EVENT);
        filter.addAction(NotifyID.TERMINAL_CALLING_RELEASE_EVENT);
        filter.addAction(NotifyID.COMPT_VIDEO_FIRST_KEYFRAME_EVENT);
        filter.addAction(NotifyID.TERMINAL_ALIVE_EVENT);
        filter.addAction(NotifyID.TERMINAL_THREEPARTY_TALKING_EVENT);
        filter.addAction(NotifyID.VEDIO_BEEN_RESUMED_EVENT);
        filter.addAction(NotifyID.VEDIO_BEEN_PAUSED_EVENT);
        filter.addAction(NotifyID.INSERTED_BEGIN_EVENT);
        filter.addAction(NotifyID.INSERTED_SWITCH_EVENT);
        filter.addAction(NotifyID.MSG_ARRIVED_EVENT);
        filter.addAction(NotifyID.COMPT_VIDEO_SWITCH_EVENT);
        filter.addAction(NotifyID.MEDIA_NTF_STATISTIC_MOS_EVENT);
        registerReceiver(receiver, filter);

        callNum = getIntent().getStringExtra("callNum");

        initComp();

        addLog("onCreate");

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

    }

    private void initComp()
    {
        mLlRemoteSurface = (LinearLayout) findViewById(R.id.mRemoteView); // RemoteView
        mLlLocalSurface = (LinearLayout) findViewById(R.id.mLocalView); // LocalView

        MobileVTM.getInstance().setVideoContainer(this, mLlLocalSurface,
                mLlRemoteSurface); // 设置视频加载容器

        mRemoteRl = (RelativeLayout) findViewById(R.id.remoteRl);
        mlocalRl = (RelativeLayout) findViewById(R.id.localRl);
        videoHideBtn = (ImageButton) findViewById(R.id.video_hide);

        userlistLl = (LinearLayout) findViewById(R.id.userlist);

        bottomBarll = (LinearLayout) findViewById(R.id.bottomBar);

        leaveConfBtn = (Button) findViewById(R.id.leaveConf);
        makeCallBtn = (Button) findViewById(R.id.makeCall);
        makeCallBtn.setVisibility(View.GONE);
        pauseBtn = (ImageButton) findViewById(R.id.pause);
        muteBtn = (ImageButton) findViewById(R.id.mute);
        dataBtn = (ImageButton) findViewById(R.id.data_share);
        setVideoParamBtn = (Button) findViewById(R.id.set);
        setVideoParamBtn.setVisibility(View.GONE);
        screenshotBtn = (Button) findViewById(R.id.screenshot);
        logTv = (TextView) findViewById(R.id.logtext);
        logTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        msgTestEd = (EditText) findViewById(R.id.msgtest);
        msgTestEd.setText(getString(R.string.messageTest));
        msgTestBtn = (Button) findViewById(R.id.msgtestbtn);

        showmodeBtn = (Button) findViewById(R.id.showmode);
        ratoteVideoBtn = (Button) findViewById(R.id.ratote_video);
        
        //0626
        tipMsg = (TextView) findViewById(R.id.tipMsg);
        tip = (LinearLayout) findViewById(R.id.tip);
        netView = (ImageView) findViewById(R.id.netView);
        
        leaveConfBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addLog("release call(leave conf)!");
                MobileVTM.getInstance().releaseCall(); // 释放通话，结束会议
            }
        });
        makeCallBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!Tools.isEmpty(callNum))
                {
                    addLog("makeCall to " + callNum);
                    MobileVTM.getInstance().anonymousCall(callNum,
                            MEDIA_TYPE.WEBPHONE, "");
                }
            }
        });
        pauseBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setVideoPause();
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
                    refreshMute();
                }
            }
        });
        dataBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if ((isDocShare || isAppShare) && !isClicked)
                {
                    isClicked = true;

                    Intent intent = new Intent(ConferenceActivity.this,
                            ConfShareActivity.class);
                    // intent.putExtra("showType", showType);
                    startActivity(intent);
                }
                // showMenuView(v);
            }
        });
        setVideoParamBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showVideoParamsDialog();
            }
        });
        msgTestBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String msg = msgTestEd.getText().toString().trim();
                if (!Tools.isEmpty(msg))
                {
                    boolean ret = MobileVTM.getInstance().sendMsg(msg);
//                    addLog("信令消息调用  -> " + ret);
                    addLog("message send return code  -> " + ret);
                }
            }
        });
        showmodeBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showModeDialog();
            }
        });
        ratoteVideoBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showRatotoAngleDialog();
            }
        });
        
        screenshotBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                    DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                    String formatDate = format.format(new Date());
                    String filename = "screenshot"+formatDate;
                    MobileVTM.getInstance().videoRenderSnapShot(filename);
                    addLog("screenshot,picture has been stored in sdcard's VTMLOG!");
            }
        });
        
        videoHideBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isShow)
                {
                    showLoaclFull(false);
                }
                else
                {
                    showLoaclFull(true);
                }
            }
        });
        mLlRemoteSurface.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (bottomBarll.isShown())
                {
                    bottomBarll.setVisibility(View.GONE);
                }
                else
                {
                    bottomBarll.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void showLoaclFull(boolean isShow)
    {
        SurfaceView viewLocal = null;
        SurfaceView viewRemote = null;
        if (mLlLocalSurface.getChildAt(0) != null)
        {
            viewLocal = (SurfaceView) mLlLocalSurface.getChildAt(0);
        }
        if (mLlRemoteSurface.getChildAt(0) != null)
        {
            viewRemote = (SurfaceView) mLlRemoteSurface.getChildAt(0);
        }

        if (isShow)
        {
            mRemoteRl.removeAllViews();
            mlocalRl.removeAllViews();

            mRemoteRl.addView(mLlLocalSurface);
            mlocalRl.addView(mLlRemoteSurface);

            if (viewLocal != null && viewRemote != null)
            {
                viewLocal.setZOrderOnTop(false);
                viewLocal.setZOrderMediaOverlay(false);

                viewRemote.setZOrderOnTop(true);
                viewRemote.setZOrderMediaOverlay(true);
            }
        }
        else
        {
            mRemoteRl.removeAllViews();
            mlocalRl.removeAllViews();

            mRemoteRl.addView(mLlRemoteSurface);
            mlocalRl.addView(mLlLocalSurface);

            if (viewLocal != null && viewRemote != null)
            {
                viewLocal.setZOrderOnTop(true);
                viewLocal.setZOrderMediaOverlay(true);

                viewRemote.setZOrderOnTop(false);
                viewRemote.setZOrderMediaOverlay(false);
            }
        }

        this.isShow = isShow;
    }

    // private void hideLocalRl()
    // {
    // LayoutParams params = new LayoutParams(1, 1);
    // mlocalRl.setLayoutParams(params);
    // }

    // private void showLoacl(boolean isShow)
    // {
    // LayoutParams params = null;
    // int res;
    // if (isShow)
    // {
    // params = new LayoutParams(getResources().getDimensionPixelSize(
    // R.dimen.local_width), getResources().getDimensionPixelSize(
    // R.dimen.local_width));
    // params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.mRemoteView);
    // params.addRule(RelativeLayout.ALIGN_TOP, R.id.mRemoteView);
    // res = R.drawable.icon_video_hid;
    // }
    // else
    // {
    // params = new LayoutParams(1, 1);
    // res = R.drawable.icon_video_show;
    // }
    // mlocalRl.setLayoutParams(params);
    // videoHideBtn.setImageResource(res);
    // videoHideBtn.setActivated(true);
    //
    // this.isShow = isShow;
    // }

    private void refreshMute()
    {
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

    private void refreshPause()
    {
        if (isPause)
        {
            addLog("pause local video!");
            pauseBtn.setImageResource(R.drawable.icon_video_play);
        }
        else
        {
            addLog("resume local video!");
            pauseBtn.setImageResource(R.drawable.icon_video_pause);
        }
    }

    /*
     * // 移动端屏幕共享的调用，由于PDU暂时还不能提供正式版本的库，所以先注释掉
     * 
     * @SuppressWarnings("deprecation") private void showMenuView(View anchor) {
     * final View view = getLayoutInflater().inflate(R.layout.menu_as_control,
     * null);
     * 
     * // 减半分辨率共享 TextView menuAsSetParamTxt = (TextView) view
     * .findViewById(R.id.menuAsSetParamTxt);
     * 
     * // 1/4分辨率共享 TextView menuASQuarterShareTxt = (TextView) view
     * .findViewById(R.id.menuAsSetQuarterParamTxt);
     * 
     * TextView menuAsSetOwnerTxt = (TextView) view
     * .findViewById(R.id.menuAsSetOwnerTxt); final TextView menuAsStartTxt =
     * (TextView) view .findViewById(R.id.menuAsStartTxt); TextView
     * menuAsAttachTxt = (TextView) view .findViewById(R.id.menuAsAttachTxt); if
     * (isStartShare) { menuAsStartTxt.setText(R.string.menu_control_as_stop); }
     * else { menuAsStartTxt.setText(R.string.menu_control_as_start); }
     * 
     * final PopupWindow popupWindow = new PopupWindow(view, getResources()
     * .getDimensionPixelSize(R.dimen.menu_width), getResources()
     * .getDimensionPixelSize(R.dimen.menu_height));
     * menuAsSetParamTxt.setOnClickListener(new OnClickListener() {
     * 
     * @Override public void onClick(View v) {
     * MobileVTM.getInstance().asSetResolveHalf();
     * 
     * popupWindow.dismiss(); } });
     * 
     * // 1/4分辨率共享的处理按钮 menuASQuarterShareTxt.setOnClickListener(new
     * OnClickListener() {
     * 
     * @Override public void onClick(View arg0) {
     * MobileVTM.getInstance().asSetResolveQuarter();
     * 
     * popupWindow.dismiss(); } });
     * 
     * menuAsSetOwnerTxt.setOnClickListener(new OnClickListener() {
     * 
     * @Override public void onClick(View v) { boolean isSetOwnerOk =
     * MobileVTM.getInstance().asSetOwner( AS_ACTION.AS_ACTION_ADD); if
     * (isSetOwnerOk) { MobileVTM.getInstance().asSetShareRange(2000, 1500); }
     * 
     * popupWindow.dismiss(); } }); menuAsStartTxt.setOnClickListener(new
     * OnClickListener() {
     * 
     * @Override public void onClick(View v) { if (isStartShare) {
     * MobileVTM.getInstance().asStop(); isStartShare = false; } else {
     * MobileVTM.getInstance().asStart(); isStartShare = true; }
     * 
     * popupWindow.dismiss(); } }); menuAsAttachTxt.setOnClickListener(new
     * OnClickListener() {
     * 
     * @Override public void onClick(View v) { Intent intent = new
     * Intent(ConferenceActivity.this, ConfShareActivity.class);
     * startActivity(intent); popupWindow.dismiss(); } }); // 试试用 PopuoMenu
     * popupWindow.setBackgroundDrawable(new BitmapDrawable());
     * popupWindow.setOutsideTouchable(true); popupWindow.showAsDropDown(
     * anchor, 0, -(anchor.getHeight() + getResources().getDimensionPixelSize(
     * R.dimen.menu_height)));
     * 
     * }
     */

    private void setVideoPause()
    {
        if (!isPause)
        {
            // if(MobileVTM.getInstance().setVideoRotate(90))
            if (MobileVTM.getInstance().videoPause()) // 暂停本地视频
            {
                isPause = !isPause;
                refreshPause();
            }
        }
        else
        {
            // if(MobileVTM.getInstance().setVideoRotate(180))
            if (MobileVTM.getInstance().videoResume()) // 恢复暂停的本地视频
            {
                isPause = !isPause;
                refreshPause();
            }
        }
    }

    private void showVideoParamsDialog()
    {
        final List<VideoParam> videoParams = MobileVTM.getInstance()
                .getVideoParams();
        final String[] items = new String[videoParams.size()];
        VideoParam param;
        for (int i = 0; i < videoParams.size(); i++)
        {
            param = videoParams.get(i);
            items[i] = param.getxRes() + " × " + param.getyRes() + "  Frame："
                    + param.getnFrame();
        }

        Builder videoParamsDialog = new AlertDialog.Builder(this);
        videoParamsDialog.setSingleChoiceItems(items, index,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        index = which;
                    }
                });
        videoParamsDialog.setPositiveButton(getString(R.string.OK),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        videoParam = videoParams.get(index);
                        MobileVTM.getInstance().setVideoParam(videoParam);
                    }
                });
        videoParamsDialog.setNegativeButton(getString(R.string.cancel), null);
        videoParamsDialog.show();
    }

    private void showModeDialog()
    {
        final String[] items = getResources()
                .getStringArray(R.array.video_mode);

        showMode = SystemSetting.getInstance().getVideoShowMode();

        Builder modeDialog = new AlertDialog.Builder(this);
        modeDialog.setSingleChoiceItems(items, showMode,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        showMode = which;
                        if (MobileVTM.getInstance().setVideoShowMode(showMode))
                        {
//                            addLog("切换视频显示模式 -> " + items[showMode] + " ,成功");
                            addLog("switch video mode -> " + items[showMode] + " ,successfully");
                        }
                        else
                        {
//                            addLog("切换视频显示模式 -> " + items[showMode] + " ,失败");
                            addLog("switch video mode -> " + items[showMode] + " ,failed");
                        }
                    }
                });
        modeDialog.show();
    }

    private void showRatotoAngleDialog()
    {
        final String[] items = getResources().getStringArray(
                R.array.ratote_angle);

        Builder ratotoDialog = new AlertDialog.Builder(this);
        ratotoDialog.setSingleChoiceItems(items, angleId,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        angleId = which;
                        MobileVTM.getInstance().setVideoRotate(
                                Integer.parseInt(items[angleId]));
                    }
                });
        ratotoDialog.show();
    }

    private void addLog(String logText)
    {
        logTv.append(logText);
        logTv.append("\n");

        LogUtils.i(TAG, logText);
    }

    private void setUserList()
    {
        userlistLl.removeAllViews();
        List<UserInfo> userInfos = AccountInfo.getInstance().getUserInfos();

        for (final UserInfo userInfo : userInfos)
        {
            View view = getLayoutInflater().inflate(R.layout.user_imageview,
                    null);
            final ImageView imageView = (ImageView) view
                    .findViewById(R.id.image);
            imageView.setImageResource(R.drawable.ic_launcher);

            boolean isSelf = (userInfo.getUserId() == AccountInfo.getInstance()
                    .getSelfUserInfo().getUserId());
            boolean isAttachUser = (null != AccountInfo.getInstance()
                    .getAttachUser() && userInfo.getUserId() == AccountInfo
                    .getInstance().getAttachUser().getUserId());
            boolean isDisplay = AccountInfo.getInstance().isShowThirdVideo();

            if (!isSelf && isDisplay && !isAttachUser && userInfos.size() > 2)
            {
                imageView.setImageResource(R.drawable.play_voice_focus);
                imageView.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (MobileVTM.getInstance().videoSwitch(
                                userInfo.getUserId()))
                        {
                            imageView.setImageResource(R.drawable.ic_launcher);
                            setUserList();
                        }
                    }
                });
            }
            userlistLl.addView(view);
        }
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
                    addLog("join conf!");
                    int currentvolume = (int) (((float) (audioManager
                            .getStreamVolume(AudioManager.STREAM_MUSIC)) / (float) (audioManager
                            .getStreamMaxVolume(AudioManager.STREAM_MUSIC))) * (float) 100);

                    MobileVTM.getInstance().setAudioVolume(currentvolume); // 设置扬声器音量

                    LogUtils.d(TAG, "current volume = "
                            + MobileVTM.getInstance().getAudioVolume()); // 获取扬声器音量
                }
                else
                {
                    addLog("join conf error:" + recode + "!");
                }
            }
            else if (NotifyID.TERMINAL_ASSIGNCONFFORCALLER_EVENT.equals(action))
            {
                addLog("assign conf for caller !");
            }
            else if (NotifyID.COMPT_VIDEO_FIRST_KEYFRAME_EVENT.equals(action))
            {
                addLog("video first keyframe event !");
                // hideLocalRl();
            }
            else if (NotifyID.CONF_USER_ENTER_EVENT.equals(action))
            {
                addLog("has user enter conf !");
                setUserList();
            }
            else if (NotifyID.CONFERENCE_TERMINATE_EVENT.equals(action))
            {
                addLog("leaved conf!");
                setUserList();
                finish();
            }
            else if (NotifyID.CONF_USER_LEAVE_EVENT.equals(action))
            {
                addLog("has user leaved conf !");
                setUserList();
            }
            else if (NotifyID.TERMINAL_CALLING_RELEASE_EVENT.equals(action))
            {
                addLog("call colsed!");
                setUserList();
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

                isClicked = false;

                if ("1".equals(msg))
                {
                    sharedState = "begin !";
                    if ("1".equals(recode))
                    {
                        isDocShare = true; // 接收共享数据，设置显示容器
                    }
                    else if ("2".equals(recode))
                    {
                        isAppShare = true; // 接收共享数据，设置显示容器
                    }
                }
                else
                {
                    sharedState = "end !";

                    if ("1".equals(recode))
                    {
                        isDocShare = false; // 接收共享数据，设置显示容器
                    }
                    else if ("2".equals(recode))
                    {
                        isAppShare = false; // 接收共享数据，设置显示容器
                    }
                }
                if (isDocShare || isAppShare)
                {
                    dataBtn.setImageResource(R.drawable.icon_share_select);
                }
                else
                {
                    dataBtn.setImageResource(R.drawable.icon_data_select);
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
            else if (NotifyID.TERMINAL_THREEPARTY_TALKING_EVENT.equals(action))
            {
                addLog("tripartite success!");
                setUserList();
            }
            else if (NotifyID.VEDIO_BEEN_RESUMED_EVENT.equals(action))
            {
                addLog("vta was be cancel hold/mute !");
            }
            else if (NotifyID.VEDIO_BEEN_PAUSED_EVENT.equals(action))
            {
                addLog("vta was be hold/mute !");
            }
            else if (NotifyID.INSERTED_BEGIN_EVENT.equals(action))
            {
                addLog("monitor join success!");
                setUserList();
            }
            else if (NotifyID.INSERTED_SWITCH_EVENT.equals(action))
            {
                addLog("monitor switch success!");
                setUserList();
            }
            else if (NotifyID.MSG_ARRIVED_EVENT.equals(action))
            {
                NotifyMsg notifyMsg = (NotifyMsg) intent
                        .getSerializableExtra(NOTIFY.KEY_NAME);
                String msg = notifyMsg.getMsg();
                addLog("msg = " + msg);
            }
            else if (NotifyID.COMPT_VIDEO_SWITCH_EVENT.equals(action))
            {
                // recode: 0 关闭，1 打开，2 Resume，4 Pause
                // msg: 0 本地，1 远端
                NotifyMsg notifyMsg = (NotifyMsg) intent
                        .getSerializableExtra(NOTIFY.KEY_NAME);
                String recode = notifyMsg.getRecode();
                String msg = notifyMsg.getMsg();
                if ("1".equals(recode) && "0".equals(msg))
                {
                    // MobileVTM.getInstance().setVideoRotate(180);
                }
            }else if (NotifyID.MEDIA_NTF_STATISTIC_MOS_EVENT.equals(action)) {
                NotifyMsg notifyMsg = (NotifyMsg) intent
                        .getSerializableExtra(NOTIFY.KEY_NAME);
                String msg = notifyMsg.getMsg();
                float mos = Float.parseFloat(msg);
                String tip5 = "语音质量非常好，适合通话";
                String tip4 = "语音质量稍差，延迟小";
                String tip3 = "语音还可以，有一定延迟";
                String tip2 = "语音很勉强，不太适合通话";
                String tip1 = "极差，建议挂断本次通话!";
           //     int  mosFlag =   Math.round(mos);
                String tip = "";
               if (mos>2.8)
            {
               // tip = tip5;
                netView.setImageResource(R.drawable.call_signal_five);
            }else if (mos>2.4) {
               // tip = tip4;
                netView.setImageResource(R.drawable.call_signal_four);
            }else if (mos>2.0) {
                tip = tip3;
                netView.setImageResource(R.drawable.call_signal_three);
            }else if (mos>1.6) {
                tip = tip2;
                netView.setImageResource(R.drawable.call_signal_two);
            }else {
                tip = tip1;
                netView.setImageResource(R.drawable.call_signal_one);
            }
              //   addLog("mos: "+mos+", "+tip);
                 tipMsg.setText(tip);
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
    protected void onStart()
    {
        super.onStart();
        LogUtils.i(TAG, "onStart");
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        LogUtils.i(TAG, "onRestart");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        LogUtils.i(TAG, "onResume");

        isClicked = false;

        if (isPause)
        {
            setVideoPause();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        LogUtils.i(TAG, "onPause");

        if (!isPause)
        {
            setVideoPause();
        }

        if (bottomBarll != null)
        {
            bottomBarll.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        LogUtils.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy()
    {
        unregisterReceiver(receiver);
        addLog("onDestroy");
        super.onDestroy();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        MobileVTM.getInstance().releaseCall();
    }
}
