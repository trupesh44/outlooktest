package com.outlooktest.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

/**
 * Downloads Images and saves it on SDCard in the local folder created for the app
 * @author trupesh
 *
 */
public class ImageDownloadTask extends AsyncTask<String, Integer, String> {
	private static String TAG = ImageDownloadTask.class.getSimpleName();

	@Override
	protected String doInBackground(String... params) {

		if (LocalCacheUtils.downloadImages) {
			String url = null;
			if (params.length > 2) {
				url = params[2];
			}
			if (url != null) {
				if (url.startsWith("https:")) {
					createImageFromHttpsUrl(this, params);
				} else {
					createImageFromHttpUrl(this, params);
				}
			}
		}

		return null;
	}

	private static void createImageFromHttpsUrl(
			ImageDownloadTask mediaDownloadTask, String[] params) {
		InputStream input = null;
		OutputStream output = null;
		HttpsURLConnection connection = null;
		String dirName = null;
		String fileName = null;
		String url = null;
		File mediaFile = null;
		int contentLength = 0;
		long fileLength = 0;

		try {
			dirName = params[0];
			fileName = params[1];
			url = params[2];

			URL fileUrl = new URL(url);
			connection = (HttpsURLConnection) fileUrl.openConnection();
			connection.setHostnameVerifier(hostnameVerifier);
			connection.connect();

			if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
				contentLength = connection.getContentLength();
				Log.d(TAG, "doInBackground() : contentLength : "
						+ contentLength);

				input = connection.getInputStream();

				File mediaDir = new File(
						Environment.getExternalStorageDirectory(), dirName);
				if (!mediaDir.exists()) {
					mediaDir.mkdirs();
				}
				mediaFile = new File(mediaDir, fileName);
				if (!mediaFile.exists()) {
					mediaFile.createNewFile();
				}
				Log.d(TAG,
						"doInBackground() : filePath : "
								+ mediaFile.getAbsolutePath());

				output = new FileOutputStream(mediaFile);
				byte data[] = new byte[4096];
				int count;
				while ((count = input.read(data)) != -1) {
					if (mediaDownloadTask.isCancelled())
						break;
					output.write(data, 0, count);
				}
				fileLength = mediaFile.length();
				Log.d(TAG, "doInBackground() : fileLength : " + fileLength);
			} else {
				String serverResponse = "Server returned HTTP "
						+ connection.getResponseCode() + " "
						+ connection.getResponseMessage();
				Log.d(TAG, "doInBackground() : serverResponse : "
						+ serverResponse);
			}
		} catch (Exception e) {
			if (e != null) {
				Log.e(TAG, "doInBackground() : " + e.getMessage());
			}
		} finally {
			try {
				if (output != null)
					output.close();
				if (input != null)
					input.close();
			} catch (IOException ignored) {
				Log.e(TAG, "doInBackground() : " + ignored.getMessage());
			}
			if (connection != null)
				connection.disconnect();

			if (contentLength == 0 || fileLength == 0
					|| fileLength < contentLength) {
				if (mediaFile != null && mediaFile.exists()) {
					boolean fileDeleted = mediaFile.delete();
					Log.d(TAG, "doInBackground() : fileDeleted : "
							+ fileDeleted);
				}
			}
		}
		return;
	}

	private static void createImageFromHttpUrl(
			ImageDownloadTask mediaDownloadTask, String[] params) {
		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;
		String dirName = null;
		String fileName = null;
		String cdnUrl = null;
		File mediaFile = null;
		int contentLength = 0;
		long fileLength = 0;

		try {
			dirName = params[0];
			fileName = params[1];
			cdnUrl = params[2];

			URL fileUrl = new URL(cdnUrl);
			connection = (HttpURLConnection) fileUrl.openConnection();
			connection.connect();

			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				contentLength = connection.getContentLength();
				Log.d(TAG, "doInBackground() : contentLength : "
						+ contentLength);

				input = connection.getInputStream();
				File mediaDir = new File(
						Environment.getExternalStorageDirectory(), dirName);
				if (!mediaDir.exists()) {
					mediaDir.mkdirs();
				}
				mediaFile = new File(mediaDir, fileName);
				if (!mediaFile.exists()) {
					mediaFile.createNewFile();
				}
				Log.d(TAG,
						"doInBackground() : filePath : "
								+ mediaFile.getAbsolutePath());

				output = new FileOutputStream(mediaFile);
				byte data[] = new byte[4096];
				int count;
				while ((count = input.read(data)) != -1) {
					if (mediaDownloadTask.isCancelled())
						break;
					output.write(data, 0, count);
				}
				fileLength = mediaFile.length();
				Log.d(TAG, "doInBackground() : fileLength : " + fileLength);
			} else {
				String serverResponse = "Server returned HTTP "
						+ connection.getResponseCode() + " "
						+ connection.getResponseMessage();
				Log.d(TAG, "doInBackground() : serverResponse : "
						+ serverResponse);
			}
		} catch (Exception e) {
			if (e != null) {
				Log.e(TAG, "doInBackground() : " + e.getMessage());
			}
		} finally {
			try {
				if (output != null)
					output.close();
				if (input != null)
					input.close();
			} catch (IOException ignored) {
				Log.e(TAG, "doInBackground() : " + ignored.getMessage());
			}
			if (connection != null)
				connection.disconnect();

			if (contentLength == 0 || fileLength == 0
					|| fileLength < contentLength) {
				if (mediaFile != null && mediaFile.exists()) {
					boolean fileDeleted = mediaFile.delete();
					Log.d(TAG, "doInBackground() : fileDeleted : "
							+ fileDeleted);
				}
			}
		}
		return;
	}

	private static HostnameVerifier hostnameVerifier = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};
}