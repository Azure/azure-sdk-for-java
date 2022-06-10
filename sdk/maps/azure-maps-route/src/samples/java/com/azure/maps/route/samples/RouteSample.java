// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route.samples;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.GeoPoint;
import com.azure.core.models.GeoPointCollection;
import com.azure.core.models.GeoPosition;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.route.RouteAsyncClient;
import com.azure.maps.route.RouteClient;
import com.azure.maps.route.RouteClientBuilder;
import com.azure.maps.route.models.RouteDirections;
import com.azure.maps.route.models.RouteDirectionsOptions;
import com.azure.maps.route.models.RouteMatrixOptions;
import com.azure.maps.route.models.RouteMatrixQuery;
import com.azure.maps.route.models.RouteReport;

public class RouteSample {

    public RouteClient createRouteSyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.route.sync.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a builder
        RouteClientBuilder builder = new RouteClientBuilder();
        builder.credential(keyCredential);
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds the client
        RouteClient client = builder.buildClient();
        // END: com.azure.maps.route.sync.builder.key.instantiation

        return client;
    }

    public RouteClient createRouteSyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.route.sync.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a builder
        RouteClientBuilder builder = new RouteClientBuilder();
        builder.credential(tokenCredential);
        builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds a client
        RouteClient client = builder.buildClient();
        // END: com.azure.maps.route.sync.builder.ad.instantiation

        return client;
    }

    public RouteAsyncClient createRouteAsyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.route.async.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a builder
        RouteClientBuilder builder = new RouteClientBuilder();
        builder.credential(keyCredential);
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds the client
        RouteAsyncClient client = builder.buildAsyncClient();
        // END: com.azure.maps.route.async.builder.key.instantiation

        return client;
    }

    public RouteAsyncClient createRouteAsyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.route.async.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a builder
        RouteClientBuilder builder = new RouteClientBuilder();
        builder.credential(tokenCredential);
        builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds a client
        RouteAsyncClient client = builder.buildAsyncClient();
        // END: com.azure.maps.route.async.builder.ad.instantiation

        return client;
    }

    public static void main(String[] args) throws IOException {
        // authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // build client
        RouteClientBuilder builder = new RouteClientBuilder();

        // use this for key authentication
        builder.credential(keyCredential);

        // use the next 2 lines for Azure AD authentication
        // builder.credential(tokenCredential);
        // builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));

        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        RouteClient client = builder.buildClient();

        /* One-shot operations */

        // Get Route Directions
        // BEGIN: com.azure.maps.route.sync.get_route_directions
        List<GeoPosition> routePoints = Arrays.asList(
            new GeoPosition(13.42936, 52.50931),
            new GeoPosition(13.43872, 52.50274));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        RouteDirections directions = client.getRouteDirections(routeOptions);
        RouteReport report = directions.getReport(); // get the report and use it
        // END: com.azure.maps.route.sync.get_route_directions

        /*
        MapsCommon.print(directions);

        System.out.println("Get route range");
        RouteRangeOptions rangeOptions = new RouteRangeOptions(new LatLong(50.97452,5.86605), 6000.0);
        MapsCommon.print(client.getRouteRange(rangeOptions));

        System.out.println("Get route parameters");
        // supporting points
        GeoCollection supportingPoints = new GeoCollection(
            Arrays.asList(
                new GeoPoint(13.42936, 52.5093),
                new GeoPoint(13.42859, 52.50844)
                ));

        // avoid areas
        List<GeoPolygon> polygons = Arrays.asList(
            new GeoPolygon(
                new GeoLinearRing(Arrays.asList(
                    new GeoPosition(-122.39456176757811, 47.489368981370724),
                    new GeoPosition(-122.00454711914061, 47.489368981370724),
                    new GeoPosition(-122.00454711914061, 47.65151268066222),
                    new GeoPosition(-122.39456176757811, 47.65151268066222),
                    new GeoPosition(-122.39456176757811, 47.489368981370724)
                ))
            ),
            new GeoPolygon(
                new GeoLinearRing(Arrays.asList(
                    new GeoPosition(100.0, 0.0),
                    new GeoPosition(101.0, 0.0),
                    new GeoPosition(101.0, 1.0),
                    new GeoPosition(100.0, 1.0),
                    new GeoPosition(100.0, 0.0)
                ))
            )
        );
        GeoPolygonCollection avoidAreas = new GeoPolygonCollection(polygons);
        RouteDirectionsParameters parameters = new RouteDirectionsParameters()
            .setSupportingPoints(supportingPoints)
            .setAvoidVignette(Arrays.asList("AUS", "CHE"))
            .setAvoidAreas(avoidAreas);
        MapsCommon.print(client.getRouteDirectionsWithAdditionalParameters(routeOptions,
            parameters));

        RouteDirectionsOptions options1 = new RouteDirectionsOptions(
            Arrays.asList(new LatLong(47.639987, -122.128384),
                new LatLong(47.621252, -122.184408),
                new LatLong(47.596437,-122.332000)))
            .setRouteType(RouteType.FASTEST)
            .setTravelMode(TravelMode.CAR)
            .setMaxAlternatives(5);

        RouteDirectionsOptions options2 = new RouteDirectionsOptions(
            Arrays.asList(new LatLong(47.620659, -122.348934),
                new LatLong(47.610101, -122.342015)))
            .setRouteType(RouteType.ECONOMY)
            .setTravelMode(TravelMode.BICYCLE)
            .setUseTrafficData(false);

        RouteDirectionsOptions options3 = new RouteDirectionsOptions(
            Arrays.asList(new LatLong(40.759856, -73.985108),
                new LatLong(40.771136, -73.973506)))
            .setRouteType(RouteType.SHORTEST)
            .setTravelMode(TravelMode.PEDESTRIAN);

        System.out.println("Get Route Directions Batch");
        List<RouteDirectionsOptions> optionsList = Arrays.asList(options1, options2, options3);
        SyncPoller<RouteDirectionsBatchResult, RouteDirectionsBatchResult> poller =
            client.beginRequestRouteDirectionsBatch(optionsList);
        // MapsCommon.print(poller.getFinalResult());
        */

        System.out.println("Request route matrix");
        RouteMatrixQuery matrixQuery = new RouteMatrixQuery();

        // origins
        GeoPointCollection origins = new GeoPointCollection(Arrays.asList(
            new GeoPoint(52.36006, 4.85106),
            new GeoPoint(52.36187, 4.85056)
        ));

        // origins
        GeoPointCollection destinations = new GeoPointCollection(Arrays.asList(
            new GeoPoint(52.36241, 4.85003),
            new GeoPoint(52.50931, 13.42937)
        ));

        matrixQuery.setDestinations(destinations);
        matrixQuery.setOrigins(origins);

        RouteMatrixOptions matrixOptions = new RouteMatrixOptions(matrixQuery);
        MapsCommon.print(client.beginRequestRouteMatrix(matrixOptions).getFinalResult());
    }
}
