package com.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.activity.R;
import com.util.FileUtil;
import com.util.GlobalDef;
import com.util.ServerException;

public class FetchWorker {

	private static int GET_FILE_LIST = 1;
	private static int GET_FILE = 2;
	private static int UPLOAD_FILE = 3;
	private static int CREATE_LIVE_VIDEO = 5;
	private static int COMPLETE_lIVE_VIDEO = 6;
	
	private static String host;
	private static String port;
	private static String servlet;
	private static URL url ;
	private static HttpURLConnection connection;
	private static DataInputStream dis;
	private static DataOutputStream dos;
	
	private static void openConnection() throws IOException {
		host = GlobalDef.conf.SERVER_IP;
		port = GlobalDef.conf.SERVER_PROT;
		servlet = GlobalDef.conf.SERVLET_NAME;
		url = new URL("http://"+host+":"+port+"/"+servlet);
		connection = (HttpURLConnection) url.openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestMethod("GET");
		connection.setChunkedStreamingMode(0);
		dos = new DataOutputStream(connection.getOutputStream());
	}
	
	private static void closeConnection() throws IOException {
		dis.close();
		connection.disconnect();
	}
	
	public static ArrayList<Bundle> fetchFileList(String mimeType) throws ServerException {
		ArrayList<Bundle> result = new ArrayList<Bundle>();
		try {
			openConnection();
			dos.writeInt(GET_FILE_LIST);
			dos.writeUTF(mimeType);
			dos.flush();
			dos.close();
			int retVal = connection.getResponseCode();
			if (retVal != HttpURLConnection.HTTP_OK) {
				throw new ServerException("Unable To Connect To Server");
			}
			
			dis = new DataInputStream(connection.getInputStream());
			retVal = dis.readInt();
			if (retVal < 0) {
				throw new ServerException(dis.readUTF());
			}
			
			int icon = 0;
			if (mimeType.equals("image")) {
				icon = R.drawable.icon_image_file;
			} else if (mimeType.equals("video")) {
				icon = R.drawable.icon_video_file;
			} else if (mimeType.equals("pdf")) {
				icon = R.drawable.icon_pdf_file;
			}
			int fileNum = dis.readInt();
			for (int i = 0; i < fileNum; i++) {
				String name = dis.readUTF();
				String lmt = dis.readUTF();
				String size = dis.readUTF();
				String path = dis.readUTF();
				Bundle bundle = new Bundle();
				bundle.putString("name", name);
				bundle.putString("lmt", lmt);
				bundle.putString("size", size);
				bundle.putString("path", path);
				bundle.putInt("icon", icon);
				result.add(bundle);
			}
			closeConnection();
		} catch (IOException e) {
			throw new ServerException("Error While Accessing Remote Data");
		}
		return result;
	}
	
	public static File fetchFile(String path, Handler messageHandler) throws ServerException {
		File file = null;
		try {
			openConnection();
			dos.writeInt(GET_FILE);
			dos.writeUTF(path);
			dos.flush();
			dos.close();
			int retVal = connection.getResponseCode();
			if (retVal != HttpURLConnection.HTTP_OK) {
				throw new ServerException("Unable To Connect To Server");
			}
			dis = new DataInputStream(connection.getInputStream());
			retVal = dis.readInt();
			if (retVal < 0) {
				throw new ServerException(dis.readUTF());
			}
			Message message;
			System.out.println(path);
			int index = path.lastIndexOf("/");
			String name = path.substring(index+1);
			String mimeType = FileUtil.getMIMEType(name);
			if (mimeType.equals("image")) {
				file = new File(GlobalDef.conf.HOME+GlobalDef.conf.IMAGE_PATH+name);
			} else if (mimeType.equals("video")) {
				file = new File(GlobalDef.conf.HOME+GlobalDef.conf.VIDEO_PATH+name);
			} else if (mimeType.equals("pdf")) {
				file = new File(GlobalDef.conf.HOME+GlobalDef.conf.PDF_PATH+name);
			}
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			byte [] buffer = new byte [8192];
			int size = 0;
			int bytes = 10;
			while ((bytes = dis.read(buffer)) >= 0) {
				fos.write(buffer, 0, bytes);
				size += bytes;
				message = new Message();
				message.what = GlobalDef.message.PROGRESS_CHANGED;
				message.getData().putInt("size", size/1024);
				messageHandler.sendMessage(message);
			}
			fos.flush();
			fos.close();
			closeConnection();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServerException("Error While Accessing Remote Data");
		}
		return file;
	}
	
	public static void uploadFile(File file, Handler messageHandler) throws ServerException {
		try {
			openConnection();
			Message message;
			dos.writeInt(UPLOAD_FILE);
			String name = file.getName();
			dos.writeUTF(name);
			FileInputStream fis = new FileInputStream(file);
			byte [] buffer = new byte [8192];
			int size = 0;
			int bytes = 10;
			while ((bytes = fis.read(buffer)) >= 0) {
				dos.write(buffer, 0, bytes);
				size += bytes;
				message = new Message();
				message.what = GlobalDef.message.PROGRESS_CHANGED;
				message.getData().putInt("size", size/1024);
				messageHandler.sendMessage(message);
			}
			dos.flush();
			dos.close();
			fis.close();
			int retVal = connection.getResponseCode();
			if (retVal != HttpURLConnection.HTTP_OK) {
				throw new ServerException("Unable To Connect To Server");
			}
			dis = new DataInputStream(connection.getInputStream());
			retVal = dis.readInt();
			if (retVal < 0) {
				throw new ServerException(dis.readUTF());
			}
			closeConnection();
		} catch (IOException e) {
			throw new ServerException("Error Whiling Accessing Remote Data");
		}
	}
	
	public static int createLiveVideo() throws ServerException {
		try {
			openConnection();
			dos.writeInt(CREATE_LIVE_VIDEO);
			dos.flush();
			dos.close();
			int retVal = connection.getResponseCode();
			if (retVal != HttpURLConnection.HTTP_OK) {
				throw new ServerException("Unable To Connect To Server");
			}
			dis = new DataInputStream(connection.getInputStream());
			int port = dis.readInt();
			closeConnection();
			return port;
		} catch (IOException e) {
			throw new ServerException("Error Whiling Accessing Remote Data");
		}
	}
	
	public static void completeLiveVideo() throws ServerException {
		try {
			openConnection();
			dos.writeInt(COMPLETE_lIVE_VIDEO);
			dos.flush();
			dos.close();
			int retVal = connection.getResponseCode();
			if (retVal != HttpURLConnection.HTTP_OK) {
				throw new ServerException("Unable To Connect To Server");
			}
			dis = new DataInputStream(connection.getInputStream());
			closeConnection();
		} catch (IOException e) {
			throw new ServerException("Error Whiling Accessing Remote Data");
		}
	}
}
