// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.maps.weather.WeatherAsyncClient;
import com.azure.maps.weather.WeatherClient;
import com.azure.maps.weather.WeatherClientBuilder;
import com.azure.maps.weather.models.ActiveStorm;
import com.azure.maps.weather.models.ActiveStormResult;

public class SearchTropicalStorm {
    public static void main(String[] args) {
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        // builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        WeatherClient client = new WeatherClientBuilder()
            .credential(keyCredential)
            .weatherClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildClient();

        // Get Tropical Storm Search -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-tropical-storm-search
        // Search government-issued tropical storms by year, basin ID, and government ID.
        // Information about the tropical storms includes, government ID, basin ID, status, year, name and if it is subtropical.
        System.out.println("Get Tropical Storm Search Sync Client");
        // BEGIN: com.azure.maps.weather.sync.get_tropical_storm_search
        ActiveStormResult result = client.getTropicalStormActive();
        if (result.getActiveStorms().size() > 0) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            client.searchTropicalStorm(storm.getYear(), storm.getBasinId(), storm.getGovId());
        }
        // END: com.azure.maps.weather.sync.get_tropical_storm_search

        // Authenticates using subscription key
        AzureKeyCredential asyncClientKeyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        // builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential asyncClientTokenCredential = new DefaultAzureCredentialBuilder().build();

        WeatherAsyncClient asyncClient = new WeatherClientBuilder()
            .credential(asyncClientKeyCredential)
            .weatherClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildAsyncClient();

        // Get Tropical Storm Search -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-tropical-storm-search
        // Search government-issued tropical storms by year, basin ID, and government ID.
        // Information about the tropical storms includes, government ID, basin ID, status, year, name and if it is subtropical.
        System.out.println("Get Tropical Storm Search Async Client");
        // BEGIN: com.azure.maps.weather.async.get_tropical_storm_search
        ActiveStormResult activeStormResult = asyncClient.getTropicalStormActive().block();
        if (activeStormResult.getActiveStorms().size() > 0) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            asyncClient.searchTropicalStorm(storm.getYear(), storm.getBasinId(), storm.getGovId());
        }
        // END: com.azure.maps.weather.async.get_tropical_storm_search
    }
}
