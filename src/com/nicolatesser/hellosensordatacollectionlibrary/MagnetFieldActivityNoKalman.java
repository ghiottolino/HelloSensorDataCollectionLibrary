package com.nicolatesser.hellosensordatacollectionlibrary;



import java.security.acl.LastOwnerException;

import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
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

public class MagnetFieldActivityNoKalman extends Activity implements SensorEventListener {
    /* sensor data */
    SensorManager m_sensorManager;
    float []m_lastMagFields;
    float []m_lastAccels;
    private float[] m_rotationMatrix = new float[16];
    private float[] m_inclinationMatrix = new float[16];
    private float[] m_orientation = new float[4];

    /* fix random noise by averaging tilt values */
    final static int AVERAGE_BUFFER = 30;
    float []m_prevPitch = new float[AVERAGE_BUFFER];
    float m_lastPitch = 0.f;
    float m_lastYaw = 0.f;
    /* current index int m_prevEasts */
    int m_pitchIndex = 0;

    float []m_prevRoll = new float[AVERAGE_BUFFER];
    float m_lastRoll = 0.f;
    /* current index into m_prevTilts */
    int m_rollIndex = 0;
    
    
    
    float m_lastMx = 0.f;
    float m_lastMy = 0.f;
    float m_lastMz = 0.f;

    float m_lastNx = 0.f;
    float m_lastNy = 0.f;
    float m_lastNz = 0.f;

    
    double m_lastAzimuthD = 0.d;
    double m_lastPitchD = 0.d;
    double m_lastRollD = 0.d;
    
    

    /* center of the rotation */
    private float m_tiltCentreX = 0.f;
    private float m_tiltCentreY = 0.f;
    private float m_tiltCentreZ = 0.f;

    /** Rates. */
    private float nanoTtoGRate = 0.00001f;
    private final int gToCountRate = 1000000;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnet_field);
        m_sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        registerListeners();
    }

    private void registerListeners() {
        m_sensorManager.registerListener(this, m_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        m_sensorManager.registerListener(this, m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        m_sensorManager.registerListener(this, m_sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
        
    }

    private void unregisterListeners() {
        m_sensorManager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {
        unregisterListeners();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        unregisterListeners();
        super.onPause();
    }

    @Override
    public void onResume() {
        registerListeners();
        super.onResume();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    	if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
    		TextView magnet_field_accuracy = (TextView) findViewById(R.id.magnet_field_accuracy);
    		magnet_field_accuracy.setText("Accuracy :" + accuracy  + " Resolution " + sensor.getResolution());
    		if(accuracy == 3) {
    		
    			
    			Vibrator vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
    			vib.vibrate(500);
    		
    		}
    	}
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accel(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mag(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            //orientation(event);
        }
       
    }
    
    
    private void orientation(SensorEvent event) {
    
        
    	m_lastAzimuthD =   event.values[0];
        m_lastPitchD =  event.values[1];
        m_lastRollD =  event.values[2];
     
        
        updateSensorsDisplay();
    }
    
    

    private void accel(SensorEvent event) {
        if (m_lastAccels == null) {
            m_lastAccels = new float[3];
        }

        System.arraycopy(event.values, 0, m_lastAccels, 0, 3);

        /*if (m_lastMagFields != null) {
            computeOrientation();
        }*/
    }

    private void mag(SensorEvent event) {
        if (m_lastMagFields == null) {
            m_lastMagFields = new float[3];
        }

        System.arraycopy(event.values, 0, m_lastMagFields, 0, 3);

		 m_lastMx = event.values[0];
		 m_lastMy = event.values[1];
		 m_lastMz = event.values[2];

     
        if (m_lastAccels != null) {
            computeOrientation();
        }
        
        updateSensorsDisplay();
    }

   

	Filter [] m_filters = { new Filter(), new Filter(), new Filter() };

    private class Filter {
        static final int AVERAGE_BUFFER = 10;
        float []m_arr = new float[AVERAGE_BUFFER];
        int m_idx = 0;

        public float append(float val) {
            m_arr[m_idx] = val;
            m_idx++;
            if (m_idx == AVERAGE_BUFFER)
                m_idx = 0;
            return avg();
        }
        public float avg() {
            float sum = 0;
            for (float x: m_arr)
                sum += x;
            return sum / AVERAGE_BUFFER;
        }

    }

    private void computeOrientation() {
        if (SensorManager.getRotationMatrix(m_rotationMatrix, m_inclinationMatrix, m_lastAccels,m_lastMagFields)) {
            SensorManager.getOrientation(m_rotationMatrix, m_orientation);
            //SensorManager.getOrientation(m_inclinationMatrix, m_orientation);

   
            /* 1 radian = 57.2957795 degrees */
            /* [0] : yaw, rotation around z axis
             * [1] : pitch, rotation around x axis
             * [2] : roll, rotation around y axis */
            float yaw = (float) Math.toDegrees(m_orientation[0]);// * 57.2957795f;
            float pitch = (float)Math.toDegrees(m_orientation[1]);// * 57.2957795f;
            float roll =(float) Math.toDegrees(m_orientation[2]);// * 57.2957795f;
            
//            float yaw = m_orientation[0];
//            float pitch = m_orientation[1];
//            float roll = m_orientation[2];
            

            m_lastYaw = m_filters[0].append(yaw);
            m_lastPitch = m_filters[1].append(pitch);
            m_lastRoll = m_filters[2].append(roll);
        
            
            
            float[] RsInv = new float[16];
            Matrix.invertM(RsInv, 0, m_rotationMatrix, 0);

            float resultVec[] = new float[4];
            float[] geomagneticValuesAdjusted = new float[4];
            geomagneticValuesAdjusted[0] = m_lastMagFields[0];
            geomagneticValuesAdjusted[1] = m_lastMagFields[1];
            geomagneticValuesAdjusted[2] = m_lastMagFields[2];
            geomagneticValuesAdjusted[3] = 0;
            Matrix.multiplyMV(resultVec, 0, RsInv, 0, geomagneticValuesAdjusted, 0);

            for (int i = 0; i < resultVec.length; i++) {
                resultVec[i] = resultVec[i] * nanoTtoGRate * gToCountRate;
            }
            
            m_lastNx = resultVec[0];
            m_lastNy = resultVec[1];
            m_lastNz = resultVec[2];
            
            
            
            updateSensorsDisplay();
        }

    }
    
    
    
    
    private void updateSensorsDisplay() {
    	
        TextView mx = (TextView) findViewById(R.id.magnet_field_x);
        TextView my = (TextView) findViewById(R.id.magnet_field_y);
        TextView mz = (TextView) findViewById(R.id.magnet_field_z);
    	
        mx.setText("m x: " + m_lastMx);
        my.setText("m y: " + m_lastMy);
        mz.setText("m z: " + m_lastMz);
    	
    	
        TextView rt = (TextView) findViewById(R.id.orientation_y);
        TextView pt = (TextView) findViewById(R.id.orientation_x);
        TextView yt = (TextView) findViewById(R.id.orientation_z);
        yt.setText("azi z: " + m_lastAzimuthD);
        pt.setText("pitch x: " + m_lastPitchD);
        rt.setText("roll y: " + m_lastRollD);
        
        
        
    	
        TextView nx = (TextView) findViewById(R.id.n_x);
        TextView ny = (TextView) findViewById(R.id.n_y);
        TextView nz = (TextView) findViewById(R.id.n_z);
        nx.setText("n x: " + m_lastNx);
        ny.setText("n y: " + m_lastNy);
        nz.setText("n z: " + m_lastNz);
        
        
        
        
        
        
		
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












