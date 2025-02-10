// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.SimpleField;
import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchSuggester;
import com.azure.search.documents.models.SearchAudience;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.util.SearchPagedIterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@SuppressWarnings("unused")
public class SearchPackageInfoJavaDocSnippets {

    /**
    * Code snippet for creating a {@link SearchIndexClient}
    *
    * @return the created SearchIndexClient
    */
    public SearchIndexClient createSearchIndexClient() {
        // BEGIN: com.azure.search.documents.packageInfo-SearchIndexClient.instantiation
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint("{endpoint}")
            .credential(new AzureKeyCredential("{key}"))
            .buildClient();
        // END: com.azure.search.documents.packageInfo-SearchIndexClient.instantiation
        return searchIndexClient;
    }

    /**
     * Code snippet for creating a {@link SearchIndexerClient}
     *
     * @return the created SearchIndexerClient
     */
    public SearchIndexerClient createSearchIndexerClient() {
        // BEGIN: com.azure.search.documents.packageInfo-SearchIndexerClient.instantiation
        SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
            .endpoint("{endpoint}")
            .credential(new AzureKeyCredential("{key}"))
            .buildClient();
        // END: com.azure.search.documents.packageInfo-SearchIndexerClient.instantiation
        return searchIndexerClient;
    }

    /**
     * Code snippet for creating a {@link SearchClient}
     *
     * @return the created SearchClient
     */
    public SearchClient createSearchClient() {
        // BEGIN: com.azure.search.documents.packageInfo-SearchClient.instantiation
        SearchClient searchClient = new SearchClientBuilder()
            .endpoint("{endpoint}")
            .credential(new AzureKeyCredential("{key}"))
            .indexName("{indexName}")
            .buildClient();
        // END: com.azure.search.documents.packageInfo-SearchClient.instantiation
        return searchClient;
    }

    /**
     * Query using SearchDocument as a dictionary for search results
     */
    public void searchDocumentDictionary() {
        SearchClient searchClient = createSearchClient();
        // BEGIN: com.azure.search.documents.packageInfo-SearchClient.search#String
        for (SearchResult result : searchClient.search("luxury")) {
            SearchDocument document = result.getDocument(SearchDocument.class);
            System.out.printf("Hotel ID: %s%n", document.get("hotelId"));
            System.out.printf("Hotel Name: %s%n", document.get("hotelName"));
        }
        // END: com.azure.search.documents.packageInfo-SearchClient.search#String
    }


    //BEGIN: hotelExampleClass
    public static class Hotel {
        private String hotelId;
        private String hotelName;

        @SimpleField(isKey = true)
        public String getHotelId() {
            return this.hotelId;
        }

        public String getHotelName() {
            return this.hotelName;
        }

        public Hotel setHotelId(String number) {
            this.hotelId = number;
            return this;
        }

        public Hotel setHotelName(String secretPointMotel) {
            this.hotelName = secretPointMotel;
            return this;
        }
    }
    //END: hotelExampleClass

    /**
     * Query using Java model class for search results
     */
    public void searchModelClass() {
        SearchClient searchClient = createSearchClient();


        // BEGIN: com.azure.search.documents.packageInfo-SearchClient.search#String-Object-Class-Method
        for (SearchResult result : searchClient.search("luxury")) {
            Hotel hotel = result.getDocument(Hotel.class);
            System.out.printf("Hotel ID: %s%n", hotel.getHotelId());
            System.out.printf("Hotel Name: %s%n", hotel.getHotelName());
        }
        // END: com.azure.search.documents.packageInfo-SearchClient.search#String-Object-Class-Method

    }

    /**
     * Query using SearchOptions
     */
    public void searchWithOptions() {
        SearchClient searchClient = createSearchClient();
        // BEGIN: com.azure.search.documents.packageInfo-SearchClient.search#SearchOptions
        SearchOptions options = new SearchOptions()
            .setFilter("rating gt 4")
            .setOrderBy("rating desc")
            .setTop(5);
        SearchPagedIterable searchResultsIterable = searchClient.search("luxury", options, Context.NONE);
        searchResultsIterable.forEach(result -> {
            System.out.printf("Hotel ID: %s%n", result.getDocument(Hotel.class).getHotelId());
            System.out.printf("Hotel Name: %s%n", result.getDocument(Hotel.class).getHotelName());
        });
        // END: com.azure.search.documents.packageInfo-SearchClient.search#SearchOptions
    }

