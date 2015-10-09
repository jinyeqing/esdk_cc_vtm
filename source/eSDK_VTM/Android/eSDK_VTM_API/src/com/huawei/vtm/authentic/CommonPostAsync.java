package com.huawei.vtm.authentic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.huawei.vtm.common.AccountInfo;
import com.huawei.vtm.common.Constants;
import com.huawei.vtm.common.Constants.IAS_REQUEST_PATH;
import com.huawei.vtm.common.SystemSetting;
import com.huawei.vtm.utils.LogUtils;
import com.huawei.vtm.utils.Tools;

public class CommonPostAsync extends AsyncTask<Object, Object, Object>
{
    private String pathStr;

    private String[] keys;

    private PostExecuteListener listener;

    public CommonPostAsync(String pathStr, String[] keys)
    {
        this.pathStr = pathStr;

        this.keys = new String[keys.length];
        for (int i = 0; i < keys.length; i++)
        {
            this.keys[i] = keys[i];
        }
    }

    public void setOnPostExecuteListener(PostExecuteListener listener)
    {
        this.listener = listener;
    }

    @Override
    protected Object doInBackground(Object... params)
    {
        StringBuffer uri = new StringBuffer();
        if (SystemSetting.getInstance().isHttps())
        {
            uri.append(Constants.HTTPS);
        }
        else
        {
            uri.append(Constants.HTTP);
        }
        uri.append(SystemSetting.getInstance().getServerIp());
        uri.append(":");
        uri.append(SystemSetting.getInstance().getServerPort());

        LogUtils.d("URI Host | " + uri.toString());

        uri.append(pathStr);

        // 匿名呼叫时 通知呼叫状态变更 请求参数在URI中
        if (IAS_REQUEST_PATH.NOTIFY_SESSION_STATE_PATH.equals(pathStr))
        {
            for (Object obj : params)
            {
                uri.append(obj.toString());
            }
        }

        String result = Constants.RESPONSE_ERROR;
        HttpResponse httpResponse = null;

        LogUtils.i("CommonPostAsync | " + uri.toString());
        HttpPost httpPost = new HttpPost(uri.toString());
        
        
        try
        {
            String identifyCode = SystemSetting.getInstance().getIdentifyCode();
            httpPost.addHeader("Accept-Encoding", "gzip,deflate");
            httpPost.addHeader("Cache-Control", "no-cache");
            httpPost.addHeader("Connection", "Keep-Alive");
            httpPost.addHeader("Pragma", "no-cache");
            httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            httpPost.addHeader("User-Agent", "Jakarta Commons-HttpClient/3.1");
            httpPost.addHeader("IdentifyCode", identifyCode); // 请求发起匿名呼叫的
                                                              // Header
            httpPost.addHeader("sessionId", AccountInfo.getInstance()
                    .getSessionId());
            httpPost.addHeader("token", AccountInfo.getInstance().getToken());
            if (!Tools.isEmpty(AccountInfo.getInstance().getCookie()))
            {
                httpPost.addHeader("Cookie", AccountInfo.getInstance()
                        .getCookie());
            }
            LogUtils.d("CommonGetAsync | Cookie:"
                    + AccountInfo.getInstance().getCookie());
            
            
            if (IAS_REQUEST_PATH.REQUEST_CONNECT_PATH.equals(pathStr))
            {
            	 httpPost.addHeader("iasIp", SystemSetting.getInstance().getServerIp());
            	 httpPost.addHeader("iasPort", "" + SystemSetting.getInstance().getServerPort());
            }
            
            

            if (!IAS_REQUEST_PATH.NOTIFY_SESSION_STATE_PATH.equals(pathStr))
            {
                JSONObject jObject = new JSONObject();
                for (int i = 0; i < params.length; i++)
                {
                    jObject.put(keys[i], params[i]);
                }

                StringEntity entity = new StringEntity(jObject.toString(),
                        "UTF-8");

                httpPost.setEntity(entity);
            }
            
            
            HttpClient httpClient = SSLHttpClient.getHttpClient();
            httpResponse = httpClient.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() == 200)
            {
                if (IAS_REQUEST_PATH.REQUEST_CONNECT_PATH.equals(pathStr))
                {
                    Header[] headers = httpResponse.getAllHeaders();
                    for (Header header : headers)
                    {
                        LogUtils.e(header.getName() + " = " + header.getValue());
                        if ("Set-Cookie".equals(header.getName()))
                        {
                            AccountInfo.getInstance().setCookie(
                                    header.getValue().split(";")[0]);
                            break;
                        }
                    }
                }
                result = EntityUtils.toString(httpResponse.getEntity());
                return result;
            }
            else
            {
                LogUtils.e("CommonPostAsync | response error | "
                        + httpResponse.getStatusLine().getStatusCode()
                        + " error :"
                        + EntityUtils.toString(httpResponse.getEntity()));
            }
        }
        catch (UnsupportedEncodingException e)
        {
            LogUtils.e("CommonPostAsync | UnsupportedEncodingException error : "
                    + e.getMessage());
            e.printStackTrace();
        }
        catch (ClientProtocolException e)
        {
            LogUtils.e("CommonPostAsync | ClientProtocolException error : "
                    + e.getMessage());
            e.printStackTrace();
        }
        catch (ConnectTimeoutException e)
        {
            LogUtils.e("CommonPostAsync | ConnectTimeoutException error : "
                    + e.getMessage());
            e.printStackTrace();
        }
        catch (IOException e)
        {
            LogUtils.e("CommonPostAsync | IOException error : "
                    + e.getMessage());
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            LogUtils.e("CommonPostAsync | JSONException error : "
                    + e.getMessage());
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e)
        {
            LogUtils.e("CommonPostAsync | NoSuchAlgorithmException error : "
                    + e.getMessage());
            e.printStackTrace();
        }
//        finally{
//        	httpClient.getConnectionManager().shutdown();
//        }

        return result;
    }

    @Override
    protected void onPostExecute(Object result)
    {
        if (result == null)
        {
            return;
        }
        super.onPostExecute(result);

        if (listener != null)
        {
            listener.onPostExecuteListener(result);
        }
    }

}