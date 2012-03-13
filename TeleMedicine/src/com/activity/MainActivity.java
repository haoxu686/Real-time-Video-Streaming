package com.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.util.GlobalDef;

public class MainActivity extends Activity {
	
	private TextView textTitle;
	private ListView listMenu;
	private OnItemClickListener controller;
	private SimpleAdapter adapter;
	private ArrayList<HashMap<String,Object>> data;
	private String [] from = new String [] {"imageMenuItem", "textMenuItem"};
	private int [] to = new int [] {R.id.imageMenuItem, R.id.textMenuItem};
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        SharedPreferences preferences = this.getSharedPreferences("telemedicine", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (!preferences.contains("serverIp")) {
        	editor = preferences.edit();
        	editor.putString("serverIp", "192.168.1.101");
        	editor.putString("serverPort", "55880");
        	editor.putInt("localSocketPort", 55880);
        	editor.putString("archiveHome", "/sdcard/TeleMedicine");
        	editor.commit();
        }
        GlobalDef.conf.SERVER_IP = preferences.getString("serverIp", "192.168.1.101");
        GlobalDef.conf.SERVER_PROT = preferences.getString("serverPort", "55880");
        GlobalDef.conf.LOCAL_SERVER_PORT = preferences.getInt("localSocketPort", 55880);
        GlobalDef.conf.HOME = preferences.getString("archiveHome", "/sdcard/TeleMedicine");
		
        File root = new File(GlobalDef.conf.HOME);
        if (!root.exists()) {
        	root.mkdirs();
        }
        File imagePath = new File(root, GlobalDef.conf.IMAGE_PATH);
        if (!imagePath.exists()) {
        	imagePath.mkdirs();
        }
        File videoPath = new File(root, GlobalDef.conf.VIDEO_PATH);
        if (!videoPath.exists()) {
        	videoPath.mkdirs();
        }
        File pdfPath = new File(root, GlobalDef.conf.PDF_PATH);
        if (!pdfPath.exists()) {
        	pdfPath.mkdirs();
        }
        
        textTitle = (TextView) this.findViewById(R.id.textTitle);
        textTitle.setText("TeleMedicine Service");
        controller = new Controller(this);
        data = new ArrayList<HashMap<String,Object>>();
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("imageMenuItem", R.drawable.icon_localfile);
        map.put("textMenuItem", "Local Files");
        data.add(map);
        map = new HashMap<String,Object>();
        map.put("imageMenuItem", R.drawable.icon_remotefile);
        map.put("textMenuItem", "Remote Files");
        data.add(map);
        map = new HashMap<String,Object>();
        map.put("imageMenuItem", R.drawable.icon_camera);
        map.put("textMenuItem", "Camera");
        data.add(map);
        map = new HashMap<String,Object>();
        map.put("imageMenuItem", R.drawable.icon_settings);
        map.put("textMenuItem", "Settings");
        data.add(map);
        map = new HashMap<String,Object>();
        map.put("imageMenuItem", R.drawable.icon_help);
        map.put("textMenuItem", "Help");
        data.add(map);
        map = new HashMap<String,Object>();
        map.put("imageMenuItem", R.drawable.icon_exit);
        map.put("textMenuItem", "Exit");
        data.add(map);
        adapter = new SimpleAdapter(this, data, R.layout.menu_item, from, to);
        listMenu = (ListView) this.findViewById(R.id.listMenu);
        listMenu.setOnItemClickListener(controller);
        listMenu.setAdapter(adapter);
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
			if (menuItem.equals("Local Files")) {
				Intent intent = new Intent();
				intent.setClass(activity, LocalFileActivity.class);
				activity.startActivity(intent);
			} else if (menuItem.equals("Remote Files")) {
				Intent intent = new Intent();
				intent.setClass(activity, RemoteFileActivity.class);
				activity.startActivity(intent);
			} else if (menuItem.equals("Camera")) {
				Intent intent = new Intent();
				intent.setClass(activity, CameraActivity.class);
				activity.startActivity(intent);
			} else if (menuItem.equals("Settings")) {
				Intent intent = new Intent();
				intent.setClass(activity, SettingsActivity.class);
				activity.startActivity(intent);
			} else if (menuItem.equals("Help")) {
				Intent intent = new Intent();
				intent.setClass(activity, HelpActivity.class);
				activity.startActivity(intent);
			} else if (menuItem.equals("Exit")) {
				System.exit(0);
			}
		}
    	
    }
}