// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.GeoPosition;
import com.azure.maps.weather.WeatherAsyncClient;
import com.azure.maps.weather.WeatherClient;
import com.azure.maps.weather.WeatherClientBuilder;

public class GetHourlyForecast {
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

        // Get Hourly Forecast -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-hourly-forecast
        // Request detailed weather forecast by the hour for the next 1, 12, 24 (1 day), 72 (3 days), 120 (5 days),
        // and 240 hours (10 days) for the given the given coordinate location.
        // The API returns details such as temperature, humidity, wind, precipitation, and ultraviolet (UV) index.
        System.out.println("Get Hourly Forecast Sync Client");
        // BEGIN: com.azure.maps.weather.sync.get_hourly_forecast
        client.getHourlyForecast(new GeoPosition(-122.138874, 47.632346), null, 12, null);
        // END: com.azure.maps.weather.sync.get_hourly_forecast

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

        // Get Hourly Forecast -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-hourly-forecast
        // Request detailed weather forecast by the hour for the next 1, 12, 24 (1 day), 72 (3 days), 120 (5 days),
        // and 240 hours (10 days) for the given the given coordinate location.
        // The API returns details such as temperature, humidity, wind, precipitation, and ultraviolet (UV) index.
        System.out.println("Get Hourly Forecast Async Client");
        // BEGIN: com.azure.maps.weather.async.get_hourly_forecast
        asyncClient.getHourlyForecast(new GeoPosition(-122.138874, 47.632346), null, 12, null);
        // END: com.azure.maps.weather.async.get_hourly_forecast
    }
}
