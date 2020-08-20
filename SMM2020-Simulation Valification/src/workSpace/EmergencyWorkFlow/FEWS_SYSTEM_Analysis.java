
package workSpace.EmergencyWorkFlow;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.gdal.ogr.Geometry;
import asciiFunction.AsciiBasicControl;
import geo.common.CoordinateTranslate;
import geo.gdal.GdalGlobal;
import geo.gdal.GdalGlobal_DataFormat;
import geo.gdal.SpatialReader;
import geo.gdal.raster.GDAL_RASTER_Merge;
import geo.gdal.raster.GDAL_RASTER_TranslateCoordinate;
import geo.gdal.raster.GDAL_RASTER_TranslateFormat;
import usualTool.AtCommonMath;
import usualTool.AtCommonMath.StaticsModel;
import usualTool.AtFileReader;
import usualTool.AtFileWriter;
import usualTool.DateValueStorage;
import usualTool.FileFunction;
import usualTool.TimeTranslate;
import workSpace.EmergencyWorkFlow.IoTSensorVerification.IoTVerficationCluster;
import workSpace.EmergencyWorkFlow.IoTSensor_ReadFile.IoTSensorClass;
public class FEWS_SYSTEM_Analysis {
	/*
	 * WORKSPACE CONSTRUCTOR
	 * 
	 * 
	 * WORKSPACE >
	 * 
	 * @@ Counties>
	 * 
	 * ---------- EventAscii
	 * 
	 * ---------- FloodSurveying
	 * 
	 * ---------- SystemResult>
	 * 
	 * -------------------- TimeSteps
	 * 
	 * -------------------- TimeSteps_Comparison>
	 * 
	 * -----------------------------鄉鎮統計
	 * 
	 * -----------------------------熱點
	 * 
	 * -------------------- TimeSteps_Max
	 * 
	 * -------------------- TimeSteps_Max_TWD97
	 * 
	 * ---------- EventFlood.nc
	 * 
	 * --------- hotPointLocation.csv
	 * 
	 * --------- IoTBuffer_WGS.geoJson
	 * 
	 * --------- IoTLocation.csv
	 * 
	 * --------- IoTVerification.csv
	 * 
	 * --------- IoTVerification_IsFlood.csv
	 * 
	 * --------- MaxFlood_Event.asc
	 * 
	 * --------- MaxFlood_System.asc
	 * 
	 * --------- Polygon.shp
	 * 
	 * --------- statics_CountyMaxFlood_SysteOutput.csv
	 * 
	 * @@ IoT>
	 * 
	 * ------------ IoT_Position.csv
	 * 
	 * ------------ IoT_Values_1H.csv
	 * 
	 * ------------ IoTValues_Original.csv
	 * 
	 * ------------ IoT整ˋ裡表單.csv
	 * 
	 * 
	 */
	public static String workSpace = "E:\\LittleProject\\報告書\\109 - 淹水預警平台之建置與整合\\豪雨事件\\2020-05-21 1200+32H\\";
	public static String iotWorkSpace = workSpace + "\\IOT\\";
	public static String hotPointWorkSpace = workSpace + "\\HotPoint\\";
	// event
	public static String dateFormate = "yyyy/MM/dd HH:mm";
	public static String eventStart = "2020/05/21 12:00";
	public static String eventEnd = "2020/05/22 20:00";
	public static String countiesList[] = new String[] { "Yilan", "Yulin", "Pingtung", "Kaohsiung", "Tainan" };
//	public static String folderList[] = new String[] { "Tainan" };

	public static void main(String[] args) throws Exception {
	}
	// <++++++++++++++++++++++++++++>
	// <STATICS>
	// <++++++++++++++++++++++++++++>

	/*
	 *
	 * PARAMETER
	 * 
	 * input :
	 * 
	 * EventAscii(Folder) : the result of the event simulation
	 * 
	 * IoTLocation.csv : IoT stations in this county
	 * 
	 * IoTValues_1H.csv : IoT values in hour, only the stations in county
	 * 
	 * 
	 * output :
	 * 
	 * IoTVerification.csv : statics result
	 * 
	 */
	public static void EventSimulation_IoTStatics() throws Exception {
		for (String countuFolder : countiesList) {
			String countyFolder = workSpace + countuFolder;
			// IoT
			String iotPosition = countyFolder + "\\IoTLocation.csv";
			String iotValues = iotWorkSpace + "\\IoTValues_1H.csv";
			IoTSensor_ReadFile iotRead = new IoTSensor_ReadFile(iotPosition, iotValues);
			Map<String, IoTSensorClass> iotImformations = iotRead.getIoTSensorInformation();
			// Ascii
			Map<String, AsciiBasicControl> asciiMap = new TreeMap<>();
			String asciiFolder = countyFolder + "\\EventAscii\\";
			for (String fileName : new File(asciiFolder).list()) {
				String keyName = fileName.split(".asc")[0];
				asciiMap.put(keyName, new AsciiBasicControl(asciiFolder + fileName));
			}
			// verfication
			IoTSensorVerification verification = new IoTSensorVerification(iotImformations, asciiMap);
			Map<String, IoTVerficationCluster> verifications = verification.getAllIoTVerfication(3600, 3);
			// verfication selected
			List<String> locationList = new ArrayList<>();
			for (String location : iotImformations.keySet()) {
				if (iotImformations.get(location).getMaxValue() >= 0.1) {
					locationList.add(location);
				}
			}
			// output
			List<String[]> outputList = new ArrayList<>();
			List<String> timeList = new ArrayList<>();
			for (String key : asciiMap.keySet()) {
				timeList.add(TimeTranslate.getDateString(Long.parseLong(key), "yyyy/MM/dd HH:mm"));
			}
			// output: title
			List<String> title = new ArrayList<>();
			title.add("");
			locationList.forEach(e -> {
				title.add(e);
				title.add("");
			});
			outputList.add(title.parallelStream().toArray(String[]::new));
//			// output: EHP
//			List<String> EHP = new ArrayList<>();
//			EHP.add("EHP(meter)");
//			locationList.forEach(locationID -> {
//				EHP.add(selected.get(locationID).getEHP() + "");
//				EHP.add("");
//			});
//			outputList.add(EHP.parallelStream().toArray(String[]::new));
//
//			// output: ETP
//			List<String> ETP = new ArrayList<>();
//			ETP.add("ETP(hour)");
//			locationList.forEach(locationID -> {
//				ETP.add(selected.get(locationID).getETP() + "");
//				ETP.add("");
//			});
//			outputList.add(ETP.parallelStream().toArray(String[]::new));
			// output : classified
			List<String> classified = new ArrayList<>();
			classified.add("");
			locationList.forEach(e -> {
				classified.add("Observed");
				classified.add("Simulated");
			});
			outputList.add(classified.parallelStream().toArray(String[]::new));
			// output : Values
			for (int time = 0; time < timeList.size(); time++) {
				List<String> timeLine = new ArrayList<>();
				timeLine.add(timeList.get(time));
				for (String locationID : locationList) {
					timeLine.add(verifications.get(locationID).getObservedValues().get(time) + ""); // observed;
					timeLine.add(verifications.get(locationID).getDetectValues().get(time) + ""); // Detect;
				}
				outputList.add(timeLine.parallelStream().toArray(String[]::new));
			}
			// output : Save
			new AtFileWriter(outputList.parallelStream().toArray(String[][]::new),
					countyFolder + "//IoTVerification.csv").csvWriter();
		}
	}

