// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerAsyncClient;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchSuggester;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchPagedIterable;
import com.azure.search.documents.models.SearchResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Code samples for the README.md
 */
@SuppressWarnings("unused")
public class ReadmeSamples {
    private static final String ENDPOINT = "endpoint";
    private static final String ADMIN_KEY = "admin key";
    private static final String API_KEY = "api key";
    private static final String INDEX_NAME = "index name";
    private static final SearchIndexClient SEARCH_INDEX_CLIENT = new SearchIndexClientBuilder().buildClient();
    private static final SearchClient SEARCH_CLIENT = new SearchClientBuilder().buildClient();
    private static final SearchAsyncClient SEARCH_ASYNC_CLIENT = new SearchClientBuilder().buildAsyncClient();

    public void createSearchClient() {
        // BEGIN: readme-sample-createSearchClient
        SearchClient searchClient = new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .indexName(INDEX_NAME)
            .buildClient();
        // END: readme-sample-createSearchClient
    }

    public void createAsyncSearchClient() {
        // BEGIN: readme-sample-createAsyncSearchClient
        SearchAsyncClient searchAsyncClient = new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .indexName(INDEX_NAME)
            .buildAsyncClient();
        // END: readme-sample-createAsyncSearchClient
    }

    public void createIndexClient() {
        // BEGIN: readme-sample-createIndexClient
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .buildClient();
        // END: readme-sample-createIndexClient
    }

    public void createIndexAsyncClient() {
        // BEGIN: readme-sample-createIndexAsyncClient
        SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .buildAsyncClient();
        // END: readme-sample-createIndexAsyncClient
    }

    public void createIndexerClient() {
        // BEGIN: readme-sample-createIndexerClient
        SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .buildClient();
        // END: readme-sample-createIndexerClient
    }

    public void createIndexerAsyncClient() {
        // BEGIN: readme-sample-createIndexerAsyncClient
        SearchIndexerAsyncClient searchIndexerAsyncClient = new SearchIndexerClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .buildAsyncClient();
        // END: readme-sample-createIndexerAsyncClient
    }

    public void customHeaders() {
        RequestOptions requestOptions = new RequestOptions()
            .setHeader("my-header1", "my-header1-value")
            .setHeader("my-header2", "my-header2-value")
            .setHeader("my-header3", "my-header3-value");
        // Call API by passing headers in Context.
        SearchIndex index = new SearchIndex(INDEX_NAME,
            new SearchField("hotelId", SearchFieldDataType.STRING)
                .setKey(true)
                .setFilterable(true)
                .setSortable(true));
        SEARCH_INDEX_CLIENT.createIndexWithResponse(index, requestOptions);
        // Above three HttpHeader will be added in outgoing HttpRequest.
    }

    public void handleErrorsWithSyncClient() {
        // BEGIN: readme-sample-handleErrorsWithSyncClient
        try {
            Iterable<SearchResult> results = SEARCH_CLIENT.search(new SearchOptions().setSearchText("hotel"));
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
        for (SearchResult searchResult : SEARCH_CLIENT.search(new SearchOptions().setSearchText("luxury"))) {
            Map<String, Object> doc = searchResult.getAdditionalProperties();
            System.out.printf("This is hotelId %s, and this is hotel name %s.%n", doc.get("HotelId"), doc.get("HotelName"));
        }
        // END: readme-sample-searchWithDynamicType
    }

    // BEGIN: readme-sample-hotelclass
    public static class Hotel {
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
        for (SearchResult searchResult : SEARCH_CLIENT.search(new SearchOptions().setSearchText("luxury"))) {
            Map<String, Object> doc = searchResult.getAdditionalProperties();
            System.out.printf("This is hotelId %s, and this is hotel name %s.%n", doc.get("Id"), doc.get("Name"));
        }
        // END: readme-sample-searchWithStronglyType
    }

    public void searchWithSearchOptions() {
        // BEGIN: readme-sample-searchWithSearchOptions
        SearchOptions options = new SearchOptions().setSearchText("luxury")
            .setFilter("rating ge 4")
            .setOrderBy("rating desc")
            .setTop(5);
        SearchPagedIterable searchResultsIterable = SEARCH_CLIENT.search(options);
        // ...
        // END: readme-sample-searchWithSearchOptions
    }

    public void searchWithAsyncClient() {
        // BEGIN: readme-sample-searchWithAsyncClient
        SEARCH_ASYNC_CLIENT.search(new SearchOptions().setSearchText("luxury"))
            .subscribe(result -> {
                Map<String, Object> hotel = result.getAdditionalProperties();
                System.out.printf("This is hotelId %s, and this is hotel name %s.%n", hotel.get("Id"), hotel.get("Name"));
            });
        // END: readme-sample-searchWithAsyncClient
    }

    public void retrieveDocuments() {
        // BEGIN: readme-sample-retrieveDocuments
        Map<String, Object> hotel = SEARCH_CLIENT.getDocument("1").getAdditionalProperties();
        System.out.printf("This is hotelId %s, and this is hotel name %s.%n", hotel.get("Id"), hotel.get("Name"));
        // END: readme-sample-retrieveDocuments
    }

    public void batchDocumentsOperations() {
        // BEGIN: readme-sample-batchDocumentsOperations
        Map<String, Object> hotel = new LinkedHashMap<>();
        hotel.put("Id", "783");
        hotel.put("Name", "Upload Inn");

        Map<String, Object> hotel2 = new LinkedHashMap<>();
        hotel2.put("Id", "12");
        hotel2.put("Name", "Renovated Ranch");
        IndexDocumentsBatch batch = new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(hotel),
            new IndexAction().setActionType(IndexActionType.MERGE).setAdditionalProperties(hotel2));
        SEARCH_CLIENT.indexDocuments(batch);
        // END: readme-sample-batchDocumentsOperations
    }

