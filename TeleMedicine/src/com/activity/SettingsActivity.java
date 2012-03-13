package com.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

import com.util.GlobalDef;

public class SettingsActivity extends Activity {

	private ListView listSettings;
	private SimpleAdapter adapter;
	private ArrayList<HashMap<String,Object>> data;
	private String [] from = new String [] {"item"};
	private int [] to = new int [] {R.id.textSettingItem};
	private ListController listController;
	private SharedPreferences preferences;
	private View editView;
	private EditText editContent;
	private Button bConfirm;
	private Button bCancel;
	private EditController editController;
	private AlertDialog editorDialog;
	private AlertDialog alert;
	private int selectedItemIndex;
	private static final String IP_REGEX = "^([01]?\\d\\d?|2[0-4]\\d|" +
										   "25[0-5])\\.([01]?\\d\\d?|" +
										   "2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|" +
										   "2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|" +
										   "2[0-4]\\d|25[0-5])$";
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.settings);
		
		listSettings = (ListView) this.findViewById(R.id.listSettings);
		data = new ArrayList<HashMap<String,Object>>();
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("item", "Server IP");
		data.add(map);
		map = new HashMap<String,Object>();
		map.put("item", "Server Port");
		data.add(map);
		map = new HashMap<String,Object>();;
		map.put("item", "Local Server Port");
		data.add(map);
		map = new HashMap<String,Object>();;
		map.put("item", "Repository Home");
		data.add(map);
		adapter = new SimpleAdapter(this, data, R.layout.setting_item, from, to);
		listSettings.setAdapter(adapter);
		listController = new ListController();
		listSettings.setOnItemClickListener(listController);
		preferences = this.getSharedPreferences("telemedicine", Context.MODE_PRIVATE);
		
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		editView = layoutInflater.inflate(R.layout.edit, null);
		editorDialog = new AlertDialog.Builder(this).setView(editView).create();
		editContent = (EditText) editView.findViewById(R.id.editContent);
		editController = new EditController();
		bConfirm = (Button) editView.findViewById(R.id.bSettingConfirm);
		bConfirm.setOnClickListener(editController);
		bCancel = (Button) editView.findViewById(R.id.bSettingCancel);
		bCancel.setOnClickListener(editController);
		
		alert = new AlertDialog.Builder(this).setPositiveButton("OK", null).create();
	}
	
	private class ListController implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			String item = data.get(arg2).get("item").toString();
			editorDialog.setMessage("Please Enter "+item);
			selectedItemIndex = arg2;
			switch (selectedItemIndex) {
			case 0:
				editContent.setText(GlobalDef.conf.SERVER_IP);
				break;
			case 1:
				editContent.setText(GlobalDef.conf.SERVER_PROT);
				break;
			case 2:
				editContent.setText(String.valueOf(GlobalDef.conf.LOCAL_SERVER_PORT));
				break;
			case 3:
				editContent.setText(GlobalDef.conf.HOME);
				break;
			}
			editorDialog.show();
		}
		
	}

	private class EditController implements OnClickListener {
		
		public void onClick(View v) {
			editorDialog.dismiss();
			SharedPreferences.Editor editor;
			if (v == bConfirm) {
				switch (selectedItemIndex) {
				case 0:
					String serverIp = editContent.getText().toString();
					if (!serverIp.matches(IP_REGEX)) {
						alert.setMessage("Wrong Format Of Server IP!");
						alert.show();
					} else {
						GlobalDef.conf.SERVER_IP = serverIp;
						editor = preferences.edit();
						editor.putString("serverIp", serverIp);
						editor.commit();
					}
					break;
				case 1:
					String sServerPort = editContent.getText().toString();
					int iServerPort;
					try {
						iServerPort = Integer.parseInt(sServerPort);
					} catch (Exception e) {
						alert.setMessage("Wrong Format Of Server Port!");
						alert.show();
						return;
					}
					if (iServerPort > 65535 || iServerPort < 1) {
						alert.setMessage("Wrong Format Of Server Port!");
						alert.show();
						return;
					}
					GlobalDef.conf.SERVER_PROT = sServerPort;
					editor = preferences.edit();
					editor.putString("serverPort", sServerPort);
					editor.commit();
					break;
				case 2:
					String sLocalServerPort = editContent.getText().toString();
					int iLocalServerPort;
					try {
						iLocalServerPort = Integer.parseInt(sLocalServerPort);
					} catch (Exception e) {
						alert.setMessage("Wrong Format Of Local Server Port!");
						alert.show();
						return;
					}
					if (iLocalServerPort > 65535 || iLocalServerPort < 1) {
						alert.setMessage("Wrong Format Of Local Server Port!");
						alert.show();
						return;
					}
					GlobalDef.conf.LOCAL_SERVER_PORT = iLocalServerPort;
					editor = preferences.edit();
					editor.putInt("localSocketPort", iLocalServerPort);
					editor.commit();
					break;
				case 3:
					String archiveHome = editContent.getText().toString();
					GlobalDef.conf.HOME = archiveHome;
					editor = preferences.edit();
					editor.putString("archiveHome", archiveHome);
					editor.commit();
					break;
				}
			}
		}
		
	}
}
