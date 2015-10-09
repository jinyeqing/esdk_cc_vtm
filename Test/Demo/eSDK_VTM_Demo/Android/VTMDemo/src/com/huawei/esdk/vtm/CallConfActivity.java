package com.huawei.esdk.vtm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.widget.Toast;

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

public class CallConfActivity extends Activity {

	private String TAG = "CallConfActivity";

	private int callorConfFlag = 0;// call page or conf page flag

	private RelativeLayout callRLayout1, callRLayout2, confRLayout1,
			confRLayout2;

	private boolean toConfFlag = true;// control to conf page flag
	
	private boolean bottomControlFlag = false; //会议底部显示控制

	private Button queryQueueInfo;

	private Button pingIAS;

	private TextView tvLog;// call page log

	private EditText edAccessCode;

	private IntentFilter filter;

	private String calledNum;

	private LinearLayout mLlLocalSurface;

	private RelativeLayout mlocalRl;

	private SharedPreferences preferences;

	// ------------------------------------------------------------------------

	private Button leaveConfBtn;

	private Button screenshotBtn;// 截图按钮暂隐藏

	private ImageButton pauseBtn;

	private ImageButton muteBtn;

	private ImageButton dataBtn;

	private Button setVideoParamBtn;

	private Button showmodeBtn;

	private Button ratoteVideoBtn;

	private TextView logTv;// conf page 日志

	private TextView tipMsg;// 信号提示内容
	private LinearLayout tip;
	private ImageView netView;// 信号图标

	private EditText msgTestEd;

	private Button msgTestBtn;

	private ImageButton videoHideBtn;

	private LinearLayout mLlRemoteSurface;

	private RelativeLayout mRemoteRl;

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

	private LinearLayout  msgtext;// 会议视频下面框及信令框

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.call_conf);

		callRLayout1 = (RelativeLayout) findViewById(R.id.callRLayout1);
		callRLayout2 = (RelativeLayout) findViewById(R.id.callRLayout2);
		confRLayout1 = (RelativeLayout) findViewById(R.id.confRLayout1);
		confRLayout2 = (RelativeLayout) findViewById(R.id.confRLayout2);
		confRLayout1.setVisibility(View.GONE);
		confRLayout2.setVisibility(View.GONE);
