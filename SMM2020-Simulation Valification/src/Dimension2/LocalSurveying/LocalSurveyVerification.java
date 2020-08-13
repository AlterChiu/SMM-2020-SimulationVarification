package Dimension2.LocalSurveying;

import java.awt.geom.Path2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gdal.ogr.Geometry;

import asciiFunction.AsciiBasicControl;
import geo.gdal.GdalGlobal;
import main.GlobalProperty;
import usualTool.AtCommonMath;
import usualTool.AtCommonMath.StaticsModel;

public class LocalSurveyVerification {
	private List<Geometry> locatlSurveyGeoList;
	private AsciiBasicControl maxFloodAscii;

	// <=========================================>
	// <+++++++++CONSTRUCTOR++++++++++++++++++++++>
	// <=========================================>
	public LocalSurveyVerification(List<Geometry> localSurveyGeoList,
			Map<String, AsciiBasicControl> floodSimulationInformation) {

		// check coordination
		if (GlobalProperty.EPSG_NetCDF == GlobalProperty.EPSG_LocalSurvey) {
			this.locatlSurveyGeoList = localSurveyGeoList;
		} else {
			localSurveyGeoList.forEach(geo -> {
				GdalGlobal.GeometryTranslator(geo, GlobalProperty.EPSG_LocalSurvey, GlobalProperty.EPSG_NetCDF);
			});
		}

		// get maxD0
		List<String> keySet = new ArrayList<>(floodSimulationInformation.keySet());
		this.maxFloodAscii = floodSimulationInformation.get(keySet.get(keySet.size() - 1));
	}

	// <=========================================>
	// <+++++++++++ VERIFICATION++++++++++++++++++++>
	// <=========================================>
	// get match persentage
	public double getMatchPersentage(double minValue, double maxValue) throws IOException {
		int matchCount = 0;
		int missCount = 0;

		for (Geometry geo : this.locatlSurveyGeoList) {
			if (isMatch(geo, this.maxFloodAscii, minValue, maxValue)) {
				matchCount++;
			} else {
				missCount++;
			}
		}

		return (0. + matchCount) / (matchCount + missCount);
	}

	public double getMatchPersentage() throws IOException {
		return getMatchPersentage(0.001, Double.MAX_VALUE);
	}
	
	// <=========================================>
	// <+++++++++++ VERIFICATION STATIC+++++++++++++++>
	// <=========================================>
	// get average flood by polygon
	public static double getPolygonAverageValue(Geometry geo, AsciiBasicControl ascii, double minValue, double maxValue)
			throws Exception {
		return AtCommonMath.getListStatistic(getPolygonValueList(geo, ascii, minValue, maxValue), StaticsModel.getMean);
	}

	public static double getPolygonAverageValue(Geometry geo, AsciiBasicControl ascii) throws Exception {
		return AtCommonMath.getListStatistic(getPolygonValueList(geo, ascii, Double.MIN_VALUE, Double.MAX_VALUE),
				StaticsModel.getMean);
	}

	// get match count by polygon
	public static Boolean isMatch(Geometry geo, AsciiBasicControl ascii, double minValue, double maxValue)
			throws IOException {
		if (getPolygonValueList(geo, ascii, minValue, maxValue).size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	public static Boolean isMatch(Geometry geo, AsciiBasicControl ascii) throws IOException {
		return isMatch(geo, ascii, Double.MIN_VALUE, Double.MAX_VALUE);
	}

	// get values in limit by polygon
	public static List<Double> getPolygonValueList(Geometry geo, AsciiBasicControl ascii, double minValue,
			double maxValue) throws IOException {
		return ascii.getPolygonValueList(geo, minValue, maxValue);
	}

	public static List<Double> getPolygonValueList(Geometry geo, AsciiBasicControl ascii) throws IOException {
		return getPolygonValueList(geo, ascii, Double.MIN_VALUE, Double.MAX_VALUE);
	}

}
