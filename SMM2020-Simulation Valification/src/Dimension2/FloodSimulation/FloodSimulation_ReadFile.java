package Dimension2.FloodSimulation;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import asciiFunction.AsciiBasicControl;
import asciiFunction.XYZToAscii;
import main.GlobalProperty;
import netCDF.NetcdfBasicControl;
import usualTool.AtCommonMath;
import usualTool.AtDateClass;
import usualTool.AtCommonMath.StaticsModel;

public class FloodSimulation_ReadFile {

	// key : date time in long
	private Map<String, AsciiBasicControl> floodSimulationInformation = new TreeMap<>();
	private AsciiBasicControl asciiTemplate = null;
	public static String nullValue = "-999.0000";
	public static int dataDecimal = 4;

	public FloodSimulation_ReadFile(String netcdfFile) throws IOException, ParseException {
		NetcdfBasicControl nc = new NetcdfBasicControl(netcdfFile);

		// initial valueList
		List<Object> xList = nc.getVariableValues("x");
		List<Object> yList = nc.getVariableValues("y");

		List<Object> timeList = nc.getVariableValues("time");
		List<Object> valueList = nc.getVariableValues("depth_below_surface_simulated");

		// get nullValue
		nullValue = AtCommonMath.getDecimal_String(String.valueOf(
				nc.getNetFile().findVariable("depth_below_surface_simulated").findAttribute("_FillValue").getValue(0)),
				dataDecimal);

		// get first asciiTemplate
		this.asciiTemplate = getFirstAscii(xList, yList, valueList);

		// translate each timeStep to asciiFormat
		for (int timeIndex = 0; timeIndex < valueList.size(); timeIndex++) {
			// read values
			AsciiBasicControl outAscii = this.asciiTemplate.clone();
			List<Object> yContainer = (List<Object>) valueList.get(timeIndex);
			for (int yIndex = 0; yIndex < yContainer.size(); yIndex++) {

				List<Object> xContainer = (List<Object>) yContainer.get(yIndex);
				for (int xIndex = 0; xIndex < xContainer.size(); xIndex++) {

					String temptValue = AtCommonMath.getDecimal_String((float) xContainer.get(xIndex), dataDecimal);
					if (!temptValue.equals(nullValue)) {
						outAscii.setValue((double) xList.get(xIndex), (double) yList.get(yIndex), temptValue);
					}
				}
			}

			// get time
			AtDateClass temptTime = new AtDateClass(GlobalProperty.startTime, GlobalProperty.timeFormat);
			temptTime.addMinutes(AtCommonMath.getDecimal_Int((double) timeList.get(timeIndex), dataDecimal));

			// store to "floodSimulationInformation"
			this.floodSimulationInformation.put(temptTime.getDateLong() + "", outAscii);
		}

	}

	public AsciiBasicControl getAsciiTemplate() {
		return this.asciiTemplate.clone();
	}

	public Map<String, AsciiBasicControl> getFloodSimulationInformation() {
		return this.floodSimulationInformation;
	}

	public List<String> getTimeList() {
		return new ArrayList<>(this.floodSimulationInformation.keySet());
	}

	public AsciiBasicControl getMaxAsciiFlood() throws Exception {
		AsciiBasicControl outAscii = this.asciiTemplate.clone();

		for (int row = 0; row < outAscii.getRow(); row++) {
			for (int column = 0; column < outAscii.getColumn(); column++) {
				String temptValue = outAscii.getValue(column, row);

				if (!temptValue.equals(outAscii.getNullValue())) {
					List<Double> temptValueList = new ArrayList<>();

					for (String key : this.floodSimulationInformation.keySet()) {
						temptValueList.add(
								Double.parseDouble(this.floodSimulationInformation.get(key).getValue(column, row)));
					}

					outAscii.setValue(column, row,
							AtCommonMath.getListStatistic(temptValueList, StaticsModel.getMax) + "");
				}
			}
		}

		return outAscii;
	}

	private AsciiBasicControl getFirstAscii(List<Object> xList, List<Object> yList, List<Object> valueList)
			throws IOException {
		// get cellSize
		double cellSize = Math.abs((double) xList.get(0) - (double) xList.get(1))
				+ Math.abs((double) xList.get(xList.size() - 1) - (double) xList.get(xList.size() - 2));
		cellSize = AtCommonMath.getDecimal_Double(cellSize / 2, dataDecimal);

		// read values
		List<Double[]> xyzList = new ArrayList<>();
		List<Object> yContainer = (List<Object>) valueList.get(0);
		for (int yIndex = 0; yIndex < yContainer.size(); yIndex++) {

			List<Double> xContainer = (List<Double>) yContainer.get(yIndex);
			for (int xIndex = 0; xIndex < xContainer.size(); xIndex++) {

				String temptValue = AtCommonMath.getDecimal_String(xContainer.get(xIndex) + "", dataDecimal);
				if (!temptValue.equals(nullValue)) {
					xyzList.add(new Double[] { (double) xList.get(xIndex), (double) yList.get(yIndex),
							Double.parseDouble(temptValue) });
				}
			}
		}

		// xyzFormat to asciiFormat
		XYZToAscii toAscii = new XYZToAscii(xyzList);
		toAscii.setCellSize(cellSize);
		toAscii.setNullValue(nullValue);
		toAscii.start();

		// to ascii
		return new AsciiBasicControl(toAscii.getAsciiFile());
	}
}
