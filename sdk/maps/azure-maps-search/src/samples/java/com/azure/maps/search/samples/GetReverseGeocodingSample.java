// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.samples;

import java.io.IOException;
import java.util.Arrays;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.Context;
import com.azure.maps.search.MapsSearchAsyncClient;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.implementation.models.GeocodingResponse;
import com.azure.maps.search.implementation.models.ReverseGeocodingResultTypeEnum;

public class GetReverseGeocodingSample {
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

        // Get reverse geocoding
        // BEGIN: sync.get_reverse_geocoding
        System.out.println("Get Reverse Geocoding:");

        GeoPosition coordinates = new GeoPosition(-122.34255, 47.0);
        GeocodingResponse result = client.getReverseGeocoding(coordinates, Arrays.asList(ReverseGeocodingResultTypeEnum.ADDRESS), null);

        //with response
        Response<GeocodingResponse> response = client.getReverseGeocodingWithResponse(coordinates, Arrays.asList(ReverseGeocodingResultTypeEnum.ADDRESS), null, Context.NONE);
        // END: sync.get_reverse_geocoding

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

        // Get reverse geocoding
        // BEGIN: async.get_reverse_geocoding
        GeocodingResponse asyncResult = asyncClient.getReverseGeocoding(coordinates, Arrays.asList(ReverseGeocodingResultTypeEnum.ADDRESS), null).block();

        //with response
        Response<GeocodingResponse> asyncResponse = asyncClient.getReverseGeocodingWithResponse(coordinates, Arrays.asList(ReverseGeocodingResultTypeEnum.ADDRESS), null).block();
        // END: async.get_reverse_geocoding
    }
}
