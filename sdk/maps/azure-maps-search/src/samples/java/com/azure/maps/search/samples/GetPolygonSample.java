// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.samples;

import java.io.IOException;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.Context;
import com.azure.maps.search.MapsSearchAsyncClient;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.models.Boundary;

public class GetPolygonSample {

    public static void main(String[] args) throws IOException {
        MapsSearchClientBuilder builder = new MapsSearchClientBuilder();

        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        // builder.credential(tokenCredential);

        builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        MapsSearchClient client = builder.buildClient();

        // Get polygon
        // BEGIN: sync.get_polygon
        System.out.println("Get Polygons:");
        GeoPosition coordinates = new GeoPosition(-122.204141, 47.61256);

        Boundary result = client.getPolygons(coordinates, null, BoundaryResultTypeEnum.LOCALITY, ResolutionEnum.SMALL);

        //with response
        Response<Boundary> response = client.getPolygonsWithResponse(coordinates, null, BoundaryResultTypeEnum.LOCALITY, ResolutionEnum.SMALL, Context.NONE);

        // END: sync.get_polygon

        MapsSearchClientBuilder asyncClientbuilder = new MapsSearchClientBuilder();

        // Authenticates using subscription key
        AzureKeyCredential asyncClientKeyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        asyncClientbuilder.credential(asyncClientKeyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential asyncClientTokenCredential = new DefaultAzureCredentialBuilder().build();
        // asyncClientbuilder.credential(asyncClientTokenCredential);

        asyncClientbuilder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        asyncClientbuilder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        MapsSearchAsyncClient asyncClient = asyncClientbuilder.buildAsyncClient();

        // Get polygon -
        // BEGIN: async.get_polygon
        Boundary asyncResult = asyncClient.getPolygons(coordinates, null, BoundaryResultTypeEnum.LOCALITY, ResolutionEnum.SMALL).block();

        //with response
        Response<Boundary> asyncResponse = asyncClient.getPolygonsWithResponse(coordinates, null, BoundaryResultTypeEnum.LOCALITY, ResolutionEnum.SMALL, Context.NONE).block();

        // END: async.get_polygon
    }
}