//		bottomBar = (LinearLayout) findViewById(R.id.bottomBar);
		msgtext = (LinearLayout) findViewById(R.id.msgtext);
		queryQueueInfo = (Button) findViewById(R.id.queryQueueInfo);

		queryQueueInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addLog("queryQueueInfo");
				MobileVTM.getInstance().getCallQueueInfo();
			}
		});
		// 呼叫前测试与IAS的ping 暂时不开放
		pingIAS = (Button) findViewById(R.id.pingConn);
		pingIAS.setVisibility(View.GONE);
		pingIAS.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String result = MobileVTM.getInstance().getPingIAS();
				addLog(result + " ");
			}
		});

		preferences = getSharedPreferences(TestData.VTMSHARED,
				Context.MODE_PRIVATE);
		calledNum = preferences.getString(TestData.SHARED_ANONYCODE,
				TestData.ANONY_CODE);

		tvLog = (TextView) findViewById(R.id.logtext);
		tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());

		edAccessCode = (EditText) findViewById(R.id.accessCode);
		edAccessCode.setText(calledNum);

		filter = new IntentFilter();
		filter.addAction(NotifyID.TERMINAL_CALLING_RELEASE_EVENT);
		filter.addAction(NotifyID.ANONY_REQUEST_CONNECT_EVENT);
		filter.addAction(NotifyID.ANONY_NOTIFY_SESSION_STATE_EVENT);
		filter.addAction(NotifyID.ANONY_QUEUING_EVENT);
		filter.addAction(NotifyID.ANONY_RELEASE_EVENT);
		filter.addAction(NotifyID.ANONY_OCCUPY_AGENT_EVENT);
		filter.addAction(NotifyID.ANONY_MEETING_INFO_EVENT);
		filter.addAction(NotifyID.ANONY_CONFIG_VOIP_END_EVENT);

		filter.addAction(NotifyID.ANONY_CALLED_EVENT);

		filter.addAction(NotifyID.QUERY_CALL_QUEUE_EVENT);// 排队信息

		// -----------------------------------------------
		if (callorConfFlag == 1) {
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}

		filter.addAction(NotifyID.TERMINAL_ASSIGNCONFFORCALLER_EVENT);
		filter.addAction(NotifyID.CONFERENCE_JOIN_EVENT);
		filter.addAction(NotifyID.CONF_USER_ENTER_EVENT);
		filter.addAction(NotifyID.CONFERENCE_TERMINATE_EVENT);
		filter.addAction(NotifyID.CONF_USER_LEAVE_EVENT);
		filter.addAction(NotifyID.DATA_SHARE_START_EVENT);
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
		initComp();

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

	}

	private void initComp() {
		mLlRemoteSurface = (LinearLayout) findViewById(R.id.mRemoteView); // RemoteView
		mLlLocalSurface = (LinearLayout) findViewById(R.id.mLocalView); // LocalView
		mRemoteRl = (RelativeLayout) findViewById(R.id.remoteRl);
		mlocalRl = (RelativeLayout) findViewById(R.id.localRl);
		videoHideBtn = (ImageButton) findViewById(R.id.video_hide);

		userlistLl = (LinearLayout) findViewById(R.id.userlist);

		bottomBarll = (LinearLayout) findViewById(R.id.bottomBar);

		leaveConfBtn = (Button) findViewById(R.id.leaveConf);
		pauseBtn = (ImageButton) findViewById(R.id.pause);
		muteBtn = (ImageButton) findViewById(R.id.mute);
		dataBtn = (ImageButton) findViewById(R.id.data_share);
		setVideoParamBtn = (Button) findViewById(R.id.set);
		setVideoParamBtn.setVisibility(View.GONE);
		screenshotBtn = (Button) findViewById(R.id.screenshot);
		logTv = (TextView) findViewById(R.id.conflogtext);
		logTv.setMovementMethod(ScrollingMovementMethod.getInstance());
		msgTestEd = (EditText) findViewById(R.id.msgtest);
		msgTestEd.setText(getString(R.string.messageTest));
		msgTestBtn = (Button) findViewById(R.id.msgtestbtn);
		showmodeBtn = (Button) findViewById(R.id.showmode);
		ratoteVideoBtn = (Button) findViewById(R.id.ratote_video);

		tipMsg = (TextView) findViewById(R.id.tipMsg);
		tip = (LinearLayout) findViewById(R.id.tip);
		netView = (ImageView) findViewById(R.id.netView);

		leaveConfBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addConfLog("release call(leave conf)!");
				MobileVTM.getInstance().releaseCall(); // 释放通话，结束会议
			}
		});
		pauseBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setVideoPause();
			}
		});
		muteBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (MobileVTM.getInstance().mute(!isMute)) // 静音设置
				{
					isMute = !isMute;
					refreshMute();
				}
			}
		});
		dataBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ((isDocShare || isAppShare) && !isClicked) {
					isClicked = true;

					Intent intent = new Intent(CallConfActivity.this,
							ConfShareActivity.class);
					// intent.putExtra("showType", showType);
					startActivity(intent);
				}
				// showMenuView(v);
			}
		});
		setVideoParamBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showVideoParamsDialog();
			}
		});
		msgTestBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String msg = msgTestEd.getText().toString().trim();
				if (!Tools.isEmpty(msg)) {
					boolean ret = MobileVTM.getInstance().sendMsg(msg);
					// addConfLog("信令消息调用  -> " + ret);
					addConfLog("message send return code  -> " + ret);
				}
			}
		});
		showmodeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showModeDialog();
			}
		});
		ratoteVideoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showRatotoAngleDialog();
			}
		});

		screenshotBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
				String formatDate = format.format(new Date());
				String filename = "screenshot" + formatDate;
			    MobileVTM.getInstance().videoRenderSnapShot(filename);
				addConfLog("screenshot,picture has been stored in sdcard's VTMLOG!");
			}
		});

		videoHideBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isShow) {
					showLoaclFull(false);
				} else {
					showLoaclFull(true);
				}
			}
		});
		mLlRemoteSurface.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(bottomControlFlag){
					if (bottomBarll.isShown()) {
						bottomBarll.setVisibility(View.GONE);
					} else {
						bottomBarll.setVisibility(View.VISIBLE);
					}
				}
				
			}
		});
	}

	private void showLoaclFull(boolean isShow) {
		SurfaceView viewLocal = null;
		SurfaceView viewRemote = null;
		if (mLlLocalSurface.getChildAt(0) != null) {
			viewLocal = (SurfaceView) mLlLocalSurface.getChildAt(0);
		}
		if (mLlRemoteSurface.getChildAt(0) != null) {
			viewRemote = (SurfaceView) mLlRemoteSurface.getChildAt(0);
		}

		if (isShow) {
			mRemoteRl.removeAllViews();
			mlocalRl.removeAllViews();

			mRemoteRl.addView(mLlLocalSurface);
			mlocalRl.addView(mLlRemoteSurface);

			if (viewLocal != null && viewRemote != null) {
				viewLocal.setZOrderOnTop(false);
				viewLocal.setZOrderMediaOverlay(false);

				viewRemote.setZOrderOnTop(true);
				viewRemote.setZOrderMediaOverlay(true);
			}
		} else {
			mRemoteRl.removeAllViews();
			mlocalRl.removeAllViews();

			mRemoteRl.addView(mLlRemoteSurface);
			mlocalRl.addView(mLlLocalSurface);

			if (viewLocal != null && viewRemote != null) {
				viewLocal.setZOrderOnTop(true);
				viewLocal.setZOrderMediaOverlay(true);

				viewRemote.setZOrderOnTop(false);
				viewRemote.setZOrderMediaOverlay(false);
			}
		}

		this.isShow = isShow;
	}

	private void refreshMute() {
		if (isMute) {
			addConfLog("mute!");
			muteBtn.setImageResource(R.drawable.icon_mute_on);
		} else {
			addConfLog("cancle mute!");
			muteBtn.setImageResource(R.drawable.icon_mute_off);
		}
	}

	private void refreshPause() {
		if (isPause) {
			addConfLog("pause local video!");
			pauseBtn.setImageResource(R.drawable.icon_video_play);
		} else {
			addConfLog("resume local video!");
			pauseBtn.setImageResource(R.drawable.icon_video_pause);
		}
	}

	private void setVideoPause() {
		if (!isPause) {
			// if(MobileVTM.getInstance().setVideoRotate(90))
			if (MobileVTM.getInstance().videoPause()) // 暂停本地视频
			{
				isPause = !isPause;
				refreshPause();
			}
		} else {
			// if(MobileVTM.getInstance().setVideoRotate(180))
			if (MobileVTM.getInstance().videoResume()) // 恢复暂停的本地视频
			{
				isPause = !isPause;
				refreshPause();
			}
		}
	}

	private void showVideoParamsDialog() {
		final List<VideoParam> videoParams = MobileVTM.getInstance()
				.getVideoParams();
		if (videoParams == null) {
			addConfLog("video devices ability can't be achieved!");
		} else {
			final String[] items = new String[videoParams.size()];
			VideoParam param;
			for (int i = 0; i < videoParams.size(); i++) {
				param = videoParams.get(i);
				items[i] = param.getxRes() + " × " + param.getyRes()
						+ "  Frame：" + param.getnFrame();
			}

			Builder videoParamsDialog = new AlertDialog.Builder(this);
			videoParamsDialog.setSingleChoiceItems(items, index,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							index = which;
						}
					});
			videoParamsDialog.setPositiveButton(getString(R.string.OK),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							videoParam = videoParams.get(index);
							MobileVTM.getInstance().setVideoParam(videoParam);
						}
					});
			videoParamsDialog.setNegativeButton(getString(R.string.cancel),
					null);
			videoParamsDialog.show();
		}

	}

	private void showModeDialog() {
		final String[] items = getResources()
				.getStringArray(R.array.video_mode);

		showMode = SystemSetting.getInstance().getVideoShowMode();

		Builder modeDialog = new AlertDialog.Builder(this);
		modeDialog.setSingleChoiceItems(items, showMode,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						showMode = which;
						if (MobileVTM.getInstance().setVideoShowMode(showMode)) {
							// addConfLog("切换视频显示模式 -> " + items[showMode] +
							// " ,成功");
							addConfLog("switch video mode -> "
									+ items[showMode] + " ,successfully");
						} else {
							// addConfLog("切换视频显示模式 -> " + items[showMode] +
							// " ,失败");
							addConfLog("switch video mode -> "
									+ items[showMode] + " ,failed");
						}
					}
				});
		modeDialog.show();
	}

	private void showRatotoAngleDialog() {
		final String[] items = getResources().getStringArray(
				R.array.ratote_angle);

		Builder ratotoDialog = new AlertDialog.Builder(this);
		ratotoDialog.setSingleChoiceItems(items, angleId,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						angleId = which;
						MobileVTM.getInstance().setVideoRotate(
								Integer.parseInt(items[angleId]));
					}
				});
		ratotoDialog.show();
	}

	private void setUserList() {
		userlistLl.removeAllViews();
		List<UserInfo> userInfos = AccountInfo.getInstance().getUserInfos();

		for (final UserInfo userInfo : userInfos) {
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

			if (!isSelf && isDisplay && !isAttachUser && userInfos.size() > 2) {
				imageView.setImageResource(R.drawable.play_voice_focus);
				imageView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (MobileVTM.getInstance().videoSwitch(
								userInfo.getUserId())) {
							imageView.setImageResource(R.drawable.ic_launcher);
							setUserList();
						}
					}
				});
			}
			userlistLl.addView(view);
		}
	}

	private void addConfLog(String logText) {
		logTv.append(logText);
		logTv.append("\n");
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.anonymousCall: {
			String calledNum = edAccessCode.getText().toString().trim();
			toConfFlag = true;
			bottomControlFlag = false;
			if (calledNum.length() > 0) {
				Editor editor = preferences.edit();
				editor.putString(TestData.SHARED_ANONYCODE, calledNum);
				editor.commit();

				// Intent intent = new Intent(AnonymousActivity.this,
				// ConferenceActivity.class);
				// intent.putExtra("callNum", calledNum);
				// startActivity(intent);

				boolean ret = MobileVTM.getInstance().anonymousCall(calledNum,
						MEDIA_TYPE.WEBPHONE, "");
				if (ret) {
				} else {
					addLog(getString(R.string.checkServerConfig));
				}
			}
		}
			break;

		default:
			break;
		}
	}

	private void openLocalVideoPreview() {
		MobileVTM.getInstance().setConfContainer(this, mLlLocalSurface);// 开启本地预览（向导）
	}

	private void addLog(String logText) {
		tvLog.append(logText);
		tvLog.append("\n");
		LogUtils.i(TAG, logText);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		LogUtils.d(TAG, "current volume = "
				+ MobileVTM.getInstance().getAudioVolume());
		return super.onKeyDown(keyCode, event);
	};

	@Override
	protected void onStart() {
		super.onStart();
		LogUtils.i(TAG, "onStart");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		LogUtils.i(TAG, "onRestart");
	}

	@Override
	public void onBackPressed() {
		if (callorConfFlag == 0) {
			MobileVTM.getInstance().releaseCall();
			super.onBackPressed();
		} else if (callorConfFlag == 1) {
			MobileVTM.getInstance().releaseCall();
			callRLayout1.setVisibility(View.VISIBLE);
			callRLayout2.setVisibility(View.VISIBLE);
			confRLayout1.setVisibility(View.GONE);
			confRLayout2.setVisibility(View.GONE);
			callorConfFlag = 0;
		}

	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		addConfLog("onDestroy");
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// registerReceiver(receiver, filter);
		if (callorConfFlag == 1) {
			LogUtils.i(TAG, "onResume");
			isClicked = false;
			if (isPause) {
				setVideoPause();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// unregisterReceiver(receiver);
		if (callorConfFlag == 1) {
			LogUtils.i(TAG, "onPause");

			if (!isPause) {
				setVideoPause();
			}

			if (bottomBarll != null) {
				bottomBarll.setVisibility(View.GONE);
			}
		}

	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			NotifyMsg notifyMsg = (NotifyMsg) intent
					.getSerializableExtra(NOTIFY.KEY_NAME);
			LogUtils.d(TAG, notifyMsg.getAction());

			if (NotifyID.TERMINAL_CALLING_RELEASE_EVENT.equals(action)) {
				if (callorConfFlag == 0) {
					String recode = notifyMsg.getRecode();
					String msg = notifyMsg.getMsg();
					if (recode != null) {
						addLog("call closed | " + "code:" + recode
								+ ", reason:" + ((msg == null) ? "" : msg));
						toConfFlag = false;
					} else {
						addLog("call closed");
						toConfFlag = false;
					}

				} else if (callorConfFlag == 1) {
					addConfLog("call closed!");
					setUserList();
					callRLayout1.setVisibility(View.VISIBLE);
					callRLayout2.setVisibility(View.VISIBLE);
					confRLayout1.setVisibility(View.GONE);
					confRLayout2.setVisibility(View.GONE);
					// finish();
				}

			}
			if (NotifyID.ANONY_REQUEST_CONNECT_EVENT.equals(action)) {
				String recode = notifyMsg.getRecode();
				String msg = notifyMsg.getMsg();
				addLog(getString(R.string.anonymous_call_response_tip) + msg
						+ "(" + recode + ")");
			} else if (NotifyID.ANONY_CONFIG_VOIP_END_EVENT.equals(action)) {
				// addLog("匿名呼叫VOIP配置完成");
				if (callorConfFlag == 1) {
					addConfLog("voip config for anonymous call ok");
				} else if (callorConfFlag == 0) {
					addLog("voip config for anonymous call ok");
				}

			} else if (NotifyID.ANONY_QUEUING_EVENT.equals(action)) {
				addLog("call in queue");
			} else if (NotifyID.ANONY_OCCUPY_AGENT_EVENT.equals(action)) {
				// addLog("呼叫预占用柜员成功");
				if (callorConfFlag == 1) {
					addConfLog("call occupy agent event");
				} else if (callorConfFlag == 0) {
					addLog("call occupy agent event");
				}

			} else if (NotifyID.ANONY_MEETING_INFO_EVENT.equals(action)) {
				LogUtils.d("CallConfActivity call to conf page");
				logTv.setText("");
				callRLayout1.setVisibility(View.GONE);
				callRLayout2.setVisibility(View.GONE);
				confRLayout1.setVisibility(View.VISIBLE);
				confRLayout2.setVisibility(View.VISIBLE);
				bottomBarll.setVisibility(View.GONE);
				msgtext.setVisibility(View.GONE);
				callorConfFlag = 1;
				LogUtils.d("openLocalVideoPreview");
				openLocalVideoPreview();
				// 入会时将此信息置为false
				isPause = false;
				isMute = false;
				isDocShare = false;
				isAppShare = false;
				isClicked = false;
				isShow = false;
				// addLog("呼叫入会信息");
				if (callorConfFlag == 1) {
					addConfLog("get meeting info when call start");
				} else if (callorConfFlag == 0) {
					addLog("get meeting info when call start");
				}
				MobileVTM.getInstance().setVideoContainer(
						CallConfActivity.this, mLlLocalSurface,
						mLlRemoteSurface); // 设置视频加载容器
				
				
			} else if (NotifyID.ANONY_NOTIFY_SESSION_STATE_EVENT.equals(action)) {

				String recode = notifyMsg.getRecode();
				String msg = notifyMsg.getMsg();
				// addLog("通知会话状态变更响应 -> " + msg + "(" + recode + ")");
				if (callorConfFlag == 1) {
					addConfLog("call session state chanaged event -> " + msg
							+ "(" + recode + ")");
				} else if (callorConfFlag == 0) {
					addLog("call session state chanaged event -> " + msg + "("
							+ recode + ")");
				}

			} else if (NotifyID.ANONY_RELEASE_EVENT.equals(action)) {
				// addLog("呼叫请求被释放 -> " + notifyMsg.getRecode());
				// addLog("呼叫请求被释放");
				addLog("call request release event,reason:"+notifyMsg.getRecode());
			} else if (NotifyID.ANONY_CALLED_EVENT.equals(action)) {
				// addLog("呼叫请求被释放 -> " + notifyMsg.getRecode());
				// addLog("呼叫请求被释放");
				// addLog("calling event");
//				LogUtils.d("openLocalVideoPreview");
//				openLocalVideoPreview();
//				LogUtils.d("CallConfActivity call to conf page");
//				logTv.setText("");
//				callRLayout1.setVisibility(View.GONE);
//				callRLayout2.setVisibility(View.GONE);
//				confRLayout1.setVisibility(View.VISIBLE);
//				confRLayout2.setVisibility(View.VISIBLE);
//				bottomBarll.setVisibility(View.GONE);
//				msgtext.setVisibility(View.GONE);
//				callorConfFlag = 1;

			} else if (NotifyID.QUERY_CALL_QUEUE_EVENT.equals(action)) {
				String recode = notifyMsg.getRecode();
				String msg = notifyMsg.getMsg();
				if (("0000").equals(recode)) {
					if (msg == "") {
						addLog("not queue status or server error!");// 未正确查询到排队信息，可能呼叫不处于排队状态或者后台处理异常。
					} else {
						try {
							JSONObject jObject = new JSONObject(msg);
							String position = jObject.getString("position");
							String totalWaitTime = jObject
									.getString("totalWaitTime");
							String currentDeviceWaitTime = jObject
									.getString("currentDeviceWaitTime");
							String estimateWaitTime = jObject
									.getString("estimateWaitTime");

							String message = "Queueing, position=" + position
									+ " ,totalWaitTime=" + totalWaitTime + " "
									+ ",currentDeviceWaitTime="
									+ currentDeviceWaitTime
									+ " ,estimateWaitTime=" + estimateWaitTime;

							// addLog(message);
							Toast.makeText(getApplicationContext(), message,
									Toast.LENGTH_SHORT).show();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				} else {
					if (msg != null && !"".equals(msg)) {
						addLog("MCC error:" + recode + " ,msg:" + msg);
					} else {
						addLog("MCC error:" + recode);
					}
				}
			} else if (NotifyID.CONFERENCE_JOIN_EVENT.equals(action)) {
				String recode = notifyMsg.getRecode();
				if ("0".equals(recode)) {
					addConfLog("join conf!");
					int currentvolume = (int) (((float) (audioManager
							.getStreamVolume(AudioManager.STREAM_MUSIC)) / (float) (audioManager
							.getStreamMaxVolume(AudioManager.STREAM_MUSIC))) * (float) 100);

					MobileVTM.getInstance().setAudioVolume(currentvolume); // 设置扬声器音量

					LogUtils.d(TAG, "current volume = "
							+ MobileVTM.getInstance().getAudioVolume()); // 获取扬声器音量
					bottomBarll.setVisibility(View.VISIBLE);
					msgtext.setVisibility(View.VISIBLE);
					bottomControlFlag = true;
				} else {
					addConfLog("join conf error:" + recode + "!");
				}
			} else if (NotifyID.COMPT_VIDEO_FIRST_KEYFRAME_EVENT.equals(action)) {
				addConfLog("video first keyframe event !");
				// hideLocalRl();
			} else if (NotifyID.CONF_USER_ENTER_EVENT.equals(action)) {
				addConfLog("has user enter conf !");
				setUserList();
			} else if (NotifyID.CONFERENCE_TERMINATE_EVENT.equals(action)) {
				addConfLog("leaved conf!");
				setUserList();
				// tvLog.setText("");
				callRLayout1.setVisibility(View.VISIBLE);
				callRLayout2.setVisibility(View.VISIBLE);
				confRLayout1.setVisibility(View.GONE);
				confRLayout2.setVisibility(View.GONE);
				callorConfFlag = 0;

				// finish();
			} else if (NotifyID.CONF_USER_LEAVE_EVENT.equals(action)) {
				addConfLog("has user leaved conf !");
				setUserList();
			} else if (NotifyID.DATA_SHARE_START_EVENT.equals(action)) {
				String recode = notifyMsg.getRecode();
				String msg = notifyMsg.getMsg();

				String sharedType;
				String sharedState;
				if ("1".equals(recode)) {
					sharedType = "Document sharing";
				} else if ("2".equals(recode)) {
					sharedType = "Application sharing";
				} else {
					sharedType = "";
				}

				isClicked = false;

				if ("1".equals(msg)) {
					sharedState = "begin !";
					if ("1".equals(recode)) {
						isDocShare = true; // 接收共享数据，设置显示容器
					} else if ("2".equals(recode)) {
						isAppShare = true; // 接收共享数据，设置显示容器
					}
				} else {
					sharedState = "end !";

					if ("1".equals(recode)) {
						isDocShare = false; // 接收共享数据，设置显示容器
					} else if ("2".equals(recode)) {
						isAppShare = false; // 接收共享数据，设置显示容器
					}
				}
				if (isDocShare || isAppShare) {
					dataBtn.setImageResource(R.drawable.icon_share_select);
				} else {
					dataBtn.setImageResource(R.drawable.icon_data_select);
				}
				addConfLog(sharedType + " - " + sharedState);
			} else if (NotifyID.TERMINAL_ALIVE_EVENT.equals(action)) {
				String recode = notifyMsg.getRecode();
				if (!"0000".equals(recode)) {
					addConfLog("alive failed!");
					MobileVTM.getInstance().releaseCall();
				}
			} else if (NotifyID.TERMINAL_THREEPARTY_TALKING_EVENT
					.equals(action)) {
				addConfLog("tripartite success!");
				setUserList();
			} else if (NotifyID.VEDIO_BEEN_RESUMED_EVENT.equals(action)) {
				addConfLog("vta was be cancel hold/mute !");
			} else if (NotifyID.VEDIO_BEEN_PAUSED_EVENT.equals(action)) {
				addConfLog("vta was be hold/mute !");
			} else if (NotifyID.INSERTED_BEGIN_EVENT.equals(action)) {
				addConfLog("monitor join success!");
				setUserList();
			} else if (NotifyID.INSERTED_SWITCH_EVENT.equals(action)) {
				addConfLog("monitor switch success!");
				setUserList();
			} else if (NotifyID.MSG_ARRIVED_EVENT.equals(action)) {
				String msg = notifyMsg.getMsg();
				addConfLog("msg = " + msg);
			} else if (NotifyID.COMPT_VIDEO_SWITCH_EVENT.equals(action)) {
				// recode: 0 关闭，1 打开，2 Resume，4 Pause
				// msg: 0 本地，1 远端
				String recode = notifyMsg.getRecode();
				String msg = notifyMsg.getMsg();
				if ("1".equals(recode) && "0".equals(msg)) {
					// MobileVTM.getInstance().setVideoRotate(180);
				}
			} else if (NotifyID.MEDIA_NTF_STATISTIC_MOS_EVENT.equals(action)) {
				String msg = notifyMsg.getMsg();
				float mos = Float.parseFloat(msg);
				String tip5 = "语音质量非常好，适合通话";
				String tip4 = "语音质量稍差，延迟小";
				String tip3 = "语音还可以，有一定延迟";
				String tip2 = "语音很勉强，不太适合通话";
				String tip1 = "极差，建议挂断本次通话!";
				// int mosFlag = Math.round(mos);
				String tip = "";
				if (mos > 2.8) {
					// tip = tip5;
					netView.setImageResource(R.drawable.call_signal_five);
				} else if (mos > 2.4) {
					// tip = tip4;
					netView.setImageResource(R.drawable.call_signal_four);
				} else if (mos > 2.0) {
					tip = tip3;
					netView.setImageResource(R.drawable.call_signal_three);
				} else if (mos > 1.6) {
					tip = tip2;
					netView.setImageResource(R.drawable.call_signal_two);
				} else {
					tip = tip1;
					netView.setImageResource(R.drawable.call_signal_one);
				}
				// addConfLog("mos: "+mos+", "+tip);
				tipMsg.setText(tip);
			}

		}
	};

}
