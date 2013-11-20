package com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary;

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Copyright 2011 Google Inc. All Rights Reserved.



import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.telephony.NeighboringCellInfo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.MagnetometerData;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.SensorData;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.WifiData;
import com.nicolatesser.hellosensordatacollectionlibrary.sensordatacollectionlibrary.dto.WifiScanData;

/**
 * A sensor log that produces a simple, line-oriented text file. The stream
 * auto-flushes periodically to avoid data loss if the log is not properly
 * closed.
 */
public class InMemoryDataSensorLog extends BaseSensorLog {
  // We've been asked not to log SSIDs, but this log file format asks for them.
  // So we log "unknown" for the SSIDs of every access point we see.
  private static final String UNKNOWN_SSID = "UNKNOWN";
  private static final String LOG_FORMAT = "3 # nanosecond timing";

  // If we see this prefix on an AP, we don't log that MAC address. This
  // implements our AP opt-out feature, announced here:
  // http://googleblog.blogspot.com/2011/11/greater-choice-for-wireless-access.html
  private static final String OPTOUT_SSID_SUFFIX = "_nomap";

  // Fudge factor to allow us to compile under Froyo. Copied from the
  // Gingerbread source.
  private static final int SENSOR_LINEAR_ACCELERATION = 10;

  private static final Set<Character> AD_HOC_HEX_VALUES =
      new HashSet<Character>(Arrays.asList('2','6', 'a', 'e', 'A', 'E'));

  // An override for the timestamp to write into the log file.  Only used for testing.
  private static long overrideTimestamp = -1L;  // means unused.

  // The file we're writing.
  private SensorData sensorData;


  // The current line count.  Used to flush the stream every 100 lines.
  private int lineCount;

  /**
   * Opens the given file for writing this sensor log.
   */
  public InMemoryDataSensorLog(SensorData sensorData) throws IOException {
    this(sensorData, "");
  }

  /**
   * Opens the given file for writing this sensor log, writing the given line of
   * notes to the file as it is opened.
   */
  public InMemoryDataSensorLog(SensorData sensorData, String notes) throws IOException {
    this(sensorData, notes, false);
  }

  /**
   * Opens the given file for writing this sensor log, writing the given line of
   * notes to the file as it is opened.
   *
   * @param resume if true, the file is appended rather than overwritten. The
   *        metadata is written again, with a note that an append has occurred.
   */
  public InMemoryDataSensorLog(SensorData sensorData, String notes, boolean resume) throws IOException {
    this.sensorData = sensorData;

    // Log some meta-data
    logNote("metadata_log_format", LOG_FORMAT);
    logNote("metadata_system_time",
        String.valueOf(overrideTimestamp == -1 ? System.currentTimeMillis() : overrideTimestamp));
    logNote("metadata_notes", notes.replaceAll("[\\n\\r\\f]", " "));
    logNote("metadata_deviceInfo", getDeviceInfo());
    if (resume) {
      logNote("metadata_append", "file was opened for append.");
    }
  }

  private String getDeviceInfo() {
    StringBuilder builder = new StringBuilder();
    builder.append("Board: ").append(android.os.Build.BOARD);
    builder.append(" Brand: ").append(android.os.Build.BRAND);
    builder.append(" Device: ").append(android.os.Build.DEVICE);
    builder.append(" Hardware: ").append(android.os.Build.HARDWARE);
    builder.append(" Manufacturer: ").append(android.os.Build.MANUFACTURER);
    builder.append(" Model: ").append(android.os.Build.MODEL);
    builder.append(" Product: ").append(android.os.Build.PRODUCT);
    return builder.toString();
  }

  @Override
  public void close() {

	  // TODO : make average/fingerprint
	  
	  
	  
  }

  @Override
  protected void logGpsPosition(long absoluteTimeNanos, Location loc) {
	  sensorData.gpsLocations.add(loc);
  }

  @Override
  protected void logNetworkPosition(long absoluteTimeNanos, Location loc) {
    sensorData.networkLocations.add(loc);
  }

