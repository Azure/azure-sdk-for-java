// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route.samples;

import java.io.IOException;
import java.util.Arrays;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.GeoPoint;
import com.azure.core.models.GeoPointCollection;
import com.azure.maps.route.MapsRouteAsyncClient;
import com.azure.maps.route.MapsRouteClient;
import com.azure.maps.route.MapsRouteClientBuilder;
import com.azure.maps.route.models.RouteMatrixOptions;
import com.azure.maps.route.models.RouteMatrixQuery;

public class BeginRequestRouteMatrixSample {
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

        // Begin Request Route Matrix
        // BEGIN: com.azure.maps.search.sync.begin_request_route_matrix
        System.out.println("Request route matrix");
        RouteMatrixQuery matrixQuery = new RouteMatrixQuery();

        // origins
        GeoPointCollection origins = new GeoPointCollection(Arrays.asList(
            new GeoPoint(4.85106, 52.36006),
            new GeoPoint(4.85056, 52.36187)
        ));

        // destinations
        GeoPointCollection destinations = new GeoPointCollection(Arrays.asList(
            new GeoPoint(4.85003, 52.36241),
            new GeoPoint(13.42937, 52.50931)
        ));

        matrixQuery.setDestinations(destinations);
        matrixQuery.setOrigins(origins);

        RouteMatrixOptions matrixOptions = new RouteMatrixOptions(matrixQuery);
        client.beginGetRouteMatrix(matrixOptions).getFinalResult();
        // END: com.azure.maps.search.sync.begin_request_route_matrix

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

        // Begin Request Route Matrix
        // BEGIN: com.azure.maps.search.async.begin_request_route_matrix
        System.out.println("Request route matrix");
        RouteMatrixQuery matrixQuery3 = new RouteMatrixQuery();

        // origins
        GeoPointCollection origins3 = new GeoPointCollection(Arrays.asList(
            new GeoPoint(4.85106, 52.36006),
            new GeoPoint(4.85056, 52.36187)
        ));

        // destinations
        GeoPointCollection destinations3 = new GeoPointCollection(Arrays.asList(
            new GeoPoint(4.85003, 52.36241),
            new GeoPoint(13.42937, 52.50931)
        ));

        matrixQuery3.setDestinations(destinations3);
        matrixQuery3.setOrigins(origins3);

        RouteMatrixOptions matrixOptions2 = new RouteMatrixOptions(matrixQuery3);
        asyncClient.beginGetRouteMatrix(matrixOptions2).blockFirst().getFinalResult();
        // END: com.azure.maps.search.async.begin_request_route_matrix
    }
}