	// EXTRA WORKS
	// -------------------------------------------------------
	public static void FloodSurvey_KaohsiungStatics() throws IOException {
		String floodSurveyFolder = workSpace + "\\Kaohsiung\\FloodSurveying\\";
		AsciiBasicControl eventAscii = new AsciiBasicControl(workSpace + "\\Kaohsiung\\MaxFlood_Event.asc");
		AsciiBasicControl systemAscii = new AsciiBasicControl(workSpace + "\\Kaohsiung\\MaxFlood_System.asc");
		List<Geometry> geoList;
		/*
		 * none level
		 */
		geoList = new SpatialReader(floodSurveyFolder + "FloodSurveying_none.shp").getGeometryList();
		double noneLevelArea = 0.0;
		double noneLevel_eventArea = 0.0;
		double noneLevel_SystemArea = 0.0;
		for (Geometry geo : geoList) {
			noneLevelArea = noneLevelArea + geo.Area();
			noneLevel_eventArea = noneLevel_eventArea
					+ eventAscii.getCount(geo) * eventAscii.getCellSize() * eventAscii.getCellSize();
			noneLevel_SystemArea = noneLevel_SystemArea
					+ systemAscii.getCount(geo) * systemAscii.getCellSize() * systemAscii.getCellSize();
		}
		System.out.print(noneLevelArea / 10000.);
		System.out.print("\t" + noneLevel_SystemArea / 10000.);
		System.out.print("\t" + noneLevel_SystemArea / noneLevelArea * 100);
		System.out.print("\t" + noneLevel_eventArea / 10000.);
		System.out.print("\t" + noneLevel_eventArea / noneLevelArea * 100);
		System.out.println();
		/*
		 * level 0.5
		 */
		geoList = new SpatialReader(floodSurveyFolder + "FloodSurveying_0.5.shp").getGeometryList();
		double levelArea_50 = 0.0;
		double levelArea_50_event = 0.0;
		double levelArea_50_System = 0.0;
		for (Geometry geo : geoList) {
			levelArea_50 = levelArea_50 + geo.Area();
			levelArea_50_event = levelArea_50_event
					+ eventAscii.getCount(geo) * eventAscii.getCellSize() * eventAscii.getCellSize();
			levelArea_50_System = levelArea_50_System
					+ systemAscii.getCount(geo) * systemAscii.getCellSize() * systemAscii.getCellSize();
		}
		System.out.print(levelArea_50 / 10000.);
		System.out.print("\t" + levelArea_50_System / 10000.);
		System.out.print("\t" + levelArea_50_System / levelArea_50 * 100);
		System.out.print("\t" + levelArea_50_event / 10000.);
		System.out.print("\t" + levelArea_50_event / levelArea_50 * 100);
		System.out.println();
		/*
		 * level 0.5-1
		 */
		geoList = new SpatialReader(floodSurveyFolder + "FloodSurveying_0.5_1.shp").getGeometryList();
		double levelArea_50_100 = 0.0;
		double levelArea_50_100_event = 0.0;
		double levelArea_50_100_System = 0.0;
		for (Geometry geo : geoList) {
			levelArea_50_100 = levelArea_50_100 + geo.Area();
			levelArea_50_100_event = levelArea_50_100_event
					+ eventAscii.getCount(geo) * eventAscii.getCellSize() * eventAscii.getCellSize();
			levelArea_50_100_System = levelArea_50_100_System
					+ systemAscii.getCount(geo) * systemAscii.getCellSize() * systemAscii.getCellSize();
		}
		System.out.print(levelArea_50_100 / 10000.);
		System.out.print("\t" + levelArea_50_100_System / 10000.);
		System.out.print("\t" + levelArea_50_100_System / levelArea_50_100 * 100);
		System.out.print("\t" + levelArea_50_100_event / 10000.);
		System.out.print("\t" + levelArea_50_100_event / levelArea_50_100 * 0100);
		System.out.println();
		/*
		 * level 1-1.5
		 */
		geoList = new SpatialReader(floodSurveyFolder + "FloodSurveying_1_1.5.shp").getGeometryList();
		double levelArea_100_150 = 0.0;
		double levelArea_100_150_event = 0.0;
		double levelArea_100_150_System = 0.0;
		for (Geometry geo : geoList) {
			levelArea_100_150 = levelArea_100_150 + geo.Area();
			levelArea_100_150_event = levelArea_100_150_event
					+ eventAscii.getCount(geo) * eventAscii.getCellSize() * eventAscii.getCellSize();
			levelArea_100_150_System = levelArea_100_150_System
					+ systemAscii.getCount(geo) * systemAscii.getCellSize() * systemAscii.getCellSize();
		}
		System.out.print(levelArea_100_150 / 10000.);
		System.out.print("\t" + levelArea_100_150_System / 10000.);
		System.out.print("\t" + levelArea_100_150_System / levelArea_100_150 * 100);
		System.out.print("\t" + levelArea_100_150_event / 10000.);
		System.out.print("\t" + levelArea_100_150_event / levelArea_100_150 * 100);
		System.out.println();
		/*
		 * level 1.5-2
		 */
		geoList = new SpatialReader(floodSurveyFolder + "FloodSurveying_1.5_2.shp").getGeometryList();
		double levelArea_150_200 = 0.0;
		double levelArea_150_200_event = 0.0;
		double levelArea_150_200_System = 0.0;
		for (Geometry geo : geoList) {
			levelArea_150_200 = levelArea_150_200 + geo.Area();
			levelArea_150_200_event = levelArea_150_200_event
					+ eventAscii.getCount(geo) * eventAscii.getCellSize() * eventAscii.getCellSize();
			levelArea_150_200_System = levelArea_150_200_System
					+ systemAscii.getCount(geo) * systemAscii.getCellSize() * systemAscii.getCellSize();
		}
		System.out.print(levelArea_150_200 / 10000.);
		System.out.print("\t" + levelArea_150_200_System / 10000.);
		System.out.print("\t" + levelArea_150_200_System / levelArea_150_200 * 100);
		System.out.print("\t" + levelArea_150_200_event / 10000.);
		System.out.print("\t" + levelArea_150_200_event / levelArea_150_200 * 100);
		;
		System.out.println();
		/*
		 * level 2-2.5
		 */
		geoList = new SpatialReader(floodSurveyFolder + "FloodSurveying_2_2.5.shp").getGeometryList();
		double levelArea_200_250 = 0.0;
		double levelArea_200_250_event = 0.0;
		double levelArea_200_250_System = 0.0;
		for (Geometry geo : geoList) {
			levelArea_200_250 = levelArea_200_250 + geo.Area();
			levelArea_200_250_event = levelArea_200_250_event
					+ eventAscii.getCount(geo) * eventAscii.getCellSize() * eventAscii.getCellSize();
			levelArea_200_250_System = levelArea_200_250_System
					+ systemAscii.getCount(geo) * systemAscii.getCellSize() * systemAscii.getCellSize();
		}
		System.out.print(levelArea_200_250 / 10000.);
		System.out.print("\t" + levelArea_200_250_System / 10000.);
		System.out.print("\t" + levelArea_200_250_System / levelArea_200_250 * 100);
		System.out.print("\t" + levelArea_200_250_event / 10000.);
		System.out.print("\t" + levelArea_200_250_event / levelArea_200_250 * 100);
		;
		System.out.println();
		/*
		 * level 2.5
		 */
		geoList = new SpatialReader(floodSurveyFolder + "FloodSurveying_2.5.shp").getGeometryList();
		double levelArea_250 = 0.0;
		double levelArea_250_event = 0.0;
		double levelArea_250_System = 0.0;
		for (Geometry geo : geoList) {
			levelArea_250 = levelArea_250 + geo.Area();
			levelArea_250_event = levelArea_250_event
					+ eventAscii.getCount(geo) * eventAscii.getCellSize() * eventAscii.getCellSize();
			levelArea_250_System = levelArea_250_System
					+ systemAscii.getCount(geo) * systemAscii.getCellSize() * systemAscii.getCellSize();
		}
		System.out.print(levelArea_250 / 10000.);
		System.out.print("\t" + levelArea_250_System / 10000.);
		System.out.print("\t" + levelArea_250_System / levelArea_250 * 100);
		System.out.print("\t" + levelArea_250_event / 10000.);
		System.out.print("\t" + levelArea_250_event / levelArea_250 * 100);
		System.out.println();
	}

