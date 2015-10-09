package com.huawei.esdk.vtm.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
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

import com.huawei.esdk.vtm.R;
import com.huawei.esdk.vtm.TestData;
import com.huawei.esdk.vtm.call.CallPageActivity;
import com.huawei.vtm.MobileVTM;
import com.huawei.vtm.NotifyID;
import com.huawei.vtm.NotifyMsg;
import com.huawei.vtm.NotifyMsg.NOTIFY;
import com.huawei.vtm.utils.LogUtils;

public class LoginActivity extends Activity
{

    private EditText edUserId;

    private EditText edUserPaw;

    private Button btnLogin;

    private TextView tvLog;

    private ProgressDialog loginDialog;

    private String userIdStr /* = "vtm025" */;

    private String userPwdStr /* = "" */;

    private IntentFilter filter;

    private String TAG = "LoginActivity";
    
    private String sherad_userName;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        preferences = getSharedPreferences(TestData.VTMSHARED,
                Context.MODE_PRIVATE);
        sherad_userName = preferences.getString(TestData.SHARED_USERNAME, TestData.USERID);

        initView();

        filter = new IntentFilter();
        filter.addAction(NotifyID.TERMINAL_LOGIN_EVENT);
        filter.addAction(NotifyID.TERMINAL_GETTERMINALINFO_EVENT);
        filter.addAction(NotifyID.TERMINAL_GETCLIENTCONFIG_EVENT);
        filter.addAction(NotifyID.TERMINAL_VOIP_REGISTER_EVENT);

    }

    private void initView()
    {
        edUserId = (EditText) findViewById(R.id.userId);
        edUserPaw = (EditText) findViewById(R.id.userPwd);

        edUserId.setText(sherad_userName);

        btnLogin = (Button) findViewById(R.id.login);

        btnLogin.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                userIdStr = edUserId.getText().toString();
                userPwdStr = edUserPaw.getText().toString();

                if (userIdStr == null || "".equals(userIdStr))
                {
                    return;
                }

                // 登录
                boolean ret = MobileVTM.getInstance().login(userIdStr,
                        userPwdStr);
                if (ret)
                {
                    showLoginDialog();
                }
                else
                {
                    addLog(getString(R.string.checkServerConfig));
                }
            }
        });

        tvLog = (TextView) findViewById(R.id.logtext);
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private void showLoginDialog()
    {
        if (loginDialog == null)
        {
            loginDialog = new ProgressDialog(this);
            loginDialog.setCanceledOnTouchOutside(false);
            loginDialog.setTitle(getString(R.string.logining_progress_tip));
        }
        addLog("show login dialog!");
        loginDialog.show();
    }

    private void dismissLoginDialog()
    {
        if (loginDialog != null && loginDialog.isShowing())
        {
            addLog("dismiss login dialog!");
            loginDialog.dismiss();
        }
    }

    private void addLog(String logText)
    {
        tvLog.append(logText);
        tvLog.append("\n");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(receiver);
        dismissLoginDialog();
    }

    private void handlErrorLog(String recode, String msg)
    {
        MobileVTM.getInstance().logout();
        if (("-1").equals(recode))
        {
            if (msg != null && !"".equals(msg))
            {
                addLog(msg);
            }
            else
            {
                addLog("http response error");
            }
        }
        else
        {
            if (msg != null && !"".equals(msg))
            {
                addLog(msg);
            }
            else
            {
                addLog("MCC error:" + recode);
            }
        }
    }

    // 登录回调广播消息
    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            NotifyMsg notifyMsg = (NotifyMsg) intent
                    .getSerializableExtra(NOTIFY.KEY_NAME);
            String recode = notifyMsg.getRecode();
            String msg = notifyMsg.getMsg();
            LogUtils.d(TAG, notifyMsg.getAction());

            if (NotifyID.TERMINAL_LOGIN_EVENT.equals(action))
            {
                if (("0000").equals(recode))
                {
                    Editor editor = preferences.edit();
                    editor.putString(TestData.SHARED_USERNAME, userIdStr);
                    editor.commit();
                    
                    addLog("login MCC success!");
                }
                else
                {
                    handlErrorLog(recode, msg);
                    dismissLoginDialog();
                }
            }
            else if (NotifyID.TERMINAL_GETTERMINALINFO_EVENT.equals(action))
            {
                if (("0000").equals(recode))
                {
                    addLog("getTerminalInfo success!");
                }
                else
                {
                    handlErrorLog(recode, msg);
                    dismissLoginDialog();
                }
            }
            else if (NotifyID.TERMINAL_GETCLIENTCONFIG_EVENT.equals(action))
            {
                if (("0000").equals(recode))
                {
                    addLog("getClientConfig success!");
                }
                else
                {
                    handlErrorLog(recode, msg);
                    dismissLoginDialog();
                }
            }
            else if (NotifyID.TERMINAL_VOIP_REGISTER_EVENT.equals(action))
            {
                if (("0").equals(recode))
                {
                    addLog("VoIP register success!");
                    dismissLoginDialog();
                    startActivity(new Intent(getBaseContext(),
                            CallPageActivity.class));
                }
                else
                {
                    handlErrorLog(recode, msg);
                    dismissLoginDialog();
                }
            }
        }
    };

}