  @Override
  protected void logGpsNmeaDataNanos(long absoluteTimeNanos, String nmeaData) {
	  //writeLine(absoluteTimeNanos, "rawNmea", toFileString(nmeaData.trim()));
  }

  @Override
  protected void logLastKnownPosition(long absoluteTimeNanos, Location loc) {
    //writeLocationLine(absoluteTimeNanos, "latLngE7LastKnown", loc);
  }

  @Override
  protected void logManualPosition(long absoluteTimeNanos, long latE7, long lngE7) {
    //writeLine(absoluteTimeNanos, "latLngE7Marker", "" + latE7 + " " + lngE7);
  }

  @Override
  protected void logPredictedPosition(
      long absoluteTimeNanos, long latE7, long lngE7, float accuracy) {
    //writeLine(absoluteTimeNanos, "latLngE7Predicted", "" + latE7 + " " + lngE7 + " " + accuracy);
    
    Location location = new Location("latLngE7Predicted");
    location.setLatitude(latE7);
    location.setLongitude(lngE7);
    location.setAccuracy(accuracy);
    sensorData.predictedPositions.add(location);
  }

  @Override
  protected void logUndoManualPosition(long absoluteTimeNanos) {
    //writeLine(absoluteTimeNanos, "latLngE7Marker", "CANCEL_LAST_MARKER");
  }

  @Override
  protected void logNote(long absoluteTimeNanos, String noteType, String note) {
    // ensure that notes don't have characters that would bust the file format,
    // like semicolons and newlines.
    //writeLine(absoluteTimeNanos, toFileString(noteType), toFileString(note));
  }

  @Override
  protected synchronized void logSensorEvent(long absoluteTimeNanos, SensorEvent event) {
	  
	  if (event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
		  final float[] values = event.values;
		  MagnetometerData magnetometerData = new MagnetometerData();
		  magnetometerData.x = values[0];
		  magnetometerData.z = values[1];
		  magnetometerData.y = values[2];
		  sensorData.magnetometerData.add(magnetometerData);
	  }
	  
	  
	
	  
//    writeTimestamp(absoluteTimeNanos);
//    writeSensorId(getSensorName(event.sensor.getType()), 
//      event.sensor.getName());
//
//    final int vCount = event.values.length;
//    final float[] values = event.values;
//
//    // write the first sensor value
//    if (vCount > 0) {
//     buffer.append(values[0]);
//    }
//    
//    // write the rest of the sensor values, space-separated
//    for (int i = 1; i < vCount; ++i) {
//    	 buffer.append(" ");
//    	 buffer.append(values[i]);
//    }
//    
//    // Now print out the accuracy
//    buffer.append(" "); 
//    buffer.append(event.accuracy); 
//    
//    finishLogLine();
  }
 

  @Override
  protected void logWifiScan(long absoluteTimeNanos, Iterable<ScanResult> scans) {
    StringBuffer dataString = new StringBuffer();
    WifiScanData wifiScanData = new WifiScanData();
    for (ScanResult sr : scans) {
      // NOTE: We have set the SSID to be a constant in the below.
      // This is to maintain backwards compatibility with existing formats

      if (shouldLog(sr)) {
    	WifiData wifiData = new WifiData();
    	wifiData.bssid = sr.BSSID;
    	wifiData.ssid = sr.SSID;
    	wifiData.level = sr.level;
    	wifiScanData.wifiData.add(wifiData);
      }
    }

   // writeLine(absoluteTimeNanos, "wifi", dataString.toString());
    sensorData.wifiScans.add(wifiScanData);
  }
  
  /**
   * Returns true if the given scan should be logged, or false if it is an
   * ad-hoc AP or if it is an AP that has opted out of Google's collection
   * practices.
   */
  private static boolean shouldLog(final ScanResult sr) {
    // We filter out any ad-hoc devices.  Ad-hoc devices are identified by having a
    // 2,6,a or e in the second nybble.
    // See http://en.wikipedia.org/wiki/MAC_address -- ad hoc networks
    // have the last two bits of the second nybble set to 10.
    // Only apply this test if we have exactly 17 character long BSSID which should
    // be the case.
    final char secondNybble = sr.BSSID.length() == 17 ? sr.BSSID.charAt(1) : ' ';

    if(AD_HOC_HEX_VALUES.contains(secondNybble)) {
      return false;

    } else if (sr.SSID != null && sr.SSID.endsWith(OPTOUT_SSID_SUFFIX)) {
      return false;

    } else {
      return true;
    }
  }

