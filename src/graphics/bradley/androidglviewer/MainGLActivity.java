package graphics.bradley.androidglviewer;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MainGLActivity extends Activity {
	
	private GLSurfaceView mGLView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGLView = new MyGLSurfaceView(this);
		setContentView(mGLView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_gl, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	        /*
	 * The activity must call the GL surface view's
	 * onResume() on activity onResume().
	 */
        if (mGLView != null) {
            mGLView.onResume();
        }
    }
	 
    @Override
    protected void onPause() {
        super.onPause();
 
        /*
		 * The activity must call the GL surface view's
		 * onPause() on activity onPause().
		 */
		    if (mGLView != null) {
		        mGLView.onPause();
		    }
		}

}
