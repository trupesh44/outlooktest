package com.outlooktest;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.outlooktest.adapter.ImageListAdapter;
import com.outlooktest.config.Params;
import com.outlooktest.model.Data;
import com.outlooktest.model.PageInfo;
import com.outlooktest.utils.LocalCacheUtils;
import com.outlooktest.utils.RestAPIHelper;
import com.outlooktest.utils.RestAPIHelper.ImageSearchListener;
import com.outlooktest.utils.Utils;

/**
 * 
 * @author trupesh
 * 
 */
public class SearchActivity extends Activity {

	private ListView imageListView = null;
	private EditText searchBox = null;
	private Handler fetchImageListHandler = null;
	private ImageListAdapter imageAdapter = null;
	private TextView emptyListText = null;
	private Context ctx = null;
	private boolean populateList = false;
	private Resources res = null;
	private Runnable fetchImage = null;

	/**
	 * Callback for processing retrieved data for the search item
	 */
	private ImageSearchListener imageSearchListener = new ImageSearchListener() {

		@Override
		public void onSuccess(Data data) {
			if (populateList) {
				if (data != null && data.getQuery() != null
						&& data.getQuery().getPages() != null) {
					imageAdapter.setData(new ArrayList<PageInfo>(data
							.getQuery().getPages().values()));
					switchListVisibility(true);
					LocalCacheUtils.downloadImages = true;
				} else {
					emptyListText.setText(res
							.getString(R.string.no_results_found));
					switchListVisibility(false);
					imageAdapter.setData(null);
				}
			}
		}

		@Override
		public void onError(String error) {

		}

		@Override
		public void onDateTimeError() {

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		ctx = this;
		res = ctx.getResources();

		imageListView = (ListView) findViewById(R.id.image_list);
		searchBox = (EditText) findViewById(R.id.searchEditText);
		emptyListText = (TextView) findViewById(R.id.empty_list_text);

		fetchImageListHandler = new Handler();

		RestAPIHelper.setImageSearchListener(imageSearchListener);
		imageAdapter = new ImageListAdapter(this, null);

		imageListView.setAdapter(imageAdapter);

		searchBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				// Remove previous search requests
				if (fetchImage != null) {
					fetchImageListHandler.removeCallbacks(fetchImage);
				}
				LocalCacheUtils.downloadImages = false;
				switchListVisibility(false);
				// Remove the previously populated list if user has deleted the text
				if (TextUtils.isEmpty(s.toString())) {
					populateList = false;
					imageAdapter.setData(null);
					emptyListText.setText(res
							.getString(R.string.empty_list_text));
					// Delete extra images from SDCard so that SDCard does not take up more space
					Utils.deleteCachedImagesFromSdCard(Params.MAX_CACHED_IMAGES_ON_SDCARD / 2);
				} else {
					populateList = true;
					emptyListText.setText(res.getString(R.string.fetching_data));

					createRunnable(s.toString());
					// schedule search request after few seconds so that
					// multiple requests are not sent when user is typing
					fetchImageListHandler.postDelayed(fetchImage,
							Params.WAIT_FOR_USER_TEXT * Params.MILLI_SECONDS);
				}
			}
		});

	}

	@Override
	protected void onResume() {
		LocalCacheUtils.downloadImages = true;
		super.onResume();
	}

	@Override
	protected void onPause() {
		LocalCacheUtils.downloadImages = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		res = null;
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Decides whether to show listview with the results or the textview showing
	 * the current status
	 * 
	 * @param showListView
	 *            true : displays list view, false: displays text view
	 */
	private void switchListVisibility(boolean showListView) {
		if (showListView) {
			if (imageListView.getVisibility() != View.VISIBLE) {
				imageListView.setVisibility(View.VISIBLE);
			}
			if (emptyListText.getVisibility() == View.VISIBLE) {
				emptyListText.setVisibility(View.INVISIBLE);
			}
		} else {
			if (imageListView.getVisibility() == View.VISIBLE) {
				imageListView.setVisibility(View.INVISIBLE);
			}
			if (emptyListText.getVisibility() != View.VISIBLE) {
				emptyListText.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Creates and displays alerts to the user
	 * 
	 * @param title
	 * @param message
	 */
	private void alertUser(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setNeutralButton(R.string.ok_string, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setTitle(title);
		builder.setMessage(message);
		builder.create().show();
	}
	
	private void resetUI() {
		searchBox.setText("");
		emptyListText.setText(res.getString(R.string.empty_list_text));
	}

	/**
	 * Creates a runnable to send search request
	 * 
	 * @param searchText
	 *            text to be searched
	 */
	private void createRunnable(final String searchText) {

		fetchImage = new Runnable() {
			public void run() {

				Bundle b = new Bundle();
				b.putString(Params.KEY_ACTION, "query");
				b.putString(Params.KEY_PROP, "pageimages");
				b.putString(Params.KEY_FORMAT, "json");
				b.putString(Params.KEY_PIPROP, "thumbnail");
				b.putString(Params.KEY_THUMBNAIL_SIZE, "150");
				b.putString(Params.KEY_PAGES_LIMIT, "50");
				b.putString(Params.KEY_GENERATOR, "prefixsearch");
				b.putString(Params.KEY_SEARCH_STRING, searchText);
				b.putString(Params.KEY_GPS_LIMIT, "50");
				if (Utils.isNetworkAvailable(ctx)) {
					RestAPIHelper.fetch(SearchActivity.this,
							Params.FETCH_IMAGE_LIST, b);
				} else {
					alertUser(res.getString(R.string.title_offline),
							res.getString(R.string.msg_offline));
					resetUI();
				}
			}
		};
	}
}
