// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.util.Context;
import com.azure.search.models.Index;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.SearchResult;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {

    private String endpoint = "endpoint";
    private String adminKey = "admin key";
    private String apiKey = "api key";
    private String indexName = "index name";
    private SearchServiceClient searchClient = new SearchServiceClientBuilder().buildClient();
    private SearchIndexClient indexClient = new SearchIndexClientBuilder().buildClient();

    public void createSearchClient() {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new SearchApiKeyCredential(adminKey))
            .buildClient();
    }

    public void createAsyncSearchClient() {
        SearchServiceAsyncClient client = new SearchServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new SearchApiKeyCredential(adminKey))
            .buildAsyncClient();
    }

    public void createIndexClient() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(endpoint)
            .credential(new SearchApiKeyCredential(apiKey))
            .indexName(indexName)
            .buildClient();
    }

    public void createAsyncIndexClient() {
        SearchIndexAsyncClient client = new SearchIndexClientBuilder()
            .endpoint(endpoint)
            .credential(new SearchApiKeyCredential(apiKey))
            .indexName(indexName)
            .buildAsyncClient();
    }

    public void customHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.put("my-header1", "my-header1-value");
        headers.put("my-header2", "my-header2-value");
        headers.put("my-header3", "my-header3-value");
        // Call API by passing headers in Context.
        Index index = new Index().setName(indexName);
        searchClient.createIndexWithResponse(
            index,
            new RequestOptions(),
            new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers));
        // Above three HttpHeader will be added in outgoing HttpRequest.
    }

    public void handleErrorsWithSyncClient() {
        try {
            Iterable<SearchResult> results = indexClient.search("hotel");
        } catch (HttpResponseException ex) {
            // The exception contains the HTTP status code and the detailed message
            // returned from the search service
            HttpResponse response = ex.getResponse();
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Message: " + response.getBodyAsString().block());
        }
    }
}
