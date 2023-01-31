// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.GeoPosition;
import com.azure.maps.weather.WeatherAsyncClient;
import com.azure.maps.weather.WeatherClient;
import com.azure.maps.weather.WeatherClientBuilder;

public class GetMinuteForecast {
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

        // Get Minute Forecast -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-minute-forecast
        // Get Minute Forecast service returns minute-by-minute forecasts for a given location for the next 120 minutes.
        // Users can request weather forecasts in the interval of 1, 5 and 15 minutes.
        System.out.println("Get Minute Forecast Sync Client");
        // BEGIN: com.azure.maps.weather.sync.get_minute_forecast
        client.getMinuteForecast(new GeoPosition(-122.138874, 47.632346), 15, null);
        // END: com.azure.maps.weather.sync.get_minute_forecast

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

        // Get Minute Forecast -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-minute-forecast
        // Get Minute Forecast service returns minute-by-minute forecasts for a given location for the next 120 minutes.
        // Users can request weather forecasts in the interval of 1, 5 and 15 minutes.
        System.out.println("Get Minute Forecast Async Client");
        // BEGIN: com.azure.maps.weather.async.get_minute_forecast
        asyncClient.getMinuteForecast(new GeoPosition(-122.138874, 47.632346), 15, null);
        // END: com.azure.maps.weather.async.get_minute_forecast
    }
}