	public static void FloodSurvey_PingtungStatics() throws IOException {
		String floodSurveyFolder = workSpace + "\\Pingtung\\FloodSurveying\\";
		AsciiBasicControl eventAscii = new AsciiBasicControl(workSpace + "\\Pingtung\\MaxFlood_Event.asc");
		AsciiBasicControl systemAscii = new AsciiBasicControl(workSpace + "\\Pingtung\\MaxFlood_System.asc");
		List<Geometry> geoList;
		/*
		 * none level
		 */
		geoList = new SpatialReader(floodSurveyFolder + "FloodSurveying.shp").getGeometryList();
		double noneLevelArea = 0.0;
		double noneLevel_eventArea = 0.0;
		double noneLevel_SystemArea = 0.0;
		for (Geometry geo : geoList) {
			noneLevelArea = noneLevelArea + geo.Area();
			noneLevel_eventArea = noneLevel_eventArea
					+ eventAscii.getCount(geo) * eventAscii.getCellSize() * eventAscii.getCellSize();
			noneLevel_SystemArea = noneLevel_SystemArea
					+ systemAscii.getCount(geo) * systemAscii.getCellSize() * systemAscii.getCellSize();
		}
		System.out.print(noneLevelArea / 10000.);
		System.out.print("\t" + noneLevel_SystemArea / 10000.);
		System.out.print("\t" + noneLevel_SystemArea / noneLevelArea * 100);
		System.out.print("\t" + noneLevel_eventArea / 10000.);
		System.out.print("\t" + noneLevel_eventArea / noneLevelArea * 100);
		System.out.println();
	}
	// <++++++++++++++++++++++++++++>
	// <DATA PREPARE>
	// <++++++++++++++++++++++++++++>

