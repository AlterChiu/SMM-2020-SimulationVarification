package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.gdal.ogr.Geometry;

import Dimension1.OneDimensionVerificationCluster;
import Dimension1.DisCharge.DischargeVerification;
import Dimension1.DisCharge.Discharge_ReadFile;
import Dimension1.WaterLevel.WaterLevelVerification;
import Dimension2.EMIC.EMICVerification;
import Dimension2.EMIC.EMIC_ReadFile;
import Dimension2.FloodSimulation.FloodSimulationMax_ReadFile;
import Dimension2.FloodSimulation.FloodSimulation_ReadFile;
import Dimension2.IoTSensor.IoTSensorVerification;
import Dimension2.IoTSensor.IoTSensor_ReadFile;
import Dimension2.IoTSensor.IoTSensor_ReadFile.IoTSensorClass;
import asciiFunction.AsciiBasicControl;
import asciiFunction.XYZToAscii;
import geo.gdal.GdalGlobal;
import geo.gdal.SpatialReader;
import netCDF.NetcdfBasicControl;
import usualTool.AtDateClass;
import usualTool.AtFileWriter;

public class MainClass {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		/*
		 * test 1D
		 */
		String observeDischarge = "E:\\LittleProject\\報告書\\109 - SMM\\測試\\驗證系統\\Verification\\Observation\\1D\\Discharge\\ObservedDischarge.xml";
		String obserWaterLevel = "E:\\LittleProject\\報告書\\109 - SMM\\測試\\驗證系統\\Verification\\Observation\\1D\\WaterLevel\\ObservedWaterLevel.xml";
		String simulationDischarge = "E:\\LittleProject\\報告書\\109 - SMM\\測試\\驗證系統\\Verification\\Simulation\\1D\\Discharge\\SimulatedDischarge.xml";
		String simulationWaterLevel = "E:\\LittleProject\\報告書\\109 - SMM\\測試\\驗證系統\\Verification\\Simulation\\1D\\WaterLevel\\SimulatedWaterLevel.xml";

		/*
		 * test 2D
		 */
		String observeEMIC = "E:\\LittleProject\\報告書\\109 - SMM\\測試\\驗證系統\\Verification\\Observation\\2D\\EMIC\\EMIC.json";
		String observeIoT = "E:\\LittleProject\\報告書\\109 - SMM\\測試\\驗證系統\\Verification\\Observation\\2D\\IoTSensor\\ObservedDischarge.xml";
		String observeLocalSurvey = "E:\\LittleProject\\報告書\\109 - SMM\\測試\\驗證系統\\Verification\\Observation\\2D\\LocalSurveying\\surveyPolygon.shp";
		String simulationFlood = "E:\\LittleProject\\報告書\\109 - 淹水預警平台之建置與整合\\淹水數據測試\\雲林\\2018-08-23 0000_Combine.nc";
		String simulationFloodMax = "E:\\LittleProject\\報告書\\109 - SMM\\測試\\驗證系統\\Verification\\Simulation\\2D\\FloodResult-max\\Event_00001_Flood_Max.nc";

		/*
		 * verification
		 */
		String ncFile = "E:\\LittleProject\\報告書\\109 - 淹水預警平台之建置與整合\\淹水數據測試\\宜蘭\\2019-10-31 1600_Flood.nc";
		Map<String, AsciiBasicControl> floodSimulations = new FloodSimulation_ReadFile(ncFile)
				.getFloodSimulationInformation();

		Map<String, IoTSensorClass> iotValues = new IoTSensor_ReadFile(
				"E:\\LittleProject\\報告書\\109 - 淹水預警平台之建置與整合\\淹水數據測試\\宜蘭\\201910311600-H-30H.csv",
				"E:\\LittleProject\\報告書\\109 - 淹水預警平台之建置與整合\\淹水數據測試\\宜蘭\\宜蘭測站位置.csv").getIoTSensorInformation();
		
		iotValues.get("縣府4").getTimeList().forEach(e -> System.out.println(e.getDateLong()));
		
//
		IoTSensorVerification verification = new IoTSensorVerification(iotValues, floodSimulations);
		System.out.println("縣府3");
		verification.getIoTDetectValues("縣府3", 3600, 2).forEach(e -> System.out.print(e + "\t"));
		
