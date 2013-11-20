package com.nicolatesser.hellosensordatacollectionlibrary;

import java.io.IOException;

import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.LocationLoggerService;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.SensorCollector;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.SensorData;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.hardware.SensorManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MagnetFieldActivity extends Activity {


	private SensorCollector sensorCollector;
	
	private boolean running = false;
		
	private SensorData sensorData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_magnet_field);
		
		sensorData = new SensorData();
		try {

			sensorCollector = new SensorCollector(this,sensorData);

		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();
		}
		
		startOrResume();
		
		findViewById(R.id.stopCollecting).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (sensorCollector!=null){
					sensorCollector.pause();
				}
				
			}
		});
		
		
		
		
		
		
	}
	
//	
//	public void updateSensorsDisplay(){
//		SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)()
//		SensorManager.getOrientation(R, values);
//		
//	}
	
	
	
	
	
	public void startOrResume() {
		
		sensorData = new SensorData();
		
		if (sensorCollector.isClosed()){
			try {
				
				sensorCollector = new SensorCollector(this,sensorData);
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();
    			
			}
		}
		
		if (sensorCollector.isStarted()) {
			sensorCollector.resume();
		} else {
			sensorCollector.start();
		}
	}


	@Override
	protected void onPause() {
		sensorCollector.pause();
		super.onPause();
	}

	@Override
	protected void onStop() {
		sensorCollector.close();
		super.onStop();
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
