package graphics.bradley.androidglviewer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

public class MyGLSurfaceView extends GLSurfaceView implements OnScaleGestureListener {
	
	private float mPrevX;
	private float mPrevY;
	
	private MyGL20Renderer mRend;
	
	private ScaleGestureDetector scaler;
	private boolean scaleMode;
	
	private final float TOUCH_SCALE_FACTOR = 180F/ 320;
	
	public MyGLSurfaceView(Context context) {
		super(context);
		
		// Setup OpenGL ES 2.0 context
		setEGLContextClientVersion(2);
		
		// Setup the Renderer
		mRend = new MyGL20Renderer();
		setRenderer(mRend);
		
		// Only draw stuff that has changed
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		
		//Setup listener for scaling
		scaler = new ScaleGestureDetector(context, this);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		
		//Pass event to scaler!
		scaler.onTouchEvent(e);
		
		//Do not handle here if we are scaling!
		if(scaleMode)
			return true;
		
		float x = e.getX();
		float y = e.getY();
		
		switch (e.getAction()) {
		case MotionEvent.ACTION_MOVE:
			
			float dx = x - mPrevX;
			float dy = y - mPrevY;
			
			// reverse direction of rotation above the mid-line
			if (y > getHeight() / 2) {
				dx = dx * -1;
			}
			
			// reverse direction of rotation to left of the mid-line
			if (x < getWidth() / 2) {
				dy = dy * -1;
			}
			
			if(Math.abs(dx) > Math.abs(dy)) {
				mRend.mXAngle += (dx) * TOUCH_SCALE_FACTOR;	// = 180.0f / 320
			}
			else {
				mRend.mYAngle += (dy) * TOUCH_SCALE_FACTOR;
			}
			
			requestRender();
		}
		
		mPrevX = x;
		mPrevY = y;
		
		return true;
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		// TODO Auto-generated method stub
		float zoom = detector.getScaleFactor();
		
		if (zoom > 1f) // We are zooming out
			mRend.mZoom += .2f;
		else
			mRend.mZoom -= .2f;
		
		// Make sure zoom is in a reasonable range!
		if (mRend.mZoom < -15f)
			mRend.mZoom = -15f;
		else if (mRend.mZoom > -3f)
			mRend.mZoom = -3f;
		
		requestRender();
		
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		
		scaleMode = true;
		
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		
		scaleMode = false;
		
	}

}
