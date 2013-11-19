package graphics.bradley.androidglviewer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class MyGLSurfaceView extends GLSurfaceView {
	
	private float mPrevX;
	private float mPrevY;
	
	private MyGL20Renderer mRend;
	
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
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		
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
			
			mRend.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;	// = 180.0f / 320
			requestRender();
		}
		
		mPrevX = x;
		mPrevY = y;
		
		return true;
	}

}
