// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.maps.weather.WeatherAsyncClient;
import com.azure.maps.weather.WeatherClient;
import com.azure.maps.weather.WeatherClientBuilder;
import com.azure.maps.weather.models.ActiveStorm;
import com.azure.maps.weather.models.ActiveStormResult;
import com.azure.maps.weather.models.TropicalStormForecastOptions;

public class GetTropicalStormForecast {
    public static void main(String[] args) {
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        // builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET
        // env variables
        // DefaultAzureCredential tokenCredential = new
        // DefaultAzureCredentialBuilder().build();

        WeatherClient client = new WeatherClientBuilder()
                .credential(keyCredential)
                .weatherClientId(System.getenv("MAPS_CLIENT_ID"))
                .buildClient();

        // Get Tropical Storm Forecast -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-tropical-storm-forecast
        // Get individual government-issued tropical storm forecasts.
        // Information about the forecasted tropical storms includes, location, status,
        // date the forecast was created, window, wind speed and wind radii.
        System.out.println("Get Tropical Storm Forecast Sync Client");
        // BEGIN: com.azure.maps.weather.sync.get_tropical_storm_forecast
        ActiveStormResult result = client.getTropicalStormActive();
        if (result.getActiveStorms().size() > 0) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            TropicalStormForecastOptions forecastOptions = new TropicalStormForecastOptions(storm.getYear(),
                    storm.getBasinId(), storm.getGovernmentId())
                    .setIncludeWindowGeometry(true);
            client.getTropicalStormForecast(forecastOptions);
        }
        // END: com.azure.maps.weather.sync.get_tropical_storm_forecast

        // Authenticates using subscription key
        AzureKeyCredential asyncClientKeyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        // builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET
        // env variables
        // DefaultAzureCredential asyncClientTokenCredential = new
        // DefaultAzureCredentialBuilder().build();

        WeatherAsyncClient asyncClient = new WeatherClientBuilder()
                .credential(asyncClientKeyCredential)
                .weatherClientId(System.getenv("MAPS_CLIENT_ID"))
                .buildAsyncClient();

        // Get Tropical Storm Forecast -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-tropical-storm-forecast
        // Get individual government-issued tropical storm forecasts.
        // Information about the forecasted tropical storms includes, location, status,
        // date the forecast was created, window, wind speed and wind radii.
        System.out.println("Get Tropical Storm Forecast Async Client");
        // BEGIN: com.azure.maps.weather.async.get_tropical_storm_forecast
        ActiveStormResult activeStormResult = client.getTropicalStormActive();
        if (activeStormResult.getActiveStorms().size() > 0) {
            ActiveStorm storm = activeStormResult.getActiveStorms().get(0);
            TropicalStormForecastOptions forecastOptions = new TropicalStormForecastOptions(storm.getYear(),
                    storm.getBasinId(), storm.getGovernmentId())
                    .setIncludeWindowGeometry(true);
            asyncClient.getTropicalStormForecast(forecastOptions);
        }
        // END: com.azure.maps.weather.async.get_tropical_storm_forecast
    }
}
