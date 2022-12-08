// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.samples;

import java.io.IOException;
import java.util.Arrays;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.GeoPosition;
import com.azure.maps.search.MapsSearchAsyncClient;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.models.SearchPointOfInterestCategoryOptions;

public class SearchPointOfInterestCategorySample {
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

        // Search POI Category -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-poi-category
        // BEGIN: com.azure.maps.search.sync.search_poi_category
        System.out.println("Get Point of Interest Category:");

        // complete - search for italian restaurant in NYC
        client.searchPointOfInterestCategory(
            new SearchPointOfInterestCategoryOptions("pasta", new GeoPosition(-74.011454, 40.706270))
                .setCategoryFilter(Arrays.asList(7315))
                .setTop(3));

        // with response
        client.searchPointOfInterestCategoryWithResponse(
            new SearchPointOfInterestCategoryOptions("pasta", new GeoPosition(-74.011454, 40.706270))
                .setCategoryFilter(Arrays.asList(7315))
                .setTop(3),
            null).getStatusCode();
        // END: com.azure.maps.search.sync.search_poi_category

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

        // Search POI Category -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-poi-category
        // BEGIN: com.azure.maps.search.async.search_poi_category
        System.out.println("Get Point of Interest Category:");

        // complete - search for italian restaurant in NYC
        asyncClient.searchPointOfInterestCategory(
            new SearchPointOfInterestCategoryOptions("pasta", new GeoPosition(-74.011454, 40.706270))
                .setCategoryFilter(Arrays.asList(7315))
                .setTop(3));

        // with response
        asyncClient.searchPointOfInterestCategoryWithResponse(
            new SearchPointOfInterestCategoryOptions("pasta", new GeoPosition(-74.011454, 40.706270))
                .setCategoryFilter(Arrays.asList(7315))
                .setTop(3)).block().getStatusCode();
        // END: com.azure.maps.search.async.search_poi_category
    }
}