	/*
	 * PARAMETER
	 * 
	 * divide each location station to each county
	 * 
	 * 
	 * 
	 * input :
	 * 
	 * IoT_Position.csv
	 * 
	 * Polygon.shp
	 * 
	 * 
	 * 
	 * output :
	 * 
	 * IoTLocation.csv
	 * 
	 */
	public static void IoTStation_LocationDivision() throws IOException {
		String IoTLocation[][] = new AtFileReader(iotWorkSpace + "\\IoT_Position.csv").getCsv(1, 0);
		for (String county : countiesList) {
			String countyFolder = workSpace + county + "\\";
			SpatialReader sp = new SpatialReader(countyFolder + "Polygon.shp");
			List<Geometry> polygons = sp.getGeometryList();
			List<Map<String, Object>> attrTables = sp.getAttributeTable();
			Geometry mergePolygon = GdalGlobal.mergePolygons(sp.getGeometryList());
			List<String[]> countyOutput = new ArrayList<>();
			countyOutput.add(new String[] { "CT_Name", "ID", "Name", "WGS_X", "WGS_Y", "TWD_X", "TWDY" });
			for (String temptLine[] : IoTLocation) {
				double wgs_x = Double.parseDouble(temptLine[2]);
				double wgs_y = Double.parseDouble(temptLine[3]);
				List<String> outList = new ArrayList<>(Arrays.asList(temptLine));
				Geometry wgs_iotGeo = GdalGlobal.CreatePoint(wgs_x, wgs_y);
				Geometry twd_iotGeo = GdalGlobal.GeometryTranslator(wgs_iotGeo, GdalGlobal.WGS84_prj4,
						GdalGlobal.TWD97_121_prj4);
				if (mergePolygon.Contains(twd_iotGeo)) {
					for (int index = 0; index < polygons.size(); index++) {
						if (polygons.get(index).Contains(twd_iotGeo)) {
							outList.add(0, attrTables.get(index).get("CT_Name") + "");
							outList.add(twd_iotGeo.GetX() + "");
							outList.add(twd_iotGeo.GetY() + "");
							countyOutput.add(outList.parallelStream().toArray(String[]::new));
							break;
						}
					}
				}
			}
			new AtFileWriter(countyOutput.parallelStream().toArray(String[][]::new), countyFolder + "IoTLocation.csv")
					.csvWriter();
		}
	}

	/*
	 * PARAMETER
	 * 
	 * sort IoT_Values to hour
	 * 
	 * input :
	 * 
	 * IoTValues_Original.csv : original values with date, which date format in
	 * "yyyy/MM/dd HH:mm"
	 * 
	 * 
	 * 
	 * output :
	 * 
	 * IoTValues_1H : result
	 * 
	 */
	public static void IotValues_ReSotrage() throws ParseException, IOException {
		String originslIoTValuesAdd = iotWorkSpace + "\\IoTValues_Original.csv";
		String saveIoTValuesAdd = iotWorkSpace + "\\IoTValues_1H.csv";
		DateValueStorage date = new DateValueStorage(new AtFileReader(originslIoTValuesAdd).getCsv(1, 0),
				"yyyy/MM/dd HH:mm");
		date.setStartDate(eventStart, dateFormate);
		date.setEndDate(eventEnd, dateFormate);
		date.saveToHour(saveIoTValuesAdd, StaticsModel.getMax);
	}

	public static void IoTStatics_IsFlood() throws Exception {
		for (String county : folderList) {
			System.out.println(county);
			String countyFolder = workSpace + county + "\\";
			// ascii
			AsciiBasicControl ascii_SystemMax = new AsciiBasicControl(countyFolder + "MaxFlood_System.asc");
			AsciiBasicControl ascii_EventMax = new AsciiBasicControl(countyFolder + "MaxFlood_Event.asc");
			// iot
			String iotValues = "E:\\LittleProject\\報告書\\109 - 淹水預警平台之建置與整合\\豪雨事件\\2020-05-21 1200+32H\\IOT\\IoTValues_1H.csv";
			String iotPosition = countyFolder + "\\IoTLocation.csv";
			String iotLocations[][] = new AtFileReader(iotPosition).getCsv(1, 0);
			IoTSensor_ReadFile iotRead = new IoTSensor_ReadFile(iotPosition, iotValues);
			Map<String, IoTSensorClass> iotImformation = iotRead.getIoTSensorInformation();
			List<String[]> outList = new ArrayList<>();
			outList.add(new String[] { "ID", "address", "twdX", "twdY", "Observed", "Historical", "Forecast" });
			for (String temptLine[] : iotLocations) {
				List<String> temptOut = new ArrayList<>();
				String id = temptLine[1];
				String name = temptLine[2];
				double x = Double.parseDouble(temptLine[5]);
				double y = Double.parseDouble(temptLine[6]);
				Geometry bufferGeo = GdalGlobal.CreatePoint(x, y).Buffer(60.0);
				temptOut.add(id);
				temptOut.add(name);
				temptOut.add(x + "");
				temptOut.add(y + "");
				temptOut.add(iotImformation.get(id).getMaxValue() + "");
				temptOut.add(AtCommonMath.getListStatistic(ascii_EventMax.getPolygonValueList(bufferGeo),
						StaticsModel.getMax) + "");
				temptOut.add(AtCommonMath.getListStatistic(ascii_SystemMax.getPolygonValueList(bufferGeo),
						StaticsModel.getMax) + "");
				outList.add(temptOut.parallelStream().toArray(String[]::new));
			}
			new AtFileWriter(outList.parallelStream().toArray(String[][]::new),
					countyFolder + "\\IoTVerification_IsFlood.csv").tabWriter();
		}
	}

