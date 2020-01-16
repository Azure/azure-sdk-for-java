// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.search.models.Index;
import com.azure.search.models.RequestOptions;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {

    private String ENDPOINT = "endpoint";
    private String ADMIN_KEY = "admin key";
    private String API_KEY = "api key";
    private String INDEX_NAME = "index name";
    private SearchServiceClient searchClient = new SearchServiceClientBuilder().buildClient();

    public void createSearchClient() {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(ADMIN_KEY))
            .buildClient();
    }

    public void createAsyncSearchClient() {
        SearchServiceAsyncClient client = new SearchServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(ADMIN_KEY))
            .buildAsyncClient();
    }

    public void createIndexClient() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(API_KEY))
            .indexName(INDEX_NAME)
            .buildClient();
    }

    public void createAsyncIndexClient() {
        SearchIndexAsyncClient client = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(API_KEY))
            .indexName(INDEX_NAME)
            .buildAsyncClient();
    }

    public void customHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.put("my-header1", "my-header1-value");
        headers.put("my-header2", "my-header2-value");
        headers.put("my-header3", "my-header3-value");
        // Call API by passing headers in Context.
        Index index = new Index().setName(INDEX_NAME);
        searchClient.createIndexWithResponse(
            index,
            new RequestOptions(),
            new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers));
        // Above three HttpHeader will be added in outgoing HttpRequest.
    }
}
