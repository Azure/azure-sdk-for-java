package com.azure.maps.service;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.maps.service.models.FeatureStatesStructure;
import com.azure.maps.service.models.StatesetCreatedResponse;
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
        HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key",
                new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
        MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();
        String datasetId = args[0];

        StylesObject styles = MapsCommon.readJson(
                MapsCommon.readContent(MapsCommon.getResource("/featurestate_sample_create.json")), StylesObject.class);
        StatesetCreatedResponse result = client.getFeatureStates().createStateset(datasetId, styles);
        System.out.println("Created stateset");
        MapsCommon.print(result);
        String statesetId = result.getStatesetId();

        String featureId = "stateValueN";
        String stateKeyNames = "keyName1";
        try {
            PagedIterable<StatesetInfoObject> list = client.getFeatureStates().listStateset();
            System.out.println("List statesets:");
            for (StatesetInfoObject item : list) {
                MapsCommon.print(item);
            }

            styles = MapsCommon.readJson(
                    MapsCommon.readContent(MapsCommon.getResource("/featurestate_sample_put.json")),
                    StylesObject.class);
            client.getFeatureStates().putStateset(statesetId, styles);
            System.out.println("Updated stateset");

            FeatureStatesStructure structure = MapsCommon.readJson(
                    MapsCommon.readContent(MapsCommon.getResource("/featurestate_sample_update_states.json")),
                    FeatureStatesStructure.class);
            client.getFeatureStates().updateStates(statesetId, featureId, structure);
            System.out.println("Updated stateset");

            System.out.println(String.format("Get states with stateset_id {}", statesetId));
            MapsCommon.print(client.getFeatureStates().getStateset(statesetId));

            System.out
                    .println(String.format("Get states with stateset_id {} and feature_id {}", statesetId, featureId));
            MapsCommon.print(client.getFeatureStates().getStates(statesetId, featureId));

            client.getFeatureStates().deleteState(statesetId, featureId, stateKeyNames);
            System.out.println("Deleted state");
        } catch (HttpResponseException err) {
            System.out.println(err);
        } finally {
            client.getFeatureStates().deleteStateset(statesetId);
            System.out.println("Deleted stateset");
        }
    }
}
