package com.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.net.FetchWorker;
import com.util.GlobalDef;
import com.util.ServerException;


public class RemoteFileActivity extends Activity {

	private TextView textTitle;
	private ListView listMenu;
	private OnItemClickListener controller;
	private SimpleAdapter adapter;
	private ArrayList<HashMap<String,Object>> data;
	private MessageController messageController;
	private String [] from = new String [] {"imageMenuItem", "textMenuItem"};
	private int [] to = new int [] {R.id.imageMenuItem, R.id.textMenuItem};
	private ProgressDialog pdLoading;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        textTitle = (TextView) this.findViewById(R.id.textTitle);
        textTitle.setText("Remote Files");
        controller = new Controller(this);
        data = new ArrayList<HashMap<String,Object>>();
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("imageMenuItem", R.drawable.icon_image);
        map.put("textMenuItem", "Image");
        data.add(map);
        map = new HashMap<String,Object>();
        map.put("imageMenuItem", R.drawable.icon_video);
        map.put("textMenuItem", "Video");
        data.add(map);
        map = new HashMap<String,Object>();
        map.put("imageMenuItem", R.drawable.icon_pdf);
        map.put("textMenuItem", "PDF Document");
        data.add(map);
        map = new HashMap<String, Object>();
        map.put("imageMenuItem", R.drawable.icon_help);
        map.put("textMenuItem", "Help");
        data.add(map);
        adapter = new SimpleAdapter(this, data, R.layout.menu_item, from, to);
        listMenu = (ListView) this.findViewById(R.id.listMenu);
        listMenu.setOnItemClickListener(controller);
        listMenu.setAdapter(adapter);
        
        messageController = new MessageController(this);
        pdLoading = new ProgressDialog(this);
        pdLoading.setTitle("");
        pdLoading.setMessage("Loading. Please Wait...");
        pdLoading.setIndeterminate(true);
    }
    
    private class Controller implements OnItemClickListener {

    	private Activity activity;
    	
    	public Controller(Activity activity) {
    		this.activity = activity;
    	}
    	
		@SuppressWarnings("unchecked")
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			HashMap<String,String> map = (HashMap<String, String>) arg0.getItemAtPosition(arg2);
			String menuItem = map.get("textMenuItem");
			if (menuItem.equals("Image")) {
				new Thread(new FetchFileListAction("image")).start();
			} else if (menuItem.equals("Video")) {
				pdLoading.show();
				new Thread(new FetchFileListAction("video")).start();
			} else if (menuItem.equals("PDF Document")) {
				pdLoading.show();
				new Thread(new FetchFileListAction("pdf")).start();
			} else if (menuItem.equals("Live Video")) {
				
			} else if (menuItem.equals("Help")) {
				Intent intent = new Intent();
				intent.setClass(activity, HelpActivity.class);
				activity.startActivity(intent);
			}
		}
    }
    
    private class MessageController extends Handler {
    	
    	private Activity activity;
    	
    	public MessageController(Activity activity) {
    		this.activity = activity;
    	}
    	
    	public void handleMessage(Message message) {
    		switch (message.what) {
    		case GlobalDef.message.DATA_EXCHANGE_BEGIN:
    			pdLoading.show();
    			break;
	    	case GlobalDef.message.FETCH_FILELIST_OK:
	    		pdLoading.dismiss();
	    		ArrayList<Bundle> result = message.getData().getParcelableArrayList("data");
	    		Intent intent = new Intent();
				intent.setClass(activity, RemoteFileExplorerActivity.class);
				intent.putParcelableArrayListExtra("files", result);
				activity.startActivity(intent);
	    		break;
	    	case GlobalDef.message.ERROR:
	    	break;
    		}
    	}
    }
    
    private class FetchFileListAction implements Runnable {

    	private String mimeType;
    	
    	public FetchFileListAction(String mimeType) {
    		this.mimeType = mimeType;
    	}
    	
		@Override
		public void run() {
			ArrayList<Bundle> result = null;
			Message message = new Message();
			message.what = GlobalDef.message.DATA_EXCHANGE_BEGIN;
			messageController.sendMessage(message);
			try {
				result = FetchWorker.fetchFileList(mimeType);
			} catch (ServerException e) {
				message = new Message();
				message.what = GlobalDef.message.ERROR;
				message.getData().putString("message", e.getMessage());
				messageController.sendMessage(message);
				e.printStackTrace();
				return;
			}
			message = new Message();
			message.what = GlobalDef.message.FETCH_FILELIST_OK;
			message.getData().putParcelableArrayList("data", result);
			messageController.sendMessage(message);
		}
    	
    }
}
