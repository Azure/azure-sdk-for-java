// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.GeoPosition;
import com.azure.maps.weather.WeatherAsyncClient;
import com.azure.maps.weather.WeatherClient;
import com.azure.maps.weather.WeatherClientBuilder;
import com.azure.maps.weather.models.DailyDuration;

public class GetAirQualityDailyForecasts {
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

        // Get Air Quality Daily Forecasts - 
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-air-quality-daily-forecasts
        // Get detailed information about the concentration of pollutants and overall status of forecasted daily air quality.
        // The service can provide forecasted daily air quality information for the upcoming 1 to 7 days.
        // Information includes, pollution levels, air quality index values, the dominant pollutant, and a brief statement summarizing risk level and suggested precautions.
        System.out.println("Get Air Quality Daily Forecasts Sync Client");
        // BEGIN: com.azure.maps.weather.sync.get_air_quality_daily_forecasts
        client.getAirQualityDailyForecasts(
            new GeoPosition(-122.138874, 47.632346), "en", DailyDuration.TWO_DAYS);
        // END: com.azure.maps.weather.sync.get_air_quality_daily_forecasts

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

        // Get Air Quality Daily Forecasts -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-air-quality-daily-forecasts
        // Get detailed information about the concentration of pollutants and overall status of forecasted daily air quality.
        // The service can provide forecasted daily air quality information for the upcoming 1 to 7 days.
        // Information includes, pollution levels, air quality index values, the dominant pollutant, and a brief statement summarizing risk level and suggested precautions.
        System.out.println("Get Air Quality Daily Forecasts Async Client");
        // BEGIN: com.azure.maps.weather.async.get_air_quality_daily_forecasts
        asyncClient.getAirQualityDailyForecasts(
            new GeoPosition(-122.138874, 47.632346), "en", DailyDuration.TWO_DAYS);
        // END: com.azure.maps.weather.async.get_air_quality_daily_forecasts
    }
}
