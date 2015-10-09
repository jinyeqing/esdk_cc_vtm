package com.huawei.vtm.authentic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

import com.huawei.vtm.common.AccountInfo;
import com.huawei.vtm.common.Constants;
import com.huawei.vtm.common.SystemSetting;
import com.huawei.vtm.utils.LogUtils;

public class CommonGetAsync extends AsyncTask<Object, Object, Object>
{
    private String pathStr;

    private PostExecuteListener listener;

    public CommonGetAsync(String pathStr)
    {
        this.pathStr = pathStr;
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

        for (Object obj : params)
        {
            uri.append(obj.toString());
        }

        String result = Constants.RESPONSE_ERROR;
        HttpResponse httpResponse = null;

        LogUtils.i("CommonGetAsync | " + uri.toString());
        HttpGet httpGet = new HttpGet(uri.toString());

        httpGet.addHeader("Cache-Control", "no-cache");
        httpGet.addHeader("Connection", "Keep-Alive");
        httpGet.addHeader("Pragma", "no-cache");
        httpGet.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpGet.addHeader("User-Agent", "eSpace VTM");
        httpGet.addHeader("sessionId", AccountInfo.getInstance().getSessionId());
        httpGet.addHeader("token", AccountInfo.getInstance().getToken());
        httpGet.addHeader("Cookie", AccountInfo.getInstance().getCookie());
        LogUtils.d("CommonGetAsync | Cookie:"
                + AccountInfo.getInstance().getCookie());
        HttpClient httpClient =  SSLHttpClient.getHttpClient();
        try
        {
            httpResponse = httpClient.execute(httpGet);

            if (httpResponse.getStatusLine().getStatusCode() == 200)
            {
                result = EntityUtils.toString(httpResponse.getEntity());
                return result;
            }
            else
            {
                LogUtils.e("CommonGetAsync | response error | "
                        + httpResponse.getStatusLine().getStatusCode()
                        + " error :"
                        + EntityUtils.toString(httpResponse.getEntity()));
            }

        }
        catch (UnsupportedEncodingException e)
        {
            LogUtils.e("CommonGetAsync | UnsupportedEncodingException error : "
                    + e.getMessage());
            e.printStackTrace();
        }
        catch (ClientProtocolException e)
        {
            LogUtils.e("CommonGetAsync | ClientProtocolException error : "
                    + e.getMessage());
            e.printStackTrace();
        }
        catch (IOException e)
        {
            LogUtils.e("CommonGetAsync | IOException error : " + e.getMessage());
            e.printStackTrace();
        }
//        finally{
//        	 httpClient.getConnectionManager().shutdown();
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
