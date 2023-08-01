// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.samples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.GeoLineString;
import com.azure.core.models.GeoPosition;
import com.azure.maps.search.MapsSearchAsyncClient;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.models.OperatingHoursRange;
import com.azure.maps.search.models.SearchAddressResult;
import com.azure.maps.search.models.SearchAlongRouteOptions;

public class SearchAlongRouteSample {
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

        // Post search along route -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-along-route
        // BEGIN: com.azure.maps.search.sync.search_along_route
        System.out.println("Search Along Route");

        // create route points
        List<GeoPosition> points = new ArrayList<>();
        points.add(new GeoPosition(-122.143035, 47.653536));
        points.add(new GeoPosition(-122.187164, 47.617556));
        points.add(new GeoPosition(-122.114981, 47.570599));
        points.add(new GeoPosition(-122.132756, 47.654009));
        GeoLineString route = new GeoLineString(points);

        // simple
        client.searchAlongRoute(new SearchAlongRouteOptions("burger", 1000, route));

        // options
        client.searchAlongRoute(
            new SearchAlongRouteOptions("burger", 1000, route)
                .setCategoryFilter(Arrays.asList(7315))
                .setTop(5));

        // complete
        client.searchAlongRouteWithResponse(
            new SearchAlongRouteOptions("burger", 1000, route)
                .setCategoryFilter(Arrays.asList(7315))
                .setTop(5),
            null).getStatusCode();
        // END: com.azure.maps.search.sync.search_along_route

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

         // Post search along route -
         // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-along-route
        // BEGIN: com.azure.maps.search.async.search_along_route
        System.out.println("Search Along Route");

        // create route points
        List<GeoPosition> getPolygonPoints = new ArrayList<>();
        getPolygonPoints.add(new GeoPosition(-122.143035, 47.653536));
        getPolygonPoints.add(new GeoPosition(-122.187164, 47.617556));
        getPolygonPoints.add(new GeoPosition(-122.114981, 47.570599));
        getPolygonPoints.add(new GeoPosition(-122.132756, 47.654009));
        GeoLineString getPolygonRoute = new GeoLineString(getPolygonPoints);

        // simple
        SearchAddressResult result = asyncClient.searchAlongRoute(
            new SearchAlongRouteOptions("burger", 1000, getPolygonRoute)).block();

        // options
        asyncClient.searchAlongRoute(
            new SearchAlongRouteOptions("burger", 1000, getPolygonRoute)
                .setCategoryFilter(Arrays.asList(7315))
                .setTop(5)).block();

        // complete
        asyncClient.searchAlongRouteWithResponse(
            new SearchAlongRouteOptions("burger", 1000, getPolygonRoute)
            .setCategoryFilter(Arrays.asList(7315)).setOperatingHours(OperatingHoursRange.NEXT_SEVEN_DAYS)
            .setTop(5)).block().getStatusCode();
        // END: com.azure.maps.search.async.search_along_route
    }

}
