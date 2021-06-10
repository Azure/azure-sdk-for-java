package com.azure.maps.service;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.maps.service.models.AliasCreateResponseV2;
import com.azure.maps.service.models.AliasListItem;
import com.azure.maps.service.models.FeatureStateObject;
import com.azure.maps.service.models.FeatureStatesStructure;
import com.azure.maps.service.models.StatesetCreatedResponse;
import com.azure.maps.service.models.StatesetGetResponse;
import com.azure.maps.service.models.StatesetInfoObject;
import com.azure.maps.service.models.StylesObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class FeaturestateSample {
	public static void main(String[] args) throws JsonMappingException, JsonProcessingException {
		if (args.length != 1) {
			System.out.println("Usage FeaturestateSample.java <dataset_id>");
			return;
		}
		FeatureStates featureState = MapsCommon.createMapsClient().getFeatureStates();
		String datasetId = args[0];
	    String statesetId = createStateset(featureState, datasetId);
	    String featureId = "stateValueN";
	    String stateKeyNames = "keyName1";
	    try {
	    	listStateset(featureState);
	    	putStateset(featureState, statesetId);
	    	//updateStates(featureState, statesetId, featureId);
	    	getStateset(featureState, statesetId);
	    	//getStates(featureState, statesetId, featureId);
	    	//deleteState(featureState, statesetId, featureId, stateKeyNames);
		} catch(HttpResponseException err) {
			System.out.println(err);
		} finally {
			deleteStateset(featureState, statesetId);
		}
	}

	public static String createStateset(FeatureStates featureState, String datasetId) throws JsonMappingException, JsonProcessingException {
		StylesObject styles = MapsCommon.readJson(MapsCommon.readContent(MapsCommon.getResource("/featurestate_sample_create.json")), StylesObject.class);
        StatesetCreatedResponse result = featureState.createStateset(datasetId, styles);
        System.out.println("Created stateset");
	    MapsCommon.print(result);
        return result.getStatesetId();
	}
	
	public static void deleteState(FeatureStates featureState, String statesetId, String featureId, String stateKeyName){
		featureState.deleteState(statesetId, featureId, stateKeyName);
	    System.out.println("Deleted state");
	}
	
	public static void deleteStateset(FeatureStates featureState, String statesetId){
		featureState.deleteStateset(statesetId);
	    System.out.println("Deleted stateset");
	}
	
	public static void getStates(FeatureStates featureState, String statesetId, String featureId) throws JsonProcessingException{
		FeatureStatesStructure result = featureState.getStates(statesetId, featureId);
		System.out.println(String.format("Get states with stateset_id {} and feature_id {}",
				statesetId, featureId));
	    MapsCommon.print(result);
	}
	
	public static void getStateset(FeatureStates featureState, String statesetId) throws JsonProcessingException{
		StatesetGetResponse result = featureState.getStateset(statesetId);
		System.out.println(String.format("Get states with stateset_id {}", statesetId));
	    MapsCommon.print(result);
	}
	
	public static void listStateset(FeatureStates featureState) throws JsonProcessingException{
		PagedIterable<StatesetInfoObject> list = featureState.listStateset();
		System.out.println("List statesets:");
		for (StatesetInfoObject item : list) {
		    MapsCommon.print(item);
		}
	}
	
	public static void putStateset(FeatureStates featureState, String statesetId) throws JsonMappingException, JsonProcessingException{
		StylesObject styles = MapsCommon.readJson(MapsCommon.readContent(MapsCommon.getResource("/featurestate_sample_put.json")), StylesObject.class);
        featureState.putStateset(statesetId, styles);
        System.out.println("Updated stateset");
	}
	
	public static void updateStates(FeatureStates featureState, String statesetId, String featureId) throws JsonMappingException, JsonProcessingException{
		FeatureStatesStructure structure = MapsCommon.readJson(MapsCommon.readContent(MapsCommon.getResource("/featurestate_sample_update_states.json")), FeatureStatesStructure.class);
	    featureState.updateStates(statesetId, featureId, structure);
		System.out.println("Updated stateset");
	}
}
