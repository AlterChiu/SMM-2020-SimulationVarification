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

public class FloodSimulationMax_ReadFile {
	private Map<String, AsciiBasicControl> floodSimulationInformation = new TreeMap<>();
	private AsciiBasicControl maxFloodinnformation = null;
	public static String nullValue = "-999.000";
	public static int dataDecimal = 4;

	public FloodSimulationMax_ReadFile(String netcdfFile) throws IOException, ParseException {
		NetcdfBasicControl nc = new NetcdfBasicControl(netcdfFile);

		// initial valueList
		List<Object> xList = nc.getVariableValues("x");
		List<Object> yList = nc.getVariableValues("y");

		// get cellSize
		double cellSize = Math.abs((double) xList.get(0) - (double) xList.get(1))
				+ Math.abs((double) xList.get(xList.size() - 1) - (double) xList.get(xList.size() - 2));
		cellSize = AtCommonMath.getDecimal_Double(cellSize / 2, dataDecimal);

		// get nullValue
		nullValue = AtCommonMath.getDecimal_String(String.valueOf(
				nc.getNetFile().findVariable("depth_below_surface_simulated").findAttribute("_FillValue").getValue(0)),
				dataDecimal);

		// initial ncValues
		List<Object> timeList = nc.getVariableValues("time");
		List<Object> valueList = nc.getVariableValues("Depth_Max");

		// translate each timeStep to asciiFormat
		List<Double[]> xyzList = new ArrayList<>();

		// read values
		List<Object> yContainer = (List<Object>) valueList.get(0);
		for (int yIndex = 0; yIndex < yContainer.size(); yIndex++) {

			List<Object> xContainer = (List<Object>) yContainer.get(yIndex);
			for (int xIndex = 0; xIndex < xContainer.size(); xIndex++) {
				xyzList.add(new Double[] { (double) xList.get(xIndex), (double) yList.get(yIndex),
						(double) xContainer.get(xIndex) });
			}
		}

		// get time
		AtDateClass temptTime = new AtDateClass(GlobalProperty.startTime, GlobalProperty.timeFormat);
		temptTime.addMinutes(AtCommonMath.getDecimal_Int((double) timeList.get(0), dataDecimal));

		// xyzFormat to asciiFormat
		XYZToAscii toAscii = new XYZToAscii(xyzList);
		toAscii.setCellSize(cellSize);
		toAscii.setNullValue(nullValue);
		toAscii.start();

		// store to "floodSimulationInformation"
		this.maxFloodinnformation = new AsciiBasicControl(toAscii.getAsciiFile());
		this.floodSimulationInformation.put(temptTime.getDateString(GlobalProperty.timeFormat),
				this.maxFloodinnformation);
	}

	public Map<String, AsciiBasicControl> getFloodSimulationInformation() {
		return this.floodSimulationInformation;
	}

	public AsciiBasicControl getMaxFloodAscii() {
		return this.maxFloodinnformation;
	}
}
