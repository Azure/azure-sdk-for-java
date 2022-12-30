// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route.samples;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.GeoCollection;
import com.azure.core.models.GeoLinearRing;
import com.azure.core.models.GeoPoint;
import com.azure.core.models.GeoPolygon;
import com.azure.core.models.GeoPolygonCollection;
import com.azure.core.models.GeoPosition;
import com.azure.maps.route.MapsRouteAsyncClient;
import com.azure.maps.route.MapsRouteClient;
import com.azure.maps.route.MapsRouteClientBuilder;
import com.azure.maps.route.models.RouteDirections;
import com.azure.maps.route.models.RouteDirectionsOptions;
import com.azure.maps.route.models.RouteDirectionsParameters;
import com.azure.maps.route.models.RouteReport;

public class GetRouteDirectionsSample {
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

        // Get Route Directions
        // BEGIN: com.azure.maps.route.sync.get_route_directions
        System.out.println("Get route directions");
        List<GeoPosition> routePoints = Arrays.asList(
            new GeoPosition(13.42936, 52.50931),
            new GeoPosition(13.43872, 52.50274));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        RouteDirections directions = client.getRouteDirections(routeOptions);
        RouteReport report = directions.getReport(); // get the report and use it
        // END: com.azure.maps.route.sync.get_route_directions

        // Get Route Directions Parameters
        // BEGIN: com.azure.maps.route.sync.get_route_directions_parameters
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
        client.getRouteDirections(routeOptions,
            parameters);
        // END: com.azure.maps.route.sync.get_route_directions_parameters

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

        // Get Route Directions
        // BEGIN: com.azure.maps.route.async.get_route_directions
        System.out.println("Get route directions");
        List<GeoPosition> routePoints2 = Arrays.asList(
            new GeoPosition(13.42936, 52.50931),
            new GeoPosition(13.43872, 52.50274));
        RouteDirectionsOptions routeOptions2 = new RouteDirectionsOptions(routePoints2);
        RouteDirections directions4 = asyncClient.getRouteDirections(routeOptions2).block();
        RouteReport report2 = directions4.getReport(); // get the report and use it
        // END: com.azure.maps.route.async.get_route_directions

        // Get Route Directions Parameters
        // BEGIN: com.azure.maps.route.async.get_route_directions_parameters
        System.out.println("Get route parameters");
        // supporting points
        GeoCollection supportingPoints2 = new GeoCollection(
            Arrays.asList(
                new GeoPoint(13.42936, 52.5093),
                new GeoPoint(13.42859, 52.50844)
                ));

        // avoid areas
        List<GeoPolygon> polygons2 = Arrays.asList(
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
        GeoPolygonCollection avoidAreas2 = new GeoPolygonCollection(polygons2);
        RouteDirectionsParameters parameters2 = new RouteDirectionsParameters()
            .setSupportingPoints(supportingPoints2)
            .setAvoidVignette(Arrays.asList("AUS", "CHE"))
            .setAvoidAreas(avoidAreas2);
        asyncClient.getRouteDirections(routeOptions2,
            parameters2).block();
        // END: com.azure.maps.route.async.get_route_directions_parameters
    }
}
