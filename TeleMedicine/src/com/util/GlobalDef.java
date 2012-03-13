package com.util;

public class GlobalDef {

	public static final class conf {
		public static String SERVER_IP = "192.168.1.101";
		public static String SERVER_PROT = "55880";
		public static int LOCAL_SERVER_PORT = 55880;
		public static final String SERVLET_NAME="JerryMouse/TeleMedicineServlet";
		public static String HOME = "/sdcard/TeleMedicine/";
		public static final String IMAGE_PATH = "/image/";
		public static final String VIDEO_PATH = "/video/";
		public static final String PDF_PATH = "/pdf/";
	}
	
	public static final class message {
		public static final int FETCH_FILELIST_OK = 1;
		public static final int FETCH_FILE_OK = 2;
		public static final int UPLOAD_FILE_OK = 3;
		public static final int PROGRESS_CHANGED = 4;
		public static final int DATA_EXCHANGE_BEGIN = 5;
		public static final int FILE_REPLACEMENT_CONFIRM = 6;
		public static final int LOCAL_SERVER_STARTED = 7;
		public static final int LOCAL_SERVER_STOPPED = 8;
		public static final int FILE_DELETE_CONFIRM = 9;
		public static final int FILE_DELETE_CANCEL = 10;
		public static final int FILE_ACTION_PERFORMED = 11;
		public static final int CREATE_LIVE_VIDEO_OK = 12;
		public static final int COMPLETE_LIVE_VIDEO_OK = 13;
		public static final int CAMERA_MODE_CHANGED = 14;
		public static final int ERROR = -1;
		public static final int FILE_REPLACEMENT_CANCEL = -2;
	}
}
