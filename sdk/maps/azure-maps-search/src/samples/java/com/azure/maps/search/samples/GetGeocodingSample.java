// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.samples;

import java.io.IOException;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.models.GeoPosition;
import com.azure.maps.search.MapsSearchAsyncClient;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.implementation.models.GeocodingResponse;
import com.azure.maps.search.implementation.models.SearchesGetGeocodingHeaders;
import com.azure.maps.search.models.BaseSearchOptions;

public class GetGeocodingSample {
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

        // Get Geocoding -
        // BEGIN: sync.get_geocoding
        System.out.println("Get Geocoding:");

        //simple
        client.getGeocoding(new BaseSearchOptions().setQuery("1 Microsoft Way, Redmond, WA 98052"));

        //with multiple options
        GeocodingResponse result = client.getGeocoding(
            new BaseSearchOptions().setCoordinates(new GeoPosition(-74.011454, 40.706270)).setTop(5));

        // with response
        ResponseBase<SearchesGetGeocodingHeaders, GeocodingResponse> response = client.getGeocodingWithResponse(
            new BaseSearchOptions().setCoordinates(new GeoPosition(-74.011454, 40.706270)).setTop(5), null);

        // with response no custom header
        Response<GeocodingResponse> responseNoHeader = client.getGeocodingNoCustomHeaderWithResponse(
            new BaseSearchOptions().setCoordinates(new GeoPosition(-74.011454, 40.706270)).setTop(5), null);
        // END: sync.get_geocoding

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

        // Get Geocoding:
        // BEGIN: async.get_geocoding
        System.out.println("Get Geocoding:");

        // simple
        asyncClient.getGeocoding(new BaseSearchOptions().setQuery("1 Microsoft Way, Redmond, WA 98052"));

        // with multiple options
        GeocodingResponse asyncResult = asyncClient.getGeocoding(
            new BaseSearchOptions().setCoordinates(new GeoPosition(-74.011454, 40.706270)).setTop(5)).block();

        // with response
        ResponseBase<SearchesGetGeocodingHeaders, GeocodingResponse> asyncResponse = asyncClient.getGeocodingWithResponse(
            new BaseSearchOptions().setCoordinates(new GeoPosition(-74.011454, 40.706270)).setTop(5)).block();

        // with response no custom header
        Response<GeocodingResponse> asyncResponseNoCustomHeader = asyncClient.getGeocodingNoCustomHeaderWithResponse(
            new BaseSearchOptions().setCoordinates(new GeoPosition(-74.011454, 40.706270)).setTop(5)).block();
        // END: async.get_geocoding
    }

}
