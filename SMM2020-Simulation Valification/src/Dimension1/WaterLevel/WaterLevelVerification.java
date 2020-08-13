package Dimension1.WaterLevel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Dimension1.OneDimensionVerificationCluster;
import Dimension1.WaterLevel.WaterLevel_ReadFile.WaterLevelClass;
import usualTool.AtCommonMath;
import usualTool.AtDateClass;

public class WaterLevelVerification {

	// <+++++++++++++++++++++++++++>
	// <++++++++ Global Values +++++++++>
	// <+++++++++++++++++++++++++++>
	// variable
	private double missingValue = OneDimensionVerificationCluster.missingValue;

	// observation
	// keys : locationID
	private Map<String, WaterLevelClass> observationMap;

	// simulation
	// keys : locationID
	private Map<String, WaterLevelClass> simulationMap;

	// overlapping locationID
	private List<String> locationIDs;

	// <+++++++++++++++++++++++++++>
	// <+++++++ Constructor +++++++++++>
	// <+++++++++++++++++++++++++++>

	/*
	 * @ timeZone = +8
	 */

	// observationFile : observed discharge in piXml format( from IoT sensor)
	// simulationFile : simulation discharge in piXml format ( from FEWS )
	public WaterLevelVerification(String observationFile, String simulationFile) throws Exception {

		// observation
		this.observationMap = new WaterLevel_ReadFile(observationFile).getLocationWaterLevels();

		// simulation
		this.simulationMap = new WaterLevel_ReadFile(simulationFile).getLocationWaterLevels();

		// check LocationID
		checkOverLappingLocationID();
	}

	private void checkOverLappingLocationID() throws Exception {
		List<String> locationIDs = new ArrayList<>();
		for (String key : this.observationMap.keySet()) {
			if (this.simulationMap.containsKey(key)) {
				locationIDs.add(key);
			} else {
				new Exception("*WARN*\tlocationId " + key + " not include in simulationFiles");
			}
		}
		this.locationIDs = locationIDs;
	}

	// <+++++++++++++++++++++++++++>
	// <+++++++ Cluster Functions ++++++++>
	// <+++++++++++++++++++++++++++>

	public Map<String, Double> getLocationsCoefficient() {
		Map<String, Double> outMap = new LinkedHashMap<>();
		this.locationIDs.forEach(locationID -> {
			outMap.put(locationID, this.getCoefficient(locationID));
		});
		return outMap;
	}

	public Map<String, Double> getLocationsHeighestWaterLevelError() {
		Map<String, Double> outMap = new LinkedHashMap<>();
		this.locationIDs.forEach(locationID -> {
			outMap.put(locationID, this.getHeighestWaterLevelError(locationID));
		});
		return outMap;
	}

	public Map<String, Integer> getLocationsHeighestWaterLevelDelay() {
		Map<String, Integer> outMap = new LinkedHashMap<>();
		this.locationIDs.forEach(locationID -> {
			outMap.put(locationID, this.getHeighestWaterLevelDelay(locationID));
		});
		return outMap;
	}

	public Map<String, OneDimensionVerificationCluster> getLocationsVerification() {
		Map<String, OneDimensionVerificationCluster> outMap = new LinkedHashMap<>();
		this.locationIDs.forEach(locationID -> {

			OneDimensionVerificationCluster temptCluster = new OneDimensionVerificationCluster();
			temptCluster.setLocationID(locationID);

			// CE
			temptCluster.setCE(this.getCoefficient(locationID));

			// EHp
			temptCluster.setEHP(this.getHeighestWaterLevelError(locationID));

			// ETp
			temptCluster.setETp(this.getHeighestWaterLevelDelay(locationID));

			outMap.put(locationID, temptCluster);
		});

		return outMap;
	}
	// <+++++++++++++++++++++++++++>
	// <+++++ Verification Functions +++++++>
	// <+++++++++++++++++++++++++++>

	// get coefficient values
	public double getCoefficient(String locationID) {
		// CE
		// value much more closer to 1, that means there is less error between
		// simulation and observation

		double observedAverage = new AtCommonMath(this.observationMap.get(locationID).getValueList()).getMean();
		List<Double> observedValueList = this.observationMap.get(locationID).getValueList();
		List<Double> simulationValueList = this.simulationMap.get(locationID).getValueList();

		double ce = 0.0;
		for (int index = 0; index < observedValueList.size(); index++) {
			ce = ce + Math.pow((observedValueList.get(index) - simulationValueList.get(index)), 2)
					/ Math.pow((observedValueList.get(index) - observedAverage), 2);
		}
		return 1 - ce;
	}

	// get error of waterLevel, EHp
	public double getHeighestWaterLevelError(String locationID) {

		// EHp
		// the smaller the EHp, that mean smaller the error
		Boolean valuesMissing = false;

		WaterLevelClass simulationWaterLevel = this.simulationMap.get(locationID);
		WaterLevelClass observationWaterLevel = this.observationMap.get(locationID);

		int simulationTimeIndex = simulationWaterLevel.getMaxValueTimeIndex();
		int observationTimeIndex = observationWaterLevel.getMaxValueTimeIndex();

		// check for values are all missing or not, SIMULATION
		double simulationMaxDischarge = 0.0;
		try {
			simulationMaxDischarge = simulationWaterLevel.getValue(simulationTimeIndex);
		} catch (Exception e) {
			valuesMissing = true;
			new Exception("*ERROR*\tno date in simulation values in location " + locationID);
		}

		// check for values are all missing or not, OBSERVATION
		double observationMaxDischarge = 0.0;
		try {
			observationMaxDischarge = observationWaterLevel.getValue(observationTimeIndex);
		} catch (Exception e) {
			valuesMissing = true;
			new Exception("*ERROR*\tno date in simulation values in location " + locationID);
		}

		// return value, if there is any maxDischarge is missing, return
		// "this.missingValue"
		if (valuesMissing) {
			return this.missingValue;
		} else {
			return simulationMaxDischarge - observationMaxDischarge;
		}

	}

	// get time delay while max waterLevel happening, ETp(in Second)
	public int getHeighestWaterLevelDelay(String locationID) {

		// ETp
		// the smaller the ETp, that mean less the delay in max waterLevel happening

		Boolean valuesMissing = false;

		WaterLevelClass simulationWatetrLevel = this.simulationMap.get(locationID);
		WaterLevelClass observationWatetrLevel = this.observationMap.get(locationID);

		int simulationTimeIndex = simulationWatetrLevel.getMaxValueTimeIndex();
		int observationTimeIndex = observationWatetrLevel.getMaxValueTimeIndex();

		// check for values are all missing or not, SIMULATION
		try {
			simulationWatetrLevel.getValue(simulationTimeIndex);
		} catch (Exception e) {
			valuesMissing = true;
			new Exception("*ERROR*\tno date in simulation values in location " + locationID);
		}

		// check for values are all missing or not, OBSERVATION
		try {
			observationWatetrLevel.getValue(observationTimeIndex);
		} catch (Exception e) {
			valuesMissing = true;
			new Exception("*ERROR*\tno date in simulation values in location " + locationID);
		}

		// return time delay, if maxDischarge is missing, return "missingValue"
		if (valuesMissing) {
			return (int) this.missingValue;
		} else {
			AtDateClass simulationDate = simulationWatetrLevel.getTime(simulationTimeIndex);
			AtDateClass observationDate = observationWatetrLevel.getTime(observationTimeIndex);

			return AtCommonMath.getDecimal_Int(simulationDate.getSecondPass() - observationDate.getSecondPass(), 1);
		}

	}
}
