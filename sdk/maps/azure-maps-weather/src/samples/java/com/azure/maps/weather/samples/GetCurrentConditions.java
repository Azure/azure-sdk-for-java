// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.GeoPosition;
import com.azure.maps.weather.WeatherAsyncClient;
import com.azure.maps.weather.WeatherClient;
import com.azure.maps.weather.WeatherClientBuilder;

public class GetCurrentConditions {
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

        // Get Current Conditions -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-current-conditions
        // Get Current Conditions service returns detailed current weather conditions such as precipitation, temperature and wind for a given coordinate location.
        // Also, observations from the past 6 or 24 hours for a particular location can be retrieved.
        System.out.println("Get Current Conditions Sync Client");
        // BEGIN: com.azure.maps.weather.sync.get_current_conditions
        client.getCurrentConditions(new GeoPosition(-122.125679, 47.641268),
            null, true, null, null);
        // END: com.azure.maps.weather.sync.get_current_conditions

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

        // Get Current Conditions -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-current-conditions
        // Get Current Conditions service returns detailed current weather conditions such as precipitation, temperature and wind for a given coordinate location.
        // Also, observations from the past 6 or 24 hours for a particular location can be retrieved.
        System.out.println("Get Current Conditions Async Client");
        // BEGIN: com.azure.maps.weather.async.get_current_conditions
        asyncClient.getCurrentConditions(new GeoPosition(-122.125679, 47.641268),
            null, true, null, null);
        // END: com.azure.maps.weather.async.get_current_conditions
    }
}