	/*
	 * PARAMETER
	 * 
	 * divide HotPoint location to each county
	 * 
	 * input :
	 * 
	 * 淹水熱點 - 熱點位置.csv
	 * 
	 * polygon.shp : polygon of each counties boundary
	 * 
	 * 
	 * 
	 * 
	 * output:
	 * 
	 * hotPointLocation: result
	 */
	public static void Hotpoint_LocationDivision() throws IOException {
		String hotPointLocation[][] = new AtFileReader(hotPointWorkSpace + "\\淹水熱點 - 熱點位置.csv").getCsv(1, 0);
		for (String county : countiesList) {
			String countyFolder = workSpace + county + "\\";
			SpatialReader sp = new SpatialReader(countyFolder + "Polygon.shp");
			List<Geometry> polygons = sp.getGeometryList();
			List<Map<String, Object>> attrTables = sp.getAttributeTable();
			Geometry mergePolygon = GdalGlobal.mergePolygons(sp.getGeometryList());
			List<String[]> countyOutput = new ArrayList<>();
			countyOutput.add(new String[] { "縣市", "鄉鎮", "村", "路口", "TWD_X", "TWDY" });
			for (String temptLine[] : hotPointLocation) {
				double x = Double.parseDouble(temptLine[4]);
				double y = Double.parseDouble(temptLine[5]);
				List<String> outList = new ArrayList<>(Arrays.asList(temptLine));
				outList.remove(1);
				Geometry iotGeo = GdalGlobal.CreatePoint(x, y);
				if (mergePolygon.Contains(iotGeo)) {
					for (int index = 0; index < polygons.size(); index++) {
						if (polygons.get(index).Contains(iotGeo)) {
							outList.add(1, attrTables.get(index).get("CT_Name") + "");
							countyOutput.add(outList.parallelStream().toArray(String[]::new));
							break;
						}
					}
				}
			}
			new AtFileWriter(countyOutput.parallelStream().toArray(String[][]::new),
					countyFolder + "hotPointLocation.csv").csvWriter();
		}
	}

	public static void HotpointStatics_SystemOutput() throws IOException {
		for (String folder : folderList) {
			System.out.println(folder);
			// timeList
			List<String> timeList = new ArrayList<>();
			// asciiList
			List<AsciiBasicControl> asciiList = new ArrayList<>();
			String asciiFolder = workSpace + folder + "\\SystemResult\\TimeSteps_Max\\";
			for (String asciiName : new File(asciiFolder).list()) {
				asciiList.add(new AsciiBasicControl(asciiFolder + asciiName));
				timeList.add(asciiName.replace(".asc", ""));
			}
			// HotPoints
			String hotPointContent[][] = new AtFileReader(workSpace + folder + "\\hotPointLocation.csv").getCsv(1, 0);
			List<Geometry> polygons = new ArrayList<>();
			for (String temptLine[] : hotPointContent) {
				polygons.add(GdalGlobal.CreatePoint(Double.parseDouble(temptLine[4]), Double.parseDouble(temptLine[5]))
						.Buffer(3000));
			}
			// statics
			for (int index = 0; index < asciiList.size(); index++) {
				// outList
				List<String[]> outList = new ArrayList<>();
				List<String> title = new ArrayList<>();
				title.add("縣市");
				title.add("鄉鎮");
				title.add("村");
				title.add("路口");
				title.add("TWD_X");
				title.add("TWD_Y");
				title.add("FloodDepth(m)");
				title.add("Area_10-30cm(公頃)");
				title.add("Area_30-50cm(公頃)");
				title.add("Area_50cm以上(公頃)");
				outList.add(title.parallelStream().toArray(String[]::new));
				for (int pointIndex = 0; pointIndex < polygons.size(); pointIndex++) {
					List<String> polygonStatics = new ArrayList<>(Arrays.asList(hotPointContent[pointIndex]));
					String temptValue = AtCommonMath.getDecimal_String(
							asciiList.get(index).getValue(polygons.get(pointIndex), 0.01, Double.MAX_VALUE), 3);
					String temptArea_10 = AtCommonMath
							.getDecimal_String(asciiList.get(index).getCount(polygons.get(pointIndex), 0.1, 0.3)
									* Math.pow(asciiList.get(index).getCellSize(), 2) / 10000., 3);
					String temptArea_30 = AtCommonMath
							.getDecimal_String(asciiList.get(index).getCount(polygons.get(pointIndex), 0.3, 0.5)
									* Math.pow(asciiList.get(index).getCellSize(), 2) / 10000., 3);
					String temptArea_50 = AtCommonMath.getDecimal_String(
							asciiList.get(index).getCount(polygons.get(pointIndex), 0.5, Double.MAX_VALUE)
									* Math.pow(asciiList.get(index).getCellSize(), 2) / 10000.,
							3);
					polygonStatics.add(temptValue);
					polygonStatics.add(temptArea_10);
					polygonStatics.add(temptArea_30);
					polygonStatics.add(temptArea_50);
					outList.add(polygonStatics.parallelStream().toArray(String[]::new));
				}
				new AtFileWriter(outList.parallelStream().toArray(String[][]::new), workSpace + folder
						+ "\\SystemResult\\TimeSteps_Comparison\\熱點\\" + timeList.get(index) + "_CountyMaxFlood.csv")
								.csvWriter();
			}
		}
	}

