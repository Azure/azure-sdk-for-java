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
import java.util.Arrays;
import java.util.Collections;
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
    private SearchAsyncClient searchAsyncClient = new SearchClientBuilder().buildAsyncClient();

    public void createSearchClient() {
        SearchClient searchClient = new SearchClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(adminKey))
            .indexName(indexName)
            .buildClient();
    }

    public void createAsyncSearchClient() {
        SearchAsyncClient searchAsyncClient = new SearchClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(adminKey))
            .indexName(indexName)
            .buildAsyncClient();
    }

    public void createIndexClient() {
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();
    }

    public void createIndexAsyncClient() {
        SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildAsyncClient();
    }

    public void createIndexerClient() {
        SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();
    }

    public void createIndexerAsyncClient() {
        SearchIndexerAsyncClient searchIndexerAsyncClient = new SearchIndexerClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildAsyncClient();
    }

    public void customHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.put("my-header1", "my-header1-value");
        headers.put("my-header2", "my-header2-value");
        headers.put("my-header3", "my-header3-value");
        // Call API by passing headers in Context.
        SearchIndex index = new SearchIndex(indexName);
        searchIndexClient.createIndexWithResponse(
            index,
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
            System.out.println("Message: " + ex.getMessage());
        }
    }

    public void sandBoxConnection() {
        // We'll connect to the Azure Cognitive Search public sandbox and send a
        // query to its "nycjobs" index built from a public dataset of available jobs
        // in New York.
        String serviceName = "azs-playground";
        String indexName = "nycjobs";
        String apiKey = "252044BE3886FE4A8E3BAA4F595114BB";

        // Create a SearchClient to send queries
        String serviceEndpoint = String.format("https://%s.search.windows.net/", serviceName);
        AzureKeyCredential credential = new AzureKeyCredential(apiKey);
        SearchClient client = new SearchClientBuilder()
            .endpoint(serviceEndpoint)
            .credential(credential)
            .indexName(indexName)
            .buildClient();

        // Let's get the top 5 jobs related to Microsoft
        SearchPagedIterable searchResultsIterable = client.search("Microsoft", new SearchOptions().setTop(5),
            Context.NONE);
        for (SearchResult searchResult: searchResultsIterable) {
            SearchDocument document = searchResult.getDocument(SearchDocument.class);
            String title = (String) document.get("business_title");
            String description = (String) document.get("job_description");
            System.out.printf("The business title is %s, and here is the description: %s.%n",
                title, description);
        }
    }

    public void searchWithDynamicType() {
        SearchPagedIterable searchResultsIterable = searchClient.search("luxury");
        for (SearchResult searchResult: searchResultsIterable) {
            SearchDocument doc = searchResult.getDocument(SearchDocument.class);
            String id = (String) doc.get("hotelId");
            String name = (String) doc.get("hotelName");
            System.out.printf("This is hotelId %s, and this is hotel name %s.%n", id, name);
        }
    }

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

    public void searchWithStronglyType() {
        SearchPagedIterable searchResultsIterable = searchClient.search("luxury");
        for (SearchResult searchResult: searchResultsIterable) {
            Hotel doc = searchResult.getDocument(Hotel.class);
            String id = doc.getId();
            String name = doc.getName();
            System.out.printf("This is hotelId %s, and this is hotel name %s.%n", id, name);
        }
    }

    public void searchWithSearchOptions() {
        int stars = 4;
        SearchOptions options = new SearchOptions()
            .setFilter(String.format("rating ge %s", stars))
            .setOrderBy("rating desc")
            .setTop(5);
        SearchPagedIterable searchResultsIterable = searchClient.search("luxury", options, Context.NONE);
        // ...
    }

    public void searchWithAsyncClient() {
        searchAsyncClient.search("luxury")
            .subscribe(result -> {
                Hotel hotel = result.getDocument(Hotel.class);
                System.out.printf("This is hotelId %s, and this is hotel name %s.%n", hotel.getId(), hotel.getName());
            });
    }

    public void retrieveDocuments() {
        Hotel hotel = searchClient.getDocument("1", Hotel.class);
        System.out.printf("This is hotelId %s, and this is hotel name %s.%n", hotel.getId(), hotel.getName());
    }

    public void batchDocumentsOperations() {
        IndexDocumentsBatch<Hotel> batch = new IndexDocumentsBatch<Hotel>();
        batch.addUploadActions(Collections.singletonList(new Hotel().setId("783").setName("Upload Inn")));
        batch.addMergeActions(Collections.singletonList(new Hotel().setId("12").setName("Renovated Ranch")));
        searchClient.indexDocuments(batch);
    }

    public void createIndex() {
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
            .setKey(true)
            .setFilterable(true)
            .setFacetable(true));
        searchFieldList.add(new SearchField("address", SearchFieldDataType.COMPLEX)
            .setFields(Arrays.asList(
                new SearchField("streetAddress", SearchFieldDataType.STRING).setSearchable(true),
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
            )));

        // Prepare suggester.
        SearchSuggester suggester = new SearchSuggester("sg", Collections.singletonList("hotelName"));
        // Prepare SearchIndex with index name and search fields.
        SearchIndex index = new SearchIndex("hotels").setFields(searchFieldList).setSuggesters(
            Collections.singletonList(suggester));
        // Create an index
        searchIndexClient.createIndex(index);
    }

    public void createIndexUseFieldBuilder() {
        List<SearchField> searchFields = SearchIndexClient.buildSearchFields(Hotel.class, null);
        searchIndexClient.createIndex(new SearchIndex("index", searchFields));
    }
}
