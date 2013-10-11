package com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
	
	public void writeLocationsLog() throws IOException{
		
		File file = new File("/sdcard/Download/locationsLog.txt");
		// if file doesn't exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
					
		FileOutputStream out = new FileOutputStream(file, false);

		List<NormalizedSensorData> storedLocations = getLocations();
		
		String content = "";
		for (NormalizedSensorData storedLocation: storedLocations){
			content+="***********************\n";
			content+=storedLocation.id+"\n\n";
			content+="***********************\n";
			content+=storedLocation.getContent()+"\n\n";	
		}
		
		byte[] contentInBytes = content.getBytes();
		out.write(contentInBytes);
		out.flush();
		out.close();
		
	}
	
}
