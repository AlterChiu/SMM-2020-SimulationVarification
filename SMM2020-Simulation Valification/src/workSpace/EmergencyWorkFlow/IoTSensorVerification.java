package workSpace.EmergencyWorkFlow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import asciiFunction.AsciiBasicControl;
import usualTool.AtCommonMath;
import usualTool.AtDateClass;
import workSpace.EmergencyWorkFlow.IoTSensor_ReadFile.IoTSensorClass;
import usualTool.AtCommonMath.StaticsModel;

public class IoTSensorVerification {
	private Map<String, IoTSensorClass> IoTSensorInformations;
	private Map<String, AsciiBasicControl> floodInformations;

	public IoTSensorVerification(Map<String, IoTSensorClass> IoTSensorInformations,
			Map<String, AsciiBasicControl> floodInformations) {

		this.floodInformations = floodInformations;
		this.IoTSensorInformations = IoTSensorInformations;
	}

	// <==========================================================>
	// <+++++++++++ GET floodValues by IotStation+++++++++++++++++++++++++++>
	// <==========================================================>
	// timeDuration is base reference to IoT sensor informations
	// timeStep in second
	// detect grid for buffer grid(0 -> 1*1 , 1 -> 3*3....)
	public List<Double> getIoTDetectValues(String locationID, int timeStep, int detectGrid) throws ParseException {
		if (this.IoTSensorInformations.containsKey(locationID)) {

			// initialize
			IoTSensorClass iotSensorClass = this.IoTSensorInformations.get(locationID);
			List<AtDateClass> timeList = iotSensorClass.getTimeList();
			List<Double> valueList = iotSensorClass.getValueList();
			List<Double> detectValuesList = new ArrayList<>();

			// loop for each timeStep
			for (int index = 0; index < timeList.size(); index++) {
				String asciiFloodTimeKey = timeList.get(index).addSecond(timeStep).getDateLong() + "";

				// get detected value from asciiFile
				if (floodInformations.containsKey(asciiFloodTimeKey)) {
					detectValuesList.add(getCloestValueFromDetectGrid(floodInformations.get(asciiFloodTimeKey),
							detectGrid, iotSensorClass.getX(), iotSensorClass.getY(), valueList.get(index)));
				} else {
					detectValuesList.add(0.);
				}
			}

			// return result
			return detectValuesList;
		} else {
			new Exception("*ERROR* there is no locationID : " + locationID + ", in IoTSensor informations");
			return null;
		}
	}

	public List<Double> getIoTDetectValues(String locationID, int timeStep, double bufferRadius) throws ParseException {
		String firstTimeKey = new ArrayList<>(this.floodInformations.keySet()).get(0);
		double asciiCellSize = this.floodInformations.get(firstTimeKey).getCellSize();
		int detectGrid = new BigDecimal(bufferRadius / asciiCellSize).setScale(1, RoundingMode.UP).intValue();
		return getIoTDetectValues(locationID, timeStep, detectGrid);
	}

	// <==========================================================>
	// <+++++++++GET Time Shuffle while Max Value+++++++++++++++++++++++++++>
	// <==========================================================>
	// return the time shuffle while max value happen
	// iotSensor time while max value - asciiFlood max value time
	// timeStep in second
	public double getETP(String location, int timeStep, int detectGrid) throws Exception {
		List<Double> detectValues = getIoTDetectValues(location, timeStep, detectGrid);
		int detectValuesMaxTime = new AtCommonMath(detectValues)
				.getCloestIndex(AtCommonMath.getListStatistic(detectValues, StaticsModel.getMax));

		List<Double> iotValues = this.IoTSensorInformations.get(location).getValueList();
		int iotValuesMaxTime = new AtCommonMath(iotValues)
				.getCloestIndex(AtCommonMath.getListStatistic(iotValues, StaticsModel.getMax));

		return iotValuesMaxTime - detectValuesMaxTime;
	}

	public double getETP(String location, int timeStep, double deteccLength) throws Exception {
		String firstTimeKey = new ArrayList<>(this.floodInformations.keySet()).get(0);
		double asciiCellSize = this.floodInformations.get(firstTimeKey).getCellSize();
		int detectGrid = new BigDecimal(deteccLength / asciiCellSize).setScale(1, RoundingMode.UP).intValue();
		return getETP(location, timeStep, detectGrid);
	}

	public Map<String, Double> getAllETP(int timeStep, int detectGrid) {
		List<String> locationIdList = new ArrayList<>(this.IoTSensorInformations.keySet());
		Map<String, Double> outValue = new TreeMap<>();

		// output the differ of max value (iot - simulation)
		locationIdList.forEach(locationID -> {
			try {
				outValue.put(locationID, getETP(locationID, timeStep, detectGrid));
			} catch (Exception e) {
			}
		});
		return outValue;
	}

	public Map<String, Double> getAllETP(int timeStep, double detectLength) {
		String firstTimeKey = new ArrayList<>(this.floodInformations.keySet()).get(0);
		double asciiCellSize = this.floodInformations.get(firstTimeKey).getCellSize();
		int detectGrid = new BigDecimal(detectLength / asciiCellSize).setScale(1, RoundingMode.UP).intValue();
		return getAllETP(timeStep, detectGrid);
	}

	// <==========================================================>
	// <+++++++++GET Max Differ of Max Value+++++++++++++++++++++++++++++++>
	// <==========================================================>
	// get maxValue (iotSensor - asciiFlood)
	// timeStep in second
	public double getEHP(String location, int timeStep, int detectGrid) throws ParseException, Exception {
		return this.IoTSensorInformations.get(location).getMaxValue() - AtCommonMath
				.getListStatistic(getIoTDetectValues(location, timeStep, detectGrid), StaticsModel.getMax);
	}

