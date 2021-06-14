package com.azure.maps.service;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.fasterxml.jackson.core.JsonProcessingException;

public class WFSSample {
    public static void main(String[] args) throws JsonProcessingException {
        if (args.length != 1) {
            System.out.println("Usage WFSSample.java <dataset_id>");
            return;
        }
        String datasetId = args[0];
        HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key",
                new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
        MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();

        client.getWFS().deleteFeature(datasetId, "facility", "FCL39");
        System.out.println("Delete Feature");

        System.out.println("Get Collection");
        MapsCommon.print(client.getWFS().getCollection(datasetId, "facility"));

        System.out.println("Get Collection public static voidinition");
        MapsCommon.print(client.getWFS().getCollectionDefinition(datasetId, "facility"));

        System.out.println("Get Collections");
        MapsCommon.print(client.getWFS().getCollections(datasetId));

        System.out.println("Get Conformance");
        MapsCommon.print(client.getWFS().getConformance(datasetId));

        System.out.println("Get Feature");
        MapsCommon.print(client.getWFS().getFeature(datasetId, "unit", "UNIT39"));

        System.out.println("Get Features");
        MapsCommon.print(client.getWFS().getFeatures(datasetId, "unit", 1, "-123,46,-120,47", null));

        System.out.println("Get Landing Page");
        MapsCommon.print(client.getWFS().getLandingPage(datasetId));
    }
}
