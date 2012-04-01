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

	// Number of bytes per vertex (7 elements per vertex)
	private final int mStrideBytes = 7 * mBytesPerFloat;

	private final int mPositionDataSize = 3;
	private final int mColorDataSize = 4;
	private final int mNormalDataSize = 4;

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
		mCubeNormals= ByteBuffer
				.allocateDirect(cubeNormalData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();

		// Put vertices data into float buffers
		mCubePositions.put(cubePositionData).position(0);
		mCubeColors.put(cubeColorData).position(0);
		mCubeNormals.put(cubeNormalData).position(0);
	}

	public void onDrawFrame(GL10 arg0) {
		// TODO Auto-generated method stub
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

		// Do a complete rotation every 10 seconds
		long time = SystemClock.uptimeMillis() % 10000L;
		float angleInDegrees = (360.0f / 10000.0f) * (int) time;
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0, 0, 1.0f);
		drawTriangle(mTriangle1Vertices);

		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0, -1.0f, 0);
		Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0, 1, 0);
		drawTriangle(mTriangle2Vertices);

		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0, 1.0f, 0);
		Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1, 0, 0);
		drawTriangle(mTriangle3Vertices);
	}

	public void onSurfaceChanged(GL10 arg0, int width, int height) {
		// TODO Auto-generated method stub
		// Sets the viewport size to be same as our surface
		GLES20.glViewport(0, 0, width, height);

		// Create a new perspective projection matrix where the height would
		// remain same while the width varies with the aspect ratio of the
		// surface
		// Note to ythen: This should be to handle changes on screen orientation
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
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

		// Set eye behind origin
		final float eyeX = 0;
		final float eyeY = 0;
		final float eyeZ = 1.5f;

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

		final String vertexShader = "uniform mat4 u_MVPMatrix;	\n"
				+ "attribute vec4 a_Position;	\n"
				+ "attribute vec4 a_Color;	\n" + "varying vec4 v_Color;	\n"
				+ "void main() {	\n" + "	v_Color = a_Color;	\n"
				+ "	gl_Position = u_MVPMatrix * a_Position;	\n" + "}	\n";

		final String fragmentShader = "precision mediump float;	\n"
				+ "varying vec4 v_Color;	\n" + "void main() {	\n"
				+ "	gl_FragColor = v_Color;	\n" + "}	\n";

		// Loading vertex shader into OpenGLES2.0
		int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

		if (vertexShaderHandle != 0) {
			// Set shader source then compile it
			GLES20.glShaderSource(vertexShaderHandle, vertexShader);
			GLES20.glCompileShader(vertexShaderHandle);

			// Get compilation status
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS,
					compileStatus, 0);

			// If failed to compile, delete the shader
			if (compileStatus[0] == 0) {
				Log.e("CustomRenderer",
						GLES20.glGetShaderInfoLog(vertexShaderHandle));
				GLES20.glDeleteShader(vertexShaderHandle);
				vertexShaderHandle = 0;
			}
		}

		if (vertexShaderHandle == 0)
			throw new RuntimeException("Error creating vertex shader");

		// Loading fragment shader into OpenGLES2.0
		int fragmentShaderHandle = GLES20
				.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

		if (fragmentShaderHandle != 0) {
			// Set shader source then compile it
			GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);
			GLES20.glCompileShader(fragmentShaderHandle);

			// Get compilation status
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(fragmentShaderHandle,
					GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If failed to compile, delete the shader
			if (compileStatus[0] == 0) {
				Log.e("CustomRenderer",
						GLES20.glGetShaderInfoLog(fragmentShaderHandle));
				GLES20.glDeleteShader(fragmentShaderHandle);
				fragmentShaderHandle = 0;
			}
		}

		if (fragmentShaderHandle == 0)
			throw new RuntimeException("Error creating fragment shader");

		// Link vertex and fragment shader together into a program
		int programHandle = GLES20.glCreateProgram();

		if (programHandle != 0) {
			// Bind vertex shader
			GLES20.glAttachShader(programHandle, vertexShaderHandle);

			// Bind fragment shader
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);

			// Bind attributes
			GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
			GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

			// Link them into a program
			GLES20.glLinkProgram(programHandle);

			// Get the link status
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS,
					linkStatus, 0);

			// If linking failed, delete the program
			if (linkStatus[0] == 0) {
				Log.e("CustomRenderer",
						GLES20.glGetProgramInfoLog(programHandle));
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}

			if (programHandle == 0)
				throw new RuntimeException("Error creating program");
		}

		// References used to pass data into the program
		mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle,
				"u_MVPMatrix");
		mPositionHandle = GLES20.glGetAttribLocation(programHandle,
				"a_Position");
		mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");

		// Tell OpenGL to use this program is rendering
		GLES20.glUseProgram(programHandle);
	}

	/**
	 * Draws a triangle from the given vertex data
	 * 
	 * @param aTriangleBuffer
	 *            The buffer containing vertex data
	 */
	private void drawTriangle(final FloatBuffer aTriangleBuffer) {
		// TODO Auto-generated method stub
		aTriangleBuffer.position(mPositionOffset);
		GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
				GLES20.GL_FLOAT, false, mStrideBytes, aTriangleBuffer);
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		aTriangleBuffer.position(mColorOffset);
		GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize,
				GLES20.GL_FLOAT, false, mStrideBytes, aTriangleBuffer);
		GLES20.glEnableVertexAttribArray(mColorHandle);

		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
	}
}
