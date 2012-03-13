package action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;

import servlet.TeleMedicineServlet;
import util.StreamServer;

public class CreateLiveVideoAction implements Action {

	@Override
	public void execute(DataInputStream dis, DataOutputStream dos, TeleMedicineServlet servlet) {
		File dir = new File(servlet.getLiveVideoPath());
		Date now = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
		File archive = new File(dir, sdf.format(now));
		archive.mkdirs();
		File descriptor = new File(archive, "descriptor.txt");
		try {
			FileOutputStream fos = new FileOutputStream(descriptor);
			fos.write("-1".getBytes());
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ServletContext application = servlet.getServletContext();
		StreamServer server = new StreamServer(archive.getAbsolutePath(), 55881, application);
		server.start();
		try {
			dos.writeInt(55881);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

}
