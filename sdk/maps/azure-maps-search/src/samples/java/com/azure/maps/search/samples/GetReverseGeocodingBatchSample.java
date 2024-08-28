
package com.azure.maps.search.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.Context;
import com.azure.maps.search.MapsSearchAsyncClient;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.implementation.models.*;

import java.io.IOException;
import java.util.Arrays;

public class GetReverseGeocodingBatchSample {
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
        // BEGIN: sync.get_reverse_geocoding_batch
        System.out.println("Get Reverse Geocoding Batch:");

        //with multiple items
        ReverseGeocodingBatchRequestBody body = new ReverseGeocodingBatchRequestBody();
        ReverseGeocodingBatchRequestItem item1 = new ReverseGeocodingBatchRequestItem();
        ReverseGeocodingBatchRequestItem item2 = new ReverseGeocodingBatchRequestItem();
        item1.setCoordinates(new GeoPosition(-122.34255, 47.0));
        item2.setCoordinates(new GeoPosition(-122.34255, 47.0));
        body.setBatchItems(Arrays.asList(item1, item2));

        GeocodingBatchResponse result = client.getReverseGeocodingBatch(body);

        // with response
        Response<GeocodingBatchResponse> response = client.getReverseGeocodingBatchWithResponse(body, Context.NONE);

        // END: sync.get_reverse_geocoding_batch

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
        // BEGIN: async.get_reverse_geocoding_batch
        System.out.println("Get Reverse Geocoding Batch:");


        GeocodingBatchResponse asyncResult = asyncClient.getReverseGeocodingBatch(body).block();

        // with response
        Response<GeocodingBatchResponse> asyncResponse = asyncClient.getReverseGeocodingBatchWithResponse(body).block();

        // END: async.get_reverse_geocoding_batch
    }

}
