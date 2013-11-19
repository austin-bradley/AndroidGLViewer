package graphics.bradley.androidglviewer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import graphics.bradley.androidglviewer.Sphere;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

public class MyGL20Renderer implements GLSurfaceView.Renderer {

	public volatile float mAngle;
	
	private final float[] mVMatrix = new float[16];
	private final float[] mProjMatrix = new float[16];
	private final float[] mNormalMat = new float[16]; 
	private final float[] mMVPMatrix = new float[16];
	private final float[] mRotationMatrix = new float[16];
	private final float[] mTemp = new float [16];

	private Sphere mSphere;

	private Cube mCube;

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig cfg) {
		// TODO Auto-generated method stub
		
		GLES20.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
		GLES20.glEnable(GL10.GL_DEPTH_TEST);
	    GLES20.glDepthFunc(GL10.GL_LEQUAL);
	    
	    mSphere = new Sphere(1.0f, 20, 40);
	    mCube = new Cube();
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		
		// Draw background.
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		
		// Set camera position (view matrix)
		Matrix.setLookAtM(mVMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
		
		// Calculate the projection and view transformation
		// temp = Proj*View; 
		Matrix.multiplyMM(mTemp, 0, mProjMatrix, 0, mVMatrix, 0);
		
		Matrix.setRotateM(mRotationMatrix,  0,  mAngle,  0,  1.0f,  0.0f);
		
		// MVP = Proj*View*Rot
		Matrix.multiplyMM(mMVPMatrix,  0,  mTemp, 0, mRotationMatrix,  0);
		
		
		// normal mat = transpose(inv(modelview)); 
		Matrix.multiplyMM(mTemp, 0, mVMatrix, 0, mRotationMatrix, 0);
		
		float [] tt = new float[16]; 
		Matrix.invertM(tt, 0, mTemp, 0);
		Matrix.transposeM(mNormalMat, 0, tt, 0);
		
		mSphere.draw(mMVPMatrix, mNormalMat, mTemp);
		mCube.draw(mMVPMatrix, mNormalMat);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		
		GLES20.glViewport(0, 0, width, height);
		
		float ratio = (float) width / height;
		
		Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 1, 7);
	}
	
	// compile GLSL code prior to using it in OpenGL ES environment
		public static int loadShader(int type, String shaderCode) {
			
			// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
			// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
			int shader = GLES20.glCreateShader(type);
			
			// add the source code to the shader and compile it
			GLES20.glShaderSource(shader, shaderCode);
			GLES20.glCompileShader(shader);
			
			return shader;
		}
}
