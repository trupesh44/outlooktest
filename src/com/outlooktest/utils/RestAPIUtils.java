package com.outlooktest.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.outlooktest.config.Params;

public class RestAPIUtils {

	private URL stream = null;
	private HttpURLConnection connection = null;
	private final static String TAG = RestAPIUtils.class.getSimpleName();

	protected static String sendHttpRequest(String req)
			throws ClientProtocolException, IOException {
		HttpClient httpget = new DefaultHttpClient();
		HttpGet request = new HttpGet(req);
		HttpResponse httpResponse = httpget.execute(request);
		HttpEntity httpEntity = httpResponse.getEntity();
		String response = null;
		try {
			response = EntityUtils.toString(httpEntity);
		} catch (ParseException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return response;
	}

	public String fetchImageList(Bundle bundle) {

		return invokeMethod(Params.HTTP_METHOD_POST,
				Params.REST_API_METHOD_FETCH_IMAGE_LIST, bundle);
	}

	private String invokeMethod(String httpMethodtype, String methodName,
			Bundle params) {
		String response = null;
		String serviceUrl = getServiceUrl(methodName, params);
		Log.d(TAG, "invokeMethod() : serviceUrl : " + serviceUrl);

		if (httpMethodtype.equalsIgnoreCase(Params.HTTP_METHOD_GET)) {
			response = invokeGet(serviceUrl, params);
		} else if (httpMethodtype.equalsIgnoreCase(Params.HTTP_METHOD_POST)) {
			response = invokePost(serviceUrl, params);
		}
		return response;
	}

	private String getServiceUrl(String methodName, Bundle params) {

		String baseUrl = Params.REST_API_BASE_URL;
		StringBuilder sb = new StringBuilder(baseUrl);
		return sb.toString();
	}

	private String invokeGet(String serviceUrl, Bundle params) {
		String response = null;
		String newServiceUrl = getParameterizedServiceUrl(serviceUrl, params);
		try {
			stream = new URL(newServiceUrl);
			connection = (HttpURLConnection) stream.openConnection();
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(30000);
			connection.connect();
			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				response = readStream(connection.getInputStream());
			} else {
				response = connection.getResponseMessage();
			}
		} catch (Exception e) {
			Log.e(TAG, "invokeGet() : " + e.getMessage());
		} finally {
			if (connection != null) {
				connection.disconnect();
				connection = null;
			}
		}
		return response;
	}

	private String invokePost(String serviceUrl, Bundle params) {
		String response = null;
		try {
			stream = new URL(serviceUrl);
			connection = (HttpURLConnection) stream.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(30000);

			String paramQuery = getPostParamQuery(params);

			if (!TextUtils.isEmpty(paramQuery)) {
				OutputStream os = connection.getOutputStream();
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(os, "UTF-8"));
				writer.write(paramQuery);
				writer.flush();
				writer.close();
				os.close();
			}

			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				response = readStream(connection.getInputStream());
			} else {
				response = connection.getResponseMessage();
			}
		} catch (Exception e) {
			Log.e(TAG, "invokePost() : " + e.getMessage());
			response = e.getMessage();
		} finally {
			if (connection != null) {
				connection.disconnect();
				connection = null;
			}
		}
		return response;
	}

	private String getParameterizedServiceUrl(String serviceUrl, Bundle params) {
		StringBuilder sb = new StringBuilder();
		sb.append(serviceUrl);
		sb.append(File.separator);
		if (params != null && !params.isEmpty()) {
			sb.append("?");
			Iterator<String> iterator = params.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();

				if (params.get(key).getClass().isArray()) {
					String[] valueArray = params.getStringArray(key);
					for (int nCount = 0; nCount < valueArray.length; nCount++) {
						sb.append(key + "[]");
						sb.append("=");
						sb.append(valueArray[nCount]);
						if (nCount < valueArray.length - 1) {
							sb.append("&");
						}
					}
				} else {
					String value = params.get(key).toString();
					sb.append(key);
					sb.append("=");
					sb.append(value);
				}
				if (iterator.hasNext()) {
					sb.append("&");
				}
			}
		}
		Log.d(TAG,
				"getParameterizedServiceUrl() : serviceUrl : " + sb.toString());
		return sb.toString();
	}

	private String readStream(InputStream in) {
		BufferedReader reader = null;
		StringBuffer buffer = new StringBuffer();
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
		} catch (IOException e) {
			Log.e(TAG, "readStream() : " + e.getMessage());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Log.e(TAG, "readStream() : " + e.getMessage());
				}
			}
		}
		return buffer.toString();
	}

	private String getPostParamQuery(Bundle params)
			throws UnsupportedEncodingException {
		boolean first = true;
		StringBuilder result = new StringBuilder();
		List<NameValuePair> keyValueList = new ArrayList<NameValuePair>();
		if (params != null && !params.isEmpty()) {
			Log.e(TAG, "getPostParamQuery() : params : " + params);
			Iterator<String> iterator = params.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				if (params.get(key).getClass().isArray()) {
					String[] valueArray = params.getStringArray(key);
					for (int nCount = 0; nCount < valueArray.length; nCount++) {

						keyValueList.add(new BasicNameValuePair(key + "[]",
								valueArray[nCount]));
					}
				} else {
					String value = params.get(key).toString();
					keyValueList.add(new BasicNameValuePair(key, value));
				}

			}
		}
		for (NameValuePair pair : keyValueList) {
			if (first) {
				first = false;
			} else {
				result.append("&");
			}
			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}
		return result.toString();
	}

	public static boolean isJSONValid(String JSON_STRING) {
		if (JSON_STRING != null) {
			try {
				new JSONObject(JSON_STRING);
			} catch (JSONException ex) {
				try {
					new JSONArray(JSON_STRING);
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage());
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
}