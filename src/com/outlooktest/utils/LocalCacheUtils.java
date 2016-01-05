package com.outlooktest.utils;

import java.io.File;

import com.outlooktest.config.Params;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class LocalCacheUtils {

	private static final String TAG = "LocalCacheUtils";
	public static final String IMAGE_FOLDER = "Images";
	public static boolean downloadImages = false;

	public static synchronized Bitmap setThumbnailImage(
			AsyncImageLoader asyncImageLoader, String imageUrl,
			String folderName, int reqWidth, int reqHeight) {
		Bitmap cachedImage = null;
		try {
			if (imageUrl != null) {

				String imageName = null;
				imageName = String.valueOf(imageUrl.hashCode());

				String imagePath = Params.getOutlookLocalDir() + File.separator
						+ folderName + File.separator + imageName;

				String filePath = isValidImagePath(imagePath, imageUrl);

				if (filePath != null && !filePath.isEmpty()) {

					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inPreferredConfig = Bitmap.Config.ARGB_8888;
					options.inJustDecodeBounds = true;
					cachedImage = BitmapFactory.decodeFile(filePath, options);
					options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
					options.inJustDecodeBounds = false;
					cachedImage = BitmapFactory.decodeFile(filePath, options);
				}
			}
		} catch (OutOfMemoryError e) {
			Log.d("Trupesh", " Image Crash ");
			asyncImageLoader.clearAll();
		}

		return cachedImage;
	}
	
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
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

	private static String isValidImagePath(final String imagePath, final String cdnPath){

		String validFileName = "";
		boolean isFileExists = false;
		final String dirPath = imagePath.substring(0, imagePath.lastIndexOf("/"));
		final String imageName = imagePath.substring(imagePath.lastIndexOf("/")+1);

		String cdnDirPath = Environment.getExternalStorageDirectory()+ File.separator + dirPath;

		//Format image name to .sod. 
		String imageFormatedName = null;
		if(imageName.contains(".")){
			imageFormatedName = imageName.substring(0, imageName.indexOf("."))+".sod"; 
		}else{
			imageFormatedName = imageName +".sod";
		}

		File imagePathFile = new File(cdnDirPath, imageFormatedName); 

		//Check for valid file path.
		if(imagePathFile.exists()){
			isFileExists = true;
			validFileName = imagePathFile.getPath();

		}

		//If file does not exists, then create the folder and download the file.		
		if(!isFileExists && cdnPath != null && !cdnPath.isEmpty()){
			checkOldImages(cdnDirPath, imageFormatedName);
			ImageDownloadTask mediaDownloadTask = new ImageDownloadTask();
			mediaDownloadTask.execute(dirPath, imageFormatedName, cdnPath);			
		}		
		return validFileName;		
	}

	/**
	 * this method is used to check whether app needs to delete the old images
	 *
	 */

	private static void checkOldImages(String cdnDirPath, String imageFormatedName) {
		String name = imageFormatedName.substring(0, imageFormatedName.lastIndexOf("."));
		int number = getIntegerFromString(name);
		if(number > 0)
		{
			int nDigits =  (int) (Math.floor(Math.log10(Math.abs(number))) + 1);
			int startIndex = (number-2) < 0 ? 0 : (number-2);
			for(int i = startIndex; i <number; i++)
			{
				File file = new File(cdnDirPath, name.substring(0,name.length()-nDigits)+i+".sod");
				if(file.exists())
				{
					Log.d(TAG, "detele old imgae = "+ file.getAbsolutePath());
					file.delete();	
				}
			}
		}
	}

	/**
	 * this method is used to get number of image name for deleting old images
	 *
	 */
	private static int getIntegerFromString(String str) {
		if (str == null) {
			return 0;
		}
		int length = str.length();
		if (length == 0) {
			return 0;
		}
		boolean hasInteger = false;
		StringBuffer strBuffer = new StringBuffer();
		for (int i = str.length()-1; i >= 0 ; i--) {
			char c = str.charAt(i);
			if (c >= '0' && c <= '9') {
				strBuffer.insert(0, c);
				hasInteger = true;
			}
			else
			{
				break;
			}
		}
		if(hasInteger)
		{
			return Integer.parseInt(strBuffer.toString());
		}
		else
		{
			return 0;
		}
	}
}
