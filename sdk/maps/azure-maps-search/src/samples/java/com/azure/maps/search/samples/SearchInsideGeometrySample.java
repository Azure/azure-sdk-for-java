// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.samples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.GeoLinearRing;
import com.azure.core.models.GeoPolygon;
import com.azure.core.models.GeoPosition;
import com.azure.maps.search.MapsSearchAsyncClient;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.models.SearchInsideGeometryOptions;

public class SearchInsideGeometrySample {
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

        // Search inside geometry -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-along-route
        // BEGIN: com.azure.maps.search.sync.search_inside_geometry
        System.out.println("Search Inside Geometry");

        // create GeoPolygon
        List<GeoPosition> coordinates = new ArrayList<>();
        coordinates.add(new GeoPosition(-122.43576049804686, 37.7524152343544));
        coordinates.add(new GeoPosition(-122.43301391601562, 37.70660472542312));
        coordinates.add(new GeoPosition(-122.36434936523438, 37.712059855877314));
        coordinates.add(new GeoPosition(-122.43576049804686, 37.7524152343544));
        GeoLinearRing ring = new GeoLinearRing(coordinates);
        GeoPolygon polygon = new GeoPolygon(ring);

        // simple
        client.searchInsideGeometry(
            new SearchInsideGeometryOptions("Leland Avenue", polygon));

        // options
        client.searchInsideGeometry(
            new SearchInsideGeometryOptions("Leland Avenue", polygon)
                .setTop(5));

        // complete
        client.searchInsideGeometryWithResponse(
            new SearchInsideGeometryOptions("Leland Avenue", polygon)
                .setTop(5),
            null).getStatusCode();
        // END: com.azure.maps.search.sync.search_inside_geometry

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

        // Search inside geometry -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-along-route
        // BEGIN: com.azure.maps.search.async.search_inside_geometry
        System.out.println("Search Inside Geometry");

        // create GeoPolygon
        List<GeoPosition> searchInsideGeometryCoordinates = new ArrayList<>();
        searchInsideGeometryCoordinates.add(new GeoPosition(-122.43576049804686, 37.7524152343544));
        searchInsideGeometryCoordinates.add(new GeoPosition(-122.43301391601562, 37.70660472542312));
        searchInsideGeometryCoordinates.add(new GeoPosition(-122.36434936523438, 37.712059855877314));
        searchInsideGeometryCoordinates.add(new GeoPosition(-122.43576049804686, 37.7524152343544));
        GeoLinearRing searchInsideGeometryRing = new GeoLinearRing(searchInsideGeometryCoordinates);
        GeoPolygon searchInsideGeometryPolygon = new GeoPolygon(searchInsideGeometryRing);

        // simple
        asyncClient.searchInsideGeometry(
            new SearchInsideGeometryOptions("Leland Avenue", searchInsideGeometryPolygon));

        // options
        asyncClient.searchInsideGeometry(
            new SearchInsideGeometryOptions("Leland Avenue", searchInsideGeometryPolygon)
                .setTop(5));

        // complete
        asyncClient.searchInsideGeometryWithResponse(
            new SearchInsideGeometryOptions("Leland Avenue", searchInsideGeometryPolygon)
                .setTop(5)).block().getStatusCode();
        // END: com.azure.maps.search.async.search_inside_geometry
    }
}
