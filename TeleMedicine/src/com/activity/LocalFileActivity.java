package com.activity;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.util.FileUtil;
import com.util.GlobalDef;


public class LocalFileActivity extends Activity {

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
        
        textTitle = (TextView) this.findViewById(R.id.textTitle);
        textTitle.setText("Local Files");
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
        map.put("imageMenuItem", R.drawable.icon_videostream);
        map.put("textMenuItem", "Live Video");
        data.add(map);
        map = new HashMap<String, Object>();
        map.put("imageMenuItem", R.drawable.icon_help);
        map.put("textMenuItem", "Help");
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
			if (menuItem.equals("Image")) {
				Intent intent = new Intent();
				intent.setClass(activity, ImageExplorerActivity.class);
				activity.startActivity(intent);
			} else if (menuItem.equals("Video")) {
				Intent intent = new Intent();
				intent.setClass(activity, VideoExplorerActivity.class);
				activity.startActivity(intent);
			} else if (menuItem.equals("PDF Document")) {
				File dir = new File(GlobalDef.conf.HOME, GlobalDef.conf.PDF_PATH);
				File [] files = dir.listFiles();
				ArrayList<File> pdfFiles = new ArrayList<File>(files.length);
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory() || !FileUtil.getMIMEType(files[i].getName()).equals("pdf")) {
						continue;
					}
					pdfFiles.add(files[i]);
				}
				Intent intent = new Intent();
				intent.setClass(activity, PDFExplorerActivity.class);
				intent.putExtra("files", pdfFiles);
				activity.startActivity(intent);
			} else if (menuItem.equals("Live Video")) {
				Intent intent = new Intent();
				intent.setClass(activity, LiveVideoActivity.class);
				activity.startActivity(intent);
			} else if (menuItem.equals("Help")) {
				Intent intent = new Intent();
				intent.setClass(activity, HelpActivity.class);
				activity.startActivity(intent);
			}
		}
    }
}
