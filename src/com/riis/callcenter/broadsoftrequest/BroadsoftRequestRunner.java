package com.riis.callcenter.broadsoftrequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.os.AsyncTask;
import android.util.Base64;

import com.riis.callcenter.broadsoftrequest.BroadsoftRequests.BroadsoftRequest;

public class BroadsoftRequestRunner {
	private static final String ERROR_CODE = "ERROR";

	public static String username;
	public static String password;
	public static String broadsoftUrl;
	public static String authorizationString;
	public static boolean hasSetCredentials = false;
	public static boolean isUsingHttps = false;

	public static boolean hasInitializedHttps = false;

	private BroadsoftResponseListener responseListener;
	private AsyncTask<String, String, String> requestTask;

	public BroadsoftRequestRunner(String username, String password, String broadsoftUrl) {
		setCredentials(username, password, broadsoftUrl);
	}

	public BroadsoftRequestRunner() {
		if (!hasSetCredentials) {
			throw new RuntimeException("Credentials have not been set!");
		}
	}

	public static void setCredentials(String username, String password, String broadsoftUrl) {
		if (username != null) {
			BroadsoftRequestRunner.username = username;
		}

		if (password != null) {
			BroadsoftRequestRunner.password = password;
		}

		if (broadsoftUrl != null) {
			BroadsoftRequestRunner.broadsoftUrl = broadsoftUrl;
			if (!broadsoftUrl.endsWith("/")) {
				BroadsoftRequestRunner.broadsoftUrl += "/";
			}

			isUsingHttps = broadsoftUrl.startsWith("https");
		}

		authorizationString = generateAuthorizationString(BroadsoftRequestRunner.username, BroadsoftRequestRunner.password);
		hasSetCredentials = true;
		
		HttpsWorkAround.trustAllHosts();
	}

	private static String generateAuthorizationString(String username, String password) {
		String authorizationString = username + ":" + password;

		authorizationString = Base64.encodeToString(authorizationString.getBytes(), Base64.DEFAULT);
		authorizationString = "Basic " + authorizationString;

		return authorizationString;
	}

	public String runRequest(BroadsoftRequest request, BroadsoftResponseListener responseListener) {
		this.responseListener = responseListener;

		String completedRequestUrl = completeUrl(request.getRequestUrl());
		requestTask = new BroadsoftRequestTask().execute(completedRequestUrl);

		return null;
	}

	public String runRequest(BroadsoftRequest request, BroadsoftResponseListener responseListener, String parameter) {
		this.responseListener = responseListener;

		String completedRequestUrl = completeUrl(request.getRequestUrl(), parameter);
		requestTask = new BroadsoftRequestTask().execute(completedRequestUrl);

		return null;
	}

	public String runRequest(BroadsoftRequest request, BroadsoftResponseListener responseListener, String parameter, String putData) {
		this.responseListener = responseListener;

		String completedRequestUrl = completeUrl(request.getRequestUrl(), parameter);
		requestTask = new BroadsoftRequestTask().execute(completedRequestUrl, putData);

		return null;
	}

	public void cancelRequest() {
		requestTask.cancel(true);
	}

	private String completeUrl(String url, String... parameters) {
		String completedUrl = url;

		completedUrl = completedUrl.replace(BroadsoftRequests.URL_TAG, broadsoftUrl);
		completedUrl = completedUrl.replace(BroadsoftRequests.USERNAME_TAG, username);

		for (String parameter : parameters) {
			completedUrl = completedUrl.replaceFirst(BroadsoftRequests.PARAMETER_TAG, parameter);
		}

		return completedUrl;
	}

	class BroadsoftRequestTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			String responseString = null;

			try {
				if (params.length == 1) {
			        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			        trustStore.load(null, null);

			        SSLSocketFactory sf = new HttpsWorkaroundSocketFactory(trustStore);
			        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			        HttpParams httpParams = new BasicHttpParams();
			        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
			        HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);

			        SchemeRegistry registry = new SchemeRegistry();
			        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			        registry.register(new Scheme("https", sf, 443));

			        ClientConnectionManager ccm = new ThreadSafeClientConnManager(httpParams, registry);

			        HttpClient httpClient = new DefaultHttpClient(ccm, httpParams);
					
					HttpGet httpRequest = new HttpGet(params[0]);
					httpRequest.addHeader("Host", formatUrlForHostHeader(broadsoftUrl));
					httpRequest.addHeader("Authorization", authorizationString);
					HttpResponse response = httpClient.execute(httpRequest);

					StatusLine statusLine = response.getStatusLine();

					if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						response.getEntity().writeTo(out);
						out.close();
						responseString = out.toString();
					} else {
//						ByteArrayOutputStream out = new ByteArrayOutputStream();
//						response.getEntity().writeTo(out);
//						out.close();
//						responseString = out.toString();
						response.getEntity().getContent().close();
						throw new IOException(statusLine.getReasonPhrase());
					}
				} else if (params.length == 2) {
					//Posting
					HttpURLConnection httpCon = null;
					URL url = new URL(params[0]);
					if (url.getProtocol().toLowerCase().equals("https")) {
						HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
						httpCon = https;
					} else {
						httpCon = (HttpURLConnection) url.openConnection();
					}

					httpCon.setRequestProperty("Host", formatUrlForHostHeader(broadsoftUrl));
					httpCon.setRequestProperty("Authorization", authorizationString);
					httpCon.setRequestProperty("Content-Type", "text/xml; charset=ISO_8859_1");
					httpCon.setRequestProperty("Accept", "text/xml");
					httpCon.setDoInput(true);
					httpCon.setDoOutput(true);
					httpCon.setRequestMethod("PUT");
					OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream(), "ISO_8859_1");
					out.write(params[1]);
					out.close();

					if (httpCon.getErrorStream() == null) {
						return "";
					} else {
						return ERROR_CODE;
					}

					// We don't need to actually get the response here
				}
			} catch (Exception e) {
				responseString = ERROR_CODE + "~" + e.getMessage() + " ";
			}

			return responseString;
		}

		private String formatUrlForHostHeader(String unformattedUrl) {
			String formattedUrl = unformattedUrl;
			formattedUrl = formattedUrl.replace("http://", "").replace("https://", "");

			if (formattedUrl.endsWith("/")) {
				formattedUrl = formattedUrl.substring(0, formattedUrl.length() - 1);
			}

			return formattedUrl;
		}

		@Override
		protected void onPostExecute(String responseString) {
			super.onPostExecute(responseString);

			boolean success = !responseString.contains(ERROR_CODE);

			if (success) {
				responseListener.onRequestCompleted(responseString, success, "");
			} else {
				String[] responseParts = responseString.split("~");
				responseListener.onRequestCompleted(responseParts[0], success, responseParts[1]);
			}
		}
	}
}