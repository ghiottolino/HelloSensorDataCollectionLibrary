package com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

public class NormalizedSensorData {

	public String id;
	
	public Location gpsLocation;
	
	public Location networkLocation;
	
	public Location predictedPosition;
	
	public WifiScanData wifiScan;
	
	public MagnetometerData magnetometerData;
	

	public String getContent(){
		StringBuffer toReturn = new StringBuffer();
		toReturn.append("\n");
		toReturn.append("\n");
		toReturn.append("\n");
		
		toReturn.append(toString("GPS Location",gpsLocation));
		toReturn.append(toString("Network Location",networkLocation));
		toReturn.append(toString("Predicted Location",predictedPosition));
		toReturn.append(toString(magnetometerData));

		
		if (wifiScan!=null && (wifiScan.wifiData!=null)){
			toReturn.append("*** WIFI SCAN RESULT \n\n");
			for(WifiData wifiData : wifiScan.wifiData){
				toReturn.append(toString(wifiData));
			}
		}
		
		return toReturn.toString();
		
	}
	
	
	public String toString(){
		return id;
	}
	
	public StringBuffer toString(MagnetometerData magnetometerData){
		StringBuffer toReturn = new StringBuffer();
		if (magnetometerData!=null){
			toReturn.append("Magnetometer"+"\n\n");
			toReturn.append("x:"+magnetometerData.x+"\n");
			toReturn.append("y:"+magnetometerData.y+"\n");
			toReturn.append("z:"+magnetometerData.z+"\n");
			toReturn.append("\n");	
		}
		return toReturn;
	}
	
	
	public StringBuffer toString(String title, Location location){
		StringBuffer toReturn = new StringBuffer();
		if (location!=null){
			toReturn.append(title+"\n\n");
			toReturn.append("lat:"+location.getLatitude()+"\n");
			toReturn.append("lng:"+location.getLongitude()+"\n");
			toReturn.append("acc:"+location.getAccuracy()+"\n");
			toReturn.append("\n");	
		}
		return toReturn;
	}

	
	public StringBuffer toString(WifiData wifiData){
		StringBuffer toReturn = new StringBuffer();
		if (wifiData!=null){
			toReturn.append(wifiData.ssid+"("+wifiData.bssid+")\n\n");
			toReturn.append("bssid:"+wifiData.bssid+"\n");
			toReturn.append("ssid:"+wifiData.ssid+"\n");
			toReturn.append("level:"+wifiData.level+"\n");
			toReturn.append("acc:"+wifiData.accuracy+"\n");
			toReturn.append("\n");	
		}
		return toReturn;
	}
	
	
	
	
	
}