    public void createIndex() {
        // BEGIN: readme-sample-createIndex
        List<SearchField> searchFieldList = new ArrayList<>();
        searchFieldList.add(new SearchField("HotelId", SearchFieldDataType.STRING)
            .setKey(true)
            .setFilterable(true)
            .setSortable(true));
        searchFieldList.add(new SearchField("HotelName", SearchFieldDataType.STRING)
            .setSearchable(true)
            .setFilterable(true)
            .setSortable(true));
        searchFieldList.add(new SearchField("Description", SearchFieldDataType.STRING)
            .setSearchable(true)
            .setAnalyzerName(LexicalAnalyzerName.EU_LUCENE));
        searchFieldList.add(new SearchField("Tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
            .setSearchable(true)
            .setFilterable(true)
            .setFacetable(true));
        searchFieldList.add(new SearchField("Address", SearchFieldDataType.COMPLEX)
            .setFields(new SearchField("StreetAddress", SearchFieldDataType.STRING).setSearchable(true),
                new SearchField("City", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setFilterable(true)
                    .setFacetable(true)
                    .setSortable(true),
                new SearchField("StateProvince", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setFilterable(true)
                    .setFacetable(true)
                    .setSortable(true),
                new SearchField("Country", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setFilterable(true)
                    .setFacetable(true)
                    .setSortable(true),
                new SearchField("PostalCode", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setFilterable(true)
                    .setFacetable(true)
                    .setSortable(true)));

        // Prepare suggester.
        SearchSuggester suggester = new SearchSuggester("sg", "hotelName");
        // Prepare SearchIndex with index name and search fields.
        SearchIndex index = new SearchIndex("hotels", searchFieldList).setSuggesters(suggester);
        // Create an index
        SEARCH_INDEX_CLIENT.createIndex(index);
        // END: readme-sample-createIndex
    }

    public void createIndexUseFieldBuilder() {
        // BEGIN: readme-sample-createIndexUseFieldBuilder
        List<SearchField> searchFields = SearchIndexClient.buildSearchFields(Hotel.class);
        SEARCH_INDEX_CLIENT.createIndex(new SearchIndex("index", searchFields));
        // END: readme-sample-createIndexUseFieldBuilder
    }

    public void nationalCloud() {
        // BEGIN: readme-sample-nationalCloud
        // Create a SearchClient that will authenticate through AAD in the China national cloud.
        SearchClient searchClient = new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .indexName(INDEX_NAME)
            .credential(new DefaultAzureCredentialBuilder()
                .authorityHost(AzureAuthorityHosts.AZURE_CHINA)
                .build())
            .audience(SearchAudience.AZURE_CHINA)
            .buildClient();
        // END: readme-sample-nationalCloud
    }

    public void searchClientWithTokenCredential() {
        // BEGIN: readme-sample-searchClientWithTokenCredential
        String indexName = "nycjobs";

        // Get the service endpoint from the environment
        String endpoint = Configuration.getGlobalConfiguration().get("SEARCH_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        // Create a client
        SearchClient client = new SearchClientBuilder()
            .endpoint(endpoint)
            .indexName(indexName)
            .credential(credential)
            .buildClient();
        // END: readme-sample-searchClientWithTokenCredential
    }
}
