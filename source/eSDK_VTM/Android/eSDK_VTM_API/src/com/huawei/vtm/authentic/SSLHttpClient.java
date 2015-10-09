package com.huawei.vtm.authentic;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import com.huawei.vtm.common.Constants;
import com.huawei.vtm.utils.LogUtils;

public class SSLHttpClient {

	private static HttpClient defaultClient = null;

	public static HttpClient getHttpClient() {
		if (defaultClient == null) {

			HttpParams params = new BasicHttpParams();
			
			 // timeout: connect to the server
	        HttpConnectionParams.setConnectionTimeout(params, Constants.TIMEOUT);
	        // timeout: transfer data from server
	        HttpConnectionParams.setSoTimeout(params, Constants.TIMEOUT); 
	        
	        // use expect-continue handshake
	        HttpProtocolParams.setUseExpectContinue(params, true);
	        // disable stale check
	        HttpConnectionParams.setStaleCheckingEnabled(params, false);
	        
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);  
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8); 

			// disable Nagle algorithm
	        HttpConnectionParams.setTcpNoDelay(params, true); 


			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));

			try {
				KeyStore store = KeyStore
						.getInstance(KeyStore.getDefaultType());
				store.load(null, null);
				SSLSocketFactory sf = new SSLSocketFactoryEx(store);
				sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				registry.register(new Scheme("https", sf, 443));

			} catch (KeyStoreException e) {
				LogUtils.e("SSLHttpClient | KeyStoreException error : "
						+ e.toString());
			} catch (NoSuchAlgorithmException e) {
				LogUtils.e("SSLHttpClient | NoSuchAlgorithmException error : "
						+ e.toString());
			} catch (CertificateException e) {
				LogUtils.e("SSLHttpClient | CertificateException error : "
						+ e.toString());
			} catch (IOException e) {
				LogUtils.e("SSLHttpClient | IOException error : "
						+ e.toString());
			} catch (KeyManagementException e) {
				LogUtils.e("SSLHttpClient | KeyManagementException error : "
						+ e.toString());
			} catch (UnrecoverableKeyException e) {
				LogUtils.e("SSLHttpClient | UnrecoverableKeyException error : "
						+ e.toString());
			}

			 ClientConnectionManager manager = new ThreadSafeClientConnManager(
					params, registry);

			defaultClient = new DefaultHttpClient(manager, params);

		}
		
		return defaultClient;
	}
}
