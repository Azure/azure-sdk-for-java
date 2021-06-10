package com.azure.maps.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.maps.service.models.DestinationType;
import com.azure.maps.service.models.MetroAreaDetailType;
import com.azure.maps.service.models.MetroAreaInfoResponse;
import com.azure.maps.service.models.MetroAreaQueryType;
import com.azure.maps.service.models.MetroAreaResponse;
import com.azure.maps.service.models.ModeType;
import com.azure.maps.service.models.NearbyTransitResponse;
import com.azure.maps.service.models.OriginType;
import com.azure.maps.service.models.RealTimeArrivalsResponse;
import com.azure.maps.service.models.ResponseFormat;
import com.azure.maps.service.models.TransitItineraryDetailType;
import com.azure.maps.service.models.TransitItineraryResponse;
import com.azure.maps.service.models.TransitLineDetailType;
import com.azure.maps.service.models.TransitLineInfoResponse;
import com.azure.maps.service.models.TransitRouteResponse;
import com.azure.maps.service.models.TransitStopDetailType;
import com.azure.maps.service.models.TransitStopInfoResponse;
import com.azure.maps.service.models.TransitStopQueryType;
import com.azure.maps.service.models.TransitTypeFilter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class MobilitySample {

	public static void main(String[] args) throws JsonProcessingException{
    	HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key", new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
    	MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();

	    System.out.println("Get metro area info");
	    MapsCommon.print(client.getMobilities().getMetroAreaInfoPreview(
		        ResponseFormat.JSON, 121, Arrays.asList(MetroAreaDetailType.AGENCIES)));

	    System.out.println("Get metro area");
	    MapsCommon.print(client.getMobilities().getMetroAreaPreview(
		        ResponseFormat.JSON, "40.648677,-74.010535", MetroAreaQueryType.POSITION, null));

	    System.out.println("Get nearby transit");
	    MapsCommon.print(client.getMobilities().getNearbyTransitPreview(ResponseFormat.JSON, "40.693393,-73.988310"));
	    

	    System.out.println("Get realtime arrivals");
	    MapsCommon.print(client.getMobilities().getRealTimeArrivalsPreview(ResponseFormat.JSON, "121---19919516"));

	    System.out.println("Get transit line info");
	    MapsCommon.print(client.getMobilities().getTransitLineInfoPreview(
		        ResponseFormat.JSON, "121---373227", null, Arrays.asList(TransitLineDetailType.STOPS), null));

	    System.out.println("Get transit stop");
	    MapsCommon.print(client.getMobilities().getTransitStopInfoPreview(
		        ResponseFormat.JSON, "121---14013676", null, TransitStopQueryType.STOP_ID, Arrays.asList(TransitStopDetailType.LINES), null));

	    
		TransitRouteResponse result = client.getMobilities().getTransitRoutePreview(
	        ResponseFormat.JSON, "41.948437, -87.655334", "41.878876, -87.635918", null, OriginType.POSITION, 
	        DestinationType.POSITION, Arrays.asList(ModeType.PUBLIC_TRANSIT), 
	        Arrays.asList(TransitTypeFilter.BUS), null, null, null, null, null, null, null);
	    System.out.println("Get transit route");
	    MapsCommon.print(result);
	    List<String> itineraryIds = result.getResults().stream().map(itineraryResult -> itineraryResult.getItineraryId()).collect(Collectors.toList());
	    
	    for(String itineraryId : itineraryIds) {
		    System.out.println("Get transit itinerary:");
		    MapsCommon.print(client.getMobilities().getTransitItineraryPreview(
			        ResponseFormat.JSON, itineraryId, Arrays.asList(TransitItineraryDetailType.GEOMETRY), null));
	    }
	}

}
