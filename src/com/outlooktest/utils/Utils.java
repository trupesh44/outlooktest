package com.outlooktest.utils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.DisplayMetrics;

import com.outlooktest.config.Params;

public class Utils {

	public static int pxToDp(Context context, int px) {
		DisplayMetrics displayMetrics = context.getResources()
				.getDisplayMetrics();
		return (int) ((px / displayMetrics.density) + 0.5);
	}

	public static int dpToPx(Context context, int dp) {

		DisplayMetrics displayMetrics = context.getResources()
				.getDisplayMetrics();
		return (int) ((dp * displayMetrics.density) + 0.5);
	}
	
	public static void deleteCachedImagesFromSdCard(int delImageCount) {
		String imageDir = Environment.getExternalStorageDirectory()
				+ File.separator + Params.getOutlookLocalDir() + File.separator
				+ LocalCacheUtils.IMAGE_FOLDER;
		File imageDirPath = new File(imageDir);
		File[] images = imageDirPath.listFiles();
		if (images != null
				&& images.length > Params.MAX_CACHED_IMAGES_ON_SDCARD) {
			Arrays.sort(images, new Comparator<File>() {
				public int compare(File f1, File f2) {
					if (f1.lastModified() > f2.lastModified()) {
						return 1;
					} else if (f1.lastModified() < f2.lastModified()){
						return -1;
					} else {
						return 0;
					}
				}
			});

			for (int i = 0; i < delImageCount; i++) {
				images[i].delete();
			}
		}
	}
	
	public static boolean isNetworkAvailable(Context ctx) {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}
}
