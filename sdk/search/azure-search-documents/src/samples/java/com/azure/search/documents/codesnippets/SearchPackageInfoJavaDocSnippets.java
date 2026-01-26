// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.implementation.models.SearchPostOptions;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.SimpleField;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchSuggester;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.SearchAudience;
import com.azure.search.documents.models.SearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        for (SearchResult result : searchClient.search(new SearchPostOptions().setSearchText("luxury"))) {
            Map<String, Object> document = result.getAdditionalProperties();
            System.out.printf("Hotel ID: %s%n", document.get("HotelId"));
            System.out.printf("Hotel Name: %s%n", document.get("HotelName"));
        }
        // END: com.azure.search.documents.packageInfo-SearchClient.search#String
    }


    //BEGIN: hotelExampleClass
    public static class Hotel {
        private String hotelId;
        private String hotelName;

        @SimpleField(name = "HotelId", isKey = true)
        public String getHotelId() {
            return this.hotelId;
        }

        @SimpleField(name = "HotelName")
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
        for (SearchResult result : searchClient.search(new SearchPostOptions().setSearchText("luxury"))) {
            Map<String, Object> hotel = result.getAdditionalProperties();
            System.out.printf("Hotel ID: %s%n", hotel.get("HotelId"));
            System.out.printf("Hotel Name: %s%n", hotel.get("HotelName"));
        }
        // END: com.azure.search.documents.packageInfo-SearchClient.search#String-Object-Class-Method

    }

    /**
     * Query using SearchOptions
     */
    public void searchWithOptions() {
        SearchClient searchClient = createSearchClient();
        // BEGIN: com.azure.search.documents.packageInfo-SearchClient.search#SearchOptions
        SearchPostOptions options = new SearchPostOptions().setSearchText("luxury")
            .setFilter("rating gt 4")
            .setOrderBy("rating desc")
            .setTop(5);
        searchClient.search(options).forEach(result -> {
            System.out.printf("Hotel ID: %s%n", result.getAdditionalProperties().get("HotelId"));
            System.out.printf("Hotel Name: %s%n", result.getAdditionalProperties().get("HotelName"));
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
        List<SearchField> searchFields = SearchIndexClient.buildSearchFields(Hotel.class);
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
        SearchSuggester suggester = new SearchSuggester("sg", Collections.singletonList("hotelName"));
        // Prepare SearchIndex with index name and search fields.
        SearchIndex index = new SearchIndex("hotels", searchFieldList).setSuggesters(suggester);
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
        Map<String, Object> hotel = searchClient.getDocument("1").getAdditionalProperties();
        System.out.printf("Hotel ID: %s%n", hotel.get("HotelId"));
        System.out.printf("Hotel Name: %s%n", hotel.get("HotelName"));
        // END: com.azure.search.documents.packageInfo-SearchClient.getDocument#String-String
    }

    /**
     * Adding documents to your index.
     */
    public void uploadDocuments() {
        SearchClient searchClient = createSearchClient();
        // BEGIN: com.azure.search.documents.packageInfo-SearchClient.uploadDocuments#Iterable-boolean-boolean
        Map<String, Object> hotel = new LinkedHashMap<>();
        hotel.put("HotelId", "783");
        hotel.put("HotelName", "Upload Inn");

        Map<String, Object> hotel2 = new LinkedHashMap<>();
        hotel2.put("HotelId", "12");
        hotel2.put("HotelName", "Renovated Ranch");

        IndexDocumentsBatch batch = new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(hotel),
            new IndexAction().setActionType(IndexActionType.MERGE).setAdditionalProperties(hotel2));
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
            searchClient.search(new SearchPostOptions().setSearchText("hotel"))
                .forEach(result -> System.out.println(result.getAdditionalProperties().get("hotelName")));
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
