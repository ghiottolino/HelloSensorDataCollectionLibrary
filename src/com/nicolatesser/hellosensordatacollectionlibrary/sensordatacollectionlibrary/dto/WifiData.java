package com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto;

public class WifiData {

	public String bssid;
	
	public String ssid;
	
	public int level;
	
	// represents how sure we are to find this signal here (how often has been registered / how many scans)
	public float accuracy;
	
}
