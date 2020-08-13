package Dimension2.EMIC;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import Dimension2.EMIC.EMIC_ReadFile.EmicReportInformation;
import asciiFunction.AsciiBasicControl;
import main.GlobalProperty;
import usualTool.AtDateClass;

public class EMICVerification {

	// <+++++++++++++++++++++++++++>
	// <++++++++ Global Values +++++++++>
	// <+++++++++++++++++++++++++++>
	private Map<String, EmicReportInformation> emicInformations;

	// <+++++++++++++++++++++++++++>
	// <+++++++ Constructor +++++++++++>
	// <+++++++++++++++++++++++++++>
	// to check emicReport in timeStep, consider in each timeStep in (milliSecond)
	public EMICVerification(Map<String, AsciiBasicControl> floodInformations,
			Map<String, EmicReportInformation> emicInformations, int timeStep) {

		emicInformations.keySet().forEach(timeKey -> {
			EmicReportInformation emicInformation = emicInformations.get(timeKey);
			AtDateClass timeClass = emicInformation.getTime();

			// get floodAscii by the next timeStep of EMIC report time
			String asciiTimeKey = "";
			try {
				asciiTimeKey = timeClass.addSecond((int) timeStep * 1000).getDateString(GlobalProperty.timeFormat);
			} catch (ParseException e) {
				new Exception("*ERROR* timeStep error while EMIC verification");
			}

			// set floodAscii to EMIC report, if not present, set null
			Optional.ofNullable(floodInformations.get(asciiTimeKey))
					.ifPresent(ascii -> emicInformation.setFloodAscii(ascii));
		});

		this.emicInformations = emicInformations;
	}

	// to check emicReport in whole event, use maxD0
	public EMICVerification(Map<String, AsciiBasicControl> floodInformations,
			Map<String, EmicReportInformation> emicInformations) {

		AsciiBasicControl maxD0 = floodInformations.get(new ArrayList<String>(floodInformations.keySet()).get(0));
		emicInformations.keySet().forEach(timeKey -> {
			emicInformations.get(timeKey).setFloodAscii(maxD0);
		});

		this.emicInformations = emicInformations;
	}

	// <+++++++++++++++++++++++++++++++++++++++>
	// <+++++++++ Return Function +++++++++++++++++++>
	// <+++++++++++++++++++++++++++++++++++++++>
	public double getMatchPersentage(double bufferLength) {

		int matchPoint = 0;
		int onFloodSimulationPoint = 0;

		for (String timeKey : emicInformations.keySet()) {
			EmicReportInformation emicInformation = this.emicInformations.get(timeKey);

			if (emicInformation.isOnFloodSimulation()) {
				onFloodSimulationPoint++;

				if (emicInformation.isFlood(bufferLength)) {
					matchPoint++;
				}
			}
		}

		if (onFloodSimulationPoint == 0) {
			new Exception("*WARN* there is no EMIC report point on simulation area");
			return 0;
		} else {
			return (double) matchPoint / onFloodSimulationPoint;
		}
	}

	public double getPersentageInSimulationArea() {
		int notOnFloodSimulationPoint = 0;
		int onFloodSimulationPoint = 0;

		for (String timeKey : emicInformations.keySet()) {
			EmicReportInformation emicInformation = this.emicInformations.get(timeKey);

			if (emicInformation.isOnFloodSimulation()) {
				onFloodSimulationPoint++;
			} else {
				notOnFloodSimulationPoint++;
			}
		}

		if (notOnFloodSimulationPoint == 0 && onFloodSimulationPoint == 0) {
			new Exception("*WARN* there is no EMIC report point");
			return 0;
		} else {
			return (double) onFloodSimulationPoint / (notOnFloodSimulationPoint + onFloodSimulationPoint);
		}
	}
}
