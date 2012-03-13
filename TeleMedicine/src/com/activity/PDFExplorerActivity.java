package com.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import android.widget.AdapterView.OnItemLongClickListener;

import com.net.FetchWorker;
import com.util.GlobalDef;
import com.util.ServerException;

public class PDFExplorerActivity extends Activity {

	private ArrayList<File> files;
	private TextView textFileExplorerHeader;
	private ListView listFile;
	private ArrayList<HashMap<String, Object>> data;
	private SimpleAdapter adapter;
	private ListFileController controller;
	private String [] from = new String [] {"imageFileItem", "textFileItemName", "textFileItemLMT", "textFileItemSize"};
	private int [] to = new int [] {R.id.imageFileItem, R.id.textFileItemName, R.id.textFileItemLMT, R.id.textFileItemSize};
	private AlertDialog dialogChoice;
	private AlertDialog dialogWarn;
	private MessageController messageController;
	private int selectedFileIndex;
	private final String [] choices = new String [] {"View", "Upload", "Delete"};
	private ProgressDialog pdUpload;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.file_explorer);
		
		files = (ArrayList<File>) this.getIntent().getExtras().get("files");
		listFile = (ListView) this.findViewById(R.id.listFile);
		textFileExplorerHeader = (TextView) this.findViewById(R.id.textFileExplorerHeader);
		textFileExplorerHeader.setText("Local PDF Documents");
		
		data = new ArrayList<HashMap<String, Object>>();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		HashMap<String, Object> map;
		File file;
		for (int i = 0; i < files.size(); i++) {
			file = files.get(i);
			map = new HashMap<String, Object>();
			map.put("imageFileItem", R.drawable.icon_pdf_file);
			map.put("textFileItemName", file.getName());
			Date lmt = new Date(file.lastModified());
			map.put("textFileItemLMT", sdf.format(lmt));
			map.put("textFileItemSize", String.valueOf(file.length()/1024)+"KB");
			data.add(map);
		}
		adapter = new SimpleAdapter(this, data, R.layout.file_item, from, to);
		listFile.setAdapter(adapter);
		controller = new ListFileController(this);
		listFile.setOnItemClickListener(controller);
		listFile.setOnItemLongClickListener(controller);
		
		messageController  = new MessageController(this);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("");
		builder.setItems(choices, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					Message message =  new Message();
					message.what = GlobalDef.message.FILE_ACTION_PERFORMED;
					message.getData().putInt("operator", 0);
					messageController.sendMessage(message);
				} else if (which == 1) {
					Message message =  new Message();
					message.what = GlobalDef.message.FILE_ACTION_PERFORMED;
					message.getData().putInt("operator", 1);
					messageController.sendMessage(message);
				} else if (which == 2) {
					dialogWarn.show();
				}
			}
		});
		dialogChoice = builder.create();
		
		builder = new AlertDialog.Builder(this);
		builder.setTitle("Warning");
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Message message = new Message();
				message.what = GlobalDef.message.FILE_DELETE_CONFIRM;
				messageController.sendMessage(message);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Message message = new Message();
				message.what = GlobalDef.message.FILE_DELETE_CANCEL;
				messageController.sendMessage(message);
			}
		});
		builder.setMessage("Are You Sure You Want To Delete?");
		dialogWarn = builder.create();
		
		pdUpload = new ProgressDialog(this);
		pdUpload.setMessage("Uploading...");
		pdUpload.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	}
	
	private class ListFileController implements OnItemClickListener, OnItemLongClickListener {

		private Activity activity;
		
		public ListFileController(Activity activity) {
			this.activity = activity;
		}
		
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			selectedFileIndex = arg2;
			dialogChoice.setTitle(files.get(arg2).getName());
			dialogChoice.show();
			return true;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			File file = files.get(arg2);
			Intent intent = new Intent();
			intent.setPackage("com.adobe.reader");
			intent.setDataAndType(Uri.fromFile(file), "application/pdf");
			activity.startActivity(intent);
		}
		
	}
	
	private class MessageController extends Handler {
		
		private Activity activity;
		
		public MessageController(Activity activity) {
			this.activity = activity;
		}
		
		public void handleMessage(Message message) {
			switch (message.what) {
			case GlobalDef.message.FILE_ACTION_PERFORMED:
				int operator = message.getData().getInt("operator");
				if (operator == 0) {
					File file = files.get(selectedFileIndex);
					Intent intent = new Intent();
					intent.setPackage("com.adobe.reader");
					intent.setDataAndType(Uri.fromFile(file), "application/pdf");
					activity.startActivity(intent);
				} else if (operator == 1) {
					new Thread(new UploadFileAction()).start();
				}
				break;
			case GlobalDef.message.FILE_DELETE_CANCEL:
				dialogWarn.dismiss();
				break;
			case GlobalDef.message.FILE_DELETE_CONFIRM:
				files.remove(selectedFileIndex);
				data.remove(selectedFileIndex);
				File file = files.get(selectedFileIndex);
				files.remove(selectedFileIndex);
				file.delete();
				adapter.notifyDataSetChanged();
				break;
			case GlobalDef.message.DATA_EXCHANGE_BEGIN:
				pdUpload.setMax((int) (files.get(selectedFileIndex).length()/1024));
				pdUpload.setProgress(0);
				pdUpload.show();
				break;
			case GlobalDef.message.PROGRESS_CHANGED:
				int size = message.getData().getInt("size");
				pdUpload.setProgress(size);
				break;
			case GlobalDef.message.UPLOAD_FILE_OK:
				pdUpload.dismiss();
				Toast.makeText(activity, "Upload Succeed", Toast.LENGTH_SHORT).show();
				break;
			case GlobalDef.message.ERROR:
				String error = message.getData().getString("message");
				System.out.println(error);
				break;
			}
		}
	}
	
	private class UploadFileAction implements Runnable {

		public void run() {
			Message message = new Message();
			message.what = GlobalDef.message.DATA_EXCHANGE_BEGIN;
			messageController.sendMessage(message);
			try {
				FetchWorker.uploadFile(files.get(selectedFileIndex), messageController);
			} catch (ServerException e) {
				message = new Message();
				message.what = GlobalDef.message.ERROR;
				message.getData().putString("message", e.getMessage());
				messageController.sendMessage(message);
				e.printStackTrace();
				return;
			}
			message = new Message();
			message.what = GlobalDef.message.UPLOAD_FILE_OK;
			messageController.sendMessage(message);
		}

	}
}
