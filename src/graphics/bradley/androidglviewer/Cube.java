package graphics.bradley.androidglviewer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;
import android.util.Log;

public class Cube {
	 private final String vertexShaderCode =
             "uniform mat4 uMVPMatrix;" +
             "uniform mat4 uMVMatrix;"+
             "uniform mat4 uNormalMat;"+
             "attribute vec4 vPosition;" +
             "attribute vec4 vColor;" +
             "attribute vec3 vNormal;"+
             "varying vec4 varyingColor;" +
             "varying vec3 varyingPos;"+
             "varying vec3 varyingNormal;"+
             "void main() {" +
             "    varyingColor = vColor;" +
             "	  vec4 t = uNormalMat*vec4(vNormal, 0.0);"+
             "	  varyingNormal.xyz = t.xyz;"+
             " 	  t = uMVMatrix*vPosition;"+
             "    varyingPos.xyz = t.xyz;"+
             "    gl_Position = uMVPMatrix * vPosition;" +
             "}";

	 private final String fragmentShaderCode =
				"precision mediump float;" +
				"varying vec4 varyingColor; "+
				"varying vec3 varyingNormal;" +
				"varying vec3 varyingPos;" +		
				"uniform vec3 lightDir; " +
				"void main() {" +
				"   float kd = 0.9, ks = 0.9; " +
				"   vec4 light = vec4(1.0, 1.0, 1.0, 1.0); " +											
				"   vec3 Nn = normalize(varyingNormal); " +
				"   vec3 Ln = normalize(lightDir); " +
				"   vec4 diffuse = kd* light * max(dot(Nn, Ln), 0.0);" +
				"	gl_FragColor = varyingColor*diffuse;" +
				//"   gl_FragColor = varyingColor*diffuse + specular; " +
				"}";
	 
	  private FloatBuffer vertexBuffer;
      private FloatBuffer colorBuffer;
      private ShortBuffer drawListBuffer;
      private FloatBuffer normalBuffer;
     
  	  private final int mProgram;
  	  private int mPositionHandle;
  	  private int mColorHandle, mNormalHandle;
  	 private int mMVPMatrixHandle, mNormalMatHandle;
     
      // number of coordinates per vertex in this array
      static final int COORDS_PER_VERTEX = 3;

	public static final int DRAWTOP = 0;
	public static final int DRAWLEG = 1;
     
      // vertex coords array for glDrawArrays() =====================================
      // A cube has 6 sides and each side has 2 triangles, therefore, a cube consists
      // of 36 vertices (6 sides * 2 tris * 3 vertices = 36 vertices). And, each
      // vertex is 3 components (x,y,z) of floats, therefore, the size of vertex
      // array is 108 floats (36 * 3 = 108).
      static float vertices[] = { 1, 1, 1,  -1, 1, 1,  -1,-1, 1,      // v0-v1-v2 (front)
                             -1,-1, 1,   1,-1, 1,   1, 1, 1,      // v2-v3-v0

                              1, 1, 1,   1,-1, 1,   1,-1,-1,      // v0-v3-v4 (right)
                              1,-1,-1,   1, 1,-1,   1, 1, 1,      // v4-v5-v0

                              1, 1, 1,   1, 1,-1,  -1, 1,-1,      // v0-v5-v6 (top)
                             -1, 1,-1,  -1, 1, 1,   1, 1, 1,      // v6-v1-v0

                             -1, 1, 1,  -1, 1,-1,  -1,-1,-1,      // v1-v6-v7 (left)
                             -1,-1,-1,  -1,-1, 1,  -1, 1, 1,      // v7-v2-v1

                             -1,-1,-1,   1,-1,-1,   1,-1, 1,      // v7-v4-v3 (bottom)
                              1,-1, 1,  -1,-1, 1,  -1,-1,-1,      // v3-v2-v7

                              1,-1,-1,  -1,-1,-1,  -1, 1,-1,      // v4-v7-v6 (back)
                             -1, 1,-1,   1, 1,-1,   1,-1,-1 };    // v6-v5-v4

