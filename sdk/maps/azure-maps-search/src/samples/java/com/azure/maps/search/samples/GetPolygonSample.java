// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.samples;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.models.GeoPosition;
import com.azure.maps.search.MapsSearchAsyncClient;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.models.FuzzySearchOptions;
import com.azure.maps.search.models.GeographicEntityType;
import com.azure.maps.search.models.SearchAddressResult;

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

        // Get polygon -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-polygon
        // BEGIN: com.azure.maps.search.sync.get_polygon
        SearchAddressResult results = client.fuzzySearch(
            new FuzzySearchOptions("1 Microsoft Way", new GeoPosition(-74.011454, 40.706270))
                .setTop(5));
        Response<SearchAddressResult> response = client.fuzzySearchWithResponse(
            new FuzzySearchOptions("Monaco").setEntityType(GeographicEntityType.COUNTRY)
                .setTop(5), null);
        String id = response.getValue().getResults().get(0).getDataSource().getGeometry();
        List<String> ids = results.getResults().stream()
            .filter(item -> item.getDataSource() != null && item.getDataSource().getGeometry() != null)
            .map(item -> item.getDataSource().getGeometry())
            .collect(Collectors.toList());
        ids.add(id);

        if (ids != null && !ids.isEmpty()) {
            System.out.println("Get Polygon: " + ids);
            client.getPolygons(ids);
            client.getPolygonsWithResponse(ids, null).getValue().getClass();
        }
        // END: com.azure.maps.search.sync.get_polygon

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
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-polygon
        // BEGIN: com.azure.maps.search.async.get_polygon
        Response<SearchAddressResult> fuzzySearchResponse = asyncClient.fuzzySearchWithResponse(
            new FuzzySearchOptions("Monaco").setEntityType(GeographicEntityType.COUNTRY)
                .setTop(5)).block();
        String fuzzySearchId = fuzzySearchResponse.getValue().getResults().get(0).getDataSource().getGeometry();
        List<String> getPolygonIds = results.getResults().stream()
            .filter(item -> item.getDataSource() != null && item.getDataSource().getGeometry() != null)
            .map(item -> item.getDataSource().getGeometry())
            .collect(Collectors.toList());
        getPolygonIds.add(fuzzySearchId);

        if (ids != null && !getPolygonIds.isEmpty()) {
            System.out.println("Get Polygon: " + ids);
        }
        // END: com.azure.maps.search.async.get_polygon
    }

}
