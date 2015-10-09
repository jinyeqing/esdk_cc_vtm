package com.huawei.esdk.vtm.call;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.huawei.esdk.vtm.ConferenceActivity;
import com.huawei.esdk.vtm.R;
import com.huawei.esdk.vtm.TestData;
import com.huawei.vtm.MobileVTM;
import com.huawei.vtm.NotifyID;
import com.huawei.vtm.NotifyMsg;
import com.huawei.vtm.NotifyMsg.NOTIFY;
import com.huawei.vtm.utils.LogUtils;

public class CallPageActivity extends Activity
{
	private String TAG = "CallPageActivity";

	private EditText calledPhoneText;

	private Button logoutBtn;

	private Button makeCallBtn;


	private TextView logTv;

	private ProgressDialog callDialog;

	private IntentFilter filter;

	private String calledNum;
	

	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.call_page_activity);

		preferences = getSharedPreferences(TestData.VTMSHARED, Context.MODE_PRIVATE);
		calledNum = preferences.getString(TestData.SHARED_CODE, TestData.CODE);

		filter = new IntentFilter();
		filter.addAction(NotifyID.TERMINAL_TALKING_EVENT);
		filter.addAction(NotifyID.TERMINAL_ASSIGNCONFFORCALLER_EVENT);
		filter.addAction(NotifyID.TERMINAL_CALLING_RELEASE_EVENT);
		filter.addAction(NotifyID.TERMINAL_ALIVE_EVENT);
		filter.addAction(NotifyID.TERMINAL_LOGOUT_EVENT);

		initView();
	}
	

	private void initView()
	{

		calledPhoneText = (EditText) findViewById(R.id.calleeTextField);
		calledPhoneText.setText(calledNum);

		logoutBtn = (Button) findViewById(R.id.logout);
		makeCallBtn = (Button) findViewById(R.id.makeCallBtn);

		logTv = (TextView) findViewById(R.id.logtext);
		logTv.setMovementMethod(ScrollingMovementMethod.getInstance());

		logoutBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (!MobileVTM.getInstance().isLogin()) // 判断登录状态
				{
					addLog("already logout");
				} else
				{
					MobileVTM.getInstance().logout(); // 登出
				}
			}
		});

		makeCallBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String calledNum = calledPhoneText.getText().toString().trim();

				if (calledNum.length() > 0)
				{
					Editor editor = preferences.edit();
					editor.putString(TestData.SHARED_CODE, calledNum);
					editor.commit();

					if (MobileVTM.getInstance().makeCall(calledNum, "", "") == -1) // 发起呼叫
					{
						addLog("make call to " + calledNum + " error!");
					}
					else
					{
						addLog("make call to " + calledNum + "!");
						showCallDialog();
					}
				}
			}
		});

	}

	private void showCallDialog()
	{
		if (callDialog == null)
		{
			callDialog = new ProgressDialog(this);
			callDialog.setCanceledOnTouchOutside(false);
			callDialog.setTitle(getString(R.string.calling_progress_tip));
			callDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.hangup), new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					addLog("release call!");
					MobileVTM.getInstance().releaseCall();
				}
			});
		}
		addLog("show call dialog!");
		callDialog.show();
	}

	private void dismissCallDialog()
	{
		if (callDialog != null && callDialog.isShowing())
		{
			addLog("dismiss call dialog!");
			callDialog.dismiss();
		}
	}

	private void addLog(String logText)
	{
		logTv.append(logText);
		logTv.append("\n");
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if (!MobileVTM.getInstance().isLogin())
		{
			finish();
		}
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		unregisterReceiver(receiver);
	}

	private BroadcastReceiver receiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			NotifyMsg notifyMsg = (NotifyMsg) intent.getSerializableExtra(NOTIFY.KEY_NAME);
			String recode = notifyMsg.getRecode();
			String msg = notifyMsg.getMsg();
			LogUtils.d(TAG, notifyMsg.getAction());

			if (NotifyID.TERMINAL_LOGOUT_EVENT.equals(action))
			{
				if (("0000").equals(recode))
				{
					addLog("logout success!");
					finish();
				} else
				{
					handlErrorLog(recode, msg);
				}
			} else if (NotifyID.TERMINAL_ALIVE_EVENT.equals(action))
			{
				if (!("0000").equals(recode))
				{
					addLog("alive failed!");
					finish();
				}
			} else if (NotifyID.TERMINAL_TALKING_EVENT.equals(action))
			{
				if (("0000").equals(recode))
				{
					addLog("call talking!");
					finish();
				}
			} else if (NotifyID.TERMINAL_ASSIGNCONFFORCALLER_EVENT.equals(action))
			{
				if (("0000").equals(recode))
				{
					addLog("requset meetting resouce success!");
					dismissCallDialog();
					startActivity(new Intent(CallPageActivity.this, ConferenceActivity.class));
				} else
				{
					handlErrorLog(recode, msg);
				}
			} else if (NotifyID.TERMINAL_CALLING_RELEASE_EVENT.equals(action))
			{
				addLog("call closed!");
				dismissCallDialog();
			}
		}
	};

	private void handlErrorLog(String recode, String msg)
	{
		if (("-1").equals(recode))
		{
			if (msg != null && !"".equals(msg))
			{
				addLog(msg);
			} else
			{
				addLog("http response error");
			}
		} else
		{
			if (msg != null && !"".equals(msg))
			{
				addLog(msg);
			} else
			{
				addLog("MCC error:" + recode);
			}
		}
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		dismissCallDialog();
		if (!MobileVTM.getInstance().isLogin())
		{
			addLog("already logout");
		} else
		{
			MobileVTM.getInstance().logout();
		}
	}

}
