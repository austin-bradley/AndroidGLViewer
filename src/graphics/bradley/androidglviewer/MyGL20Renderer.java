package graphics.bradley.androidglviewer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import graphics.bradley.androidglviewer.Sphere;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

public class MyGL20Renderer implements GLSurfaceView.Renderer {

	public volatile float mXAngle;
	public volatile float mYAngle;
	public volatile float mZoom;
	
	private final float[] mVMatrix = new float[16];
	private final float[] mProjMatrix = new float[16];
	private final float[] mNormalMatrix = new float[16]; 
	private final float[] mMVPMatrix = new float[16];
	private final float[] mRotationMatrixX = new float[16];
	private final float[] mRotationMatrixY = new float[16];
	private final float[] mPVMatrix = new float [16];
	private final float[] mTempMatrix = new float[16];
	private final float[] mMVMatrix = new float[16];

	private Sphere mBall1;	// Three pool balls
	private Sphere mBall2;
	private Sphere mBall3;
	private Cube mTableTop; // Top of pool table and all legs
	private Cube mLeg1;
	private Cube mLeg2;
	private Cube mLeg3;
	private Cube mLeg4;
	private Cube mRail1;
	private Cube mRail2;
	private Cube mRail3;
	private Cube mRail4;

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig cfg) {
		// TODO Auto-generated method stub
		
		GLES20.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
		GLES20.glEnable(GL10.GL_DEPTH_TEST);
	    GLES20.glDepthFunc(GL10.GL_LEQUAL);
	    
	    //Set default zoom level.
	    mZoom = -6f;
	    
	    mTableTop = new Cube(Cube.DRAWTOP);
	    mLeg1 = new Cube(Cube.DRAWLEG);
	    mLeg2 = new Cube(Cube.DRAWLEG);
	    mLeg3 = new Cube(Cube.DRAWLEG);
	    mLeg4 = new Cube(Cube.DRAWLEG);
	    mRail1 = new Cube(Cube.DRAWLEG);
	    mRail2 = new Cube(Cube.DRAWLEG);
	    mRail3 = new Cube(Cube.DRAWLEG);
	    mRail4 = new Cube(Cube.DRAWLEG);
	    
	    mBall1 = new Sphere(1,30,60);
	    mBall2 = new Sphere(1,30,60);
	    mBall3 = new Sphere(1,30,60);
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		
		// Draw background.
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
		
		// Set camera position (view matrix)
		Matrix.setLookAtM(mVMatrix, 0, 0, 0, mZoom, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
		
		// PV Matrix
		Matrix.multiplyMM(mPVMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
		
		// Rotation for x axis
		Matrix.setRotateM(mRotationMatrixX,  0,  mXAngle,  0,  1.0f,  0f);
		
		//Rotation for y axis
		Matrix.setRotateM(mRotationMatrixY, 0, mYAngle, 1.0f, 0, 0);
		
		// Apply both rotations in sequence.
		
		// MVP = Proj*View*Rot
		Matrix.multiplyMM(mTempMatrix,  0,  mPVMatrix, 0, mRotationMatrixX,  0);
		Matrix.multiplyMM(mMVPMatrix, 0, mTempMatrix, 0, mRotationMatrixY, 0);
		
		// Rotation of View Matrix
		Matrix.multiplyMM(mTempMatrix, 0, mVMatrix, 0, mRotationMatrixX, 0);
		Matrix.multiplyMM(mMVMatrix, 0, mTempMatrix, 0, mRotationMatrixY, 0);
		
		//Normal matrix = transpose(inv(modelview)) 
		Matrix.invertM(mTempMatrix, 0, mMVMatrix, 0);
		Matrix.transposeM(mNormalMatrix, 0, mTempMatrix, 0);
		
		drawTable();
		drawBalls();
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		
		GLES20.glViewport(0, 0, width, height);
		
		float ratio = (float) width / height;
		
		Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 1, 20);
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
		
		private void drawTable(){
			float[] scalerMatrix = new float[16];
			float[] finalMVPMatrix = new float[16];
			
			//Draw top
			Matrix.setIdentityM(scalerMatrix, 0);
			Matrix.scaleM(scalerMatrix, 0, 2.25f, 0.1f, 1.0f);
			
			Matrix.multiplyMM(finalMVPMatrix, 0, mMVPMatrix, 0, scalerMatrix, 0);
			mTableTop.draw(finalMVPMatrix, mNormalMatrix, mMVMatrix);
			
			float[] tempMatrix = new float[16];
			// Draw legs
			Matrix.setIdentityM(scalerMatrix,  0);
			Matrix.scaleM(scalerMatrix, 0, .12f, .85f, .12f);
			Matrix.multiplyMM(tempMatrix, 0, mMVPMatrix, 0, scalerMatrix, 0);
			
			
			//First leg
			Matrix.translateM(finalMVPMatrix, 0, tempMatrix, 0, 18f, -.9f, -7.5f);
			mLeg1.draw(finalMVPMatrix, mNormalMatrix, mMVMatrix);
			
			//Second leg
			Matrix.translateM(finalMVPMatrix, 0, tempMatrix, 0, 18f, -.9f, 7.5f);
			mLeg2.draw(finalMVPMatrix, mNormalMatrix, mMVMatrix);
			
			//Third leg
			Matrix.translateM(finalMVPMatrix, 0, tempMatrix, 0, -18f, -.9f, -7.5f);
			mLeg3.draw(finalMVPMatrix, mNormalMatrix, mMVMatrix);
			
			//Fourth leg
			Matrix.translateM(finalMVPMatrix, 0, tempMatrix, 0, -18f, -.9f, 7.5f);
			mLeg4.draw(finalMVPMatrix, mNormalMatrix, mMVMatrix);
			
			// Draw Rails
			
			//Long Rails
			Matrix.setIdentityM(scalerMatrix,  0);
			Matrix.scaleM(scalerMatrix, 0, 2.30f, .25f, .1f);
			Matrix.multiplyMM(tempMatrix, 0, mMVPMatrix, 0, scalerMatrix, 0);
			
			
			//First rail
			Matrix.translateM(finalMVPMatrix, 0, tempMatrix, 0, 0, .5f, -9.1f);
			mRail1.draw(finalMVPMatrix, mNormalMatrix, mMVMatrix);
			
			//Second rail
			Matrix.translateM(finalMVPMatrix, 0, tempMatrix, 0, 0, .5f, 9.1f);
			mRail2.draw(finalMVPMatrix, mNormalMatrix, mMVMatrix);
			
			//Short Rails
			Matrix.setIdentityM(scalerMatrix,  0);
			Matrix.scaleM(scalerMatrix, 0, .1f, .25f, 1.0f);
			Matrix.multiplyMM(tempMatrix, 0, mMVPMatrix, 0, scalerMatrix, 0);
			
			//Third rail
			Matrix.translateM(finalMVPMatrix, 0, tempMatrix, 0, 22.1f, .5f, 0f);
			mRail3.draw(finalMVPMatrix, mNormalMatrix, mMVMatrix);
			
			//Fourth rail
			Matrix.translateM(finalMVPMatrix, 0, tempMatrix, 0, -22.1f, .5f, 0f);
			mRail4.draw(finalMVPMatrix, mNormalMatrix, mMVMatrix);
		}
		
		private void drawBalls() {
			float[] scalerMatrix = new float[16];
			float[] finalMVPMatrix = new float[16];
			float[] tempMatrix = new float[16];
			
			Matrix.setIdentityM(scalerMatrix, 0);
			Matrix.scaleM(scalerMatrix, 0, .1f, 0.1f, .1f);
			Matrix.multiplyMM(tempMatrix, 0, mMVPMatrix, 0, scalerMatrix, 0);
			
			Matrix.translateM(finalMVPMatrix, 0, tempMatrix, 0, -5.25f, 1.85f, .35f);
			mBall1.draw(finalMVPMatrix, mNormalMatrix, mMVMatrix);
			
			Matrix.translateM(finalMVPMatrix, 0, tempMatrix, 0, 2.25f, 1.85f, -1f);
			mBall2.draw(finalMVPMatrix, mNormalMatrix, mMVMatrix);
			
			Matrix.translateM(finalMVPMatrix, 0, tempMatrix, 0, .35f, 1.85f, -2.5f);
			mBall3.draw(finalMVPMatrix, mNormalMatrix, mMVMatrix);
			
			
		}
}
