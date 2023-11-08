// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.GeoPosition;
import com.azure.maps.weather.WeatherAsyncClient;
import com.azure.maps.weather.WeatherClient;
import com.azure.maps.weather.WeatherClientBuilder;

public class GetDailyForecast {
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

        // Get Daily Forecast -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-daily-forecast
        // The service returns detailed weather forecast such as temperature and wind by day for the next 1, 5, 10, 15, 25, or 45 days for a given coordinate location.
        // The response include details such as temperature, wind, precipitation, air quality, and UV index.
        System.out.println("Get Daily Forecast Sync Client");
        // BEGIN: com.azure.maps.weather.sync.get_daily_forecast
        client.getDailyForecast(new GeoPosition(30.0734812, 62.6490341), null, 5, null);
        // END: com.azure.maps.weather.sync.get_daily_forecast

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

        // Get Daily Forecast -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-daily-forecast
        // The service returns detailed weather forecast such as temperature and wind by day for the next 1, 5, 10, 15, 25, or 45 days for a given coordinate location.
        // The response include details such as temperature, wind, precipitation, air quality, and UV index.
        System.out.println("Get Daily Forecast Async Client");
        // BEGIN: com.azure.maps.weather.async.get_daily_forecast
        asyncClient.getDailyForecast(new GeoPosition(30.0734812, 62.6490341), null, 5, null);
        // END: com.azure.maps.weather.async.get_daily_forecast
    }
}
