
package com.azure.maps.search.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.maps.search.MapsSearchAsyncClient;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.implementation.models.GeocodingBatchRequestItem;
import com.azure.maps.search.models.GeocodingBatchRequestBody;
import com.azure.maps.search.models.GeocodingBatchResponse;

import java.io.IOException;
import java.util.Arrays;

public class GetGeocodingBatchSample {
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

        // Get Geocoding Batch
        // BEGIN: sync.get_geocoding_batch
        System.out.println("Get Geocoding Batch:");

        //with multiple items
        GeocodingBatchRequestBody body = new GeocodingBatchRequestBody();
        GeocodingBatchRequestItem addressLineItem = new GeocodingBatchRequestItem();
        addressLineItem.setAddressLine("400 Broad St");
        GeocodingBatchRequestItem queryItem = new GeocodingBatchRequestItem();
        queryItem.setQuery("15171 NE 24th St, Redmond, WA 98052, United States");
        body.setBatchItems(Arrays.asList(addressLineItem, queryItem));

        GeocodingBatchResponse result = client.getGeocodingBatch(body);

        // with response
        Response<GeocodingBatchResponse> response = client.getGeocodingBatchWithResponse(body, Context.NONE);

        // END: sync.get_geocoding_batch

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
        // BEGIN: async.get_geocoding_batch
        System.out.println("Get Geocoding:");


        GeocodingBatchResponse asyncResult = asyncClient.getGeocodingBatch(body).block();

        // with response
        Response<GeocodingBatchResponse> asyncResponse = asyncClient.getGeocodingBatchWithResponse(body).block();

        // END: async.get_geocoding_batch
    }

}
