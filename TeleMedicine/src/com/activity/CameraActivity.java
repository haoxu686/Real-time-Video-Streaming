package com.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;

import com.util.GlobalDef;
import com.view.ModeChoiceDialog;

public class CameraActivity extends Activity {

	private SurfaceView surfaceView;
	private SurfaceHolder holder;
	private Camera camera;
	private MediaRecorder recorder;
	private ModeChoiceDialog dlgModeChoice;
	private String state;
	private String doing;
	private MessageController messageController;
	private Timer timer;
	private SimpleDateFormat sdf;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		this.setContentView(R.layout.preview);
		
		surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
		SurfaceController surfaceController = new SurfaceController();
		surfaceView.setOnClickListener(surfaceController);
		surfaceView.setOnLongClickListener(surfaceController);
		surfaceView.setOnKeyListener(surfaceController);
		surfaceView.setFocusable(true);
		surfaceView.setFocusableInTouchMode(true);
		
		holder = surfaceView.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(new PreviewController());
		
		recorder = new MediaRecorder();
		
		messageController = new MessageController();
		dlgModeChoice = new ModeChoiceDialog(this, R.style.Transparent, messageController);
		
		sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	}
	
	private class SurfaceController implements OnClickListener, OnLongClickListener, OnKeyListener {

		@Override
		public void onClick(View arg0) {
			if (doing.equals("Idle")) {
				camera.autoFocus(null);
			}
		}

		@Override
		public boolean onLongClick(View v) {
			if (!doing.equals("Idle")) {
				return true;
			}
			dlgModeChoice.show();
			timer = new Timer();
			timer.schedule(new DialogExpTask(), 5000);
			return true;
		}

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode != KeyEvent.KEYCODE_DPAD_CENTER) {
				return false;
			}
			if (event.getAction() != KeyEvent.ACTION_UP) {
				return false;
			}
			if (state.equals("Photo")) {
				doing = "Photo";
				camera.takePicture(null, null, new PictureCallback() {
					@Override
					public void onPictureTaken(byte[] data, Camera camera) {
						String timeStamp = sdf.format(new Date(System.currentTimeMillis()));
						File file = new File(GlobalDef.conf.HOME+GlobalDef.conf.IMAGE_PATH, timeStamp+".jpg");
						try {
							FileOutputStream fos = new FileOutputStream(file);
							fos.write(data);
							fos.close();
						} catch (IOException e1) {
							e1.printStackTrace();
							return;
						}
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
							return;
						}
						camera.startPreview();
						doing = "Idle";
					}
				});
			} else if (state.equals("Video")) {
				if (doing.equals("Idle")) {
					camera.unlock();
					recorder.setCamera(camera);
					recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
					recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
					recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
					recorder.setVideoFrameRate(15);
					recorder.setVideoSize(320, 240);
					recorder.setVideoEncodingBitRate(3000000);
					recorder.setOutputFile(GlobalDef.conf.HOME+GlobalDef.conf.VIDEO_PATH+sdf.format(new Date(System.currentTimeMillis()))+".3gp");
					recorder.setPreviewDisplay(holder.getSurface());
					try {
						recorder.prepare();
					} catch (IllegalStateException e) {
						e.printStackTrace();
						return false;
					} catch (IOException e) {
						e.printStackTrace();
						return false;
					}
					recorder.start();
					doing = "Video";
				} else if (doing.equals("Video")) {
					recorder.stop();
					camera.lock();
					doing = "Idle";
				}
				
			}
			return false;
		}
		
	}
	
	private class PreviewController implements SurfaceHolder.Callback {

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			camera = Camera.open();
			Camera.Parameters params = camera.getParameters();
			List<Integer> fpsRange = params.getSupportedPreviewFrameRates();
			int frameRate = fpsRange.get(fpsRange.size()-1);
			params.setPreviewFrameRate(frameRate);
			params.setSceneMode(Camera.Parameters.SCENE_MODE_LANDSCAPE);
			params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
			params.setPreviewSize(320, 240);
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			camera.setParameters(params);
			try {
				camera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			camera.startPreview();
			state = "Photo";
			doing = "Idle";
			surfaceView.requestFocus();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (doing.equals("Video")) {
				recorder.stop();
				camera.lock();
			}
			recorder.release();
			camera.stopPreview();
			camera.release();
		}
		
	}
	
	private class MessageController extends Handler {
		
		public void handleMessage(Message message) {
			switch (message.what) {
			case GlobalDef.message.CAMERA_MODE_CHANGED:
				state = message.getData().getString("mode");
				System.out.println(state);
				timer.cancel();
				timer = new Timer();
				timer.schedule(new DialogExpTask(), 3000);
				break;
			}
		}
	}
	
	private class DialogExpTask extends TimerTask {

		@Override
		public void run() {
			dlgModeChoice.dismiss();
		}
		
	}

}
