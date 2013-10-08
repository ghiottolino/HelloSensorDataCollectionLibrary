package com.nicolatesser.hellosensordatacollectionlibrary;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.nicolatesser.hellosensordatacollectionlibrary.FullscreenActivity.SensorDataOnClickListener;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.LocationLoggerService;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.SensorCollector;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.NormalizedSensorData;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.SensorData;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private SensorCollector sensorCollector;
	
	private boolean running = false;
		
	private SensorData sensorData;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sensorData = new SensorData();
		try {

			sensorCollector = new SensorCollector(this,sensorData);
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		findViewById(R.id.storeLocation).setOnClickListener(new SensorDataOnClickListener(sensorData));
		
		findViewById(R.id.matchLocation).setOnClickListener(new LocationMatcherOnClickListener());
		
		
		
		
	}


	
	public void startOrResume() {
		
		sensorData = new SensorData();
		
		if (sensorCollector.isClosed()){
			try {
				
				sensorCollector = new SensorCollector(this,sensorData);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (sensorCollector.isStarted()) {
			sensorCollector.resume();
		} else {
			sensorCollector.start();
		}
	}

	@Override
	protected void onResume() {
		startOrResume();
		super.onResume();
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
	
	
public class LocationMatcherOnClickListener implements OnClickListener{
		
	
		
		public LocationMatcherOnClickListener(){
		}
		
		@Override
		public void onClick(View v) {
			
			    final SensorData inputSensorData = new SensorData();
			    try {

			    	final SensorCollector sensorCollector = new SensorCollector(getApplicationContext(),inputSensorData);
			    	sensorCollector.start();
			        final Timer timer = new Timer();
			        final Handler handler = new Handler();
			        
			        final Runnable machtBestLocation = new Runnable() {
			            @Override
			            public void run() {
			               	sensorCollector.close();
			            	NormalizedSensorData normalizedInputSensorData = inputSensorData.normalize();
							NormalizedSensorData match = LocationLoggerService.getInstance().matchLocation(normalizedInputSensorData);
							String message = "I could not match any location from the stored location database";
							if (match!=null){
								message = "The matched location is "+ match.id;
							}
							
							Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
			            }
			        };
			        
			        
			        
			        
			    	TimerTask task = new TimerTask() {
			            @Override
			            public void run() {
			            	handler.post(machtBestLocation);

			    			
			            }
			        };
	
			        timer.schedule(task, 1000); 
			        
				} catch (IOException e) {

				}
			
		}
		
		
		
	}
	
	
	public class SensorDataOnClickListener implements OnClickListener{
		
		private SensorData sensorData;
		
		public SensorDataOnClickListener(SensorData sensorData){
			this.sensorData= sensorData;
		}
		
		@Override
		public void onClick(View v) {
			if (!running) {
						
						startOrResume();
						running = true;
						((Button) findViewById(R.id.storeLocation))
								.setText("Stop Logging this Location");
						
						

			} else {
				
				sensorCollector.pause();
						((Button) findViewById(R.id.storeLocation))
								.setText("Log this Location");
						running = false;
						
				NormalizedSensorData data = sensorData.normalize();
						
				LocationLoggerService.getInstance().storeLocation(data);

			}
			
			

			
		}
		
		
		
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