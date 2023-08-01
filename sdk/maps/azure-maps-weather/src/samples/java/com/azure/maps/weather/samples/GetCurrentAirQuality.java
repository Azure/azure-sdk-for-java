// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.GeoPosition;
import com.azure.maps.weather.WeatherAsyncClient;
import com.azure.maps.weather.WeatherClient;
import com.azure.maps.weather.WeatherClientBuilder;

public class GetCurrentAirQuality {
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

        // Get Current Air Quality -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-current-air-quality
        // Get detailed information about the concentration of pollutants and overall status for current air quality.
        // Information includes, pollution levels, air quality index values, the dominant pollutant, and a brief statement summarizing risk level and suggested precautions.
        System.out.println("Get Current Air Quality Sync Client");
        // BEGIN: com.azure.maps.weather.sync.get_current_air_quality
        client.getCurrentAirQuality(
            new GeoPosition(-122.138874, 47.632346), "es", false);
        // END: com.azure.maps.weather.sync.get_current_air_quality

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

        // Get Current Air Quality -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-current-air-quality
        // Get detailed information about the concentration of pollutants and overall status for current air quality.
        // Information includes, pollution levels, air quality index values, the dominant pollutant, and a brief statement summarizing risk level and suggested precautions.
        System.out.println("Get Current Air Quality Async Client");
        // BEGIN: com.azure.maps.weather.async.get_current_air_quality
        asyncClient.getCurrentAirQuality(
            new GeoPosition(-122.138874, 47.632346), "es", false);
        // END: com.azure.maps.weather.async.get_current_air_quality
    }
}
