package com.outlooktest.adapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.outlooktest.R;
import com.outlooktest.model.PageInfo;
import com.outlooktest.model.Thumbnail;
import com.outlooktest.utils.AsyncImageLoader;
import com.outlooktest.utils.AsyncImageLoader.ImageCallback;
import com.outlooktest.utils.LocalCacheUtils;

public class ImageListAdapter extends BaseAdapter {

	private List<PageInfo> mPages = null;
	private Context mCtx = null;
	private AsyncImageLoader asyncImageLoader = null;

	public ImageListAdapter(Context ctx, List<PageInfo> pages) {
		if (pages != null) {
			mPages = pages;
		}
		mCtx = ctx;
		asyncImageLoader = AsyncImageLoader.getInstance(mCtx);
	}

	/**
	 * Reload the list with new Data
	 * 
	 * @param pages
	 *            New Data that needs to be populated
	 */
	public void setData(List<PageInfo> pages) {
		mPages = pages;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		int count = 0;
		if (mPages != null) {
			count = mPages.size();
		}
		return count;
	}

	@Override
	public PageInfo getItem(int pos) {
		return mPages.get(pos);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View rowView = convertView;
		PageInfo pageInfo = mPages.get(position);
		// reuse views
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) mCtx
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.image_list_item, null);
			// configure view holder
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) rowView.findViewById(R.id.imageName);
			viewHolder.image = (ImageView) rowView.findViewById(R.id.tnimage);
			viewHolder.progressLayout = (LinearLayout) rowView
					.findViewById(R.id.progressLayout);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		holder.text.setText(pageInfo.getTitle());
		holder.position = position;
		renderImageIcon(holder, pageInfo, position);

		return rowView;
	}

	/**
	 * Asynchronously load the image from URL
	 * 
	 * @param holder
	 *            ViewHolder for list item
	 * @param pageInfo
	 *            list item data
	 */
	private void renderImageIcon(final ViewHolder holder, PageInfo pageInfo, final int position) {

		Thumbnail thumbNailInfo = pageInfo.getThumbnail();

		if (thumbNailInfo == null) {
			holder.image.setImageResource(R.drawable.ic_no_image_available);
			return;
		}

		String imageUrl = thumbNailInfo.getSource();
		int width = 150;
		int height = 150;
		try {
			new URL(imageUrl);
			width = Integer.parseInt(thumbNailInfo.getWidth());
			height = Integer.parseInt(thumbNailInfo.getHeight());

		} catch (MalformedURLException e) {
			holder.image.setImageResource(R.drawable.ic_no_image_available);
			return;
		} catch (NumberFormatException ne) {
			holder.image.setImageResource(R.drawable.ic_no_image_available);
			return;
		}

		holder.image.setTag(imageUrl);
		Bitmap cachedImage = null;
		// Check if image is present on SDCard
		cachedImage = LocalCacheUtils.setThumbnailImage(asyncImageLoader,
				imageUrl, LocalCacheUtils.IMAGE_FOLDER, width, height);
		if (cachedImage == null) {
			// Load image from URL
			cachedImage = asyncImageLoader.loadDrawable(imageUrl,
					new ImageCallback() {
						public void imageLoaded(Bitmap imageBitmap,
								String imageUrl) {
							holder.progressLayout.setVisibility(View.GONE);
							holder.image.setVisibility(View.VISIBLE);
							if (holder.position == position) {
								if (imageBitmap != null) {
									holder.image.setImageBitmap(imageBitmap);
								} else {
									holder.image
											.setImageResource(R.drawable.ic_no_image_available);
								}
							}

						}
					});
			if (cachedImage == null) {
				holder.progressLayout.setVisibility(View.VISIBLE);
				holder.image.setVisibility(View.GONE);
			}
		}

		if (cachedImage != null) {
			holder.image.setImageBitmap(cachedImage);
		}
	}

	/**
	 * View Holder for List item
	 * @author trupesh
	 *
	 */
	static class ViewHolder {
		public TextView text;
		public ImageView image;
		public LinearLayout progressLayout;
		int position;
	}

}
