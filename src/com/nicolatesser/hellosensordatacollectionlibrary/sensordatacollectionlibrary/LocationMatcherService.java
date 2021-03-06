package com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.location.Location;

import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.LocationMatchResult;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.MagnetometerData;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.NormalizedSensorData;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.WifiData;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.WifiScanData;

public class LocationMatcherService {

	
	private static LocationMatcherService instance;
	
	
	
	public static  LocationMatcherService getInstance(){
		if (instance==null){
			instance = new LocationMatcherService();
		}
		return instance;
	}
	

	
	
	

	
	public List<LocationMatchResult> matchLocation(NormalizedSensorData inputSensorData){

		// get all known locations
		List<NormalizedSensorData> locations = LocationLoggerService.getInstance().getLocations();
				
		List<LocationMatchResult> result = new ArrayList<LocationMatchResult>();
		 
		NormalizedSensorData bestMatch = null;
		int bestScore = 0;
		
		
		for (NormalizedSensorData knownSensorData : locations){
			int score = compareNormalizedSensorData(knownSensorData , inputSensorData);
			if (score>bestScore){
				bestScore=score;
				bestMatch = knownSensorData;
			}
			
			LocationMatchResult locationMatchResult = new LocationMatchResult();
			locationMatchResult.location = knownSensorData;
			locationMatchResult.score = score;
			
			result.add(locationMatchResult);
		}
		
		Collections.sort(result);
		
		return result;
		
	}



	private int compareNormalizedSensorData(
			NormalizedSensorData knownSensorData,
			NormalizedSensorData inputSensorData) {
		
		int score = 0;
		
		int GPS_LOCATION_MULTIPLIER = 1;
		int NETWORK_LOCATION_MULTIPLIER = 1;
		int PREDICTED_LOCATION_MULTIPLIER = 1;
		int WIFI_MULTIPLIER = 2;
		int MAGNETOMETER_MULTIPLIER = 2;
		
		
		score+=GPS_LOCATION_MULTIPLIER*compareLocation(knownSensorData.gpsLocation,inputSensorData.gpsLocation);
		score+=NETWORK_LOCATION_MULTIPLIER*compareLocation(knownSensorData.networkLocation,inputSensorData.networkLocation);
		score+=PREDICTED_LOCATION_MULTIPLIER*compareLocation(knownSensorData.predictedPosition,inputSensorData.predictedPosition);
		
		score+=WIFI_MULTIPLIER*compareWifiData(knownSensorData.wifiScan, inputSensorData.wifiScan);
		score+=MAGNETOMETER_MULTIPLIER*compareMagnetometerData(knownSensorData.magnetometerData, inputSensorData.magnetometerData);
		
		
		return score;
	}
	
	
	private float compareMagnetometerData(
			MagnetometerData knownMagnetometerData,
			MagnetometerData inputMagnetometerData) {
		
		float score = 0;
		if (knownMagnetometerData==null || inputMagnetometerData==null){
			return score;
		}
		
		float distancex = Math.abs(knownMagnetometerData.x - inputMagnetometerData.x);
		float distancey = Math.abs(knownMagnetometerData.y - inputMagnetometerData.y);
		float distancez = Math.abs(knownMagnetometerData.z - inputMagnetometerData.z);

		float distance =distancex+distancey+distancez;
		
		score = proportionalScore(distance, 100);
		
		return score;
	}
	
	
	
	/**
	 * Return a score (from 0 to 100) given a distance and a max
	 * distance==max --> 0
	 * distance==0 -->100
	 *
	 * score = ((max-distance)/max)*100
	 * 
	 * @param distance
	 * @param scaleMax the maximum of the scale, if the distance is bigger, score is null
	 * @return
	 */
	private float proportionalScore(float distance, float scaleMax){
		float score = 0;
		if (distance>=scaleMax) score= 0;
		else if (distance==0){
			score= 100;
		}
		else{
			score= ((scaleMax-distance)/scaleMax)*100;
		}
		
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
		
		float distanceInMeter =  Math.abs(results[0]);
		
		score = proportionalScore(distanceInMeter, 30);
		
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

		
		float missingWifiDataScore = compareMissingWifiData(knownWifiMap, inputWifiMap);
		
		score = score * missingWifiDataScore / 100;
		
		
		return score;
	}
	
	
	
	private float compareMissingWifiData(Map<String, WifiData> knownWifiMap,Map<String, WifiData> inputWifiMap){
		
		float score = 0;
		Set<String> knownKeys = knownWifiMap.keySet();
		Set<String> inputKeys = inputWifiMap.keySet();
		
		int knownKeysSize = knownKeys.size();
		int inputKeysSize = inputKeys.size();
		
		inputKeys.retainAll(knownKeys); // for intersection
		int intersectionSize = inputKeys.size();
		
		
		
		int delta = knownKeysSize+inputKeysSize-(intersectionSize*2);
		
		if (delta==0){
			score = 100;
		}
		else{
			score = 100 - (100*delta/(knownKeysSize+inputKeysSize));
		}
		
		return score;
	}
	
	
	
	
}
