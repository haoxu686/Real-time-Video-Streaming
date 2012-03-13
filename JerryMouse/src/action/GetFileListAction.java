package action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import servlet.TeleMedicineServlet;
import util.MimeFilter;

public class GetFileListAction implements Action {

	private String mimeType;
	private String rootPath;
	private String mimePath;

	@Override
	public void execute(DataInputStream dis, DataOutputStream dos, TeleMedicineServlet servlet) {
		rootPath = servlet.getArchiveHome();
		try {
			mimeType = dis.readUTF();
			File selectedDir = null;
			if (mimeType.equals("image")) {
				mimePath = servlet.getImagePath();
				selectedDir = new File(mimePath);
			} else if (mimeType.equals("video")) {
				mimePath = servlet.getVideoPath();
				selectedDir = new File(mimePath);
			} else if (mimeType.equals("pdf")) {
				mimePath = servlet.getPDFPath();
				selectedDir = new File(mimePath);
			} 
			if (selectedDir == null || !selectedDir.exists()) {
				dos.writeInt(-1);
				dos.writeUTF("Directory Doesn't Exist!");
				dos.flush();
				return;
			}
			dos.writeInt(1);
			File [] files = selectedDir.listFiles(new MimeFilter(mimeType));
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			dos.writeInt(files.length);
			for (int i = 0; i < files.length; i++) {
				dos.writeUTF(files[i].getName());
				Date lmt = new Date(files[i].lastModified());
				dos.writeUTF(sdf.format(lmt));
				dos.writeUTF(String.valueOf(files[i].length()/1024));
				String path = files[i].getAbsolutePath().substring(rootPath.length()+1);
				path = path.replace('\\', '/');
				dos.writeUTF(path);
			}
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}
