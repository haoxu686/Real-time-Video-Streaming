package com.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.net.FetchWorker;
import com.util.FileUtil;
import com.util.GlobalDef;
import com.util.ServerException;

public class VideoExplorerActivity extends Activity {
	
	private TextView textGalleryHeader;
	private Gallery gallery;
	private GalleryAdapter adapter;
	private String repository;
	private GalleryController galleryController;
	private AlertDialog dialogChoice;
	private AlertDialog dialogWarn;
	private MessageController messageController;
	private int selectedFileIndex;
	private File selectedFile;
	private final String [] choices = new String [] {"View", "Upload", "Delete"};
	private ProgressDialog pdUpload;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.media_explorer);
		
		repository = GlobalDef.conf.HOME+GlobalDef.conf.VIDEO_PATH;
		File dir = new File(repository);
		File [] files = dir.listFiles();
		adapter = new GalleryAdapter(this, files);
		galleryController = new GalleryController(this);
		gallery = (Gallery) this.findViewById(R.id.gallery);
		textGalleryHeader = (TextView) this.findViewById(R.id.galleryHeader);
		gallery.setAdapter(adapter);
		gallery.setOnItemClickListener(galleryController);
		gallery.setOnItemLongClickListener(galleryController);
		gallery.setOnItemSelectedListener(galleryController);
		
		messageController = new MessageController(this);
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
		pdUpload.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pdUpload.setMessage("Uploading...");
		
		textGalleryHeader.setText("0/0");
	}
	
	private class GalleryController implements OnItemClickListener, OnItemLongClickListener, OnItemSelectedListener {

		private Activity activity;
		
		public GalleryController(Activity activity) {
			this.activity = activity;
		}
		
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			File file = (File) arg0.getItemAtPosition(arg2);
			Intent intent = new Intent();
			intent.setClass(activity, VideoDisplayActivity.class);
			intent.putExtra("path", file.getAbsolutePath());
			activity.startActivity(intent);
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			File file = (File) arg0.getItemAtPosition(arg2);
			selectedFileIndex = arg2;
			selectedFile = file;
			dialogChoice.setTitle(file.getName());
			dialogChoice.show();
			return true;
		}

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			File file = (File) arg0.getItemAtPosition(arg2);
			textGalleryHeader.setText(arg2+1+"/"+arg0.getAdapter().getCount()+" "+file.length()/1024+"KB");
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
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
					Intent intent = new Intent();
					intent.setClass(activity, VideoDisplayActivity.class);
					intent.putExtra("path", selectedFile.getAbsolutePath());
					activity.startActivity(intent);
				} else if (operator == 1) {
					new Thread(new UploadFileAction()).start();
				}
				break;
			case GlobalDef.message.FILE_DELETE_CANCEL:
				dialogWarn.dismiss();
				break;
			case GlobalDef.message.FILE_DELETE_CONFIRM:
				System.out.println(selectedFile.getAbsolutePath());
				adapter.removeImage(selectedFile);
				selectedFile.delete();
				if (selectedFileIndex >= adapter.getCount()) {
					selectedFileIndex--;
				}
				if (adapter.getCount() == 0) {
					textGalleryHeader.setText("0/0");
					break;
				}
				selectedFile = (File) adapter.getItem(selectedFileIndex);
				textGalleryHeader.setText(selectedFileIndex+1+"/"+adapter.getCount()+" "+selectedFile.length()/1024+"KB");
				break;
			case GlobalDef.message.DATA_EXCHANGE_BEGIN:
				pdUpload.setMax((int) (selectedFile.length()/1024));
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
				FetchWorker.uploadFile(selectedFile, messageController);
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
	
	private class GalleryAdapter extends BaseAdapter {

		private Activity activity;
		private ArrayList<File> files;
		
		public GalleryAdapter(Activity activity, File [] fileList) {
			this.activity = activity;
			files = new ArrayList<File>();
			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i].isDirectory() || !FileUtil.getMIMEType(fileList[i].getName()).equals("video")) {
					continue;
				}
				files.add(fileList[i]);
			}
		}
		
		@Override
		public int getCount() {
			return files.size();
		}

		@Override
		public Object getItem(int arg0) {
			return files.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			Bitmap image = ThumbnailUtils.createVideoThumbnail(files.get(arg0).getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
			ImageView view = null;
			if (arg1 != null) {
				view = (ImageView) arg1;
				int width = image.getWidth()*180/image.getHeight();
				view.setLayoutParams(new Gallery.LayoutParams(width, 180));
				view.setImageBitmap(image);
			} else {
				view = new ImageView(activity);
				int width = image.getWidth()*180/image.getHeight();
				view.setLayoutParams(new Gallery.LayoutParams(width, 180));
				view.setImageBitmap(image);
				view.setScaleType(ImageView.ScaleType.FIT_XY);
				view.setPadding(10, 0, 10, 0);
			}
			return view;
		}
		
		public void removeImage(File file) {
			int i = 0;
			for (i = 0; i < files.size(); i++) {
				if (files.get(i) == file) {
					break;
				}
			}
			if (i != files.size()) {
				files.remove(i);
			}
			this.notifyDataSetChanged();
		}
	}
}