	public double getEHP(String location, int timeStep, double detecLength) throws ParseException, Exception {
		return this.IoTSensorInformations.get(location).getMaxValue() - AtCommonMath
				.getListStatistic(getIoTDetectValues(location, timeStep, detecLength), StaticsModel.getMax);
	}

	public Map<String, Double> getAllEHP(int timeStep, int detectGrid) {
		List<String> locationIdList = new ArrayList<>(this.IoTSensorInformations.keySet());
		Map<String, Double> outValue = new TreeMap<>();

		// output the differ of max value (iot - simulation)
		locationIdList.forEach(locationID -> {
			try {
				outValue.put(locationID, getEHP(locationID, timeStep, detectGrid));
			} catch (Exception e) {
			}
		});
		return outValue;
	}

	public Map<String, Double> getAllEHP(int timeStep, double detecLength) {
		String firstTimeKey = new ArrayList<>(this.floodInformations.keySet()).get(0);
		double asciiCellSize = this.floodInformations.get(firstTimeKey).getCellSize();
		int detectGrid = new BigDecimal(detecLength / asciiCellSize).setScale(1, RoundingMode.UP).intValue();
		return getAllEHP(timeStep, detectGrid);
	}

	// <==========================================================>
	// <+++++++++DETECT ASCII VALUES By IoTSensor +++++++++++++++++++++++++>
	// <==========================================================>
	private double getCloestValueFromDetectGrid(AsciiBasicControl ascii, int detectGrid, double x, double y,
			double detectValue) {
		List<Double> detectedValues = new ArrayList<>();

		int position[] = ascii.getPosition(x, y);
		for (int row = -1 * detectGrid; row <= 1 * detectGrid; row++) {
			for (int column = -1 * detectGrid; column <= 1 * detectGrid; column++) {
				String value = ascii.getValue(column + position[0], row + position[1]);

				if (!value.equals(ascii.getNullValue())) {
					detectedValues.add(Double.parseDouble(value));
				}
			}
		}
		try {
			return new AtCommonMath(detectedValues).getClosestValue(detectValue);
		} catch (Exception e) {
			return 0;
		}

	}

	// <==========================================================>
	// <++++++++++++++++++++ Verfivation Cluster +++++++++++++++++++++++++>
	// <==========================================================>
	public IoTVerficationCluster getIoTVerfication(String locationID, int timeStepInSecond, int detectGrid)
			throws ParseException, Exception {
		IoTVerficationCluster cluster = new IoTVerficationCluster();
		cluster.setID(locationID);
		cluster.setEHP(getEHP(locationID, timeStepInSecond, detectGrid));
		cluster.setETP(getETP(locationID, timeStepInSecond, detectGrid));
		cluster.setObservedValues(IoTSensorInformations.get(locationID).getValueList());
		cluster.setDetectValues(getIoTDetectValues(locationID, timeStepInSecond, detectGrid));
		return cluster;
	}

	public IoTVerficationCluster getIoTVerfication(String locationID, int timeStepInSecond, double detectRadius)
			throws ParseException, Exception {
		String firstTimeKey = new ArrayList<>(this.floodInformations.keySet()).get(0);
		double asciiCellSize = this.floodInformations.get(firstTimeKey).getCellSize();
		int detectGrid = new BigDecimal(detectRadius / asciiCellSize).setScale(1, RoundingMode.UP).intValue();
		return getIoTVerfication(locationID, timeStepInSecond, detectGrid);
	}

	// key : locationID
	public Map<String, IoTVerficationCluster> getAllIoTVerfication(int timeStepInSecond, int detectGrid)
			throws ParseException, Exception {

		Map<String, IoTVerficationCluster> outMap = new TreeMap<>();
		for (String locationID : IoTSensorInformations.keySet()) {
			outMap.put(locationID, getIoTVerfication(locationID, timeStepInSecond, detectGrid));
		}
		return outMap;
	}

	public Map<String, IoTVerficationCluster> getAllIoTVerfication(int timeStepInSecond, double detectRadius)
			throws ParseException, Exception {

		Map<String, IoTVerficationCluster> outMap = new TreeMap<>();
		for (String locationID : IoTSensorInformations.keySet()) {
			outMap.put(locationID, getIoTVerfication(locationID, timeStepInSecond, detectRadius));
		}
		return outMap;
	}

	public class IoTVerficationCluster {
		private String id = "";
		private double ETP = -999;
		private double EHP = -999;
		private List<Double> observedValueList = new ArrayList<>();
		private List<Double> detectedValueList = new ArrayList<>();

		public void setID(String id) {
			this.id = id;
		}

		public String getID() {
			return this.id;
		}

		public void setETP(double etp) {
			this.ETP = etp;
		}

		public double getETP() {
			return this.ETP;
		}

		public void setEHP(double ehp) {
			this.EHP = ehp;
		}

		public double getEHP() {
			return this.EHP;
		}

		public void setDetectValues(List<Double> valuesList) {
			this.detectedValueList = valuesList;
		}

		public List<Double> getDetectValues() {
			return this.detectedValueList;
		}

		public void setObservedValues(List<Double> valueList) {
			this.observedValueList = valueList;
		}

		public List<Double> getObservedValues() {
			return this.observedValueList;
		}

	}

}
