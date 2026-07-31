// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.IndexDocumentsResult;

import java.util.Collections;
import java.util.UUID;

/**
 * This example shows how to set a custom {@code x-ms-client-request-id} per request.
 * <p>
 * By default, clients are built with a policy that adds a per request generated {@code x-ms-client-request-id}. This
 * will show how to leverage {@link Context} to set a application passed {@code x-ms-client-request-id} per API call.
 */
@SuppressWarnings("unused")
public class PerCallRequestIdExample {
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");

    private static final String INDEX_NAME = "hotels-sample-index";

    public static void main(String[] args) {
        synchronousApiCall();
        asynchronousApiCall();
    }

    /**
     * This example shows how to pass {@code x-ms-client-request-id} when using a synchronous client.
     * <p>
     * Synchronous clients only accept {@link Context} in their maximum parameter overloads.
     */
    private static void synchronousApiCall() {
        SearchClient client = createBuilder().buildClient();

        IndexDocumentsBatch batch = new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(
                Collections.singletonMap("HotelId", "100")),
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(Collections.singletonMap("HotelId", "200")),
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(Collections.singletonMap("HotelId", "300")));

        // Setup context to pass custom x-ms-client-request-id.
        String customRequestId = UUID.randomUUID().toString();
        RequestOptions requestOptions = new RequestOptions()
            .setHeader(HttpHeaderName.X_MS_CLIENT_REQUEST_ID, customRequestId);

        // Print out expected 'x-ms-client-request-id' header value.
        System.out.printf("Sending request with 'x-ms-client-request-id': %s%n", customRequestId);

        // Perform index operations on a list of documents
        Response<IndexDocumentsResult> response = client.indexDocumentsWithResponse(batch, null, requestOptions);
        System.out.printf("Indexed %s documents%n", response.getValue().getResults().size());

        // Print out verification of 'x-ms-client-request-id' returned by the service response.
        System.out.printf("Received response with returned 'x-ms-client-request-id': %s%n",
            response.getHeaders().get(HttpHeaderName.X_MS_CLIENT_REQUEST_ID));
    }

    /**
     * This examples shows how to pass {@code x-ms-client-request-id} when using an asynchronous client.
     */
    private static void asynchronousApiCall() {
        SearchAsyncClient client = createBuilder().buildAsyncClient();

        IndexDocumentsBatch batch = new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(
                Collections.singletonMap("HotelId", "100")),
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(Collections.singletonMap("HotelId", "200")),
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(Collections.singletonMap("HotelId", "300")));

        // Setup context to pass custom x-ms-client-request-id.
        String customRequestId = UUID.randomUUID().toString();
        RequestOptions requestOptions = new RequestOptions()
            .setHeader(HttpHeaderName.X_MS_CLIENT_REQUEST_ID, customRequestId);

        // Print out expected 'x-ms-client-request-id' header value.
        System.out.printf("Sending request with 'x-ms-client-request-id': %s%n", customRequestId);

        // Perform index operations on a list of documents
        client.indexDocumentsWithResponse(batch, null, requestOptions)
            .doOnSuccess(response -> {
                System.out.printf("Indexed %s documents%n", response.getValue().getResults().size());

                // Print out verification of 'x-ms-client-request-id' returned by the service response.
                System.out.printf("Received response with returned 'x-ms-client-request-id': %s%n",
                    response.getHeaders().get(HttpHeaderName.X_MS_CLIENT_REQUEST_ID));
            }).block();
    }

    private static SearchClientBuilder createBuilder() {
        return new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .indexName(INDEX_NAME);
    }
}
