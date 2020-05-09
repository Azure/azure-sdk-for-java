// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SearchIndexerDataSourceClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerSkillsetClient;
import com.azure.search.documents.indexes.SearchSynonymMapClient;
import com.azure.search.documents.indexes.models.SearchOptions;
import com.azure.search.documents.indexes.models.SearchResult;
import com.azure.search.documents.models.Hotel;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.SearchField;
import com.azure.search.documents.models.SearchFieldDataType;
import com.azure.search.documents.models.SearchIndex;

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
    private SearchServiceClient searchServiceClient = new SearchServiceClientBuilder().buildClient();
    private SearchIndexClient searchIndexClient = searchServiceClient.getSearchIndexClient();
    private SearchIndexerDataSourceClient dataSourceClient = searchServiceClient.getDataSourceClient();
    private SearchIndexerClient searchIndexerClient = searchServiceClient.getSearchIndexerClient();
    private SearchIndexerSkillsetClient skillsetClient = searchServiceClient.getSkillsetClient();
    private SearchSynonymMapClient synonymMapClient = searchServiceClient.getSynonymMapClient();
    private SearchClient searchClient = new SearchClientBuilder().buildClient();

    public void createSearchServiceClient() {
        SearchServiceClient searchServiceClient = new SearchServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(adminKey))
            .buildClient();
    }

    public void createSearchServiceAsyncClient() {
        SearchServiceAsyncClient searchServiceAsyncClient = new SearchServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(adminKey))
            .buildAsyncClient();
    }

    public void createSearchClient() {
        SearchClient searchClient = new SearchClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .indexName(indexName)
            .buildClient();
    }

    public void createSearchAsyncClient() {
        SearchAsyncClient searchAsyncClient = new SearchClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .indexName(indexName)
            .buildAsyncClient();
    }

    public void createIndexClient() {
        SearchServiceClient searchServiceClient = new SearchServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();
        SearchIndexClient searchIndexclient = searchServiceClient.getSearchIndexClient();
    }

    public void createDataSourceClient() {
        SearchServiceClient searchServiceClient = new SearchServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();
        SearchIndexerDataSourceClient dataSourceClient = searchServiceClient.getDataSourceClient();
    }

    public void createIndexerClient() {
        SearchServiceClient searchServiceClient = new SearchServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();
        SearchIndexerClient searchIndexerClient = searchServiceClient.getSearchIndexerClient();
    }

    public void createSkillsetClient() {
        SearchServiceClient searchServiceClient = new SearchServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();
        SearchIndexerSkillsetClient skillsetClient = searchServiceClient.getSkillsetClient();
    }

    public void createSynonymMapClient() {
        SearchServiceClient searchServiceClient = new SearchServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();
        SearchSynonymMapClient synonymMapClient = searchServiceClient.getSynonymMapClient();
    }

    public void customHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.put("my-header1", "my-header1-value");
        headers.put("my-header2", "my-header2-value");
        headers.put("my-header3", "my-header3-value");
        // Call API by passing headers in Context.
        SearchIndex index = new SearchIndex().setName(indexName);
        searchIndexClient.createWithResponse(
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
        searchIndexClient.create(newIndex);
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
