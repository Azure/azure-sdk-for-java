// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route.samples;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.polling.SyncPoller;
import com.azure.maps.route.MapsRouteAsyncClient;
import com.azure.maps.route.MapsRouteClient;
import com.azure.maps.route.MapsRouteClientBuilder;
import com.azure.maps.route.models.RouteDirectionsBatchResult;
import com.azure.maps.route.models.RouteDirectionsOptions;
import com.azure.maps.route.models.RouteType;
import com.azure.maps.route.models.TravelMode;

public class BeginRequestRouteDirectionsBatchSample {
    public static void main(String[] args) throws IOException {
        // authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // build client
        MapsRouteClientBuilder builder = new MapsRouteClientBuilder();

        // use this for key authentication
        builder.credential(keyCredential);

        // use the next 2 lines for Azure AD authentication
        // builder.credential(tokenCredential);
        // builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));

        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        MapsRouteClient client = builder.buildClient();

        // Begin Request Route Directions Batch
        // BEGIN: com.azure.maps.search.sync.begin_request_route_directions_batch
        RouteDirectionsOptions options1 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(-122.128384, 47.639987),
                new GeoPosition(-122.184408, 47.621252),
                new GeoPosition(-122.332000, 47.596437)))
            .setRouteType(RouteType.FASTEST)
            .setTravelMode(TravelMode.CAR)
            .setMaxAlternatives(5);

        RouteDirectionsOptions options2 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(-122.348934, 47.620659),
                new GeoPosition(-122.342015, 47.610101)))
            .setRouteType(RouteType.ECONOMY)
            .setTravelMode(TravelMode.BICYCLE)
            .setUseTrafficData(false);

        RouteDirectionsOptions options3 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(-73.985108, 40.759856),
                new GeoPosition(-73.973506, 40.771136)))
            .setRouteType(RouteType.SHORTEST)
            .setTravelMode(TravelMode.PEDESTRIAN);

        System.out.println("Get Route Directions Batch");

        List<RouteDirectionsOptions> optionsList = Arrays.asList(options1, options2, options3);
        SyncPoller<RouteDirectionsBatchResult, RouteDirectionsBatchResult> poller =
            client.beginRequestRouteDirectionsBatch(optionsList);
        poller.getFinalResult();
        // END: com.azure.maps.search.sync.begin_request_route_directions_batch

        MapsRouteClientBuilder asyncClientbuilder = new MapsRouteClientBuilder();

        // Authenticates using subscription key
        AzureKeyCredential asyncClientKeyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        asyncClientbuilder.credential(asyncClientKeyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential asyncClientTokenCredential = new DefaultAzureCredentialBuilder().build();
        // asyncClientbuilder.credential(asyncClientTokenCredential);

        asyncClientbuilder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        asyncClientbuilder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        MapsRouteAsyncClient asyncClient = asyncClientbuilder.buildAsyncClient();

        // Begin Request Route Directions Batch
        // BEGIN: com.azure.maps.search.async.begin_request_route_directions_batch
        RouteDirectionsOptions options5 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(-122.128384, 47.639987),
                new GeoPosition(-122.184408, 47.621252),
                new GeoPosition(-122.332000, 47.596437)))
            .setRouteType(RouteType.FASTEST)
            .setTravelMode(TravelMode.CAR)
            .setMaxAlternatives(5);

        RouteDirectionsOptions options6 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(-122.348934, 47.620659),
                new GeoPosition(-122.342015, 47.610101)))
            .setRouteType(RouteType.ECONOMY)
            .setTravelMode(TravelMode.BICYCLE)
            .setUseTrafficData(false);

        RouteDirectionsOptions options7 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(-73.985108, 40.759856),
                new GeoPosition(-73.973506, 40.771136)))
            .setRouteType(RouteType.SHORTEST)
            .setTravelMode(TravelMode.PEDESTRIAN);

        System.out.println("Get Route Directions Batch");

        List<RouteDirectionsOptions> optionsList2 = Arrays.asList(options5, options6, options7);
        SyncPoller<RouteDirectionsBatchResult, RouteDirectionsBatchResult> poller2 =
            asyncClient.beginRequestRouteDirectionsBatch(optionsList2).getSyncPoller();
        poller2.getFinalResult();
        // END: com.azure.maps.search.async.begin_request_route_directions_batch
    }
}
