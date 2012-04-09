package com.ythen.noob3d;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

public class CustomRenderer implements Renderer {

	private final String tag = "CustomRenderer";

	private float[] mModelMatrix = new float[16];
	private float[] mViewMatrix = new float[16];
	private float[] mProjectionMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];
	private float[] mLightModelMatrix = new float[16];

	private int mMVPMatrixHandle;
	private int mMVMatrixHandle;
	private int mPositionHandle;
	private int mColorHandle;
	private int mLightPositionHandle;
	private int mNormalHandle;

	private int mBytesPerFloat = 4;

	private final int mPositionDataSize = 3;
	private final int mColorDataSize = 4;
	private final int mNormalDataSize = 3;

	private final FloatBuffer mCubePositions;
	private final FloatBuffer mCubeColors;
	private final FloatBuffer mCubeNormals;

	// Light position in model space, 4th coordinate is for translation
	// purposes, all matrix in GLES is 4x4
	private final float[] mLightPositionInModelSpace = new float[] { 0.0f,
			0.0f, 0.0f, 1.0f };

	// Used to store the current light position (after transformation via model
	// matrix)
	private final float[] mLightPositionInWorldSpace = new float[4];

	// Used to store the transformed light position in eye space (after
	// transformation via model view matrix
	private final float[] mLightPositionInEyeSpace = new float[4];

	// Handle to our per vertex cube shading program
	private int mPerVertexProgramHandle;

	// Handle to light point program
	private int mPointProgramHandle;

	public CustomRenderer() {
		// TODO Auto-generated constructor stub

		// Define points for a cube.

		// X, Y, Z
		final float[] cubePositionData = {
				// In OpenGL counter-clockwise winding is default. This means
				// that when we look at a triangle,
				// if the points are counter-clockwise we are looking at the
				// "front". If not we are looking at
				// the back. OpenGL has an optimization where all back-facing
				// triangles are culled, since they
				// usually represent the backside of an object and aren't
				// visible anyways.

				// Front face
				-1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
				-1.0f, 1.0f, 1.0f, -1.0f,
				1.0f,
				1.0f,
				1.0f,
				1.0f,

				// Right face
				1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f,
				-1.0f, 1.0f, 1.0f, -1.0f, -1.0f,
				1.0f,
				1.0f,
				-1.0f,

				// Back face
				1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f,
				1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,
				1.0f,
				-1.0f,

				// Left face
				-1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
				-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f,
				1.0f,

				// Top face
				-1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
				-1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,

				// Bottom face
				1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f,
				1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f };

		// R, G, B, A
		final float[] cubeColorData = {
				// Front face (red)
				1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 1.0f, 0.0f,
				0.0f,
				1.0f,
				1.0f,
				0.0f,
				0.0f,
				1.0f,
				1.0f,
				0.0f,
				0.0f,
				1.0f,

				// Right face (green)
				0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f,
				0.0f,
				1.0f,
				0.0f,
				1.0f,
				0.0f,
				1.0f,
				0.0f,
				1.0f,

				// Back face (blue)
				0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
				1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
				1.0f,
				1.0f,
				0.0f,
				0.0f,
				1.0f,
				1.0f,

				// Left face (yellow)
				1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f,
				0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
				1.0f,
				1.0f,
				0.0f,
				1.0f,

				// Top face (cyan)
				0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
				1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f,
				0.0f, 1.0f, 1.0f,
				1.0f,

				// Bottom face (magenta)
				1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
				1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
				1.0f, 0.0f, 1.0f, 1.0f };

		// X, Y, Z
		// The normal is used in light calculations and is a vector which points
		// orthogonal to the plane of the surface. For a cube model, the normals
		// should be orthogonal to the points of each face.
		final float[] cubeNormalData = {
				// Front face
				0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f, 0.0f,
				1.0f,
				0.0f,
				0.0f,
				1.0f,

				// Right face
				1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
				1.0f,
				0.0f,
				0.0f,

				// Back face
				0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
				0.0f,
				-1.0f,

				// Left face
				-1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f,
				0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
				0.0f,

				// Top face
				0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,

				// Bottom face
				0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
				-1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f };

		// Allocate the size of the float buffers for triangles' vertices info
		mCubePositions = ByteBuffer
				.allocateDirect(cubePositionData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mCubeColors = ByteBuffer
				.allocateDirect(cubeColorData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mCubeNormals = ByteBuffer
				.allocateDirect(cubeNormalData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();

		// Put vertices data into float buffers
		mCubePositions.put(cubePositionData).position(0);
		mCubeColors.put(cubeColorData).position(0);
		mCubeNormals.put(cubeNormalData).position(0);
	}

	protected String getVertexShader() {
		final String vertexShader = "uniform mat4 u_MVPMatrix;	\n"
				+ "uniform mat4 u_MVMatrix;	\n"
				+ "uniform vec3 u_LightPos;	\n"
				+ "attribute vec4 a_Position;	\n"
				+ "attribute vec4 a_Color;	\n"
				+ "attribute vec3 a_Normal;	\n"
				+ "varying vec4 v_Color;	\n"
				+ "void main() {	\n"
				+
				// Vertex position in eye space
				"	vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);	\n"
				+
				// Surface normal in eye space
				"	vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0));	\n"
				+
				// Distance between light source and vertex
				"	float distance = length(u_LightPos - modelViewVertex);	\n"
				+
				// Unit vector from light source to vertex
				"	vec3 lightVector = normalize(u_LightPos - modelViewVertex);	\n"
				+
				// Diffuse coefficient from dot product
				"	float diffuse = max(dot(modelViewNormal, lightVector), 0.5);	\n"
				+
				// Attenuation (damped)
				"	diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));	\n"
				+ "	v_Color = a_Color * diffuse;	\n"
				+ "	gl_Position = u_MVPMatrix * a_Position;	\n" + "}	\n";
		return vertexShader;
	}

	protected String getFragmentShader() {
		final String fragmentShader = "precision mediump float;	\n"
				+ "varying vec4 v_Color;	\n" + "void main() {	\n"
				+ "	gl_FragColor = v_Color;	\n" + "}	\n";
		return fragmentShader;
	}

	public void onDrawFrame(GL10 arg0) {
		// TODO Auto-generated method stub
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

		// Do a complete rotation every 10 seconds
		long time = SystemClock.uptimeMillis() % 10000L;
		float angleInDegrees = (360.0f / 10000.0f) * (int) time;

		GLES20.glUseProgram(mPerVertexProgramHandle);

		// Set program handles for cube drawing
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle,
				"u_MVPMatrix");
		mMVMatrixHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle,
				"u_MVMatrix");
		mLightPositionHandle = GLES20.glGetUniformLocation(
				mPerVertexProgramHandle, "u_LightPos");
		mPositionHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle,
				"a_Position");
		mColorHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle,
				"a_Color");
		mNormalHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle,
				"a_Normal");

		// Push light source to -5 in z axis, rotate in accordingly, then push 2
		// unit in z axis
		// Outcome, light source is circling with origin at -5 in a radius of 2
		Matrix.setIdentityM(mLightModelMatrix, 0);
		Matrix.translateM(mLightModelMatrix, 0, 0, 0, -5);
		Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0, 1, 0);
		Matrix.translateM(mLightModelMatrix, 0, 0, 0, 2);

		// Initially, light is at its own model space (0, 0, 0), after
		// transformation, the light is positioned somewhere in the world
		// depending on the model matrix
		Matrix.multiplyMV(mLightPositionInWorldSpace, 0, mLightModelMatrix, 0,
				mLightPositionInModelSpace, 0);
		Matrix.multiplyMV(mLightPositionInEyeSpace, 0, mViewMatrix, 0,
				mLightPositionInWorldSpace, 0);

		// Draw some cubes
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 4, 0, -7);
		Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1, 0, 0);
		drawCube();

		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, -4, 0, -7);
		Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0, 1, 0);
		drawCube();

		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0, 4, -7);
		Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0, 0, 1);
		drawCube();

		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0, -4, -7);
		drawCube();

		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0, 0, -5);
		Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1, 1, 0);
		drawCube();

		GLES20.glUseProgram(mPointProgramHandle);
		drawLight();
	}

	public void onSurfaceChanged(GL10 arg0, int width, int height) {
		// TODO Auto-generated method stub
		// Sets the viewport size to be same as our surface
		GLES20.glViewport(0, 0, width, height);

		// Create a new perspective projection matrix where the height would
		// remain same while the width varies with the aspect ratio of the
		// surface
		// Note to ythen: This should be to handle changes on screen orientation
		// Confirmed, it's used to reset projection when screen orientation
		// changed
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 1.0f;
		final float far = 10.0f;

		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near,
				far);
	}

	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		// TODO Auto-generated method stub
		// Set background color to black
		GLES20.glClearColor(0, 0, 0, 0);

		// Use culling to remove back faces, to save resources/time by not
		// drawing unseen faces
		GLES20.glEnable(GLES20.GL_CULL_FACE);

		// Enable depth testing, spend less time drawing pixels that will be
		// drawn over
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		// Set eye in front of origin
		final float eyeX = 0;
		final float eyeY = 0;
		final float eyeZ = -0.5f;

		// Look towards distance
		final float lookX = 0;
		final float lookY = 0;
		final float lookZ = -5.0f;

		// Up vector where the head pointing at
		final float upX = 0;
		final float upY = 1.0f;
		final float upZ = 0;

		// Set the view matrix, representing camera position
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY,
				lookZ, upX, upY, upZ);

		final String vertexShader = getVertexShader();

		final String fragmentShader = getFragmentShader();

		// Compile vertex shader
		final int vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER,
				vertexShader);

		// Compile fragment shader
		final int fragmentShaderHandle = compileShader(
				GLES20.GL_FRAGMENT_SHADER, fragmentShader);

		mPerVertexProgramHandle = createAndLinkProgram(vertexShaderHandle,
				fragmentShaderHandle, new String[] { "a_Position", "a_Color",
						"a_Normal" });

		// Define a simple shader program for light point source
		final String pointVertexShader = "uniform mat4 u_MVPMatrix;"
				+ "attribute vec4 a_Position;" + "void main() {"
				+ "	gl_Position = u_MVPMatrix * a_Position;"
				+ "	gl_PointSize = 5.0;" + "}";

		final String pointFragmentShader = "precision mediump float;"
				+ "void main() {" + "	gl_FragColor = vec4(1,1,1,1);" + "}";

		final int pointVertexShaderHandle = compileShader(
				GLES20.GL_VERTEX_SHADER, pointVertexShader);
		final int pointFragmentShaderHandle = compileShader(
				GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);

		mPointProgramHandle = createAndLinkProgram(pointVertexShaderHandle,
				pointFragmentShaderHandle, new String[] { "a_Position" });
	}

	/**
	 * Helper function to compile a shader
	 * 
	 * @param shaderType
	 *            The shader type
	 * @param shaderSource
	 *            The shader source code
	 * @return An OpenGL handle to the shader
	 */
	private int compileShader(final int shaderType, final String shaderSource) {
		// Loading vertex shader into OpenGLES2.0
		int shaderHandle = GLES20.glCreateShader(shaderType);
	
		if (shaderHandle != 0) {
			// Set shader source then compile it
			GLES20.glShaderSource(shaderHandle, shaderSource);
			GLES20.glCompileShader(shaderHandle);
	
			// Get compilation status
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS,
					compileStatus, 0);
	
			// If failed to compile, delete the shader
			if (compileStatus[0] == 0) {
				Log.e(tag,
						"Error compiling shader: "
								+ GLES20.glGetShaderInfoLog(shaderHandle));
				GLES20.glDeleteShader(shaderHandle);
				shaderHandle = 0;
			}
		}
	
		if (shaderHandle == 0)
			throw new RuntimeException("Error creating shader");
	
		return shaderHandle;
	}

	private int createAndLinkProgram(final int vertexShaderHandle,
			final int fragmentShaderHandle, final String[] attributes) {
		// TODO Auto-generated method stub
		// Link vertex and fragment shader together into a program
		int programHandle = GLES20.glCreateProgram();

		if (programHandle != 0) {
			// Bind vertex shader
			GLES20.glAttachShader(programHandle, vertexShaderHandle);

			// Bind fragment shader
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);

			// Bind attributes
			if (attributes != null) {
				final int size = attributes.length;
				for (int i = 0; i < size; i++) {
					GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
				}
			}

			// Link them into a program
			GLES20.glLinkProgram(programHandle);

			// Get the link status
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS,
					linkStatus, 0);

			// If linking failed, delete the program
			if (linkStatus[0] == 0) {
				Log.e(tag,
						"Error compiling program: "
								+ GLES20.glGetProgramInfoLog(programHandle));
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}

			if (programHandle == 0)
				throw new RuntimeException("Error creating program");
		}

		return programHandle;
	}

	private void drawLight() {
		// TODO Auto-generated method stub
		final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(
				mPointProgramHandle, "u_MVPMatrix");
		final int pointPositionHandle = GLES20.glGetAttribLocation(
				mPointProgramHandle, "a_Position");

		GLES20.glVertexAttrib3f(pointPositionHandle,
				mLightPositionInModelSpace[0], mLightPositionInModelSpace[1],
				mLightPositionInModelSpace[2]);
		GLES20.glDisableVertexAttribArray(pointPositionHandle);

		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		
		// DRAW THE POINT HERE
		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
	}

	private void drawCube() {
		// TODO Auto-generated method stub
		mCubePositions.position(0);
		GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
				GLES20.GL_FLOAT, false, 0, mCubePositions);
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		mCubeColors.position(0);
		GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize,
				GLES20.GL_FLOAT, false, 0, mCubeColors);
		GLES20.glEnableVertexAttribArray(mColorHandle);

		mCubeNormals.position(0);
		GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize,
				GLES20.GL_FLOAT, false, 0, mCubeNormals);
		GLES20.glEnableVertexAttribArray(mNormalHandle);

		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
		GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

		GLES20.glUniform3f(mLightPositionHandle, mLightPositionInEyeSpace[0],
				mLightPositionInEyeSpace[1], mLightPositionInEyeSpace[2]);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
	}
}
