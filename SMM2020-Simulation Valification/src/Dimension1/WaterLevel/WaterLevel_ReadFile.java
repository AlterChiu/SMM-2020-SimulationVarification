package Dimension1.WaterLevel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.OperationNotSupportedException;

import FEWS.PIXml.AtPiXmlReader;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import usualTool.AtDateClass;

public class WaterLevel_ReadFile {
	private Map<String, WaterLevelClass> locationWaterLeve = new TreeMap<>();
	
	/*
	 * @ timeZone = +8
	 */

	public WaterLevel_ReadFile(String piXmlFile) throws OperationNotSupportedException, IOException {
		List<TimeSeriesArray> timeSeriesArrays = new AtPiXmlReader().getTimeSeriesArrays(piXmlFile);

		for (TimeSeriesArray timeSeriesArray : timeSeriesArrays) {
			String locationID = timeSeriesArray.getHeader().getLocationId();

			// check the locationId isPresent or not
			if (!this.locationWaterLeve.containsKey(locationID)) {
				// initial waterLevel class
				WaterLevelClass temptWaterLevel = new WaterLevelClass();

				// setting header of location
				temptWaterLevel.setID(locationID);
				temptWaterLevel.setName(timeSeriesArray.getHeader().getLocationName());

				// putting it back to map
				this.locationWaterLeve.put(locationID, temptWaterLevel);
			}

			// setting value to this location
			for (int index = 0; index < timeSeriesArray.size(); index++) {
				this.locationWaterLeve.get(locationID).addValue(timeSeriesArray.getValue(index),
						timeSeriesArray.getTime(index), timeSeriesArray.isMissingValue(index));
			}

		}

	}

	// <+++++++++++++++++++++++++++++++++++++++>
	// <+++++++++ Return Function +++++++++++++++++++++>
	// <+++++++++++++++++++++++++++++++++++++++>
	public Map<String, WaterLevelClass> getLocationWaterLevels() {
		return this.locationWaterLeve;
	}
	
	public List<String> getLocationIDs() {
		return new ArrayList<>(this.locationWaterLeve.keySet());
	}
	// <+++++++++++++++++++++++++++++++++++++++>
	// <+++++++++ Private Class +++++++++++++++++++++>
	// <+++++++++++++++++++++++++++++++++++++++>

	public class WaterLevelClass {
		private String id = "";
		private String name = "";

		private List<Double> valueList = new ArrayList<>();
		private List<AtDateClass> timeList = new ArrayList<>();
		private List<Boolean> missingList = new ArrayList<>();
		private double maxValue = Double.MIN_VALUE;
		private int maxTimeIndex = Integer.MIN_VALUE;

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
	}
}
