package graphics.bradley.androidglviewer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;
import android.util.Log;

// square shape to be drawn in the context of an OpenGL ES view
public class Sphere {

	private final String vertexShaderCode =
			"uniform mat4 uMVPMatrix, uMVMatrix, uNormalMat;" +
			"attribute vec4 vPosition;" +
			"attribute vec4 vColor;" +
			"attribute vec3 vNormal;" + 
			"varying vec4 varyingColor; varying vec3 varyingNormal; " +
			"varying vec3 varyingPos; " + 
			"void main() {" +
			"	varyingColor = vColor;" +
			"	vec4 t = uNormalMat*vec4(vNormal, 0.0);" +
			"   varyingNormal.xyz = t.xyz; "+
			"   t = uMVMatrix*vPosition;"+ 
			"   varyingPos.xyz = t.xyz; "+
			"   gl_Position =    uMVPMatrix  * vPosition ;" +
			"}";
	// fragment code to perform diffuse + specular shading; 
	private final String fragmentShaderCode =
			"precision mediump float;" +
			"varying vec4 varyingColor; varying vec3 varyingNormal;" +
			"varying vec3 varyingPos;" +		
			"uniform vec3 lightDir; " +
			"void main() {" +
			"	float Ns = 100.0;  "+ // for shiness; 
			"   float kd = 0.9, ks = 0.9; " +
			"   vec4 light = vec4(1.0, 1.0, 1.0, 1.0); " +
			"   vec4 lightS = vec4(1.0, 1.0, 1.0, 1.0); " + // the specular highlight is redish
			"   vec3 Nn = normalize(varyingNormal); " +
			"   vec3 Ln = normalize(lightDir); " +
			"   vec4 diffuse = kd* light * max(dot(Nn, Ln), 0.0); " +
			"   vec3 Ref = reflect(Nn, Ln); " + // get the reflectance vector; 
			"   float dotV = max(dot(Ref, normalize(varyingPos)), 0.0); " + // since we are in eye space, the eye position is at 0, 0, 0, so the view direction is simply (varyingPos- (0, 0, 0))
			"   vec4 specular = lightS*ks*pow(dotV, Ns); " +
			"	gl_FragColor = varyingColor*diffuse + specular; " +
			//"   gl_FragColor = varyingColor*diffuse + specular; " +
			"}";
	
	private FloatBuffer vertexBuffer;
	private FloatBuffer colorBuffer; 
	private FloatBuffer normalBuffer; 
	private ShortBuffer drawListBuffer;
	
	private final int mProgram;
	private int mPositionHandle;
	private int mColorHandle, mNormalHandle;
	private int mMVPMatrixHandle, mNormalMatHandle;
	
	// number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 3;
	
	// vertex coords array for glDrawArrays() =====================================
	// A cube has 6 sides and each side has 2 triangles, therefore, a cube consists
	// of 36 vertices (6 sides * 2 tris * 3 vertices = 36 vertices). And, each
	// vertex is 3 components (x,y,z) of floats, therefore, the size of vertex
	// array is 108 floats (36 * 3 = 108).
	private float [] vertices; 

	// normal array
	private float [] normals; 
	// color array
	static float [] colors; 
	
	
	private short drawOrder[] = { 0, 1, 2, 0, 2, 3 };	// order to draw vertices
	