	public static void EventNC_To_Ascii() throws IOException, ParseException {
		for (String folder : folderList) {
			String countyFolder = workSpace + folder;
			FloodSimulation_ReadFile ncRead = new FloodSimulation_ReadFile(countyFolder + "\\EventFlood.nc");
			Map<String, AsciiBasicControl> asciiMap = ncRead.getFloodSimulationInformation();
			for (String key : asciiMap.keySet()) {
				new AtFileWriter(asciiMap.get(key).getAsciiFile(), countyFolder + "\\EventAscii\\" + key + ".asc")
						.textWriter(" ");
			}
		}
	}

	public static void EventAscii_To_Max() throws Exception {
		for (String county : folderList) {
			String countyFolder = workSpace + county + "\\";
			String eventOutAsciiFolder = countyFolder + "\\EventAscii\\";
			List<String> asciiFileAddList = new ArrayList<>();
			for (String asciiFileName : new File(eventOutAsciiFolder).list()) {
				asciiFileAddList.add(eventOutAsciiFolder + asciiFileName);
			}
			new AtFileWriter(AsciiBasicControl.getMaxAscii(asciiFileAddList).getAsciiFile(),
					countyFolder + "MaxFlood_Event.asc").textWriter(" ");
		}
	}

	public static void SystemNC_To_MaxAscii() throws Exception {
		for (String folder : folderList) {
			System.out.println(folder);
			String ncFolder = workSpace + folder + "\\SystemResult\\TimeSteps\\";
			String ascFolder = workSpace + folder + "\\SystemResult\\TimeSteps_Max\\";
			FileFunction.newFolder(ascFolder);
			for (String ncFileName : new File(ncFolder).list()) {
				if (ncFileName.contains("Max")) {
					System.out.println(ncFileName);
					FloodSimulation_ReadFile ncReader = new FloodSimulation_ReadFile(ncFolder + ncFileName);
					AsciiBasicControl maxAsii = ncReader.getMaxAsciiFlood();
					ncReader.getFloodSimulationInformation().clear();
					String dateTime = ncFileName.split("CombineMaximum_")[1].replace(".nc", "");
					dateTime = TimeTranslate.getDateString(
							TimeTranslate.getDateLong(dateTime, "yyyyMMddHHmm") + 8 * 3600000, "yyyyMMddHHmm");
					new AtFileWriter(maxAsii.getAsciiFile(), ascFolder + dateTime + ".asc").textWriter(" ");
				}
			}
		}
	}

	public static void SystemAscii_To_Max() throws Exception {
		for (String county : folderList) {
			String countyFolder = workSpace + county + "\\";
			String systemOutAsciiFolder = countyFolder + "\\SystemResult\\TimeSteps_Max_TWD97\\";
			List<String> asciiFileAddList = new ArrayList<>();
			for (String asciiFileName : new File(systemOutAsciiFolder).list()) {
				asciiFileAddList.add(systemOutAsciiFolder + asciiFileName);
			}
			new AtFileWriter(AsciiBasicControl.getMaxAscii(asciiFileAddList).getAsciiFile(),
					countyFolder + "MaxFlood_System.asc").textWriter(" ");
		}
	}

	public static void rasterTranslate() throws IOException, ParseException, InterruptedException {
		String folder = "C:\\Users\\alter\\Downloads\\EventAscii\\";
		FloodSimulation_ReadFile ncRead = new FloodSimulation_ReadFile(
				"E:\\LittleProject\\報告書\\109 - 淹水預警平台之建置與整合\\豪雨事件\\2020-05-21 1200+32H\\Tainan\\EventFlood_20M.nc");
		Map<String, AsciiBasicControl> asciiMap = ncRead.getFloodSimulationInformation();
		for (String key : asciiMap.keySet()) {
			GDAL_RASTER_Merge merge = new GDAL_RASTER_Merge();
			String dem20 = folder + key + "20m.asc";
			String dem40 = folder + key + "40m.asc";
			String dem = folder + key + ".asc";
			merge.addRaster(dem20);
			merge.addRaster(dem40);
			merge.save(dem, GdalGlobal_DataFormat.DATAFORMAT_RASTER_AAIGrid);
			GDAL_RASTER_TranslateFormat translate = new GDAL_RASTER_TranslateFormat(dem);
			translate.save(dem + "fix", GdalGlobal_DataFormat.DATAFORMAT_RASTER_AAIGrid);
		}
	}

