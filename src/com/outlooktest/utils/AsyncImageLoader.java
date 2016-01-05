package com.outlooktest.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

public class AsyncImageLoader {
	private LruCache<String, Bitmap> mMemoryCache;


	private final static String TAG = AsyncImageLoader.class.getSimpleName();
	private static final ArrayList<String> imageUrlList = new ArrayList<String>();
	protected static final int TOTAL_ITEM_LIMIT = 12;	
	private static AsyncImageLoader asyncImageLoader;
	private static String extStorageParentDirPath = Environment.getExternalStorageDirectory().getParent();

	private AsyncImageLoader() {
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 4;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				imageUrlList.add(key);
				if(imageUrlList != null && imageUrlList.size() > TOTAL_ITEM_LIMIT) {
				}
				return bitmap.getRowBytes() * bitmap.getHeight() / 1024;								
			}
		};
	}

	public static synchronized AsyncImageLoader getInstance(Context context) {
		if(asyncImageLoader == null) {
			asyncImageLoader = new AsyncImageLoader();
		}
		return asyncImageLoader;
	}

	/**
	 * this method is used to load images. It checks whether the bitmap is in memory cache, if not, download the image.
	 * @param imageUrl
	 * @param imageCallback
	 * @return
	 */
	public Bitmap loadDrawable(final String imageUrl, final ImageCallback imageCallback) {
		final String imageKey = imageUrl;  
		final Bitmap bitmap = getBitmapFromMemoryCache(imageKey);  
		if (bitmap != null) {  
			Log.d(TAG, "get memory url ="+imageKey);
			return bitmap;  
		} 

		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Bitmap) message.obj, imageUrl);
			}
		};

		new Thread() {
			@Override
			public void run() {
				synchronized (this) {
					Bitmap bitmap = null;
					try {
						Thread.sleep(50);
						bitmap = loadImageFromUrl(imageUrl);
						if(bitmap != null && !bitmap.isRecycled()){		
							addBitmapToMemoryCache(imageUrl, bitmap);
						}						 
						Message message = handler.obtainMessage(0, bitmap);
						handler.sendMessage(message);
					} 
					catch (InterruptedException e) {
						Log.e(TAG, e.getMessage(), e);
					}
					catch (OutOfMemoryError e) {
						onOutOfMemoryError(bitmap, e);											
					}
				}
			}
		}.start();
		return null;
	}

	/**
	 * this method is used to load images. It checks whether the bitmap is in memory cache, if not, download the image.
	 * @param imageUrl
	 * @param imageCallback
	 * @return
	 */
	public void loadDrawable(final String imageUrl) {
		final String imageKey = imageUrl;  
		Bitmap bitmap = getBitmapFromMemoryCache(imageKey);  
		if (bitmap != null) {  
			Log.d(TAG, "has url ="+imageKey);
			return;  
		} 
		else
		{
			new Thread() {
				@Override
				public void run() {
					Bitmap bitmap = null;
					try {
						bitmap = loadImageFromUrl(imageKey);
						if(bitmap != null){
							addBitmapToMemoryCache(imageKey, bitmap);
						}
					}
					catch (OutOfMemoryError e) {
						onOutOfMemoryError(bitmap, e);											
					}					
				}
			}.start();
		}
	}

	private void onOutOfMemoryError(Bitmap bitmap, OutOfMemoryError e) {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
			Log.i(TAG, "onOutOfMemoryError() : Bitmap recycled");
		}

		Log.e(TAG, e.toString(), e);
		clearAll();
		System.gc();				
	}

	/**
	 * this method is used to load network images or local images
	 * @param url
	 * @return
	 */	   
	
	public Bitmap loadImageFromUrl(String url) throws OutOfMemoryError {
		/**
		 * load network data
		 */
		URL mediaUrl = null;
		Bitmap bitmap = null;
		try {
			if (!URLUtil.isValidUrl(url)) {
				return bitmap;
			}
			mediaUrl = new URL(url);
			bitmap = url.startsWith("https:") ? getBitmapFromHttpsUrl(mediaUrl)
					: getBitmapFromHttpUrl(mediaUrl);
			return bitmap != null && !bitmap.isRecycled() ? bitmap : null;
		} catch (MalformedURLException e1) {
			Log.e(TAG, "loadImageFromUrl() : " + e1.getMessage());
			return null;
		} catch (OutOfMemoryError e) {
			String error = e == null || e.getMessage() == null ? "OutOfMemoryError"
					: e.getMessage();
			throw new OutOfMemoryError(error);
		}

	}
	public Bitmap getHttpDataInLocalMode(String url) throws OutOfMemoryError {
		Bitmap bitmap = null;
		URL mediaUrl = null;
		try {
			if(!URLUtil.isValidUrl(url)){
				return bitmap;
			}
			mediaUrl = new URL(url);
			bitmap = processedImageURL(mediaUrl);
			if(bitmap != null){
				Log.d(TAG, "bitmap width="+bitmap.getWidth());
				Log.d(TAG, "bitmap height="+bitmap.getHeight());
			}

			return bitmap;
		} 
		catch (MalformedURLException e1) {
			Log.e(TAG, "getHttpDataInLocalMode() : "+e1.getMessage());
			return null;
		} 
		catch (OutOfMemoryError e) {
			String error = e == null || e.getMessage() == null ? "OutOfMemoryError" : e.getMessage();
			throw new OutOfMemoryError(error);
		}	
	}
	
	/**
	 * add Bitmap To Memory Cache
	 * 
	 * @param key
	 * @param bitmap
	 */
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemoryCache(key) == null) {	
			synchronized (mMemoryCache) {
				if(mMemoryCache != null) {				
					mMemoryCache.put(key, bitmap);
				}
			}						
		}
	}  

	/**
	 * get Bitmap From Memory Cache
	 * @param key
	 * @return
	 */
	public Bitmap getBitmapFromMemoryCache(String key) { 		
		Bitmap bitmap = null;
		synchronized(mMemoryCache) {
			if(mMemoryCache != null) {
				bitmap = mMemoryCache.get(key);
				if(bitmap != null && !bitmap.isRecycled()) {				
					return bitmap;  
				}
			}
		}		
		return null;
	}  

	public interface ImageCallback {
		public void imageLoaded(Bitmap imageBitmap, String imageUrl);

	}

	/** 
	 * Removes the cache for particular key entry.
	 * @param key
	 */
	public Bitmap remove(String key) {		
		Log.d(TAG, "remove Key: "+ key);
		synchronized (mMemoryCache) {
			return mMemoryCache.remove(key);	
		}		
	}

	public void clearAll() {
		Log.d(TAG, "clearAll");
		try {
			Bitmap bitmap;
			synchronized (mMemoryCache) {
				if(imageUrlList != null && mMemoryCache != null) {	
					Iterator<String> iterator = imageUrlList.iterator();
					while(iterator.hasNext()) {
						String key = iterator.next();
						bitmap = mMemoryCache.get(key);					
						if (bitmap != null) {
							iterator.remove();							
							mMemoryCache.remove(key);	
							bitmap.recycle();
							bitmap = null;							
						}						
					}									
				}
			}																				
		}
		catch(ConcurrentModificationException e){
			Log.e(TAG, e == null || e.getMessage() == null ? "ConcurrentModificationException" : e.getMessage());
			imageUrlList.clear();
			mMemoryCache.evictAll();
		}
	}

	/**
	 * release the previous bitmap to avoid OOM
	 * @param imageUrl
	 */
	public void releaseBitmap(String imageUrl) {
		Bitmap delBitmap;
		synchronized (mMemoryCache) {
			delBitmap = mMemoryCache.get(imageUrl);
			if (delBitmap != null) {
				remove(imageUrl);
				delBitmap.recycle();
			}
		}			
	}

	private Bitmap processedImageURL(URL mediaUrl) {
		Bitmap bitmap = null;
		InputStream i;
		if(mediaUrl == null || mediaUrl.toString().equalsIgnoreCase("http:"))
			return null;
		try {
			Log.d(TAG, "Media URL : "+ mediaUrl);			
			i = (InputStream) mediaUrl.openStream();			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(i, null, options);
			i.close();

			// Find the correct scale value. It should be a power of 2
			int resizeScale = 1;			
		
			// Load pre-scaled bitmap
			options.inSampleSize = resizeScale;
			i = (InputStream) mediaUrl.getContent();
			options.inJustDecodeBounds = false;  
			bitmap = BitmapFactory.decodeStream(i, null, options);
		} 
		catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}		
		return bitmap;
	}


	/**
	 * Returns an immutable bitmap from subset of the source bitmap, transformed by the corrected rotation matrix. 
	 * The new bitmap may be the same object as source, or a copy may have been made. It is initialized with the same 
	 * density as the original bitmap.
	 * 
	 * @param originalBitmap
	 * @param url: Path of the image file.
	 * @return
	 */
	private Bitmap correctBitmap(Bitmap originalBitmap, String url) throws OutOfMemoryError {
		//Bitmap correctBmp = null;
		try {

			File f = new File(url);
			ExifInterface exif = new ExifInterface(f.getPath());
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			int angle = 0;

			if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
				angle = 90;
			}
			else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
				angle = 180;
			}
			else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
				angle = 270;
			}

			Matrix mat = new Matrix();
			mat.postRotate(angle);
			originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), mat, true);			
		}
		catch (IOException e) {
			Log.w("TAG", "-- Error in setting image");
		} 
		catch (OutOfMemoryError e) {
			String error = e == null || e.getMessage() == null ? "OutOfMemoryError" : e.getMessage();
			throw new OutOfMemoryError(error);
		}
		return originalBitmap;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	private static Bitmap getScaledBitmap(String url) {
		Bitmap bitmap = null;						
		if(!TextUtils.isEmpty(url) && url.startsWith(extStorageParentDirPath)) {
			File file = new File(url);
			if(file != null && !file.exists()) {
				return bitmap;
			}
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(url, options);		

		// Find the correct scale value. It should be a power of 2
		int resizeScale = 1;
		
		Log.d(TAG, " getScaledBitmap() : resizeScale : "+ resizeScale);		
		options = new BitmapFactory.Options();
		options.inSampleSize = resizeScale;
		bitmap = BitmapFactory.decodeFile(url, options);			
		return bitmap;
	}

	public Bitmap getTNScaledBitmap(Bitmap bitmap, int tnWidth, int tnHeight) {	
		//Bitmap scaledBitmap = null;
		int newWidth = 0;
		int newHeight = 0;	

		if(bitmap == null || bitmap.isRecycled()) 
			return null;

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		if(width == 0) width = tnWidth;
		if(height == 0) height = tnHeight;

		double widthRatio = (double) tnWidth / width ;
		double heightRatio = (double) tnHeight / height ;

		if(widthRatio > heightRatio) {
			newHeight = (int)Math.round(height * widthRatio);
			newWidth = (int)Math.round(width * widthRatio);
		}
		else if(heightRatio > widthRatio) {
			newHeight = (int)Math.round(height * heightRatio);
			newWidth = (int)Math.round(width * heightRatio);
		}
		else if(heightRatio == widthRatio) {
			newHeight = (int)Math.round(height * heightRatio);
			newWidth = (int)Math.round(width * heightRatio);
		}
		
		try {
			if(newWidth > 0 && newHeight > 0) {
				bitmap.setHasAlpha(true);
				bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);				
			}			
		}
		catch (OutOfMemoryError e) {			
			onOutOfMemoryError(bitmap, e);
		}
		return bitmap;
	}
	
	private static Bitmap getBitmapFromHttpsUrl(URL mediaUrl) {
		Bitmap bitmap = null;
		InputStream inStream;
		HttpsURLConnection connection = null;
		try {
			if(mediaUrl != null) {
				connection = (HttpsURLConnection) mediaUrl.openConnection();	
				connection.setHostnameVerifier(hostnameVerifier);	
				connection.connect();
				if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {				
					Log.e(TAG, "Failed to access media : " + connection.getResponseMessage() + "mediaUrl : " + mediaUrl);									
				}	
				else {
					inStream = connection.getInputStream();	
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inPreferredConfig = Bitmap.Config.ARGB_8888;
					/*options.inJustDecodeBounds = true;
					BitmapFactory.decodeStream(inStream, new Rect(-1,-1,-1,-1), options);
					options.inSampleSize = calculateInSampleSize(options, 210, 210);*/
					options.inJustDecodeBounds = false;
					bitmap = BitmapFactory.decodeStream(inStream, new Rect(-1,-1,-1,-1), options);
					inStream.close();
				}
			}									
		} 
		catch (IOException e) {
			Log.e(TAG, "getBitmapFromHttpsUrl() : " + e.getMessage());
		}
		catch (ClassCastException e) {
			Log.e(TAG, "getBitmapFromHttpsUrl() : " + e.getMessage());
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}	
		}				
		return bitmap;
	}

	private static Bitmap getBitmapFromHttpUrl(URL mediaUrl) {
		Bitmap bitmap = null;
		InputStream inStream;
		HttpURLConnection connection = null;		
		try {
			if(mediaUrl != null) {
				connection = (HttpURLConnection) mediaUrl.openConnection();					
				connection.connect();
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {				
					Log.e(TAG, "Failed to access media : " + connection.getResponseMessage() + "mediaUrl : " + mediaUrl);									
				}	
				else {
					inStream = connection.getInputStream();	
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inPreferredConfig = Bitmap.Config.ARGB_8888;
					/*options.inJustDecodeBounds = true;
					BitmapFactory.decodeStream(inStream, new Rect(-1,-1,-1,-1), options);
					options.inSampleSize = calculateInSampleSize(options, 210, 210);*/
					options.inJustDecodeBounds = false;
					bitmap = BitmapFactory.decodeStream(inStream, new Rect(-1,-1,-1,-1), options);
					inStream.close();
				}
			}									
		} 
		catch (IOException e) {
			Log.e(TAG, "getBitmapFromHttpUrl() : " + e.getMessage());
		}
		catch (ClassCastException e) {
			Log.e(TAG, "getBitmapFromHttpUrl() : " + e.getMessage());
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}	
		}				
		return bitmap;
	}


	private static HostnameVerifier hostnameVerifier = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};
}