    /**
     * Create an index using SearchIndexClient
     */
    public void createSearchIndex() {
        SearchIndexClient searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.packageInfo-SearchIndexClient.createIndex#SearchIndex
        // Create a new search index structure that matches the properties of the Hotel class.
        List<SearchField> searchFields = SearchIndexClient.buildSearchFields(Hotel.class, null);
        searchIndexClient.createIndex(new SearchIndex("hotels", searchFields));
        // END: com.azure.search.documents.packageInfo-SearchIndexClient.createIndex#SearchIndex
    }

    /**
     * Build search fields using SearchField directly and create an index using SearchIndexClient
     */
    public void createSearchIndexWithSearchField() {
        SearchIndexClient searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.packageInfo-SearchIndexClient.createIndex#String-List-boolean
        // Create a new search index structure that matches the properties of the Hotel class.
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
        // END: com.azure.search.documents.packageInfo-SearchIndexClient.createIndex#String-List-boolean
    }

    /**
     * Retrieve a specific document using key.
     */
    public void getDocument() {
        SearchClient searchClient = createSearchClient();
        // BEGIN: com.azure.search.documents.packageInfo-SearchClient.getDocument#String-String
        Hotel hotel = searchClient.getDocument("1", Hotel.class);
        System.out.printf("Hotel ID: %s%n", hotel.getHotelId());
        System.out.printf("Hotel Name: %s%n", hotel.getHotelName());
        // END: com.azure.search.documents.packageInfo-SearchClient.getDocument#String-String
    }

    /**
     * Adding documents to your index.
     */
    public void uploadDocuments() {
        SearchClient searchClient = createSearchClient();
        // BEGIN: com.azure.search.documents.packageInfo-SearchClient.uploadDocuments#Iterable-boolean-boolean
        IndexDocumentsBatch<Hotel> batch = new IndexDocumentsBatch<Hotel>();
        batch.addUploadActions(Collections.singletonList(
                new Hotel().setHotelId("783").setHotelName("Upload Inn")));
        batch.addMergeActions(Collections.singletonList(
                new Hotel().setHotelId("12").setHotelName("Renovated Ranch")));
        searchClient.indexDocuments(batch);
        // END: com.azure.search.documents.packageInfo-SearchClient.uploadDocuments#Iterable-boolean-boolean
    }

    /**
     * Authenticate SearchClient in a national cloud.
     */
    public SearchClient createSearchClientInNationalCloud() {
        // BEGIN: com.azure.search.documents.packageInfo-SearchClient.instantiation.nationalCloud
        SearchClient searchClient = new SearchClientBuilder()
            .endpoint("{endpoint}")
            .credential(new DefaultAzureCredentialBuilder()
                .authorityHost("{national cloud endpoint}")
                .build())
            .audience(SearchAudience.AZURE_PUBLIC_CLOUD) //set the audience of your cloud
            .buildClient();
        // END: com.azure.search.documents.packageInfo-SearchClient.instantiation.nationalCloud
        return searchClient;
    }

    /**
     * Handle Search Error Response
     */
    public void handleSearchError() {
        SearchClient searchClient = createSearchClient();
        // BEGIN: com.azure.search.documents.packageInfo-SearchClient.search#String-Object-Class-Error
        try {
            Iterable<SearchResult> results = searchClient.search("hotel");
            results.forEach(result -> {
                System.out.println(result.getDocument(Hotel.class).getHotelName());
            });
        } catch (HttpResponseException ex) {
            // The exception contains the HTTP status code and the detailed message
            // returned from the search service
            HttpResponse response = ex.getResponse();
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Message: " + ex.getMessage());
        }
        // END: com.azure.search.documents.packageInfo-SearchClient.search#String-Object-Class-Error
    }

}
