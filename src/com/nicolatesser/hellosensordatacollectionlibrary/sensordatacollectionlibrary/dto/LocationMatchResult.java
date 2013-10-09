package com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto;

public class LocationMatchResult implements Comparable<LocationMatchResult>{
	
	public NormalizedSensorData location;
	
	public int score;

	@Override
	public int compareTo(LocationMatchResult another) {
		if (this.score==another.score) return 0;
		else if  (this.score<another.score) return 1;
		else return -1;
	}

}
