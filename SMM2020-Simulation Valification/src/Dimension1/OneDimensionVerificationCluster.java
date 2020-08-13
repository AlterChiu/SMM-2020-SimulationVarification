package Dimension1;

import main.GlobalProperty;

public class OneDimensionVerificationCluster {

	private String id = "";
	public static double missingValue = GlobalProperty.missingValue;

	// error of waterLevel, EHp
	private double EHp = missingValue;

	// time delay while max waterLevel happening, ETp(in Second)
	private int ETp = (int) missingValue;

	// coefficient values
	private double CE = missingValue;

	public void setEHP(double EHp) {
		this.EHp = EHp;
	}

	public double getEHp() {
		return this.EHp;
	}

	public void setETp(int ETp) {
		this.ETp = ETp;
	}

	public int getETp() {
		return this.ETp;
	}

	public void setCE(double CE) {
		this.CE = CE;
	}

	public double getCE() {
		return this.CE;
	}

	public void setLocationID(String id) {
		this.id = id;
	}

	public String getLocation() {
		return this.id;
	}
}
