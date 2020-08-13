package main;

public class GlobalProperty {

	public static double missingValue = -999;
	public static String timeFormat = "yyyy-MM-dd HH";
	public static String startTime = "1970-01-01 08";
	public static int timeZone = +8;
	
	// file format from fondusNetcdf
	public static int EPSG_NetCDF = 3826;
	
	// timeSeries API from Deltares
	public static int EPSG_EMIC = 4326;
	public static int EPSG_IoT = 4326;
	
	// not sure
	public static int EPSG_LocalSurvey = 4326;

}