      // normal array
      static float normals[]  = { 0, 0, 1,   0, 0, 1,   0, 0, 1,      // v0-v1-v2 (front)
                              0, 0, 1,   0, 0, 1,   0, 0, 1,      // v2-v3-v0

                              1, 0, 0,   1, 0, 0,   1, 0, 0,      // v0-v3-v4 (right)
                              1, 0, 0,   1, 0, 0,   1, 0, 0,      // v4-v5-v0

                              0, 1, 0,   0, 1, 0,   0, 1, 0,      // v0-v5-v6 (top)
                              0, 1, 0,   0, 1, 0,   0, 1, 0,      // v6-v1-v0

                             -1, 0, 0,  -1, 0, 0,  -1, 0, 0,      // v1-v6-v7 (left)
                             -1, 0, 0,  -1, 0, 0,  -1, 0, 0,      // v7-v2-v1

                              0,-1, 0,   0,-1, 0,   0,-1, 0,      // v7-v4-v3 (bottom)
                              0,-1, 0,   0,-1, 0,   0,-1, 0,      // v3-v2-v7

                              0, 0,-1,   0, 0,-1,   0, 0,-1,      // v4-v7-v6 (back)
                              0, 0,-1,   0, 0,-1,   0, 0,-1 };    // v6-v5-v4

      // color array
      static float colorsTop[]   = { 0, 1, 0,   0, 1, 0,    0, 1, 0,      // v0-v1-v2 (front)
    	  						  0, 1, 0,   0, 1, 0,    0, 1, 0,      // v2-v3-v0

    	  						  0, 1, 0,   0, 1, 0,    0, 1, 0,      // v0-v3-v4 (right)
    	  						  0, 1, 0,   0, 1, 0,    0, 1, 0,      // v4-v5-v0

    	  						  0, 1, 0,   0, 1, 0,    0, 1, 0,      // v0-v5-v6 (top)
    	  						  0, 1, 0,   0, 1, 0,    0, 1, 0,      // v6-v1-v0

    	  						  0, 1, 0,   0, 1, 0,    0, 1, 0,      // v1-v6-v7 (left)
    	  						  0, 1, 0,   0, 1, 0,    0, 1, 0,      // v7-v2-v1

    	  						  0, 1, 0,   0, 1, 0,    0, 1, 0,      // v7-v4-v3 (bottom)
    	  						  0, 1, 0,   0, 1, 0,    0, 1, 0,      // v3-v2-v7

    	  						  0, 1, 0,   0, 1, 0,    0, 1, 0,      // v4-v7-v6 (back)
    	  						  0, 1, 0,   0, 1, 0,    0, 1, 0, };    // v6-v5-v4
      
   // color array
      static float colorsLegs[]   = { 0, 0, 0,   0, 0, 0,    0, 0, 0,      // v0-v1-v2 (front)
							    	  0, 0, 0,   0, 0, 0,    0, 0, 0,      // v2-v3-v0
														
							    	  0, 0, 0,   0, 0, 0,    0, 0, 0,      // v0-v3-v4 (right)
							    	  0, 0, 0,   0, 0, 0,    0, 0, 0,      // v4-v5-v0
														
							    	  0, 0, 0,   0, 0, 0,    0, 0, 0,      // v0-v5-v6 (top)
							    	  0, 0, 0,   0, 0, 0,    0, 0, 0,      // v6-v1-v0
														
							    	  0, 0, 0,   0, 0, 0,    0, 0, 0,      // v1-v6-v7 (left)
							    	  0, 0, 0,   0, 0, 0,    0, 0, 0,      // v7-v2-v1
														
							    	  0, 0, 0,   0, 0, 0,    0, 0, 0,      // v7-v4-v3 (bottom)
							    	  0, 0, 0,   0, 0, 0,    0, 0, 0,      // v3-v2-v7
														
							    	  0, 0, 0,   0, 0, 0,    0, 0, 0,      // v4-v7-v6 (back)
							    	  0, 0, 0,   0, 0, 0,    0, 0, 0, };    // v6-v5-v4

     
     
      private short drawOrder[] = { 0, 1, 2, 0, 2, 3 };      // order to draw vertices
     
      private final int vertexCount = vertices.length / COORDS_PER_VERTEX;
      private final int vertexStride = COORDS_PER_VERTEX * 4;       // bytes per vertex
     
  	  // set the light direction in the eye coordinate; 
  	  float lightDir[] = {0.0f, 1.0f, 8.0f}; 
      
