// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.GeoPosition;
import com.azure.maps.weather.WeatherAsyncClient;
import com.azure.maps.weather.WeatherClient;
import com.azure.maps.weather.WeatherClientBuilder;

public class GetDailyIndices {
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

        // Get Daily Indices -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-daily-indices
        // Azure Maps Indices API returns index values that will guide end users to plan future activities.
        // For example, a health mobile application can notify users that today is good weather for
        // running or for other outdoors activities like for playing golf,
        // and retail stores can optimize their digital marketing campaigns based on predicted index values.
        System.out.println("Get Daily Indices Sync Client");
        // BEGIN: com.azure.maps.weather.sync.get_daily_indices
        client.getDailyIndices(new GeoPosition(-79.37849, 43.84745), null, null, null, 11);
        // END: com.azure.maps.weather.sync.get_daily_indices

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

        // Get Daily Indices -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-daily-indices
        // Azure Maps Indices API returns index values that will guide end users to plan future activities.
        // For example, a health mobile application can notify users that today is good weather for
        // running or for other outdoors activities like for playing golf,
        // and retail stores can optimize their digital marketing campaigns based on predicted index values.
        System.out.println("Get Daily Indices Async Client");
        // BEGIN: com.azure.maps.weather.async.get_daily_indices
        asyncClient.getDailyIndices(new GeoPosition(-79.37849, 43.84745), null, null, null, 11);
        // END: com.azure.maps.weather.async.get_daily_indices
    }
}
