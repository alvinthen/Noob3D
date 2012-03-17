package com.ythen.noob3d;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;

public class CustomRenderer implements Renderer {

	private float[] mModelMatrix = new float[16];
	private float[] mViewMatrix = new float[16];
	private float[] mProjectionMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];	
	
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

	}

}