		System.out.println("縣府4");
		verification.getIoTDetectValues("縣府4", 3600, 2).forEach(e -> System.out.print(e + "\t"));

//		// 1D discharge
//		System.out.println("ONE DIMENSIOB DISCHARGE");
//		System.out.println("===========================================================");
//		DischargeVerification dischargeVerified = new DischargeVerification(observeDischarge, simulationDischarge);
//		Map<String, OneDimensionVerificationCluster> dischargeVerifiedCluster = dischargeVerified
//				.getLocationsVerification();
//
//		dischargeVerifiedCluster.keySet().forEach(location -> {
//			System.out.println(location);
//			OneDimensionVerificationCluster verifiedCluster = dischargeVerifiedCluster.get(location);
//			System.out.println("CE : " + verifiedCluster.getCE()); // Coefficient
//			System.out.println("EHP : " + verifiedCluster.getEHp()); // differ of depth while max depth happened
//			System.out.println("ETP : " + verifiedCluster.getETp()); // time lag while max depth happened
//		});
//		System.out.println("");
//		System.out.println("");
//
//		// 1D discharge
//		System.out.println("ONE DIMENSIOB WATERLEVEL");
//		System.out.println("===========================================================");
//		WaterLevelVerification waterLevelVerified = new WaterLevelVerification(obserWaterLevel, simulationWaterLevel);
//		Map<String, OneDimensionVerificationCluster> waterLevelVerifiedCluster = waterLevelVerified
//				.getLocationsVerification();
//
//		waterLevelVerifiedCluster.keySet().forEach(location -> {
//			System.out.println(location);
//			OneDimensionVerificationCluster verifiedCluster = waterLevelVerifiedCluster.get(location);
//			System.out.println("CE : " + verifiedCluster.getCE()); // Coefficient
//			System.out.println("EHP : " + verifiedCluster.getEHp()); // differ of depth while max depth happened
//			System.out.println("ETP : " + verifiedCluster.getETp()); // time lag while max depth happened
//		});
//		System.out.println("");
//		System.out.println("");
//
//		/*
//		 * 2D
//		 */
//		// 2D simulation
//		FloodSimulation_ReadFile floodSimulation = new FloodSimulation_ReadFile(simulationFlood);
//		FloodSimulationMax_ReadFile maxFloodSimulation = new FloodSimulationMax_ReadFile(simulationFloodMax);
//
//		// 2D EMIC
//		System.out.println("TWO DIMENSIOB EMIC");
//		System.out.println("===========================================================");
//		EMIC_ReadFile emic = new EMIC_ReadFile(observeEMIC);
//
//		// for verification in timeStep(milliSecond)
//		System.out.println("For each timeSteps");
//		EMICVerification emicVerification = new EMICVerification(floodSimulation.getFloodSimulationInformation(),
//				emic.getEmicInformation(), 3600000); // test in 1hour
//		System.out.println("buffer250m : " + emicVerification.getMatchPersentage(250)); // buffer for 250 meters
//		System.out.println("buffer1200m : " + emicVerification.getMatchPersentage(1200)); // buffer for 1200 meters
//
//		System.out.println("");
//		System.out.println("");
//		System.out.println("For MaxD0");
//		EMICVerification emicVerificationMax = new EMICVerification(maxFloodSimulation.getFloodSimulationInformation(),
//				emic.getEmicInformation());
//		System.out.println("buffer250m : " + emicVerificationMax.getMatchPersentage(250)); // buffer for 250 meters
//		System.out.println("buffer1200m : " + emicVerificationMax.getMatchPersentage(1200)); // buffer for 1200 meters
//
//		// 2D IoT
//		System.out.println("TWO DIMENSIOB IOT");
//		System.out.println("===========================================================");
//		IoTSensor_ReadFile iot = new IoTSensor_ReadFile(observeIoT);
//		IoTSensorVerification iotVerification = new IoTSensorVerification(iot.getIoTSensorInformation(),
//				floodSimulation.getFloodSimulationInformation());
//		// timeStep in milliSecond
//		// detected grid for buffer(0-> 1*1 , 1-> 3*3)
//		iotVerification.getAllIoTDetectErrors(3600000, 1);
//		
//		iotVerificationCluster.keySet().forEach(location -> {
//			IoTSensorClass verifiedCluster = iotVerificationCluster.get(location);
//			System.out.println(verifiedCluster.get);
//		});
//
//		System.out.println("");
//		System.out.println("");

	}
}
