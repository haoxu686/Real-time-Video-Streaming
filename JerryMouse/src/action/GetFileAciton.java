package action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import servlet.TeleMedicineServlet;

public class GetFileAciton implements Action {

	private String path;
	private FileInputStream fis;
	
	@Override
	public void execute(DataInputStream dis, DataOutputStream dos, TeleMedicineServlet servlet) {
		try {
			path = dis.readUTF();
			File file = new File(servlet.getArchiveHome(), path);
			if (!file.exists()) {
				dos.writeInt(-1);
				dos.writeUTF("File Doesn't Exist!");
				dos.flush(); 
				return;
			}
			dos.writeInt(1);
			fis = new FileInputStream(file);
			byte [] buffer = new byte [8096];
			int bytes = 0;
			while ((bytes = fis.read(buffer)) >= 0) {
				dos.write(buffer, 0, bytes);
			}
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
