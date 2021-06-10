// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.maps.service;

import java.util.Arrays;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.maps.service.models.CoordinatesPairAbbreviated;
import com.azure.maps.service.models.LinesResult;
import com.azure.maps.service.models.ResponseFormat;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ElevationSample{
	public static void main(String[] args) throws JsonProcessingException {
    	HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key", new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
    	MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();

	    System.out.println("Get Data for bounding box");
	    MapsCommon.print(client.getElevations().getDataForBoundingBox(ResponseFormat.JSON, Arrays.asList("-121.66853362143818", "46.84646479863713", "-121.65853362143818", "46.85646479863713"), 3, 3));

        System.out.println("Get Data for points");
	    MapsCommon.print(client.getElevations().getDataForPoints(ResponseFormat.JSON, Arrays.asList("-121.66853362143818,46.84646479863713")));
		// TODO cannot send multiple points for commented paths
	    //System.out.println("Get Data for polyline");
	    //MapsCommon.print(client.getElevations().getDataForPolyline(ResponseFormat.JSON, Arrays.asList("-121.66853362143818,46.84646479863713", "-121.65853362143818,46.85646479863713")));
	    
		//CoordinatesPairAbbreviated coord1 = new CoordinatesPairAbbreviated().setLat(46.84646479863713).setLon(-121.66853362143818);
		//CoordinatesPairAbbreviated coord2 = new CoordinatesPairAbbreviated().setLat(46.856464798637127).setLon(-121.68853362143818);
        //System.out.println("Get Data for multiple points");
	    //MapsCommon.print(client.getElevations().postDataForPoints(ResponseFormat.JSON, Arrays.asList(coord1, coord2)));

		CoordinatesPairAbbreviated coord1 = new CoordinatesPairAbbreviated().setLat(46.84646479863713).setLon(-121.66853362143818);
		CoordinatesPairAbbreviated coord2 = new CoordinatesPairAbbreviated().setLat(46.856464798637127).setLon(-121.68853362143818);
		LinesResult result = client.getElevations().postDataForPolyline(ResponseFormat.JSON, Arrays.asList(coord1, coord2));
	    System.out.println("Get Data for long polyline");
	    MapsCommon.print(result);
	}
}