	private int vertexCount; 
	private final int vertexStride = COORDS_PER_VERTEX * 4;	// bytes per vertex
	
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
	
	
	private void createSphere(double r, int lats, int longs) {
		        int i, j;
		        
		        
		        // there are lats*longs number of quads, each requires two triangles with six vertices, each vertex takes 3 floats; 
		        vertices = new float[lats*longs*6*3];
		        normals = new float[lats*longs*6*3]; 
		        colors = new float[lats*longs*6*3]; 
		        
		        vertexCount = vertices.length / COORDS_PER_VERTEX;
		        int triIndex = 0; 
		        for(i = 0; i < lats; i++) {
		            double lat0 = Math.PI * (-0.5 + (double) (i) / lats);
		           double z0  = Math.sin(lat0);
		           double zr0 =  Math.cos(lat0);
		    
		           double lat1 = Math.PI * (-0.5 + (double) (i+1) / lats);
		           double z1 = Math.sin(lat1);
		           double zr1 = Math.cos(lat1);
		    
		           
		           //glBegin(GL_QUAD_STRIP);
		           for(j = 0; j < longs; j++) {
		               double lng = 2 * Math.PI * (double) (j - 1) / longs;
		               double x = Math.cos(lng);
		               double y = Math.sin(lng);
		    
		               
		               lng = 2 * Math.PI * (double) (j) / longs;
		               double x1 = Math.cos(lng);
		               double y1 = Math.sin(lng);
		               
		               //glNormal3f(x * zr0, y * zr0, z0);
		               //glVertex3f(x * zr0, y * zr0, z0);
		               //glNormal3f(x * zr1, y * zr1, z1);
		               //glVertex3f(x * zr1, y * zr1, z1);
		               
		               // the first triangle
		               vertices[triIndex*9 + 0 ] = (float)(x * zr0);    vertices[triIndex*9 + 1 ] = (float)(y * zr0);   vertices[triIndex*9 + 2 ] = (float) z0;
		               vertices[triIndex*9 + 3 ] = (float)(x * zr1);    vertices[triIndex*9 + 4 ] = (float)(y * zr1);   vertices[triIndex*9 + 5 ] = (float) z1;
		               vertices[triIndex*9 + 6 ] = (float)(x1 * zr0);   vertices[triIndex*9 + 7 ] = (float)(y1 * zr0);  vertices[triIndex*9 + 8 ] = (float) z0;
		               
		               triIndex ++; 
		               vertices[triIndex*9 + 0 ] = (float)(x1 * zr0);   vertices[triIndex*9 + 1 ] = (float)(y1 * zr0);  	vertices[triIndex*9 + 2 ] = (float) z0;
		               vertices[triIndex*9 + 3 ] = (float)(x * zr1);    vertices[triIndex*9 + 4 ] = (float)(y * zr1);   	vertices[triIndex*9 + 5 ] = (float) z1;  
		               vertices[triIndex*9 + 6 ] = (float)(x1 * zr1);    vertices[triIndex*9 + 7 ] = (float)(y1 * zr1); 	vertices[triIndex*9 + 8 ] = (float) z1;
		               
		               // in this case, the normal is the same as the vertex, plus the normalization; 
		               for (int kk = -9; kk<9 ; kk++) {
		            	   normals[triIndex*9 + kk] = vertices[triIndex*9+kk];
		            	   if((triIndex*9 + kk)%3 == 2)
		            		   colors[triIndex*9 + kk] = 1;
		            	   else
		            		   colors[triIndex*9 + kk] = 0;		            	   
		               }		               
		               triIndex ++; 
		           }
		           //glEnd();
		       }
	}
	
	public Sphere(double r, int lats, int longs) {
		
		// first create the sphere data; 
		createSphere(r, lats, longs); 
		
		// initialize vertex byte buffer for shape coordinates
		ByteBuffer bb = ByteBuffer.allocateDirect(
				// (# of coordinate values * 4 bytes per float
				vertices.length * 4);
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
		mProgram = GLES20.glCreateProgram();				// create empty OpenGL ES program
		GLES20.glAttachShader(mProgram,  vertexShader);		// add the vertex shader to program
		GLES20.glAttachShader(mProgram, fragmentShader);	// add the fragment shader to program
		GLES20.glLinkProgram(mProgram);						// creates OpenGL ES program executables
		
	}
	
	
	
	public void draw(float[] mvpMatrix, float [] normalMat, float [] mvMat) {	// pass in the calculated transformation matrix
		
		//
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
		
		// get handle to fragment shader's light direction
		int light = GLES20.glGetUniformLocation(mProgram, "lightDir");
		
		// Set color for drawing the triangle
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
		//GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexCount);
		
		//for (int i = 0; i< vertexCount; i+=3) 
			//GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, i, 3);
		
		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mColorHandle);
		GLES20.glDisableVertexAttribArray(mNormalHandle);
	}
}

