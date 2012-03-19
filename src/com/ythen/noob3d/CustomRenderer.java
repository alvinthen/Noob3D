package com.ythen.noob3d;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;
import android.widget.Toast;

public class CustomRenderer implements Renderer {

	private float[] mModelMatrix = new float[16];
	private float[] mViewMatrix = new float[16];
	private float[] mProjectionMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];

	private int mMVPMatrixHandle;
	private int mPositionHandle;
	private int mColorHandle;
	
	private int mBytesPerFloat = 4;

	private final FloatBuffer mTriangle1Vertices;
	private final FloatBuffer mTriangle2Vertices;
	private final FloatBuffer mTriangle3Vertices;

	public CustomRenderer() {
		// TODO Auto-generated constructor stub

		// Define points for triangles.

		// This triangle is red, green, and blue.
		final float[] triangle1VerticesData = {
				// X, Y, Z,
				// R, G, B, A
				-0.5f, -0.25f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,

				0.5f, -0.25f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f,

				0.0f, 0.559016994f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f };

		// This triangle is yellow, cyan, and magenta.
		final float[] triangle2VerticesData = {
				// X, Y, Z,
				// R, G, B, A
				-0.5f, -0.25f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,

				0.5f, -0.25f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,

				0.0f, 0.559016994f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f };

		// This triangle is white, gray, and black.
		final float[] triangle3VerticesData = {
				// X, Y, Z,
				// R, G, B, A
				-0.5f, -0.25f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f,

				0.5f, -0.25f, 0.0f, 0.5f, 0.5f, 0.5f, 1.0f,

				0.0f, 0.559016994f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f };

		// Allocate the size of the float buffers for triangles' vertices info
		mTriangle1Vertices = ByteBuffer
				.allocateDirect(triangle1VerticesData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTriangle2Vertices = ByteBuffer
				.allocateDirect(triangle2VerticesData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTriangle3Vertices = ByteBuffer
				.allocateDirect(triangle3VerticesData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();

		// Put vertices data into float buffers
		mTriangle1Vertices.put(triangle1VerticesData).position(0);
		mTriangle2Vertices.put(triangle2VerticesData).position(0);
		mTriangle3Vertices.put(triangle3VerticesData).position(0);
	}

	public void onDrawFrame(GL10 arg0) {
		// TODO Auto-generated method stub

	}

	public void onSurfaceChanged(GL10 arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

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
				+ "attribute vec4 a_position;	\n"
				+ "attribute vec4 a_Color;	\n" + "varying vec4 v_Color;	\n"
				+ "void main() {	\n" + "	v_Color = a_Color;	\n"
				+ "	glPosition = u_MVPMatrix * a_Position;	\n" + "}	\n";

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
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
			
			// If linking failed, delete the program
			if(linkStatus[0] == 0) {
				Log.e("CustomRenderer", GLES20.glGetProgramInfoLog(programHandle));
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
			
			if (programHandle == 0)
				throw new RuntimeException("Error creating program");
		}
		
		mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
		mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
		mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");
	}

}
