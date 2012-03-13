package com.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoDisplayActivity extends Activity {

	String path;
	private VideoView videoView;
	private MediaController mediaController;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.video_display);
		
		path = this.getIntent().getExtras().getString("path");
		videoView = (VideoView) this.findViewById(R.id.videoView);
		mediaController = new MediaController(this);
		videoView.setVideoPath(path);
		videoView.setMediaController(mediaController);
		mediaController.setAnchorView(videoView);
		videoView.requestFocus();
		videoView.start();
	}
}
