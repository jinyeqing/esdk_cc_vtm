package com.huawei.esdk.vtm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.huawei.esdk.vtm.login.LoginActivity;
import com.huawei.vtm.MobileVTM;
import com.huawei.vtm.common.AccountInfo;
import com.huawei.vtm.utils.Tools;

public class StartActivity extends Activity implements OnClickListener
{

    private RadioGroup radioGroup;

    private EditText edIP;

    private EditText edPort;

    private CheckBox cbIsHttps;

    private Button btnGOTOCall;

    private LinearLayout tokenlayout;

    private EditText edToken;

    private String IPStr /* = "10.166.46.163" */;

    private int port /* = "8080" */;

    private boolean isHttps = false;

    private boolean isAnonymous = true;

    private SharedPreferences preferences;

    private String shared_anonyIp;

    private String shared_anonyPort;

    private boolean shared_anonyisHttps;

    private String shared_ip;

    private String shared_port;

    private boolean shared_isHttps;

    private String shared_token;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        preferences = getSharedPreferences(TestData.VTMSHARED,
                Context.MODE_PRIVATE);
        initShareData();

        radioGroup = (RadioGroup) findViewById(R.id.callType);
        radioGroup
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId)
                    {
                        switch (checkedId)
                        {
                            case R.id.anonymousCall:

                                edIP.setText(shared_anonyIp);
                                edPort.setText(shared_anonyPort);
                                cbIsHttps.setChecked(shared_anonyisHttps);
                                btnGOTOCall.setText(R.string.anonymous);

                                tokenlayout.setVisibility(View.VISIBLE);
                                isAnonymous = true;

                                break;
                            case R.id.no_anonymousCall:

                                edIP.setText(shared_ip);
                                edPort.setText(shared_port);
                                cbIsHttps.setChecked(shared_isHttps);
                                btnGOTOCall.setText(R.string.no_anonymous);

                                tokenlayout.setVisibility(View.GONE);
                                isAnonymous = false;

                                break;

                            default:
                                break;
                        }
                    }
                });

        edIP = (EditText) findViewById(R.id.serverIP);
        edPort = (EditText) findViewById(R.id.port);

        cbIsHttps = (CheckBox) findViewById(R.id.isHttps);
        cbIsHttps.setChecked(isHttps);
        cbIsHttps.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked)
            {
                isHttps = isChecked;
            }
        });

        btnGOTOCall = (Button) findViewById(R.id.go_to);
        tokenlayout = (LinearLayout) findViewById(R.id.tokenlayout);
        edToken = (EditText) findViewById(R.id.token);
        edToken.setText(shared_token);

        RadioButton rbAnony = (RadioButton) findViewById(R.id.anonymousCall);
        rbAnony.setChecked(true);
    }

    private boolean setHostAddr()
    {
        IPStr = edIP.getText().toString();
        String portStr = edPort.getText().toString();

        if (Tools.isEmpty(IPStr) || Tools.isEmpty(portStr))
        {
            return false;
        }
        port = Integer.parseInt(portStr);

        // 设置服务器地址
        return MobileVTM.getInstance().setHostAddr(IPStr, port, isHttps);

    }

    @Override
    public void onClick(View v)
    {
        if (!setHostAddr())
        {
            // Toast.makeText(this, "IP地址或端口号格式有误", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, getString(R.string.checkServerConfig),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Editor editor = preferences.edit();

        switch (v.getId())
        {
            case R.id.go_to:
            {
                AccountInfo.getInstance().setAnonymous(isAnonymous);
                if (isAnonymous)
                {

                    editor.putString(TestData.SHARED_ANONYIP, IPStr);
                    editor.putString(TestData.SHARED_ANONYPORT,
                            String.valueOf(port));
                    editor.putBoolean(TestData.SHARED_ANONYISHTTPS, isHttps);

                    String token = edToken.getText().toString().trim();
                    if (!Tools.isEmpty(token)
                            && MobileVTM.getInstance().setToken(token))
                    {
                        editor.putString(TestData.SHARED_TOKEN, token);

                        Intent intent = new Intent(StartActivity.this,
                                CallConfActivity.class);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(StartActivity.this,
                        		getString(R.string.token_tip), 0)
                                .show();
                    }
                }
                else
                {
                    editor.putString(TestData.SHARED_IP, IPStr);
                    editor.putString(TestData.SHARED_PORT, String.valueOf(port));
                    editor.putBoolean(TestData.SHARED_ISHTTPS, isHttps);

                    Intent intent = new Intent(StartActivity.this,
                            LoginActivity.class);
                    startActivity(intent);
                }
            }
                break;

            default:
                break;
        }

        editor.commit();
    }

    private void initShareData()
    {
        shared_anonyIp = preferences.getString(TestData.SHARED_ANONYIP,
                TestData.ANONY_IPSTR);
        shared_anonyPort = preferences.getString(TestData.SHARED_ANONYPORT,
                TestData.ANONY_PORT);
        shared_anonyisHttps = preferences.getBoolean(
                TestData.SHARED_ANONYISHTTPS, true);
        shared_ip = preferences.getString(TestData.SHARED_IP, TestData.IPSTR);
        shared_port = preferences
                .getString(TestData.SHARED_PORT, TestData.PORT);
        shared_isHttps = preferences.getBoolean(TestData.SHARED_ISHTTPS, false);
        shared_token = preferences.getString(TestData.SHARED_TOKEN,
                TestData.TOKEN);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        cbIsHttps.setChecked(isHttps);

        initShareData();
    }

}
