package com.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ZoomControls;

public class ImageDisplayActivity extends Activity {

	String path;
	private ImageView imageView;
	private Bitmap orginImage;
	private Bitmap curImage;
	private int curScale;
	private static final int MAX_PIX = 1200000;
	private static final int PAN_MULTIPLE = 3;
	private ZoomControls zoomControls;
	private Matrix imageMatrix;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		/**
		 * Initialize UI
		 */
		this.setContentView(R.layout.image_display);
		this.setTitle("Image");
		String path = this.getIntent().getExtras().getString("path");
		imageView = (ImageView) this.findViewById(R.id.imageView);
		imageView.setScaleType(ImageView.ScaleType.MATRIX);
		
		/**
		 * Load image from local file system
		 */
		orginImage = BitmapFactory.decodeFile(path);
		imageView.setImageBitmap(orginImage);
		imageMatrix = imageView.getImageMatrix();
		curImage = orginImage;
		curScale = 0;
		imageView.setOnTouchListener(new PanController());
		
		/**
		 * Initialize zoom controller
		 */
		zoomControls = (ZoomControls) this.findViewById(R.id.zoomControls);
		zoomControls.setIsZoomInEnabled(true);
		zoomControls.setIsZoomOutEnabled(true);
		zoomControls.setOnZoomInClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				curScale++;
				float scale = (float) Math.pow(1.25, curScale);
				float size = orginImage.getWidth()*orginImage.getHeight()*scale*scale;
				if (size > MAX_PIX) {
					curScale--;
					return;
				}
				imageMatrix = new Matrix();
				imageMatrix.postScale(scale, scale);
				curImage = Bitmap.createBitmap(orginImage, 0, 0, orginImage.getWidth(), orginImage.getHeight(), imageMatrix, true);
				imageView.setImageBitmap(curImage);
			}
		});
		zoomControls.setOnZoomOutClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				curScale--;
				float scale = (float) Math.pow(1.25, curScale);
				imageMatrix = new Matrix();
				imageMatrix.postScale(scale, scale);
				curImage = Bitmap.createBitmap(orginImage, 0, 0, orginImage.getWidth(), orginImage.getHeight(), imageMatrix, true);
				imageView.setImageBitmap(curImage);
			}
		});
	}
	
	private class PanController implements OnTouchListener {

		private boolean down = false;
		private float oldX = -1;
		private float oldY = -1;
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			switch (arg1.getAction()) {
			case MotionEvent.ACTION_DOWN:
				down = true;
				break;
			case MotionEvent.ACTION_UP:
				down = false;
				oldX = -1;
				oldY = -1;
				break;
			case MotionEvent.ACTION_MOVE:
				if (!down) {
					break;
				}
				float newX = arg1.getX();
				float newY = arg1.getY();
				if (oldX < 0 || oldY < 0) {
					oldX = newX;
					oldY = newY;
					break;
				}
				int offsetX = (int) (oldX-newX)*PAN_MULTIPLE;
				int offsetY = (int) (oldY-newY)*PAN_MULTIPLE;
				int leftBound = imageView.getScrollX()+offsetX;
				int rightBound = leftBound+imageView.getWidth();
				int topBound = imageView.getScrollY()+offsetY;
				int bottomBound = topBound+imageView.getHeight();
				if (leftBound < 0) {
					offsetX = -imageView.getScrollX();
				}
				if (rightBound > curImage.getWidth()) {
					offsetX = curImage.getWidth()-imageView.getScrollX()-imageView.getWidth();
				}
				if (topBound < 0) {
					offsetY = -imageView.getScrollY();
				}
				if (bottomBound > curImage.getHeight()) {
					offsetY = curImage.getHeight()-imageView.getScrollY()-imageView.getHeight();
				}
				imageView.scrollBy(offsetX, offsetY);
				oldX = newX;
				oldY = newY;
				break;
			default:
				break;
			}
			return true;
		}
	}
}
