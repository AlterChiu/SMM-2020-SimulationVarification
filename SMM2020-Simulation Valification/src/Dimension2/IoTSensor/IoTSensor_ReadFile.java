package Dimension2.IoTSensor;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.OperationNotSupportedException;

import Dimension1.OneDimensionVerificationCluster;
import FEWS.PIXml.AtPiXmlReader;
import geo.common.CoordinateTranslate;
import main.GlobalProperty;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import usualTool.AtDateClass;
import usualTool.AtFileReader;
import usualTool.TimeTranslate;

public class IoTSensor_ReadFile {

	// key : IoTSensor id
	private Map<String, IoTSensorClass> locationIoTSensor = new TreeMap<>();

	// read IoTSensor from xmlFormat
	// coordinate in TWD97
	public IoTSensor_ReadFile(String piXmlFile) throws OperationNotSupportedException, IOException {

		/*
		 * FEWS OUTPUT
		 */

		List<TimeSeriesArray> timeSeriesArrays = new AtPiXmlReader().getTimeSeriesArrays(piXmlFile);

		for (TimeSeriesArray timeSeriesArray : timeSeriesArrays) {
			String locationID = timeSeriesArray.getHeader().getLocationId();

			// check the locationId isPresent or not
			if (!this.locationIoTSensor.containsKey(locationID)) {
				// initial waterLevel class
				IoTSensorClass temptIoTSensor = new IoTSensorClass();

				// setting header of location
				temptIoTSensor.setID(locationID);
				temptIoTSensor.setName(timeSeriesArray.getHeader().getLocationName());

				// translate wgs84 to twd97
				double coordinateWGS84[] = new double[] { timeSeriesArray.getHeader().getGeometry().getX(0),
						timeSeriesArray.getHeader().getGeometry().getY(0) };
				double coordinateTWD97[] = CoordinateTranslate.Wgs84ToTwd97(coordinateWGS84[0], coordinateWGS84[1]);
				temptIoTSensor.setX(coordinateTWD97[0]);
				temptIoTSensor.setY(coordinateTWD97[1]);

				// putting it back to map
				this.locationIoTSensor.put(locationID, temptIoTSensor);
			}

			// setting value without missing values to this location
			for (int index = 0; index < timeSeriesArray.size(); index++) {
				this.locationIoTSensor.get(locationID).addValue(timeSeriesArray.getValue(index),
						timeSeriesArray.getTime(index), timeSeriesArray.isMissingValue(index));
			}
		}

	}

	// <========================TEMPT FUNCTION====================================>
	public IoTSensor_ReadFile(String valueAdd, String locationAdd)
			throws IOException, NumberFormatException, ParseException {
		// get position
		Map<String, Double[]> positionMap = new TreeMap<>();
		String[][] positionContent = new AtFileReader(locationAdd).getCsv(1, 0);
		for (String[] temptLine : positionContent) {
			positionMap.put(temptLine[0],
					new Double[] { Double.parseDouble(temptLine[3]), Double.parseDouble(temptLine[4]) });
		}

		// get values
		String[][] valuesContent = new AtFileReader(valueAdd).getCsv();
		for (int column = 1; column < valuesContent[0].length; column++) {
			String id = valuesContent[0][column].split("_")[0];
			IoTSensorClass iotSensor = new IoTSensorClass();
			
			iotSensor.setID(id);
			iotSensor.setX(positionMap.get(id)[0]);
			iotSensor.setY(positionMap.get(id)[1]);

			for (int row = 1; row < valuesContent.length; row++) {
				iotSensor.addValue(Double.parseDouble(valuesContent[row][column]),
						TimeTranslate.getDateLong(valuesContent[row][0], "yyyy/MM/dd HH:mm"), false);
			}

			this.locationIoTSensor.put(id, iotSensor);
		}
	}

	public Map<String, IoTSensorClass> getIoTSensorInformation() {
		return this.locationIoTSensor;
	}

	public class IoTSensorClass {
		private String id = "";
		private String name = "";

		private List<Double> valueList = new ArrayList<>();
		private List<AtDateClass> timeList = new ArrayList<>();
		private List<Boolean> missingList = new ArrayList<>();
		private double maxValue = Double.MIN_VALUE;
		private int maxTimeIndex = Integer.MIN_VALUE;

		// coordination
		private double x = GlobalProperty.missingValue;
		private double y = GlobalProperty.missingValue;

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		public void setID(String id) {
			this.id = id;
		}

		public String getID() {
			return this.id;
		}

		public void addValue(double value, long longTime, Boolean isMissing) {
			if (isMissing) {
				this.valueList.add(value);
				this.missingList.add(true);

			} else {
				this.valueList.add(value);
				this.missingList.add(false);

				// check max value, and it's timeIndex
				if (value > this.maxValue) {
					this.maxValue = value;
					this.maxTimeIndex = this.timeList.size();
				}
			}

			this.timeList.add(new AtDateClass(longTime));

		}

		public List<AtDateClass> getTimeList() {
			return this.timeList;
		}

		public List<Double> getValueList() {
			return this.valueList;
		}

		public int size() {
			return this.valueList.size();
		}

		public AtDateClass getTime(int index) {
			return this.timeList.get(index);
		}

		public Double getValue(int index) {
			return this.valueList.get(index);
		}

		public double getMaxValue() {
			return this.maxValue;
		}

		public AtDateClass getMaxValueTime() {
			return this.getTime(this.maxTimeIndex);
		}

		public int getMaxValueTimeIndex() {
			return this.maxTimeIndex;
		}

		public Boolean isMissing(int index) {
			return this.missingList.get(index);
		}

		public List<Boolean> getMissingList() {
			return this.missingList;
		}

		public void setX(double x) {
			this.x = x;
		}

		public double getX() {
			return this.x;
		}

		public void setY(double y) {
			this.y = y;
		}

		public double getY() {
			return this.y;
		}

	}
}
