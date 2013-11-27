package com.nicolatesser.hellosensordatacollectionlibrary;



import java.security.acl.LastOwnerException;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.hardware.SensorManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;



import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;


/**
 * Activity for gathering magnetic field statistics.
 */
public class MagnetFieldActivityKalman extends Activity implements SensorEventListener {

    public static final int KALMAN_STATE_MAX_SIZE = 80;
    public static final double MEASUREMENT_NOISE = 5;

    /** Sensor manager. */
    private SensorManager mSensorManager;
    /** Magnetometer spec. */
//    private TextView vendor;
//    private TextView resolution;
//    private TextView maximumRange;

    /** Magnetic field coordinates measurements. */
    private TextView magneticXTextView;
    private TextView magneticYTextView;
    private TextView magneticZTextView;

    /** Sensors. */
    private Sensor mAccelerometer;
    private Sensor mGeomagnetic;
    private float[] accelerometerValues;
    private float[] geomagneticValues;

    /** Flags. */
    private boolean specDefined = false;
    private boolean kalmanFiletring = false;

    /** Rates. */
    private float nanoTtoGRate = 0.00001f;
    private final int gToCountRate = 1000000;

    /** Kalman vars. */
//    private KalmanState previousKalmanStateX;
//    private KalmanState previousKalmanStateY;
//    private KalmanState previousKalmanStateZ;
    
    private int previousKalmanStateCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnet_field);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGeomagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//
//        vendor = (TextView) findViewById(R.id.vendor);
//        resolution = (TextView) findViewById(R.id.resolution);
//        maximumRange = (TextView) findViewById(R.id.maximumRange);

        magneticXTextView = (TextView) findViewById(R.id.magnet_field_x);
        magneticYTextView = (TextView) findViewById(R.id.magnet_field_y);
        magneticZTextView = (TextView) findViewById(R.id.magnet_field_z);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGeomagnetic, SensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * Refresh statistics.
     *
     * @param view - refresh button view.
     */
    public void onClickRefreshMagneticButton(View view) {
        resetKalmanFilter();
    }

    /**
     * Switch Kalman filtering on/off
     *
     * @param view - Klaman filetring switcher (checkbox)
     */
    public void onClickKalmanFilteringCheckBox(View view) {
        CheckBox kalmanFiltering = (CheckBox) view;
        this.kalmanFiletring = kalmanFiltering.isChecked();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }
        synchronized (this) {
            switch(sensorEvent.sensor.getType()){
                case Sensor.TYPE_ACCELEROMETER:
                    accelerometerValues = sensorEvent.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
//                    if (!specDefined) {
//                        vendor.setText("Vendor: " + sensorEvent.sensor.getVendor() + " " + sensorEvent.sensor.getName());
//                        float resolutionValue = sensorEvent.sensor.getResolution() * nanoTtoGRate;
//                        resolution.setText("Resolution: " + resolutionValue);
//                        float maximumRangeValue = sensorEvent.sensor.getMaximumRange() * nanoTtoGRate;
//                        maximumRange.setText("Maximum range: " + maximumRangeValue);
//                    }
                    geomagneticValues = sensorEvent.values.clone();
                    break;
            }
            if (accelerometerValues != null && geomagneticValues != null) {
                float[] Rs = new float[16];
                float[] I = new float[16];

                if (SensorManager.getRotationMatrix(Rs, I, accelerometerValues, geomagneticValues)) {

                    float[] RsInv = new float[16];
                    Matrix.invertM(RsInv, 0, Rs, 0);

                    float resultVec[] = new float[4];
                    float[] geomagneticValuesAdjusted = new float[4];
                    geomagneticValuesAdjusted[0] = geomagneticValues[0];
                    geomagneticValuesAdjusted[1] = geomagneticValues[1];
                    geomagneticValuesAdjusted[2] = geomagneticValues[2];
                    geomagneticValuesAdjusted[3] = 0;
                    Matrix.multiplyMV(resultVec, 0, RsInv, 0, geomagneticValuesAdjusted, 0);

                    for (int i = 0; i < resultVec.length; i++) {
                        resultVec[i] = resultVec[i] * nanoTtoGRate * gToCountRate;
                    }

                    magneticXTextView.setText(Float.toString(geomagneticValuesAdjusted[0]));
                    magneticYTextView.setText(Float.toString(geomagneticValuesAdjusted[1]));
                    magneticZTextView.setText(Float.toString(geomagneticValuesAdjusted[2]));
                    
//                    if (kalmanFiletring) {
//
//                        KalmanState currentKalmanStateX = new KalmanState(MEASUREMENT_NOISE, accelerometerValues[0], (double)resultVec[0], previousKalmanStateX);
//                        previousKalmanStateX = currentKalmanStateX;
//
//                        KalmanState currentKalmanStateY = new KalmanState(MEASUREMENT_NOISE, accelerometerValues[1], (double)resultVec[1], previousKalmanStateY);
//                        previousKalmanStateY = currentKalmanStateY;
//
//                        KalmanState currentKalmanStateZ = new KalmanState(MEASUREMENT_NOISE, accelerometerValues[2], (double)resultVec[2], previousKalmanStateZ);
//                        previousKalmanStateZ = currentKalmanStateZ;
//
//                        if (previousKalmanStateCounter == KALMAN_STATE_MAX_SIZE) {
//                            magneticXTextView.setText("x: " + previousKalmanStateX.getX_estimate());
//                            magneticYTextView.setText("y: " + previousKalmanStateY.getX_estimate());
//                            magneticZTextView.setText("z: " + previousKalmanStateZ.getX_estimate());
//
//                            resetKalmanFilter();
//                        } else {
//                            previousKalmanStateCounter++;
//                        }
//
//                    } else {
//                        magneticXTextView.setText("x: " + resultVec[0]);
//                        magneticYTextView.setText("y: " + resultVec[1]);
//                        magneticZTextView.setText("z: " + resultVec[2]);
//                    }
                }
            }
        }
    }

    private void resetKalmanFilter() {
//        previousKalmanStateX = null;
//        previousKalmanStateY = null;
//        previousKalmanStateZ = null;
        previousKalmanStateCounter = 0;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;

		switch (item.getItemId()) {

		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpTo(this, new Intent(this,
					LocationListActivity.class));
			return true;

		case R.id.action_home:

			intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			return true;

		case R.id.action_list:

			intent = new Intent(this, LocationListActivity.class);
			startActivity(intent);
			return true;

		}

		return super.onOptionsItemSelected(item);
	}
}










