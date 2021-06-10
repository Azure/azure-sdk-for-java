package com.azure.maps.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
		Mobilities mobility = MapsCommon.createMapsClient().getMobilities();
		getMetroAreaInfoPreview(mobility);
		getMetroAreaPreview(mobility);
		getNearbyTransitPreview(mobility);
		getRealTimeArrivalsPreview(mobility);
	    List<String> itineraryIds = getTransitRoutePreview(mobility);
	    for(String id : itineraryIds) {
	    	getTransitItineraryPreview(mobility, id);
	    }
	    getTransitLineInfoPreview(mobility);
	    getTransitStopInfoPreview(mobility);
	}
	
	public static void getMetroAreaInfoPreview(Mobilities mobility) throws JsonProcessingException{
	    MetroAreaInfoResponse result = mobility.getMetroAreaInfoPreview(
		        ResponseFormat.JSON, 121, Arrays.asList(MetroAreaDetailType.AGENCIES));
	    System.out.println("Get metro area info");
	    MapsCommon.print(result);
	}
	
	
	public static void getMetroAreaPreview(Mobilities mobility) throws JsonProcessingException{
		MetroAreaResponse result = mobility.getMetroAreaPreview(
		        ResponseFormat.JSON, "40.648677,-74.010535", MetroAreaQueryType.POSITION, null);
	    System.out.println("Get metro area");
	    MapsCommon.print(result);
	}
	
	
	public static void getNearbyTransitPreview(Mobilities mobility) throws JsonProcessingException{
		NearbyTransitResponse result = mobility.getNearbyTransitPreview(ResponseFormat.JSON, "40.693393,-73.988310");
	    System.out.println("Get nearby transit");
	    MapsCommon.print(result);
	}
	
	
	public static void getRealTimeArrivalsPreview(Mobilities mobility) throws JsonProcessingException{
		RealTimeArrivalsResponse result = mobility.getRealTimeArrivalsPreview(ResponseFormat.JSON, "121---19919516");
	    System.out.println("Get realtime arrivals");
	    MapsCommon.print(result);
	}
	
	
	public static void getTransitItineraryPreview(Mobilities mobility, String itineraryId) throws JsonProcessingException{
		TransitItineraryResponse result = mobility.getTransitItineraryPreview(
	        ResponseFormat.JSON, itineraryId, Arrays.asList(TransitItineraryDetailType.GEOMETRY), null);
	    System.out.println("Get transit itinerary:");
	    MapsCommon.print(result);
	}
	
	
	public static void getTransitLineInfoPreview(Mobilities mobility) throws JsonProcessingException{

		TransitLineInfoResponse result = mobility.getTransitLineInfoPreview(
	        ResponseFormat.JSON, "121---373227", null, Arrays.asList(TransitLineDetailType.STOPS), null);
	    System.out.println("Get transit line info");
	    MapsCommon.print(result);
	}
	
	
	public static List<String> getTransitRoutePreview(Mobilities mobility) throws JsonProcessingException{
		TransitRouteResponse result = mobility.getTransitRoutePreview(
	        ResponseFormat.JSON, "41.948437, -87.655334", "41.878876, -87.635918", null, OriginType.POSITION, 
	        DestinationType.POSITION, Arrays.asList(ModeType.PUBLIC_TRANSIT), 
	        Arrays.asList(TransitTypeFilter.BUS), null, null, null, null, null, null, null);
	    System.out.println("Get transit route");
	    MapsCommon.print(result);
	    return result.getResults().stream().map(itineraryResult -> itineraryResult.getItineraryId()).collect(Collectors.toList());
	}
	
	public static void getTransitStopInfoPreview(Mobilities mobility) throws JsonProcessingException{
		TransitStopInfoResponse result = mobility.getTransitStopInfoPreview(
		        ResponseFormat.JSON, "121---14013676", null, TransitStopQueryType.STOP_ID, Arrays.asList(TransitStopDetailType.LINES), null);
	    System.out.println("Get transit stop");
	    MapsCommon.print(result);
	}

}
