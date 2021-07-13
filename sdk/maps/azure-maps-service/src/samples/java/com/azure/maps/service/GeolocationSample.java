package com.azure.maps.service;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.maps.service.models.ResponseFormat;
import com.fasterxml.jackson.core.JsonProcessingException;

public class GeolocationSample {
    public static void main(String[] args) throws JsonProcessingException {
        if (args.length != 1) {
            System.out.println("Usage GeolocationSample.java <ip>");
            return;
        }
        String ip = args[0];
        HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key",
                new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
        MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();
        System.out.println("Get location by ip");
        MapsCommon.print(client.getGeolocations().getIPToLocationPreview(ResponseFormat.JSON, ip));
    }
}
