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
import com.azure.maps.route.models.RouteMatrixResult;
import com.azure.maps.route.models.RouteType;

public class BeginGetRouteMatrixSample {
    public static void main(String[] args) throws IOException {
        // build client
        MapsRouteClientBuilder builder = new MapsRouteClientBuilder();

        // authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        // use this for key authentication
        builder.credential(keyCredential);

        // authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        // use the next 2 lines for Azure AD authentication
        // builder.credential(tokenCredential);

        builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        MapsRouteClient client = builder.buildClient();

        // Begin Get Route Matrix
        // BEGIN: com.azure.maps.search.sync.get_route_matrix
        System.out.println("Begin get route matrix");
        RouteMatrixQuery matrixQuery2 = new RouteMatrixQuery();
        GeoPointCollection origins2 = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85106, 52.36006),
            new GeoPoint(4.85056, 52.36187)));
        GeoPointCollection destinations2 = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85003, 52.36241),
            new GeoPoint(13.42937, 52.50931)));
        matrixQuery2.setDestinations(destinations2);
        matrixQuery2.setOrigins(origins2);
        RouteMatrixOptions options = new RouteMatrixOptions(matrixQuery2);
        options.setRouteType(RouteType.SHORTEST);
        options.setWaitForResults(false);
        client.beginGetRouteMatrix(options).getFinalResult();
        // END: com.azure.maps.search.sync.get_route_matrix

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

        // Begin Get Route Matrix
        // BEGIN: com.azure.maps.search.async.get_route_matrix
        System.out.println("Begin get route matrix");
        RouteMatrixQuery matrixQuery4 = new RouteMatrixQuery();
        GeoPointCollection origins4 = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85106, 52.36006),
            new GeoPoint(4.85056, 52.36187)));
        GeoPointCollection destinations4 = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85003, 52.36241),
            new GeoPoint(13.42937, 52.50931)));
        matrixQuery4.setDestinations(destinations4);
        matrixQuery4.setOrigins(origins4);
        RouteMatrixOptions options4 = new RouteMatrixOptions(matrixQuery4);
        RouteMatrixResult result2 = asyncClient.beginGetRouteMatrix(options4).blockFirst().getFinalResult().block();
        // END: com.azure.maps.search.async.get_route_matrix
    }
}
