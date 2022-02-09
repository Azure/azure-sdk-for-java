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
import com.azure.search.documents.indexes.SearchIndexerAsyncClient;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchSuggester;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.util.SearchPagedIterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 *
 * Code samples for the README.md
 */
@SuppressWarnings("unused")
public class ReadmeSamples {

    private final String endpoint = "endpoint";
    private final String adminKey = "admin key";
    private final String apiKey = "api key";
    private final String indexName = "index name";
    private final SearchIndexClient searchIndexClient = new SearchIndexClientBuilder().buildClient();
    private final SearchClient searchClient = new SearchClientBuilder().buildClient();
    private final SearchAsyncClient searchAsyncClient = new SearchClientBuilder().buildAsyncClient();

    public void createSearchClient() {
        // BEGIN: readme-sample-createSearchClient
        SearchClient searchClient = new SearchClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(adminKey))
            .indexName(indexName)
            .buildClient();
        // END: readme-sample-createSearchClient
    }

    public void createAsyncSearchClient() {
        // BEGIN: readme-sample-createAsyncSearchClient
        SearchAsyncClient searchAsyncClient = new SearchClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(adminKey))
            .indexName(indexName)
            .buildAsyncClient();
        // END: readme-sample-createAsyncSearchClient
    }

    public void createIndexClient() {
        // BEGIN: readme-sample-createIndexClient
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();
        // END: readme-sample-createIndexClient
    }

    public void createIndexAsyncClient() {
        // BEGIN: readme-sample-createIndexAsyncClient
        SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildAsyncClient();
        // END: readme-sample-createIndexAsyncClient
    }

    public void createIndexerClient() {
        // BEGIN: readme-sample-createIndexerClient
        SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();
        // END: readme-sample-createIndexerClient
    }

    public void createIndexerAsyncClient() {
        // BEGIN: readme-sample-createIndexerAsyncClient
        SearchIndexerAsyncClient searchIndexerAsyncClient = new SearchIndexerClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildAsyncClient();
        // END: readme-sample-createIndexerAsyncClient
    }

    public void customHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("my-header1", "my-header1-value");
        headers.set("my-header2", "my-header2-value");
        headers.set("my-header3", "my-header3-value");
        // Call API by passing headers in Context.
        SearchIndex index = new SearchIndex(indexName).setFields(
            new SearchField("hotelId", SearchFieldDataType.STRING)
                .setKey(true)
                .setFilterable(true)
                .setSortable(true));
        searchIndexClient.createIndexWithResponse(index,
            new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers));
        // Above three HttpHeader will be added in outgoing HttpRequest.
    }

    public void handleErrorsWithSyncClient() {
        // BEGIN: readme-sample-handleErrorsWithSyncClient
        try {
            Iterable<SearchResult> results = searchClient.search("hotel");
        } catch (HttpResponseException ex) {
            // The exception contains the HTTP status code and the detailed message
            // returned from the search service
            HttpResponse response = ex.getResponse();
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Message: " + ex.getMessage());
        }
        // END: readme-sample-handleErrorsWithSyncClient
    }

    public void searchWithDynamicType() {
        // BEGIN: readme-sample-searchWithDynamicType
        for (SearchResult searchResult : searchClient.search("luxury")) {
            SearchDocument doc = searchResult.getDocument(SearchDocument.class);
            String id = (String) doc.get("hotelId");
            String name = (String) doc.get("hotelName");
            System.out.printf("This is hotelId %s, and this is hotel name %s.%n", id, name);
        }
        // END: readme-sample-searchWithDynamicType
    }

    // BEGIN: readme-sample-hotelclass
    public class Hotel {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public Hotel setId(String id) {
            this.id = id;
            return this;
        }

        public String getName() {
            return name;
        }

        public Hotel setName(String name) {
            this.name = name;
            return this;
        }
    }
    // END: readme-sample-hotelclass

    public void searchWithStronglyType() {
        // BEGIN: readme-sample-searchWithStronglyType
        for (SearchResult searchResult : searchClient.search("luxury")) {
            Hotel doc = searchResult.getDocument(Hotel.class);
            String id = doc.getId();
            String name = doc.getName();
            System.out.printf("This is hotelId %s, and this is hotel name %s.%n", id, name);
        }
        // END: readme-sample-searchWithStronglyType
    }

    public void searchWithSearchOptions() {
        // BEGIN: readme-sample-searchWithSearchOptions
        SearchOptions options = new SearchOptions()
            .setFilter("rating ge 4")
            .setOrderBy("rating desc")
            .setTop(5);
        SearchPagedIterable searchResultsIterable = searchClient.search("luxury", options, Context.NONE);
        // ...
        // END: readme-sample-searchWithSearchOptions
    }

    public void searchWithAsyncClient() {
        // BEGIN: readme-sample-searchWithAsyncClient
        searchAsyncClient.search("luxury")
            .subscribe(result -> {
                Hotel hotel = result.getDocument(Hotel.class);
                System.out.printf("This is hotelId %s, and this is hotel name %s.%n", hotel.getId(), hotel.getName());
            });
        // END: readme-sample-searchWithAsyncClient
    }

    public void retrieveDocuments() {
        // BEGIN: readme-sample-retrieveDocuments
        Hotel hotel = searchClient.getDocument("1", Hotel.class);
        System.out.printf("This is hotelId %s, and this is hotel name %s.%n", hotel.getId(), hotel.getName());
        // END: readme-sample-retrieveDocuments
    }

    public void batchDocumentsOperations() {
        // BEGIN: readme-sample-batchDocumentsOperations
        IndexDocumentsBatch<Hotel> batch = new IndexDocumentsBatch<>();
        batch.addUploadActions(Collections.singletonList(new Hotel().setId("783").setName("Upload Inn")));
        batch.addMergeActions(Collections.singletonList(new Hotel().setId("12").setName("Renovated Ranch")));
        searchClient.indexDocuments(batch);
        // END: readme-sample-batchDocumentsOperations
    }

    public void createIndex() {
        // BEGIN: readme-sample-createIndex
        List<SearchField> searchFieldList = new ArrayList<>();
        searchFieldList.add(new SearchField("hotelId", SearchFieldDataType.STRING)
            .setKey(true)
            .setFilterable(true)
            .setSortable(true));

        searchFieldList.add(new SearchField("hotelName", SearchFieldDataType.STRING)
            .setSearchable(true)
            .setFilterable(true)
            .setSortable(true));
        searchFieldList.add(new SearchField("description", SearchFieldDataType.STRING)
            .setSearchable(true)
            .setAnalyzerName(LexicalAnalyzerName.EU_LUCENE));
        searchFieldList.add(new SearchField("tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
            .setSearchable(true)
            .setFilterable(true)
            .setFacetable(true));
        searchFieldList.add(new SearchField("address", SearchFieldDataType.COMPLEX)
            .setFields(new SearchField("streetAddress", SearchFieldDataType.STRING).setSearchable(true),
                new SearchField("city", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setFilterable(true)
                    .setFacetable(true)
                    .setSortable(true),
                new SearchField("stateProvince", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setFilterable(true)
                    .setFacetable(true)
                    .setSortable(true),
                new SearchField("country", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setFilterable(true)
                    .setFacetable(true)
                    .setSortable(true),
                new SearchField("postalCode", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setFilterable(true)
                    .setFacetable(true)
                    .setSortable(true)
            ));

        // Prepare suggester.
        SearchSuggester suggester = new SearchSuggester("sg", Collections.singletonList("hotelName"));
        // Prepare SearchIndex with index name and search fields.
        SearchIndex index = new SearchIndex("hotels").setFields(searchFieldList).setSuggesters(suggester);
        // Create an index
        searchIndexClient.createIndex(index);
        // END: readme-sample-createIndex
    }

    public void createIndexUseFieldBuilder() {
        // BEGIN: readme-sample-createIndexUseFieldBuilder
        List<SearchField> searchFields = SearchIndexClient.buildSearchFields(Hotel.class, null);
        searchIndexClient.createIndex(new SearchIndex("index", searchFields));
        // END: readme-sample-createIndexUseFieldBuilder
    }
}
