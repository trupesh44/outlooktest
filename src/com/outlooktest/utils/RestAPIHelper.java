package com.outlooktest.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.outlooktest.config.Params;
import com.outlooktest.model.Data;

public class RestAPIHelper {

	private static final String TAG = "RestAPIHelper";
	public static final String CERTIFICATE_EXCEPTION = "ExtCertPathValidatorException";
	public static final String VALIDATE_CERTIFICATE = "validate certificate";
	private static Gson gson;
	private static ImageSearchListener mImageSearchListener;

	public static void fetch(Context context, int calledAPI,
			Bundle bundleData) {
		new callRestAPI(context, bundleData, calledAPI).execute();
	}

	/**
	 * Sends Request to the server
	 * 
	 * @author trupesh
	 * 
	 */
	private static class callRestAPI extends AsyncTask<Void, Void, String> {

		private Bundle bundleData;
		private int calledAPI;
		private Context ctx;

		/**
		 * 
		 * @param ctx
		 * @param bundleData
		 *            Parameters
		 * @param calledAPI
		 *            api that needs to be called
		 */
		public callRestAPI(Context ctx, Bundle bundleData, int calledAPI) {
			this.ctx = ctx;
			this.bundleData = bundleData;
			this.calledAPI = calledAPI;
		}

		@Override
		protected String doInBackground(Void... params) {
			String response = null;
			RestAPIUtils restApi = new RestAPIUtils();

			switch (calledAPI) {
			case Params.FETCH_IMAGE_LIST:
				response = restApi.fetchImageList(bundleData);
				break;
			}
			return response;
		}

		@Override
		protected void onPostExecute(String response) {

			Log.d(TAG, "REST Response for " + calledAPI + ": " + response);
			if (response == null) {
				return;
			} else if (response.contains(CERTIFICATE_EXCEPTION)
					&& response.contains(VALIDATE_CERTIFICATE)) {
				return;
			}
			if (gson == null) {
				gson = new Gson();
			}

			switch (calledAPI) {
			case Params.FETCH_IMAGE_LIST:
				// Process the response received for fetching images
				processFetchImageListResponse(ctx, response);
				break;
			}

			return;
		}
	}

	private static void processFetchImageListResponse(Context ctx,
			String response) {
		try {
			Log.d(TAG, "Login REST response : " + response);
			Data data = null;
			if (RestAPIUtils.isJSONValid(response)) {
				data = gson.fromJson(response, Data.class);
			}

			if (data != null) {
				mImageSearchListener.onSuccess(data);
			} else {
				if (response.contains(CERTIFICATE_EXCEPTION)
						&& response.contains(VALIDATE_CERTIFICATE)) {
					mImageSearchListener.onDateTimeError();
				} else {
					mImageSearchListener.onError("");
				}
			}

		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
		}
	}

	public interface ImageSearchListener {
		public abstract void onSuccess(Data data);

		public abstract void onDateTimeError();

		public abstract void onError(String error);
	}

	public static void setImageSearchListener(ImageSearchListener listener) {
		if (listener != null) {
			mImageSearchListener = listener;
		} else {
			throw new NullPointerException(
					"You must send valid ImageSearchListener while fetching Image List");
		}
	}

}