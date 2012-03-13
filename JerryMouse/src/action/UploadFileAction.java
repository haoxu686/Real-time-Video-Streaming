package action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import servlet.TeleMedicineServlet;
import util.FileUtil;

public class UploadFileAction implements Action {

	private String fileName;
	private FileOutputStream fos;
	@Override
	public void execute(DataInputStream dis, DataOutputStream dos, TeleMedicineServlet servlet) {
		try {
			dos.writeInt(1);
			fileName = dis.readUTF();
			String type = FileUtil.getMIMEType(fileName);
			File file = null;
			if (type.equals("image")) {
				file = new File(servlet.getImagePath(), fileName);
			} else if (type.equals("video")){
				file = new File(servlet.getVideoPath(), fileName);
			} else if (type.equals("pdf")) {
				file = new File(servlet.getPDFPath(), fileName);
			}
			file.createNewFile();
			fos = new FileOutputStream(file);
			byte [] buffer = new byte [8096];
			int bytes = 10;
			int size = 0;
			while ((bytes = dis.read(buffer)) >= 0) {
				size += bytes;
				fos.write(buffer, 0, bytes);
			}
			dos.flush();
			fos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
