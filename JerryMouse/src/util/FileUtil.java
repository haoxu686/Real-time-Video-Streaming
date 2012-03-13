package util;


public class FileUtil {
	
	public static String getMIMEType(String fileName) {
		String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
		suffix = suffix.toLowerCase();
		String type = "";
		if (suffix.equals("mp3") || suffix.equals("aac") || 
				suffix.equals("amr") || suffix.equals("mpeg")) {
			type = "audio";
		} else if (suffix.equals("jpg") || suffix.equals("gif") ||
				suffix.equals("png") || suffix.equals("jpeg")) {
			type = "image";
		} else if (suffix.equals("mp4") || suffix.equals("3gp")) {
			type = "video";
		} else if (suffix.equals("pdf")) {
			type = "pdf";
		} else if (suffix.equals("liv")) {
			type = "livevideo";
		} else {
			type = "*";
		}
		return type;
	}
}