	public static void countyStastics_SystemOutput() throws Exception {
		for (String folder : folderList) {
			System.out.println(folder);
			// timeList
			List<String> timeList = new ArrayList<>();
			// asciiList
			List<AsciiBasicControl> asciiList = new ArrayList<>();
			String asciiFolder = workSpace + folder + "\\SystemResult\\TimeSteps_Max\\";
			for (String asciiName : new File(asciiFolder).list()) {
				asciiList.add(new AsciiBasicControl(asciiFolder + asciiName));
				timeList.add(asciiName.replace(".asc", ""));
			}
			// shp
			SpatialReader sp = new SpatialReader(workSpace + folder + "\\Polygon.shp");
			List<Geometry> geoList = sp.getGeometryList();
			List<Map<String, Object>> attr = sp.getAttributeTable();
			// statics
			for (int index = 0; index < asciiList.size(); index++) {
				// outList
				List<String[]> outList = new ArrayList<>();
				List<String> title = new ArrayList<>();
				title.add("CT_Name");
				title.add("FloodDepth(m)");
				title.add("Area_10-30cm(公頃)");
				title.add("Area_30-50cm(公頃)");
				title.add("Area_50cm以上(公頃)");
				outList.add(title.parallelStream().toArray(String[]::new));
				for (int county = 0; county < geoList.size(); county++) {
					List<String> polygonStatics = new ArrayList<>();
					String id = attr.get(county).get("CT_Name") + "";
					String temptValue = AtCommonMath.getDecimal_String(
							asciiList.get(index).getValue(geoList.get(county), 0.01, Double.MAX_VALUE), 3);
					String temptArea_10 = AtCommonMath
							.getDecimal_String(asciiList.get(index).getCount(geoList.get(county), 0.1, 0.3)
									* Math.pow(asciiList.get(index).getCellSize(), 2) / 10000., 3);
					String temptArea_30 = AtCommonMath
							.getDecimal_String(asciiList.get(index).getCount(geoList.get(county), 0.3, 0.5)
									* Math.pow(asciiList.get(index).getCellSize(), 2) / 10000., 3);
					String temptArea_50 = AtCommonMath
							.getDecimal_String(asciiList.get(index).getCount(geoList.get(county), 0.5, Double.MAX_VALUE)
									* Math.pow(asciiList.get(index).getCellSize(), 2) / 10000., 3);
					polygonStatics.add(id);
					polygonStatics.add(temptValue);
					polygonStatics.add(temptArea_10);
					polygonStatics.add(temptArea_30);
					polygonStatics.add(temptArea_50);
					outList.add(polygonStatics.parallelStream().toArray(String[]::new));
				}
				new AtFileWriter(outList.parallelStream().toArray(String[][]::new), workSpace + folder
						+ "\\SystemResult\\TimeSteps_Comparison\\鄉鎮統計\\" + timeList.get(index) + "_CountyMaxFlood.csv")
								.csvWriter();
			}
		}
	}

	public static void countyStastics_EventMax() throws Exception {
		for (String folder : folderList) {
			System.out.println(folder);
			// asciiList
			AsciiBasicControl eventAscii = new AsciiBasicControl(workSpace + folder + "\\MaxFlood.asc");
			// shp
			SpatialReader sp = new SpatialReader(workSpace + folder + "\\Polygon.shp");
			List<Geometry> geoList = sp.getGeometryList();
			List<Map<String, Object>> attr = sp.getAttributeTable();
			// outList
			List<String[]> outList = new ArrayList<>();
			List<String> title = new ArrayList<>();
			title.add("CT_Name");
			title.add("Event_FloodDepth(m)");
			title.add("Event_Area(公頃)");
			outList.add(title.parallelStream().toArray(String[]::new));
			// statics
			for (int index = 0; index < geoList.size(); index++) {
				System.out.print("\t" + index);
				List<String> polygonStatics = new ArrayList<>();
				String id = attr.get(index).get("CT_Name") + "";
				String eventValue = AtCommonMath
						.getDecimal_String(eventAscii.getValue(geoList.get(index), 0.01, Double.MAX_VALUE), 3);
				String eventArea_10 = AtCommonMath.getDecimal_String(eventAscii.getCount(geoList.get(index), 0.1, 0.3)
						* Math.pow(eventAscii.getCellSize(), 2) / 10000., 3);
				String eventArea_30 = AtCommonMath.getDecimal_String(eventAscii.getCount(geoList.get(index), 0.3, 0.5)
						* Math.pow(eventAscii.getCellSize(), 2) / 10000., 3);
				String eventArea_50 = AtCommonMath
						.getDecimal_String(eventAscii.getCount(geoList.get(index), 0.5, Double.MAX_VALUE)
								* Math.pow(eventAscii.getCellSize(), 2) / 10000., 3);
				polygonStatics.add(id);
				polygonStatics.add(eventValue);
				polygonStatics.add(eventArea_10);
				polygonStatics.add(eventArea_30);
				polygonStatics.add(eventArea_50);
				outList.add(polygonStatics.parallelStream().toArray(String[]::new));
			}
			System.out.println();
			new AtFileWriter(outList.parallelStream().toArray(String[][]::new),
					workSpace + folder + "\\stastics_EventReview.csv").csvWriter();
		}
	}

	/*
	 * PARAMETER
	 * 
	 * flood result of FEWS-Only System is in WGS84 this function is to translate
	 * from WGS84 to TWD97
	 * 
	 * input :
	 * 
	 * TimeSteps_Max : which max depth of flood result in each timeStep(every 3
	 * hours, so there must be time-overlap)
	 * 
	 * 
	 * 
	 * output :
	 * 
	 * TimeSteps_Max_TWD97 : result
	 */
	public static void coordinateTransalte() throws IOException, InterruptedException {
		for (String folder : countiesList) {
			String ascFolder = workSpace + folder + "\\SystemResult\\TimeSteps_Max\\";
			String translateFolder = workSpace + folder + "\\SystemResult\\TimeSteps_Max_TWD97\\";
			FileFunction.newFolder(translateFolder);
			for (String asciiFileName : new File(ascFolder).list()) {
				System.out.println(asciiFileName);
				// to tif
				GDAL_RASTER_TranslateFormat translateFormat1 = new GDAL_RASTER_TranslateFormat(
						ascFolder + asciiFileName);
				translateFormat1.save(translateFolder + asciiFileName + ".tif",
						GdalGlobal_DataFormat.DATAFORMAT_RASTER_GTiff);
				// tif coordinate translate
				GDAL_RASTER_TranslateCoordinate translate = new GDAL_RASTER_TranslateCoordinate(
						translateFolder + asciiFileName + ".tif", GdalGlobal.WGS84);
				translate.save(translateFolder + asciiFileName + "_TWD97.tif", GdalGlobal.TWD97_121);
				// .tif to asc
				GDAL_RASTER_TranslateFormat translateFormat2 = new GDAL_RASTER_TranslateFormat(
						translateFolder + asciiFileName + "_TWD97.tif");
				translateFormat2.save(translateFolder + asciiFileName, GdalGlobal_DataFormat.DATAFORMAT_RASTER_AAIGrid);
				FileFunction.delete(translateFolder + asciiFileName + "_TWD97.tif");
				FileFunction.delete(translateFolder + asciiFileName + ".tif");
			}
		}
	}

