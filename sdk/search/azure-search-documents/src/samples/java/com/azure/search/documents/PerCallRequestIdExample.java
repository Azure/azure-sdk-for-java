// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.search.documents.models.Hotel;
import com.azure.search.documents.models.IndexDocumentsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This example shows how to set a custom {@code x-ms-client-request-id} per request.
 * <p>
 * By default, clients are built with a policy that adds a per request generated {@code x-ms-client-request-id}. This
 * will show how to leverage {@link Context} to set a application passed {@code x-ms-client-request-id} per API call.
 */
public class PerCallRequestIdExample {
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");

    private static final String INDEX_NAME = "hotels-sample-index";

    /**
     * This example shows how to pass {@code x-ms-client-request-id} when using a synchronous client.
     * <p>
     * Synchronous clients only accept {@link Context} in their maximum parameter overloads.
     */
    private static void synchronousApiCall() {
        SearchClient client = createBuilder().buildClient();

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(new Hotel().setHotelId("100"));
        hotels.add(new Hotel().setHotelId("200"));
        hotels.add(new Hotel().setHotelId("300"));

        // Setup context to pass custom x-ms-client-request-id.
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-ms-client-request-id", UUID.randomUUID().toString());

        Context context = new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);

        // Print out expected 'x-ms-client-request-id' header value.
        System.out.printf("Sending request with 'x-ms-client-request-id': %s%n", headers.get("x-ms-client-request-id"));

        // Perform index operations on a list of documents
        Response<IndexDocumentsResult> response = client.mergeOrUploadDocumentsWithResponse(hotels, null, context);
        System.out.printf("Indexed %s documents%n", response.getValue().getResults().size());

        // Print out verification of 'x-ms-client-request-id' returned in the service response.
        System.out.printf("Received response with returned 'x-ms-client-request-id': %s%n",
            response.getHeaders().get("x-ms-client-request-id"));
    }

    /**
     * This examples shows how to pass {@code x-ms-client-request-id} when using an asynchronous client.
     * <p>
     * Asynchronous clients are able to accept {@link Context} in all APIs using Reactor's {@code subscriberContext}.
     */
    private static void asynchronousApiCall() {
        SearchAsyncClient client = createBuilder().buildAsyncClient();

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(new Hotel().setHotelId("100"));
        hotels.add(new Hotel().setHotelId("200"));
        hotels.add(new Hotel().setHotelId("300"));

        // Setup context to pass custom x-ms-client-request-id.
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-ms-client-request-id", UUID.randomUUID().toString());

        reactor.util.context.Context subscriberContext = reactor.util.context.Context.of(
            AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);

        // Print out expected 'x-ms-client-request-id' header value.
        System.out.printf("Sending request with 'x-ms-client-request-id': %s%n", headers.get("x-ms-client-request-id"));

        // Perform index operations on a list of documents
        client.mergeDocumentsWithResponse(hotels, null)
            .subscriberContext(subscriberContext)
            .doOnSuccess(response -> {
                System.out.printf("Indexed %s documents%n", response.getValue().getResults().size());

                // Print out verification of 'x-ms-client-request-id' returned in the service response.
                System.out.printf("Received response with returned 'x-ms-client-request-id': %s%n",
                    response.getHeaders().get("x-ms-client-request-id"));
            }).block();
    }

    private static SearchClientBuilder createBuilder() {
        return new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .indexName(INDEX_NAME);
    }
}
