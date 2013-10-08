package com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiScanData {

	public List<WifiData> wifiData = new ArrayList<WifiData>();
	
	
	public Map<String,WifiData> getAsMap(){
		
		Map<String,WifiData> wifiDataMap = new HashMap<String, WifiData>();
		for (WifiData wifi : wifiData){
			
			wifiDataMap.put(wifi.bssid, wifi);
			
		}
		return wifiDataMap;
	}
	
}