  	  public static int checkShaderError(int shader) {            

             final int[] compileStatus = new int[1];

               GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

               if (compileStatus[0] == 0) {

                       Log.e("GLES Error:", "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
                       GLES20.glDeleteShader(shader);
                       return 1;

               }
               return 0;
      }
     
     
      public Cube(int mode) {
    	  
    	  	  float[] colors;
			 // Create colors to contain correct colors
			  if (mode == DRAWLEG) {
				  colors = colorsLegs.clone();
			  }
			  else if (mode == DRAWTOP) {
				  colors = colorsTop.clone();
			  }
			  else
				  colors = colorsTop.clone();
			  	 
			     // initialize vertex byte buffer for shape coordinates
			 ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
			 // use the device hardware's native byte order
			 bb.order(ByteOrder.nativeOrder());
			
			 // create a floating point buffer from the ByteBuffer
			 vertexBuffer = bb.asFloatBuffer();
			 // add the coordinates to the FloatBuffer
			 vertexBuffer.put(vertices);
			 // set the buffer to read the first coordinate
			 vertexBuffer.position(0);
			
			
			 ByteBuffer bb2 = ByteBuffer.allocateDirect(
			              // (# of color values * 4 bytes per float
			              colors.length * 4);
			 bb2.order(ByteOrder.nativeOrder());
			
			 // create a floating point buffer from the ByteBuffer
			 colorBuffer = bb2.asFloatBuffer();
			 // add the coordinates to the FloatBuffer
			 colorBuffer.put(colors);
			 // set the buffer to read the first coordinate
			 colorBuffer.position(0);
			 
			// normal buffer; 
			ByteBuffer bb3 = ByteBuffer.allocateDirect(normals.length * 4);
			bb3.order(ByteOrder.nativeOrder());
					
			normalBuffer = bb3.asFloatBuffer();
			normalBuffer.put(normals);
			normalBuffer.position(0);
			
			 // initialize byte buffer for the draw list
			 ByteBuffer dlb = ByteBuffer.allocateDirect(
			              // (# of coordinate values * 2 bytes per short
			              drawOrder.length * 2);
			 dlb.order(ByteOrder.nativeOrder());
			 drawListBuffer = dlb.asShortBuffer();
			 drawListBuffer.put(drawOrder);
			 drawListBuffer.position(0);
			
			 int vertexShader = MyGL20Renderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
			 checkShaderError(vertexShader);
			 int fragmentShader = MyGL20Renderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
			 checkShaderError(fragmentShader);
			 mProgram = GLES20.glCreateProgram();                          // create empty OpenGL ES program
			 GLES20.glAttachShader(mProgram,  vertexShader);        // add the vertex shader to program
			 GLES20.glAttachShader(mProgram, fragmentShader);       // add the fragment shader to program
			 GLES20.glLinkProgram(mProgram);                                      // creates OpenGL ES program executables
      }
     
      public void draw(float[] mvpMatrix, float[] normalMat, float[] mvMat) {    // pass in the calculated transformation matrix
             // Add program to OpenGL ES environment
             GLES20.glUseProgram(mProgram);
            
             //GLES20.glPolygonMode(GLES20.GL_FRONT,GLES20.GL_LINE);

             // get handle to vertex shader's vPosition member
             mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            
             // Enable a handle to the triangle vertices
             GLES20.glEnableVertexAttribArray(mPositionHandle);
            
             // Prepare the triangle coordinate data
             GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                                             GLES20.GL_FLOAT, false,
                                                             vertexStride, vertexBuffer);
            
             // get handle to fragment shader's vColor member
             //mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
             // Set color for drawing the triangle
             //GLES20.glUniform4fv(mColorHandle, 1, color, 0);
             int light = GLES20.glGetUniformLocation(mProgram, "lightDir");
             GLES20.glUniform3fv(light, 1, lightDir, 0);
             
             mColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor");
            
             GLES20.glEnableVertexAttribArray(mColorHandle);
                         
             // Prepare the color data
             GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
                                                                          GLES20.GL_FLOAT, false,
                                                                          vertexStride, colorBuffer);
             
            // now deal with normals
     		mNormalHandle = GLES20.glGetAttribLocation(mProgram, "vNormal");	
     		GLES20.glEnableVertexAttribArray(mNormalHandle);
     		// Prepare the normal data
     		GLES20.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX,
     															GLES20.GL_FLOAT, false,
     															vertexStride, normalBuffer);
                         
            
             // get handle to shape's transformation matrix
             mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
             
             mNormalMatHandle = GLES20.glGetUniformLocation(mProgram, "uNormalMat");
     		
     		 int MVMatHandle = GLES20.glGetUniformLocation(mProgram, "uMVMatrix");
            
             // Apply the projection and view transformation
             GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
     		 GLES20.glUniformMatrix4fv(mNormalMatHandle, 1, false, normalMat, 0);
    		 GLES20.glUniformMatrix4fv(MVMatHandle, 1, false, mvMat, 0);
             
             // Draw the triangle
             GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
            
             // Disable vertex array
             GLES20.glDisableVertexAttribArray(mPositionHandle);
             GLES20.glDisableVertexAttribArray(mColorHandle);
             GLES20.glDisableVertexAttribArray(mNormalHandle);
      
      }

}