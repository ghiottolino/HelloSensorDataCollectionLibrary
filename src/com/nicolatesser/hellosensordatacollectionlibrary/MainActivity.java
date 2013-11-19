package com.nicolatesser.hellosensordatacollectionlibrary;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.LocationLoggerService;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.LocationMatcherService;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.SensorCollector;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.LocationMatchResult;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.NormalizedSensorData;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.SensorData;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
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
	
	private AlertDialog alertDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		alertDialog = new AlertDialog.Builder(this).create();
			
		sensorData = new SensorData();
		try {

			sensorCollector = new SensorCollector(this,sensorData);

		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();
		}
		
		findViewById(R.id.storeLocation).setOnClickListener(new SensorDataOnClickListener(sensorData));
		
		findViewById(R.id.matchLocation).setOnClickListener(new LocationMatcherOnClickListener());
		
		findViewById(R.id.printLocations).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				try {
					LocationLoggerService.getInstance().writeLocationsLog();
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();
				}
			}
		});
		
		findViewById(R.id.showNormalizedMagnetField).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), MagnetFieldActivity.class);
				startActivity(intent);
				
			}
		});
		
	}


	
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
			            	
			            	if (normalizedInputSensorData.wifiScan.wifiData.isEmpty()){
			    				Toast.makeText(getApplicationContext(), "Could not find any Wifi signal, so there are little chances that this location will be correclty recognized. If you know that there is a wifi signal, please try again",Toast.LENGTH_LONG).show();
			    			}
			            	
							List<LocationMatchResult> result = LocationMatcherService.getInstance().matchLocation(normalizedInputSensorData);
							String message = "I could not match any location from the stored location database";
							if (result!=null && !result.isEmpty()){
								
								
								
								message = "The matched location is "+ result.get(0).location.id + "\n\n";
								
								for (LocationMatchResult locationMatchResult: result){
									message+= "Position "+locationMatchResult.location.id+" matches with score "+locationMatchResult.score+ "\n";
								}
								
								
								
								
							}
							
							
							
					
							alertDialog.setTitle("Match Results");
							alertDialog.setMessage(message);
							alertDialog.show();
							
							
							
							//Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
			            }
			        };
			        
			        
			        
			        
			    	TimerTask task = new TimerTask() {
			            @Override
			            public void run() {
			            	handler.post(machtBestLocation);

			    			
			            }
			        };
	
			        timer.schedule(task, 2000); 
			        
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
						
						final Timer timer = new Timer();
				        final Handler handler = new Handler();
				        final Runnable stopCollectingDataRunnable = new Runnable() {
				            @Override
				            public void run() {
				            	stopCollectingData();
				            }
						};
						TimerTask task = new TimerTask() {
				            @Override
				            public void run() {
				            	handler.post(stopCollectingDataRunnable);

				    			
				            }
				        };
		
				        timer.schedule(task, 2000); 

						
						
						
						
						
			} else {
				
				stopCollectingData();

			}
		}
		
		
		private void stopCollectingData() {
			sensorCollector.pause();
			((Button) findViewById(R.id.storeLocation))
					.setText("Log this Location");
			running = false;

			NormalizedSensorData data = sensorData.normalize();

			if (data.wifiScan.wifiData.isEmpty()){
				Toast.makeText(getApplicationContext(), "Could not find any Wifi signal, this location will be ignored. If you know that there is a wifi signal, please try again",Toast.LENGTH_LONG).show();
			}
			else{
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
