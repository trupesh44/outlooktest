package com.outlooktest.config;

public class Params {

	/**
	 * Base REST API Url
	 */
	public static final String REST_API_BASE_URL = "https://en.wikipedia.org/w/api.php";

	public static final String HTTP_METHOD_GET = "get";
	public static final String HTTP_METHOD_POST = "post";

	public static final String REST_API_METHOD_FETCH_IMAGE_LIST = "fetchImageList";

	public static final int FETCH_IMAGE_LIST = 1;

	public static class VERSION {
		public static final int SDK_HONEYCOMB = 11;
		public static final int ICE_CREAM_SANDWICH_MR1 = 15;
		public static final int JELLY_BEAN = 16;
		public static final int KITKAT = 19;
	}

	/**
	 * Directory to save downloaded images
	 */
	private static final String IMAGES_LOCAL_DIR = "outlook_test";
	public static String getOutlookLocalDir(){
		return String.valueOf(IMAGES_LOCAL_DIR.hashCode());
	}
	
	public static final int WAIT_FOR_USER_TEXT = 1;
	public static final int MILLI_SECONDS = 1000;
	public static final int MAX_CACHED_IMAGES_ON_SDCARD = 50;
	
	/**
	 * Parameter Keys
	 */
	public static final String KEY_ACTION = "action";
	public static final String KEY_PROP = "prop";
	public static final String KEY_FORMAT = "format";
	public static final String KEY_PIPROP = "piprop";
	public static final String KEY_THUMBNAIL_SIZE = "pithumbsize";
	public static final String KEY_PAGES_LIMIT = "pilimit";
	public static final String KEY_GENERATOR = "generator";
	public static final String KEY_SEARCH_STRING = "gpssearch";
	public static final String KEY_GPS_LIMIT = "gpslimit";
	
}
