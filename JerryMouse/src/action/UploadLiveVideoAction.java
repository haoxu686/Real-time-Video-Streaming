package action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.ServletContext;

import servlet.TeleMedicineServlet;

public class UploadLiveVideoAction implements Action {

	private String liveVideoPath;
	private int seqNo;
	private File file;
	private FileOutputStream fos;
	private ServletContext application;
	
	@Override
	public void execute(DataInputStream dis, DataOutputStream dos, TeleMedicineServlet servlet) {
		try {
			liveVideoPath = servlet.getLiveVideoPath();
			seqNo = dis.readInt();
			file = new File(liveVideoPath, seqNo+".3gp");
			file.createNewFile();
			fos = new FileOutputStream(file);
			int bytes = 10;
			byte [] buffer = new byte[8096];
			while ((bytes = dis.read(buffer)) >= 0) {
				System.out.println(bytes);
				fos.write(buffer, 0, bytes);
				System.out.println(bytes);
			}
			System.out.println("OK");
			fos.flush();
			fos.close();
			application = servlet.getServletContext();
			application.setAttribute("SEQ_NO", String.valueOf(seqNo));
			File descirptor = new File(servlet.getLiveVideoPath(), "descriptor.txt");
			FileOutputStream fos = new FileOutputStream(descirptor);
			fos.write(String.valueOf(seqNo).getBytes());
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
