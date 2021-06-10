package com.azure.maps.service;

import java.io.IOException;

import com.azure.core.http.rest.Response;
import com.azure.maps.service.models.BatchRequestBody;
import com.azure.maps.service.models.GetRouteRangeResponse;
import com.azure.maps.service.models.PostRouteDirectionsRequestBody;
import com.azure.maps.service.models.PostRouteMatrixRequestBody;
import com.azure.maps.service.models.ResponseFormat;
import com.azure.maps.service.models.RouteDirectionsBatchResponse;
import com.azure.maps.service.models.RouteDirectionsResponse;
import com.azure.maps.service.models.RouteMatrixResponse;
import com.azure.maps.service.models.RouteType;
import com.azure.maps.service.models.RoutesPostRouteDirectionsBatchResponse;
import com.azure.maps.service.models.TextFormat;
import com.fasterxml.jackson.core.JsonProcessingException;

public class RouteSample {

	public static void main(String[] args) throws IOException {
		Routes route = MapsCommon.createMapsClient().getRoutes();

	    getRouteDirections(route);
	    getRouteRange(route);
	    postRouteDirections(route);
	    postRouteDirectionsBatch(route);
	    postRouteMatrix(route);
	}


	public static void getRouteDirections(Routes route) throws JsonProcessingException{
		RouteDirectionsResponse result = route.getRouteDirections(
	        TextFormat.JSON, "52.50931,13.42936:52.50274,13.43872");
	    System.out.println("Get route directions");
	    MapsCommon.print(result);
	}
	
	public static void getRouteRange(Routes route) throws JsonProcessingException{
		GetRouteRangeResponse result = route.getRouteRange(
	        TextFormat.JSON, "50.97452,5.86605", null, null, 6000.0f, null, null, null, null, null, null, null, null, null, null, null, null, 
	        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
	    System.out.println("Get route range");
	    MapsCommon.print(result);
	}
	
	public static void postRouteDirections(Routes route) throws JsonProcessingException{
	    PostRouteDirectionsRequestBody contentJson = MapsCommon.readJson(MapsCommon.readContent(MapsCommon.getResource("/route_directions_request_body.json")), PostRouteDirectionsRequestBody.class);
	    RouteDirectionsResponse result = route.postRouteDirections(
	        TextFormat.JSON, "52.50931,13.42936:52.50274,13.43872", contentJson);
	    System.out.println("Post route directions");
	    MapsCommon.print(result);
	}
	
	public static void postRouteDirectionsBatch(Routes route) throws JsonProcessingException{
		BatchRequestBody contentJson = MapsCommon.readJson(MapsCommon.readContent(MapsCommon.getResource("/route_directions_batch_request_body.json")), BatchRequestBody.class);
		RoutesPostRouteDirectionsBatchResponse result = route.postRouteDirectionsBatchWithResponseAsync(ResponseFormat.JSON, contentJson).block();
	    System.out.println("Post route directions batch");
	    String location = result.getDeserializedHeaders().getLocation();
	    String operationId = MapsCommon.getUid(location);
	    Response<RouteDirectionsBatchResponse> operation = null;
	    do {
	    	operation = route.getRouteDirectionsBatchWithResponseAsync(operationId).block();
	    } while (operation.getStatusCode() != 200 && operation.getStatusCode() == 202);
	    if (operation.getStatusCode() == 200) {
		    MapsCommon.print(result.getValue());
	    }
	}
	
	public static void postRouteMatrix(Routes route) throws JsonProcessingException{
		PostRouteMatrixRequestBody contentJson = MapsCommon.readJson(MapsCommon.readContent(MapsCommon.getResource("/route_matrix_request_body.json")), PostRouteMatrixRequestBody.class);
		RouteMatrixResponse result = route.postRouteMatrix(
	    		ResponseFormat.JSON, contentJson, Boolean.TRUE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, RouteType.SHORTEST, null);
	    System.out.println("Post route matrix");
	    MapsCommon.print(result);
	}
}