	public static void TainanEventAsciiMerge() throws IOException, InterruptedException {
		String tainanTemptFolder = workSpace + "\\Tainan\\EventAscii_temptFolder\\";
		String tainanEventFolder = workSpace + "\\Tainan\\EventAscii\\";
		for (String fileName : new File(tainanTemptFolder).list()) {
			if (fileName.contains("20m")) {
				String dem20M = tainanTemptFolder + fileName;
				String dem40M = tainanTemptFolder + fileName.replace("20m", "40m");
				GDAL_RASTER_Merge merge = new GDAL_RASTER_Merge();
				merge.addRaster(dem20M);
				merge.addRaster(dem40M);
				merge.save(tainanEventFolder + fileName.replace("20m.asc", ".tif"));
				GDAL_RASTER_TranslateFormat translate = new GDAL_RASTER_TranslateFormat(
						tainanEventFolder + fileName.replace("20m.asc", ".tif"));
				translate.save(tainanEventFolder + fileName.replace("20m", ""),
						GdalGlobal_DataFormat.DATAFORMAT_RASTER_AAIGrid);
				FileFunction.delete(tainanEventFolder + fileName.replace("20m.asc", ".tif"));
			}
		}
	}

	// =================水規所簡報==============================//
	public static void kaohsiung_SystemNC_To_AsciiList() throws IOException, ParseException {
		String sourceFolder = workSpace + "\\kaohsiung\\SystemResult\\TimeSteps\\";
		String targerFolder = workSpace + "\\水規所簡報用\\高雄預報\\";
		for (String fileName : new File(sourceFolder).list()) {
			if (fileName.contains("Combine_")) {
				String time = fileName.replace("Combine_", "").replace(".nc", "");
				FileFunction.newFolder(targerFolder + time);
				FloodSimulation_ReadFile ncReader = new FloodSimulation_ReadFile(sourceFolder + fileName);
				Map<String, AsciiBasicControl> asciiMap = ncReader.getFloodSimulationInformation();
				for (String timeKey : asciiMap.keySet()) {
					String timeKeyFormated = TimeTranslate.getDateString(Long.parseLong(timeKey), "yyyyMMddHHmm");
					new AtFileWriter(asciiMap.get(timeKey).getAsciiFile(),
							targerFolder + time + "\\" + timeKeyFormated + ".asc").textWriter(" ");
				}
			}
		}
	}

	public static void kaohsiung_SystemAscii_Statics() throws IOException {
		String id = "iot_ 1067";
		String name = "本館路188-1號前交通號誌桿";
		double wgs84_Coordinate[] = new double[] { 120.336169, 22.655494 };
		double twd97_Coordinate[] = CoordinateTranslate.Wgs84ToTwd97(wgs84_Coordinate[0], wgs84_Coordinate[1]);
		String timeList[] = new String[] { "202005211200", "202005211300", "202005211400", "202005211500",
				"202005211600", "202005211700", "202005211800", "202005211900", "202005212000", "202005212100",
				"202005212200", "202005212300", "202005220000", "202005220100", "202005220200", "202005220300",
				"202005220400", "202005220500", "202005220600", "202005220700", "202005220800", "202005220900",
				"202005221000", "202005221100", "202005221200", "202005221300", "202005221400", "202005221500",
				"202005221600", "202005221700", "202005221800", "202005221900", "202005222000" };
		double valueList[] = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.03, 0.05, 0.08, 0.11,
				0.14, 0.16, 0.19, 0.24, 0.13, 0.1, 0.07, 0.03, 0, 0, 0, 0, 0 };
		Map<String, Double> observedValueMap = new TreeMap<>();
		for (int index = 0; index < timeList.length; index++) {
			observedValueMap.put(timeList[index], valueList[index]);
		}
		// system report folder
		String systemOuputFolder = "E:\\LittleProject\\報告書\\109 - 淹水預警平台之建置與整合\\豪雨事件\\2020-05-21 1200+32H\\水規所簡報用\\高雄預報\\";
		for (String folderName : new File(systemOuputFolder).list()) {
			// start time
			System.out.print(folderName);
			for (String asciiFileName : new File(systemOuputFolder + folderName).list()) {
				String time = asciiFileName.replace(".asc", "");
				AsciiBasicControl tempAscii = new AsciiBasicControl(
						systemOuputFolder + folderName + "\\" + asciiFileName);
				AtCommonMath mathStatics = new AtCommonMath(
						tempAscii.getValueList(wgs84_Coordinate[0], wgs84_Coordinate[1], 1));
				try {
					System.out.print("\t" + mathStatics.getClosestValue(observedValueMap.get(time)));
				} catch (Exception e) {
					System.out.print("\t0");
				}
			}
			System.out.println();
		}
	}
}
