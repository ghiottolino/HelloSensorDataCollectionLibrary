package com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.location.Location;

public class SensorData {

	public List<Location> gpsLocations = new ArrayList<Location>();
	
	public List<Location> networkLocations = new ArrayList<Location>();
	
	public List<Location> predictedPositions = new ArrayList<Location>();
	
	public List<WifiScanData> wifiScans = new ArrayList<WifiScanData>();
	

	public NormalizedSensorData normalize(){
		
		NormalizedSensorData normalizedSensorData = new NormalizedSensorData();
		normalizedSensorData.gpsLocation=averageLocation(gpsLocations);
		normalizedSensorData.networkLocation=averageLocation(networkLocations);
		normalizedSensorData.predictedPosition=averageLocation(predictedPositions);
		
		normalizedSensorData.wifiScan =averageWifiScanData(wifiScans);
		
		return normalizedSensorData;
	}
	
	
	public Location averageLocation(List<Location> locations){
		if (locations.isEmpty()) return null;
		else{
			Location averageLocation = new Location(locations.get(0).getProvider());
			
			double lat = 0;
			double lng = 0;
			float accuracy = 0;
			int n = locations.size();
			
			for (Location location : locations){
				lat+=(location.getLatitude()/n);
				lng+=(location.getLongitude()/n);
				accuracy+=(location.getAccuracy()/n);
			}
			
			
			averageLocation.setLatitude(lat);
			averageLocation.setLongitude(lng);
			averageLocation.setAccuracy(accuracy);
			
			
			
			return averageLocation;
		}
		
	}
	
	
	public WifiScanData averageWifiScanData(List<WifiScanData> wifiScans){
		WifiScanData averageWifiScanData = new WifiScanData();
		int n = wifiScans.size();
		
		Map<String,List<WifiData>> wifiDataMap = new HashMap<String, List<WifiData>>();
		
		for (WifiScanData wifiScanData:wifiScans){
			
			for (WifiData wifiData : wifiScanData.wifiData){
				if (wifiDataMap.get(wifiData.bssid)==null){
					wifiDataMap.put(wifiData.bssid, new ArrayList<WifiData>());
				}
				List<WifiData> bssidWifData = wifiDataMap.get(wifiData.bssid);
				bssidWifData.add(wifiData);
				wifiDataMap.put(wifiData.bssid, bssidWifData);
			}
			
		}
		
		Set<String> bssidKeys = wifiDataMap.keySet();
		for (String bssidKey: bssidKeys)
		{
			List<WifiData> bssidWifiData = wifiDataMap.get(bssidKey);
			averageWifiScanData.wifiData.add(averageWifiData(bssidWifiData, n));
		}
	
		return averageWifiScanData;
	}
	
	
	public WifiData averageWifiData(List<WifiData> wifiData, int numberOfScans){
		WifiData averageWifiData = new WifiData();
		averageWifiData.bssid = wifiData.get(0).bssid;
		averageWifiData.ssid = wifiData.get(0).ssid;
		

		float level = 0;
		float accuracy = 0;
		int n = wifiData.size();
		
		for (WifiData wifiSignal : wifiData){
			level+=(wifiSignal.level/n);	
			accuracy+=100/numberOfScans;
		}
		
		
		averageWifiData.level = Math.round(level);
		averageWifiData.accuracy = accuracy;

		return averageWifiData;
	}
	
	
	
	
		
}