  @Override
  protected void logTelephonyScan(long absoluteTimeNanos, List<NeighboringCellInfo> scan) {
    if (scan == null) {
      return;  // nothing to log
    }

    StringBuilder b = new StringBuilder();
    for (NeighboringCellInfo info : scan) {
      b.append("\"" + info.toString() + "\" ");
    }

   // writeLine(absoluteTimeNanos, "telephony", b.toString());
  }

  /**
   * Writes a location line of the given type to the log file, unpacking the
   * location object into E7 lat, lng, and accuracy.
   */
  private void writeLocationLine(long absoluteTimeNanos, String key, Location loc) {
    long latE7 = (long) (loc.getLatitude() * 1e7);
    long lngE7 = (long) (loc.getLongitude() * 1e7);
    float accuracy = loc.getAccuracy();
    float bearing = loc.hasBearing() ? loc.getBearing() : -1.0f;
    float speed = loc.hasSpeed() ? loc.getSpeed() : -1.0f;

//    writeLine(absoluteTimeNanos, key, "" + latE7 + " " + lngE7 + " " + accuracy + " " + bearing
//        + " " + speed);
  }

  /**
   * Writes a line to the log file.
   */
//  private synchronized void writeLine(long absoluteTimeNanos, String sensor, String value) {
//    writeTimestamp(absoluteTimeNanos);
//    writeSensorType(sensor);
//    buffer.append(value);
//    finishLogLine();
//  }

  /**
   * Writes the timestamp prefix and separator to the log file.
   */
//  private void writeTimestamp(long absoluteTimeNanos) {
//    if (overrideTimestamp != -1) {
//      absoluteTimeNanos = overrideTimestamp;  // this only happens during tests
//    }
//
//    buffer.append(absoluteTimeNanos);
//    buffer.append(";");
//  }
  
  /**
   * Writes the sensor name type and separator to the log file.
   */
//  private void writeSensorType(String sensorType) {
//	  buffer.append(sensorType);
//	  buffer.append(";");
//  }
  
  /**
   * Writes the sensor type, sensor name and separator to the log file. 
   */
//  private void writeSensorId(String sensorType, String sensorName) {
//	  buffer.append(sensorType);
//	  buffer.append("/"); 
//	  buffer.append(sensorName); 
//	  buffer.append(";");
//  }

  /**
   * Writes a newline to the log file, and updates the line counters. Handles
   * flushing.
   */
  private void finishLogLine() {
    lineCount++;

  }

  /**
   * Returns the text we should write to the file for a given sensor ID.
   */
  private static String getSensorName(int sensorId) {
    switch (sensorId) {
      case Sensor.TYPE_ACCELEROMETER:
        return "accel";
      case Sensor.TYPE_GYROSCOPE:
        return "gyro";
      case Sensor.TYPE_MAGNETIC_FIELD:
        return "compass";
      case Sensor.TYPE_ORIENTATION:
        return "orientation";
      case SENSOR_LINEAR_ACCELERATION:
        return "linaccel";
    }
    return "?";
  }
  
  /**
   * Returns the given text as-is, except that any newline or semi-colon
   * characters are replaced with spaces.
   */
  private static String toFileString(String text) {
    return text.replaceAll("[;\\n\\r\\f]", " ");
  }

  /**
   * Installs a timestamp that will be used for all invocations of writeLine()
   * following this. Only used for testing.
   *
   * @param overrideTimestamp the timestamp to put into the file instead of the real collection
   * time, or -1 to restore normal timestamp logging.
   */
  static void setTimestampForTest(long overrideTimestamp) {
    InMemoryDataSensorLog.overrideTimestamp = overrideTimestamp;
  }
}
