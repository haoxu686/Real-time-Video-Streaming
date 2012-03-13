package util;

import java.io.File;
import java.io.FileFilter;

public class MimeFilter implements FileFilter {

	private String mimeType;
	
	public MimeFilter(String mimeType) {
		this.mimeType = mimeType;
	}
	
	@Override
	public boolean accept(File arg0) {
		if (FileUtil.getMIMEType(arg0.getName()).equals(mimeType)) {
			return true;
		} else {
			return false;
		}
	}

}
