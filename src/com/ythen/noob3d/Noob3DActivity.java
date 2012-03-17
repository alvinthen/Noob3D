package com.ythen.noob3d;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.Toast;

public class Noob3DActivity extends Activity {
	/** Called when the activity is first created. */

	private GLSurfaceView mGLSurfaceView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGLSurfaceView = new GLSurfaceView(this);

		// Get device's global information, to check support for OpenGLES2
		final ActivityManager am = (ActivityManager) this
				.getSystemService(ACTIVITY_SERVICE);
		final ConfigurationInfo info = am.getDeviceConfigurationInfo();
		final boolean supportGLES2 = info.reqGlEsVersion >= 0x20000;
		 
		if (supportGLES2) {
			mGLSurfaceView.setEGLContextClientVersion(2);
			mGLSurfaceView.setRenderer(new CustomRenderer());
		} else {
			Toast toast = Toast.makeText(this, R.string.notSupported,
					Toast.LENGTH_LONG);
			toast.show();
			return;
		}

		setContentView(mGLSurfaceView);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mGLSurfaceView.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mGLSurfaceView.onResume();
	}

}