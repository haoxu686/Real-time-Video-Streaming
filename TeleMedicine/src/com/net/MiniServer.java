package com.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;
import android.os.Message;

import com.util.GlobalDef;

public class MiniServer {

	private String host;
	private int port;
	private Thread server;
	private ServerSocket serverSocket;
	private Socket inSocket;
	private Socket outSocket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private Handler messageHandler;
	
	public MiniServer(Handler messageHandler, int remotePort) {
		server = new Thread(new Listener());
		this.messageHandler = messageHandler;
		port = remotePort;
	}
	
	public void start() {
		server.start();
	}
	
	public void stop() {

	}
	
	private class Listener implements Runnable {
		
		public Listener() {
			serverSocket = null;
			inSocket = null;
			outSocket = null;
			dis = null;
			dos = null;
			host = GlobalDef.conf.SERVER_IP;
		}
		
		@Override
		public void run() {
			try {
				serverSocket = new ServerSocket(GlobalDef.conf.LOCAL_SERVER_PORT);
				serverSocket.setSoTimeout(10000);
				System.out.println("STARTED");
				Message message = new Message();
				message.what = GlobalDef.message.LOCAL_SERVER_STARTED;
				messageHandler.sendMessage(message);
				inSocket = serverSocket.accept();
				inSocket.setSoTimeout(10000);
				dis = new DataInputStream(inSocket.getInputStream());
				outSocket = new Socket(host, port);
				dos = new DataOutputStream(outSocket.getOutputStream());
				int bytes = 0;
				byte[] buffer = new byte[8192];
				while ((bytes = dis.read(buffer)) >= 0) {
					dos.write(buffer, 0, bytes);
				}
				System.out.println("DONE");
				dis.close();
				dos.close();
				inSocket.close();
				outSocket.close();
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("IO Interrupted");
				try {
					if (dis != null) {
						dis.close();
					}
					if (dos != null) {
						dos.close();
					}
					if (inSocket != null) {
						inSocket.close();
					}
					if (outSocket != null) {
						outSocket.close();
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
