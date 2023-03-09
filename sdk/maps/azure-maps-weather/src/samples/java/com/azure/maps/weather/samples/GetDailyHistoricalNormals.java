// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.samples;

import java.time.LocalDate;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.GeoPosition;
import com.azure.maps.weather.WeatherAsyncClient;
import com.azure.maps.weather.WeatherClient;
import com.azure.maps.weather.WeatherClientBuilder;

public class GetDailyHistoricalNormals {
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

        // Get Daily Historical Normals -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-daily-historical-normals
        // Get Daily Historical Normals service returns climatology data such as past daily normal temperatures,
        // precipitation and cooling/heating degree day information for the day at a given coordinate location.
        // Normals are a 30-year average for temperatures and precipitation for a specific location.
        System.out.println("Get Daily Historical Normals Sync Client");
        // BEGIN: com.azure.maps.weather.sync.get_daily_historical_normals
        LocalDate before = LocalDate.now().minusDays(30);
        LocalDate today = LocalDate.now();
        client.getDailyHistoricalNormals(new GeoPosition(30.0734812, 62.6490341), before, today, null);
        // END: com.azure.maps.weather.sync.get_daily_historical_normals

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

        // Get Daily Historical Normals -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-daily-historical-normals
        // Get Daily Historical Normals service returns climatology data such as past daily normal temperatures,
        // precipitation and cooling/heating degree day information for the day at a given coordinate location.
        // Normals are a 30-year average for temperatures and precipitation for a specific location.
        System.out.println("Get Daily Historical Normals Async Client");
        // BEGIN: com.azure.maps.weather.async.get_daily_historical_normals
        LocalDate beforeDate = LocalDate.now().minusDays(30);
        LocalDate todayDate = LocalDate.now();
        asyncClient.getDailyHistoricalNormals(new GeoPosition(30.0734812, 62.6490341), beforeDate, todayDate, null);
        // END: com.azure.maps.weather.async.get_daily_historical_normals
    }
}
