package Dimension2.LocalSurveying;

import java.util.List;

import org.gdal.ogr.Geometry;

import geo.gdal.GdalGlobal;
import geo.gdal.SpatialReader;
import main.GlobalProperty;

public class LocalSurveying_ReadFile {
	private List<Geometry> geoList;

	// read a spatialFile (.shp 、 .geoJson)
	public LocalSurveying_ReadFile(String fileAdd) {
		SpatialReader sp = new SpatialReader(fileAdd);

		if (sp.getEPSG() != GlobalProperty.EPSG_LocalSurvey) {
			sp.getGeometryList().forEach(geo -> this.geoList
					.add(GdalGlobal.GeometryTranslator(geo, sp.getEPSG(), GlobalProperty.EPSG_LocalSurvey)));
		} else {
			this.geoList = sp.getGeometryList();
		}
	}

	public List<Geometry> getGeometries() {
		return this.geoList;
	}

}
