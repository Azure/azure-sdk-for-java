// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.GeoPosition;
import com.azure.maps.weather.WeatherAsyncClient;
import com.azure.maps.weather.WeatherClient;
import com.azure.maps.weather.WeatherClientBuilder;

public class GetSevereWeatherAlerts {
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

        // Get Severe Weather Alerts -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-severe-weather-alerts
        // Azure Maps Severe Weather Alerts API returns the severe weather alerts that are available worldwide
        // from both official Government Meteorological Agencies and leading global to regional weather alert providers.
        // The service can return details such as alert type, category, level and detailed description
        // about the active severe alerts for the requested location, like hurricanes, thunderstorms, lightning, heat waves or forest fires.
        System.out.println("Get Severe Weather Alerts Sync Client");
        // BEGIN: com.azure.maps.weather.sync.get_severe_weather_alerts
        client.getSevereWeatherAlerts(new GeoPosition(-85.06431274043842, 30.324604968788467), null, true);
        // END: com.azure.maps.weather.sync.get_severe_weather_alerts

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

        // Get Severe Weather Alerts -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-severe-weather-alerts
        // Azure Maps Severe Weather Alerts API returns the severe weather alerts that are available worldwide
        // from both official Government Meteorological Agencies and leading global to regional weather alert providers.
        // The service can return details such as alert type, category, level and detailed description
        // about the active severe alerts for the requested location, like hurricanes, thunderstorms, lightning, heat waves or forest fires.
        System.out.println("Get Severe Weather Alerts Async Client");
        // BEGIN: com.azure.maps.weather.async.get_severe_weather_alerts
        asyncClient.getSevereWeatherAlerts(new GeoPosition(-85.06431274043842, 30.324604968788467), null, true);
        // END: com.azure.maps.weather.async.get_severe_weather_alerts
    }
}
