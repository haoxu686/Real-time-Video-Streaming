package com.activity;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

import com.net.FetchWorker;
import com.net.MiniServer;
import com.util.GlobalDef;
import com.util.ServerException;

public class LiveVideoActivity extends Activity {

	private MiniServer miniServer;
	private Socket socket;
	private MessageController messageController;
	private SurfaceView surfaceView;
	private SurfaceHolder holder;
	private Camera camera;
	private MediaRecorder recorder;
	private boolean isRecording;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		this.setContentView(R.layout.preview);
		
		surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
		holder = surfaceView.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(new PreviewController());
		recorder = new MediaRecorder();
		isRecording = false;

		messageController = new MessageController();
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
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			params.setPreviewFrameRate(frameRate);
			params.setSceneMode(Camera.Parameters.SCENE_MODE_LANDSCAPE);
			params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
			params.setPreviewSize(320, 240);
			camera.setParameters(params);
			try {
				camera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			camera.startPreview();
			camera.unlock();
			new Thread(new CreateLiveVideoAction()).start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (isRecording) {
				recorder.stop();
			}
			recorder.release();
			camera.stopPreview();
			camera.release();
			miniServer.stop();
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			new Thread(new CompleteLiveVideoAction()).start();
		}
		
	}
	
	private class MessageController extends Handler {
		public void handleMessage(Message message) {
			switch (message.what) {
			case GlobalDef.message.CREATE_LIVE_VIDEO_OK:
				int remotePort = message.getData().getInt("remotePort");
				miniServer = new MiniServer(messageController, remotePort);
				miniServer.start();
				break;
			case GlobalDef.message.LOCAL_SERVER_STARTED:
				try {
					socket = new Socket("localhost", GlobalDef.conf.LOCAL_SERVER_PORT);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					return;
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				recorder.setCamera(camera);
				recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
				recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
				recorder.setVideoFrameRate(15);
				recorder.setVideoSize(320, 240);
				recorder.setVideoEncodingBitRate(3000000);
				ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(socket);
				recorder.setOutputFile(pfd.getFileDescriptor());
				recorder.setPreviewDisplay(holder.getSurface());
				try {
					recorder.prepare();
				} catch (IllegalStateException e) {
					e.printStackTrace();
					return;
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				recorder.start();
				isRecording = true;
				break;
			case GlobalDef.message.ERROR:
				System.out.println(message.getData().getString("message"));
			default:
				break;
			}
		}
	}
	
	private class CreateLiveVideoAction implements Runnable {

		@Override
		public void run() {
			Message message;
			int remotePort = -1;
			try {
				remotePort = FetchWorker.createLiveVideo();
			} catch (ServerException e) {
				e.printStackTrace();
				message = new Message();
				message.what = GlobalDef.message.ERROR;
				message.getData().putString("message", e.getMessage());
				messageController.sendMessage(message);
				return;
				
			}
			message = new Message();
			message.what = GlobalDef.message.CREATE_LIVE_VIDEO_OK;
			message.getData().putInt("remotePort", remotePort);
			messageController.sendMessage(message);
		}
		
	}
	
	private class CompleteLiveVideoAction implements Runnable {

		@Override
		public void run() {
			try {
				FetchWorker.completeLiveVideo();
			} catch (ServerException e) {
				e.printStackTrace();
				return;
			}
		}
		
	}

}
