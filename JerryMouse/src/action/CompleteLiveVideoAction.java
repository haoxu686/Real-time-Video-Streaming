package action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.ServletContext;

import servlet.TeleMedicineServlet;
import util.StreamServer;

public class CompleteLiveVideoAction implements Action {

	@Override
	public void execute(DataInputStream dis, DataOutputStream dos, TeleMedicineServlet servlet) {
		ServletContext application = servlet.getServletContext();
		StreamServer server = (StreamServer) application.getAttribute("server");
		server.stop();
		File descriptor = new File(server.getLiveVideoPath(), "descriptor.txt");
		try {
			descriptor.createNewFile();
			FileOutputStream fos = new FileOutputStream(descriptor);
			fos.write(String.valueOf(server.getCurrentSeq()).getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
