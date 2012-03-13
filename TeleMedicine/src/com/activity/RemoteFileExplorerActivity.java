package com.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.net.FetchWorker;
import com.util.FileUtil;
import com.util.GlobalDef;
import com.util.ServerException;

public class RemoteFileExplorerActivity extends Activity {

	private ArrayList<Bundle> files;
	private TextView textFileExplorerHeader;
	private ListView listFile;
	private ArrayList<HashMap<String, Object>> data;
	private SimpleAdapter adapter;
	private ListFileController controller;
	private String [] from = new String [] {"imageFileItem", "textFileItemName", "textFileItemLMT", "textFileItemSize"};
	private int [] to = new int [] {R.id.imageFileItem, R.id.textFileItemName, R.id.textFileItemLMT, R.id.textFileItemSize};
	private MessageController messageController;
	private File fileToDelete;
	private int selectedFileIndex;
	private ProgressDialog pdDownload;
	private AlertDialog alert;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.file_explorer);
		
		files = this.getIntent().getParcelableArrayListExtra("files");
		listFile = (ListView) this.findViewById(R.id.listFile);
		textFileExplorerHeader = (TextView) this.findViewById(R.id.textFileExplorerHeader);
		textFileExplorerHeader.setText("Remote File");
		
		data = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map;
		Bundle bundle;
		for (int i = 0; i < files.size(); i++) {
			bundle = files.get(i);
			map = new HashMap<String, Object>();
			map.put("imageFileItem", bundle.getInt("icon"));
			map.put("textFileItemName", bundle.getString("name"));
			map.put("textFileItemLMT", bundle.getString("lmt"));
			map.put("textFileItemSize", bundle.getString("size"));
			data.add(map);
		}
		adapter = new SimpleAdapter(this, data, R.layout.file_item, from, to);
		listFile.setAdapter(adapter);
		controller = new ListFileController();
		listFile.setOnItemClickListener(controller);
		
		pdDownload = new ProgressDialog(this);
		pdDownload.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pdDownload.setMessage("Downloading...");
		
		alert = new AlertDialog.Builder(this)
		.setTitle("Warning")
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Message message = new Message();
				message.what = GlobalDef.message.FILE_REPLACEMENT_CONFIRM;
				messageController.sendMessage(message);
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Message message = new Message();
				message.what = GlobalDef.message.FILE_REPLACEMENT_CANCEL;
				messageController.sendMessage(message);
			}
		})
		.create();
		messageController = new MessageController(this);
	}
	
	private class MessageController extends Handler {
		
		private Activity activity;
		
		public MessageController(Activity activity) {
			this.activity = activity;
		}
		
		public void handleMessage(Message message) {
			switch (message.what) {
			case GlobalDef.message.DATA_EXCHANGE_BEGIN:
				pdDownload.setMax(Integer.parseInt(files.get(selectedFileIndex).getString("size")));
				pdDownload.setProgress(0);
				pdDownload.show();
				break;
			case GlobalDef.message.PROGRESS_CHANGED:
				int size = message.getData().getInt("size");
				pdDownload.setProgress(size);
				break;
			case GlobalDef.message.FETCH_FILE_OK:
				pdDownload.dismiss();
				Toast.makeText(activity, "Download Succeed", Toast.LENGTH_SHORT);
				File mimeFile = (File) message.getData().getSerializable("data");
				String mimeType = FileUtil.getMIMEType(mimeFile.getName());
				Intent intent = new Intent();
				if (mimeType.equals("image")) {
					intent.setClass(activity, ImageDisplayActivity.class);
					intent.putExtra("path", mimeFile.getAbsolutePath());
				} else if (mimeType.equals("video")) {
					intent.setClass(activity, VideoDisplayActivity.class);
					intent.putExtra("path", mimeFile.getAbsolutePath());
				} else if (mimeType.equals("pdf")){
					intent.setPackage("com.adobe.reader");
					intent.setDataAndType(Uri.fromFile(mimeFile), "application/pdf");
				}
				activity.startActivity(intent);
				break;
			case GlobalDef.message.ERROR:
				String errorMessage = message.getData().getString("message");
				System.out.println(errorMessage);
				break;
			case GlobalDef.message.FILE_REPLACEMENT_CONFIRM:
				alert.dismiss();
				fileToDelete.delete();
				new Thread(new FetchFileAction(files.get(selectedFileIndex).getString("path"))).start();
				break;
			case GlobalDef.message.FILE_REPLACEMENT_CANCEL:
				alert.dismiss();
				break;
			}
		}
	}

	private class ListFileController implements OnItemClickListener {
		
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			selectedFileIndex = arg2;
			Bundle bundle = files.get(arg2);
			String name = bundle.getString("name");
			System.out.println(name);
			String mimeType = FileUtil.getMIMEType(name);
			File file = null;
			if (mimeType.equals("image")) {
				file = new File(GlobalDef.conf.HOME+GlobalDef.conf.IMAGE_PATH+name);
			} else if (mimeType.equals("video")){
				file = new File(GlobalDef.conf.HOME+GlobalDef.conf.VIDEO_PATH+name);
			} else if (mimeType.equals("pdf")) {
				file = new File(GlobalDef.conf.HOME+GlobalDef.conf.PDF_PATH+name);
			}
			if (file.exists()) {
				fileToDelete = file;
				alert.setMessage("File Exists! Replace?");
				alert.show();
			} else {
				new Thread(new FetchFileAction(bundle.getString("path"))).start();
			}
		}
		
	}
	
	private class FetchFileAction implements Runnable {

		private String path;
		
		public FetchFileAction(String path) {
			this.path = path;
		}
		
		@Override
		public void run() {
			File file = null;
			Message message = new Message();
			message.what = GlobalDef.message.DATA_EXCHANGE_BEGIN;
			messageController.sendMessage(message);
			try {
				file = FetchWorker.fetchFile(path, messageController);
			} catch (ServerException e) {
				message = new Message();
				message.what = GlobalDef.message.ERROR;
				message.getData().putString("message", e.getMessage());
				messageController.sendMessage(message);
				return;
			}
			message = new Message();
			message.what = GlobalDef.message.FETCH_FILE_OK;
			message.getData().putSerializable("data", file);
			messageController.sendMessage(message);
		}
		
	}
}
