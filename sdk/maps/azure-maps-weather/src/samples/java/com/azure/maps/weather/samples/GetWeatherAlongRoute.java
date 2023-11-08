// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.samples;

import java.util.Arrays;
import java.util.List;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.GeoPosition;
import com.azure.maps.weather.WeatherAsyncClient;
import com.azure.maps.weather.WeatherClient;
import com.azure.maps.weather.WeatherClientBuilder;
import com.azure.maps.weather.models.Waypoint;

public class GetWeatherAlongRoute {
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

        // Get Weather Along Route -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-weather-along-route
        // Weather along a route API returns hyper local (one kilometer or less),
        // up-to-the-minute weather nowcasts, weather hazard assessments, and notifications
        // along a route described as a sequence of waypoints.
        System.out.println("Get Weather Along Route Sync Client");
        // BEGIN: com.azure.maps.weather.sync.get_weather_along_route
        List<Waypoint> waypoints = Arrays.asList(
            new Waypoint(new GeoPosition(-77.037, 38.907), 0.0),
            new Waypoint(new GeoPosition(-77.009, 38.907), 10.0),
            new Waypoint(new GeoPosition(-76.928, 38.926), 20.0),
            new Waypoint(new GeoPosition(-76.852, 39.033), 30.0),
            new Waypoint(new GeoPosition(-76.732, 39.168), 40.0),
            new Waypoint(new GeoPosition(-76.634, 39.269), 50.0),
            new Waypoint(new GeoPosition(-76.612, 39.287), 60.0)
        );
        client.getWeatherAlongRoute(waypoints, "en");
        // END: com.azure.maps.weather.sync.get_weather_along_route

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

        // Get Weather Along Route -
        // https://docs.microsoft.com/en-us/rest/api/maps/weather/get-weather-along-route
        // Weather along a route API returns hyper local (one kilometer or less),
        // up-to-the-minute weather nowcasts, weather hazard assessments, and notifications
        // along a route described as a sequence of waypoints.
        System.out.println("Get Weather Along Route Async Client");
        // BEGIN: com.azure.maps.weather.async.get_weather_along_route
        List<Waypoint> waypointList = Arrays.asList(
            new Waypoint(new GeoPosition(-77.037, 38.907), 0.0),
            new Waypoint(new GeoPosition(-77.009, 38.907), 10.0),
            new Waypoint(new GeoPosition(-76.928, 38.926), 20.0),
            new Waypoint(new GeoPosition(-76.852, 39.033), 30.0),
            new Waypoint(new GeoPosition(-76.732, 39.168), 40.0),
            new Waypoint(new GeoPosition(-76.634, 39.269), 50.0),
            new Waypoint(new GeoPosition(-76.612, 39.287), 60.0)
        );
        asyncClient.getWeatherAlongRoute(waypointList, "en");
        // END: com.azure.maps.weather.async.get_weather_along_route
    }
}
