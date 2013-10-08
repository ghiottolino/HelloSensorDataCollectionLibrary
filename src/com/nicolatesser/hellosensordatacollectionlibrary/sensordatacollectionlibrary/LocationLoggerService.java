package com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.location.Location;

import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.NormalizedSensorData;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.WifiData;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.WifiScanData;

public class LocationLoggerService {

	
	private static LocationLoggerService instance;
	
	
	
	public static  LocationLoggerService getInstance(){
		if (instance==null){
			instance = new LocationLoggerService();
		}
		return instance;
	}
	
	
	private Map<Long,NormalizedSensorData> locations = new HashMap<Long, NormalizedSensorData>();
		
	public void storeLocation(NormalizedSensorData data){
		
		data.id = "Position "+(locations.size()+1);
		locations.put(Calendar.getInstance().getTimeInMillis(), data);
	}
	
	
	
	public List<NormalizedSensorData> getLocations(){
		
		Collection<NormalizedSensorData> values = locations.values();
		List<NormalizedSensorData> list = new ArrayList<NormalizedSensorData>(values);
		
		return list;
		
	}
	
	
	public NormalizedSensorData getLocationsById(String id){
		
		Collection<NormalizedSensorData> values = locations.values();
		NormalizedSensorData sensorData = null;
		for (NormalizedSensorData normalizedSensorData : values){
			if (normalizedSensorData.id.equalsIgnoreCase(id)){
				sensorData=normalizedSensorData;
			}
		}
		
		return sensorData;
		
	}
	
	
	
	// TODO : separate in another service
	
	public NormalizedSensorData matchLocation(NormalizedSensorData inputSensorData){
		
		Collection<NormalizedSensorData> values = locations.values();
		
		NormalizedSensorData bestMatch = null;
		int bestScore = 0;
		
		
		for (NormalizedSensorData knownSensorData : values){
			int score = compareNormalizedSensorData(knownSensorData , inputSensorData);
			if (score>bestScore){
				bestScore=score;
				bestMatch = knownSensorData;
			}
			
		}
		
		return bestMatch;
		
	}



	private int compareNormalizedSensorData(
			NormalizedSensorData knownSensorData,
			NormalizedSensorData inputSensorData) {
		
		int score = 0;
		
		int GPS_LOCATION_MULTIPLIER = 1;
		int NETWORK_LOCATION_MULTIPLIER = 1;
		int PREDICTED_LOCATION_MULTIPLIER = 1;
		int WIFI_MULTIPLIER = 2;
		
		
		score+=GPS_LOCATION_MULTIPLIER*compareLocation(knownSensorData.gpsLocation,inputSensorData.gpsLocation);
		score+=NETWORK_LOCATION_MULTIPLIER*compareLocation(knownSensorData.networkLocation,inputSensorData.networkLocation);
		score+=PREDICTED_LOCATION_MULTIPLIER*compareLocation(knownSensorData.predictedPosition,inputSensorData.predictedPosition);
		
		score+=WIFI_MULTIPLIER*compareWifiData(knownSensorData.wifiScan, inputSensorData.wifiScan);
		
		return score;
	}
	
	
	private float compareLocation(
			Location knownLocation,
			Location inputLocation) {
		
		float score = 0;
		if (knownLocation==null || inputLocation==null){
			return score;
		}
		
		float[] results = new float[1];
		Location.distanceBetween(knownLocation.getLatitude(), knownLocation.getLongitude(), inputLocation.getLatitude(), inputLocation.getLongitude(), results);
		
		float distanceInMeter = results[0];
		
		if (distanceInMeter>30) score= 0;
		else if (distanceInMeter==0){
			score= 100;
		}
		else{
			score= 100/distanceInMeter;
		}
		
		
		score = score * inputLocation.getAccuracy()/100 * knownLocation.getAccuracy()/100;
		
		return score;
	}
	
	

	private float compareWifiData(
			WifiScanData knownWifiScanData,
			WifiScanData inputWifiScanData) {
		
		float score = 0;
	
		if (knownWifiScanData==null || inputWifiScanData==null){
			return score;
		} 
		
		Map<String, WifiData> knownWifiMap = knownWifiScanData.getAsMap();
		Map<String, WifiData> inputWifiMap = inputWifiScanData.getAsMap();
			
		int averageScanSize = (knownWifiMap.size()+inputWifiMap.size())/2;
		
		Set<String> inputKeys = inputWifiMap.keySet();
		
		for (String inputKey:inputKeys){
			float keyScore = 0;
			
			WifiData inputWifiData = inputWifiMap.get(inputKey);
			WifiData knownWifiData = knownWifiMap.get(inputKey);
			
			if (knownWifiData!=null && inputWifiData!=null){
				
				int diffLevel = Math.abs(knownWifiData.level - inputWifiData.level);
				
				if (diffLevel>80) score= 0;
				else if (diffLevel==0){
					keyScore= 100;
				}
				else{
					keyScore= 100/diffLevel;
				}
				
				keyScore = keyScore * inputWifiData.accuracy/100 * knownWifiData.accuracy/100;
				
				score += keyScore;  // /averageScanSize
			}
			
		}

		return score;
	}
	
	
	
	
	
	
	
}
