package com.example.camerapreview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

@SuppressLint("ClickableViewAccessibility") public class CameraPreviewSampleActivity extends Activity {
	private CameraPreview mPreview;
	private RelativeLayout mLayout;

	private cameraPointerDrawable cameraPointerDrawable;
	private ImageView cameraPointer;
	private boolean cameraPointerBlink = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide status-bar
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Hide title-bar, must be before setContentView
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.camera_layout);
		mLayout = (RelativeLayout) findViewById(R.id.apps2go_receipts2go_camera_frame);
		
	
		// // Requires RelativeLayout.
		// mLayout = new RelativeLayout(this);
		// setContentView(mLayout);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set the second argument by your choice.
		// Usually, 0 for back-facing camera, 1 for front-facing camera.
		// If the OS is pre-gingerbreak, this does not have any effect.
		mPreview = new CameraPreview(this, 0,
				CameraPreview.LayoutMode.FitToParent);
		LayoutParams previewLayoutParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		previewLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		// Un-comment below lines to specify the size.
//		 previewLayoutParams.height = 500;
//		 previewLayoutParams.width = 500;

		// Un-comment below line to specify the position.
//		 mPreview.setCenterPosition(270, 130);

		mLayout.addView(mPreview, 0, previewLayoutParams);
		mPreview.setOnTouchListener(previewTouchListener);
	}

	private OnTouchListener previewTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			final int action = event.getAction();
			if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
				mLayout.removeView(cameraPointer);
				cameraPointerDrawable = new cameraPointerDrawable();
				cameraPointerDrawable.initiate(event.getX(), event.getY());
				cameraPointer = new ImageView(getApplication());
				cameraPointer.setImageDrawable(cameraPointerDrawable);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				mLayout.addView(cameraPointer, params);
				cameraPointerBlink = true;
				cameraPointerEngageAnimation();
				mPreview.getmCamera().autoFocus(afCallback);
			}

			return true;
		}
	};
	
	private AutoFocusCallback afCallback = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			cameraPointerDrawable.setFocused(true);
			cameraPointer.invalidate();
			cameraPointerBlink = false;

			animateFocusedCameraPointer();
		}

		private void animateFocusedCameraPointer() {
			Animation blink;
			blink = new AlphaAnimation(1.0f, 0.0f);
			blink.setDuration(1000);
			blink.setAnimationListener(blinkAnimationListener);
			blink.setFillAfter(true);

			cameraPointer.startAnimation(blink);
			cameraPointer.invalidate();
		}
	};
	
	private void cameraPointerEngageAnimation() {
		if(cameraPointerBlink) {
			Animation blink;
			blink = new AlphaAnimation(1.0f, 0.0f);
			blink.setDuration(200);
			blink.setAnimationListener(blinkAnimationListener);

			cameraPointer.startAnimation(blink);
			cameraPointer.invalidate();
		}
	}

	private AnimationListener blinkAnimationListener = new AnimationListener() {
		@Override
		public void onAnimationEnd(Animation animation) {
			cameraPointerEngageAnimation();
		}

		@Override
		public void onAnimationRepeat(Animation animation) {}

		@Override
		public void onAnimationStart(Animation animation) {}
	};
	
	@Override
	protected void onPause() {
		super.onPause();
		mPreview.stop();
		mLayout.removeView(mPreview); // This is necessary.
		mPreview = null;
	}

	// ////////
	private class cameraPointerDrawable extends Drawable {

		private Paint normalPaint;
		private float locX;
		private float locY;

		private final int targetSize = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, (float) 100, getResources()
						.getDisplayMetrics());

		public void initiate(float x, float y) {
			normalPaint = new Paint();
			normalPaint.setStyle(Style.STROKE);
			normalPaint.setColor(Color.RED);
			normalPaint.setStrokeWidth(4);

			locX = x;
			locY = y;
		}

		public void setFocused(boolean focused) {
			if (focused)
				normalPaint.setColor(Color.GREEN);
		}

		@Override
		public void draw(Canvas canvas) {
			canvas.drawRect(locX - targetSize / 2, locY - targetSize / 2, locX
					+ targetSize / 2, locY + targetSize / 2, normalPaint);
		}

		@Override
		public int getOpacity() {
			return 0;
		}

		@Override
		public void setAlpha(int alpha) {
			normalPaint.setAlpha(alpha);
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
		}

	}

}