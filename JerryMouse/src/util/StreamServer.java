package util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.servlet.ServletContext;

public class StreamServer {

	private ServletContext application;
	private Thread server;
	private ServerSocket serverSocket;
	private Socket socket;
	private DataInputStream dis;
	private String liveVideoPath;
	private boolean hasMore;
	private int curSeq;
	
	public StreamServer(String liveVideoPath, int port, ServletContext application) {
		server = new Thread(new Listener());
		this.liveVideoPath = liveVideoPath;
		curSeq = 0;
		this.application = application;
	}
	
	public void start() {
		hasMore = true;
		server.start();
		application.setAttribute("server", this);
	}
	
	public void stop() {
		hasMore = false;
	}
	
	public int getCurrentSeq() {
		return curSeq-1;
	}
	
	public boolean hasMore() {
		return hasMore;
	}
	
	public String getLiveVideoPath() {
		return liveVideoPath;
	}
	
	private class Listener implements Runnable {
		
		private File file;
		private FileOutputStream fos;

		public Listener() {
			dis = null;
			fos = null;
			socket = null;
			serverSocket = null;
		}
		@Override
		public void run() {
			try {
				serverSocket = new ServerSocket(55881);
				serverSocket.setSoTimeout(10000);
				socket = serverSocket.accept();
				socket.setSoTimeout(10000);
				dis = new DataInputStream(socket.getInputStream());
				file = new File(liveVideoPath, curSeq+".3gp");
				fos = new FileOutputStream(file);
				int bytes = 0;
				int header = 32;
				byte [] buffer = new byte [8192];
				while (true) {
					bytes = dis.read(buffer);
					if (header - bytes < 0) {
						break;
					} else {
						header -= bytes;
					}
				}
				int size = bytes - header;
				fos.write(buffer, header, size);
				while ((bytes = dis.read(buffer)) >= 0) {
					fos.write(buffer, 0, bytes);
					size += bytes;
					if (size/1024 >= 80) {
						fos.flush();
						fos.close();
						curSeq++;
						file = new File(liveVideoPath, curSeq+".3gp");
						fos = new FileOutputStream(file);
						size = 0;
					}
					System.out.println("Read:   " + bytes);
				}
				application.removeAttribute("server");
				dis.close();
				fos.close();
				socket.close();
				serverSocket.close();
				System.out.println("Done");
			} catch (IOException e) {
				e.printStackTrace();
				application.removeAttribute("server");
				try {
					if (dis != null) {
						dis.close();
					}
					if (fos != null) {
						fos.close();
					}
					if (socket != null) {
						socket.close();
					}
					if (serverSocket != null) {
						serverSocket.close();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
		}
		
	}
}
