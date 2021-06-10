// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.maps.service;

import java.util.Arrays;

import com.azure.maps.service.models.BoundingBoxResult;
import com.azure.maps.service.models.CoordinatesPairAbbreviated;
import com.azure.maps.service.models.LinesResult;
import com.azure.maps.service.models.PointElevationResult;
import com.azure.maps.service.models.PointsResult;
import com.azure.maps.service.models.ResponseFormat;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ElevationSample{
	public static void main(String[] args) throws JsonProcessingException {
		Elevations elevation = MapsCommon.createMapsClient().getElevations();
		getDataForBoundingBox(elevation);
		// TODO cannot send multiple points for commented paths
		//getDataForPoints(elevation);
		//getDataForPolyline(elevation);
		postDataForPoints(elevation);
		postDataForPolyline(elevation);
	}

	public static void getDataForBoundingBox(Elevations elevation) throws JsonProcessingException {
		BoundingBoxResult result = elevation.getDataForBoundingBox(ResponseFormat.JSON, Arrays.asList("-121.66853362143818", "46.84646479863713", "-121.65853362143818", "46.85646479863713"), 3, 3);
	    System.out.println("Get Data for bounding box");
	    MapsCommon.print(result);
	}


	public static void getDataForPoints(Elevations elevation) throws JsonProcessingException{
		PointsResult result = elevation.getDataForPoints(ResponseFormat.JSON, Arrays.asList("-121.66853362143818,46.84646479863713"));
        System.out.println("Get Data for points");
	    MapsCommon.print(result);
	}


	public static void getDataForPolyline(Elevations elevation) throws JsonProcessingException{
		LinesResult result = elevation.getDataForPolyline(ResponseFormat.JSON, Arrays.asList("-121.66853362143818,46.84646479863713", "-121.65853362143818,46.85646479863713"));
	    System.out.println("Get Data for polyline");
	    MapsCommon.print(result);
	}

	public static void postDataForPoints(Elevations elevation) throws JsonProcessingException{
		CoordinatesPairAbbreviated coord1 = new CoordinatesPairAbbreviated().setLat(46.84646479863713).setLon(-121.66853362143818);
		CoordinatesPairAbbreviated coord2 = new CoordinatesPairAbbreviated().setLat(46.856464798637127).setLon(-121.68853362143818);
		PointsResult result = elevation.postDataForPoints(ResponseFormat.JSON, Arrays.asList(coord1, coord2));
        System.out.println("Get Data for multiple points");
	    MapsCommon.print(result);
	}


	public static void postDataForPolyline(Elevations elevation) throws JsonProcessingException{
		CoordinatesPairAbbreviated coord1 = new CoordinatesPairAbbreviated().setLat(46.84646479863713).setLon(-121.66853362143818);
		CoordinatesPairAbbreviated coord2 = new CoordinatesPairAbbreviated().setLat(46.856464798637127).setLon(-121.68853362143818);
		LinesResult result = elevation.postDataForPolyline(ResponseFormat.JSON, Arrays.asList(coord1, coord2));
	    System.out.println("Get Data for long polyline");
	    MapsCommon.print(result);
	}
}

