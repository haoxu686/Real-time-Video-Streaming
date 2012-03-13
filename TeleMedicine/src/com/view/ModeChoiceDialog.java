package com.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.AdapterView.OnItemClickListener;

import com.activity.CameraActivity;
import com.activity.R;
import com.util.GlobalDef;

public class ModeChoiceDialog extends Dialog {

	private CameraActivity activity;
	private ArrayList<String> modes;
	private ArrayList<Boolean> listStates;
	private ModeAdapter adapter;
	private ListView listMode;
	private Handler messageHandler;
	
	public ModeChoiceDialog(Context context, int theme, Handler messageHandler) {
		super(context, theme);
		activity = (CameraActivity) context;
		this.messageHandler = messageHandler;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.camera_mode_choice);
		Display display = this.getWindow().getWindowManager().getDefaultDisplay();
		int screenWidth = display.getWidth();
		int screenHeight = display.getHeight();
		int width = screenWidth*3/4;
		int height = screenHeight*3/4;
		WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
		layoutParams.x = 0;
		layoutParams.y = 0;
		layoutParams.width = width;
		layoutParams.height = height;
		this.getWindow().setAttributes(layoutParams);
		
		listStates = new ArrayList<Boolean>();
		listStates.add(true);
		listStates.add(false);
		modes = new ArrayList<String>();
		modes.add("Photo");
		modes.add("Video");
		adapter = new ModeAdapter(modes);
		listMode = (ListView) this.findViewById(R.id.listMode);
		listMode.setAdapter(adapter);
		listMode.setOnItemClickListener(new ListViewController());
	}

	private class ListViewController implements OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			for (int i = 0; i < listStates.size(); i++) {
				listStates.set(i, false);
			}
			listStates.set(arg2, true);
			adapter.notifyDataSetChanged();
			Message message = new Message();
			message.what = GlobalDef.message.CAMERA_MODE_CHANGED;
			message.getData().putString("mode", modes.get(arg2));
			messageHandler.sendMessage(message);
		}
		
	}
	
	private class ModeAdapter extends ArrayAdapter<String> {

		public ModeAdapter(List<String> objects) {
			super(activity, R.layout.mode_item, R.id.textModeItem, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			RadioGroup rg = (RadioGroup) view.findViewById(R.id.rgModeItem);
			if (listStates.get(position)) {
				View rb = rg.getChildAt(0);
				rg.check(rb.getId());
			} else {
				rg.clearCheck();
			}
			return view;
		}
	}

}
