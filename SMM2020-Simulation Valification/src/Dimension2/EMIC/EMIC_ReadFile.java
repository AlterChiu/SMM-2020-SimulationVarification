package Dimension2.EMIC;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gdal.ogr.Geometry;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import asciiFunction.AsciiBasicControl;
import geo.common.CoordinateTranslate;
import geo.gdal.GdalGlobal;
import main.GlobalProperty;
import usualTool.AtDateClass;
import usualTool.AtFileReader;

public class EMIC_ReadFile {
	// key : date time in long
	private Map<String, EmicReportInformation> emicReport = new LinkedHashMap<>();
	private static String timeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

	// read EMIC file in JSON format
	// coordinate in TWD97
	public EMIC_ReadFile(String jsonFileAdd)
			throws JsonIOException, JsonSyntaxException, FileNotFoundException, IOException {
		JsonArray jsonContent = new AtFileReader(jsonFileAdd).getJson().getAsJsonArray();

		jsonContent.forEach(element -> {
			EmicReportInformation temptReport = new EmicReportInformation();
			JsonObject temptJsonObject = element.getAsJsonObject();

			temptReport.setID(temptJsonObject.get("CASE_ID").getAsString());

			// translate wgs84 to twd97
			double coordinateWGS84[] = new double[] { temptJsonObject.get("X").getAsDouble(),
					temptJsonObject.get("Y").getAsDouble() };
			double coordinateTWD97[] = CoordinateTranslate.Wgs84ToTwd97(coordinateWGS84[0], coordinateWGS84[1]);
			temptReport.setX(coordinateTWD97[0]);
			temptReport.setY(coordinateTWD97[1]);

			try {
				temptReport.setTime(temptJsonObject.get("CASE_DT").getAsString(), timeFormat);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			this.emicReport.put(temptReport.getTime().getDateLong() + "", temptReport);
		});

	}

	// <+++++++++++++++++++++++++++++++++++++++>
	// <+++++++++ Return Function +++++++++++++++++++>
	// <+++++++++++++++++++++++++++++++++++++++>
	public Map<String, EmicReportInformation> getEmicInformation() {
		return this.emicReport;
	}

	public List<String> getTimeList() {
		return new ArrayList<>(this.emicReport.keySet());
	}

	// <+++++++++++++++++++++++++++++++++++++++>
	// <+++++++++ Private Class +++++++++++++++++++++>
	// <+++++++++++++++++++++++++++++++++++++++>
	public class EmicReportInformation {
		private String id = "";
		private double x = GlobalProperty.missingValue;
		private double y = GlobalProperty.missingValue;
		private AtDateClass date;
		private AsciiBasicControl floodAscii;

		public void setID(String id) {
			this.id = id;
		}

		public String getID() {
			return this.id;
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

		public void setTime(String date, String timeFormat) throws ParseException {
			this.date = new AtDateClass(date, timeFormat);
		}

		public AtDateClass getTime() {
			return this.date;
		}

		public void setFloodAscii(AsciiBasicControl ascii) {
			this.floodAscii = ascii;
		}

		public AsciiBasicControl getFloodAscii() {
			return this.floodAscii;
		}

		public Boolean isFlood(double bufferLength) {

			// check coordination
			if (this.x == GlobalProperty.missingValue) {
				new Exception("*ERROR* coordinate X is missing while checkgin flooding in EMIC id : " + this.id);
				return false;
			}
			if (this.y == GlobalProperty.missingValue) {
				new Exception("*ERROR* coordinate Y is missing while checkgin flooding in EMIC id : " + this.id);
				return false;
			}

			// create buffer geometry
			Geometry bufferGeo = GdalGlobal.CreatePoint(this.x, this.y).Buffer(bufferLength);

			// check is missing
			try {
				String value = this.floodAscii.getValue(bufferGeo);
				if (!value.equals(this.floodAscii.getNullValue()) && Double.parseDouble(value) > 0) {
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				new Exception(
						"*ERROR* there is error while checking flooding, no floodAsc or time not correct , in EMIC id "
								+ this.id);
				return false;
			}
		}

		public Boolean isOnFloodSimulation() {
			try {
				return this.floodAscii.isContain(this.x, this.y);
			} catch (Exception e) {
				new Exception(
						"*ERROR* there is error while checking flooding, no floodAsc or time not correct , in EMIC id "
								+ this.id);
				return false;
			}

		}

	}

}
