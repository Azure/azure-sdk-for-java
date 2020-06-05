// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.models.Hotel;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private SearchIndexClient searchIndexClient = new SearchIndexClientBuilder().buildClient();
    private SearchClient searchClient = new SearchClientBuilder().buildClient();

    public void createSearchClient() {
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(adminKey))
            .buildClient();
    }

    public void createAsyncSearchClient() {
        SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(adminKey))
            .buildAsyncClient();
    }

    public void createIndexClient() {
        SearchClient searchClient = new SearchClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .indexName(indexName)
            .buildClient();
    }

    public void createAsyncIndexClient() {
        SearchAsyncClient searchAsyncClient = new SearchClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .indexName(indexName)
            .buildAsyncClient();
    }

    public void customHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.put("my-header1", "my-header1-value");
        headers.put("my-header2", "my-header2-value");
        headers.put("my-header3", "my-header3-value");
        // Call API by passing headers in Context.
        SearchIndex index = new SearchIndex().setName(indexName);
        searchIndexClient.createIndexWithResponse(
            index,
            new RequestOptions(),
            new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers));
        // Above three HttpHeader will be added in outgoing HttpRequest.
    }

    public void handleErrorsWithSyncClient() {
        try {
            Iterable<SearchResult> results = searchClient.search("hotel");
        } catch (HttpResponseException ex) {
            // The exception contains the HTTP status code and the detailed message
            // returned from the search service
            HttpResponse response = ex.getResponse();
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Message: " + response.getBodyAsString().block());
        }
    }

    public void createIndexWithSyncClient() {
        SearchIndex newIndex = new SearchIndex()
            .setName("index_name")
            .setFields(
                Arrays.asList(new SearchField()
                        .setName("Name")
                        .setType(SearchFieldDataType.STRING)
                        .setKey(Boolean.TRUE),
                    new SearchField()
                        .setName("Cuisine")
                        .setType(SearchFieldDataType.STRING)));
        // Create index.
        searchIndexClient.createIndex(newIndex);
    }

    public void uploadDocumentWithSyncClient() {
        List<Hotel> hotels = new ArrayList<>();
        hotels.add(new Hotel().setHotelId("100"));
        hotels.add(new Hotel().setHotelId("200"));
        hotels.add(new Hotel().setHotelId("300"));
        // Upload hotel.
        searchClient.uploadDocuments(hotels);
    }

    public void searchTextWithSyncClient() {
        // Perform a text-based search
        for (SearchResult result : searchClient.search("luxury hotel",
            new SearchOptions(), new RequestOptions(), Context.NONE)) {

            // Each result is a dynamic Map
            SearchDocument doc = result.getDocument();
            String hotelName = (String) doc.get("HotelName");
            Double rating = (Double) doc.get("Rating");

            System.out.printf("%s: %s%n", hotelName, rating);
        }
    }
}
