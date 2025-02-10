// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.Hotel;
import com.azure.search.documents.util.AutocompletePagedFlux;
import com.azure.search.documents.util.SearchPagedFlux;
import com.azure.search.documents.util.SuggestPagedFlux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class SearchAsyncClientJavaDocSnippets {

    private static SearchAsyncClient searchAsyncClient;

    private static SearchAsyncClient createSearchAsyncClientWithSearchClientBuilder() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.instantiationWithSearchClientBuilder
        SearchAsyncClient searchAsyncClient = new SearchClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .indexName("{indexName}")
            .buildAsyncClient();
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.instantiationWithSearchClientBuilder
        return searchAsyncClient;
    }

    /**
     * Uploading a document to a SearchAsyncClient index.
     */
    public static void uploadDocument() {
        searchAsyncClient = createSearchAsyncClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.uploadDocument#Map-boolean
        List<Hotel> hotels = new ArrayList<>();
        hotels.add(new Hotel().setHotelId("100"));
        hotels.add(new Hotel().setHotelId("200"));
        hotels.add(new Hotel().setHotelId("300"));
        searchAsyncClient.uploadDocuments(hotels).block();
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.uploadDocument#Map-boolean
    }

    /**
     * Merge a document in a SearchAsyncClient index.
     */
    public static void mergeDocument() {
        searchAsyncClient = createSearchAsyncClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.mergeDocument#Map
        List<Hotel> hotels = new ArrayList<>();
        hotels.add(new Hotel().setHotelId("100"));
        hotels.add(new Hotel().setHotelId("200"));
        searchAsyncClient.mergeDocuments(hotels).block();
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.mergeDocument#Map
    }

    /**
     * Delete a document in a SearchAsyncClient index.
     */
    public static void deleteDocument() {
        searchAsyncClient = createSearchAsyncClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.deleteDocument#String
        SearchDocument documentId = new SearchDocument();
        documentId.put("hotelId", "100");
        searchAsyncClient.deleteDocuments(Collections.singletonList(documentId));
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.deleteDocument#String
    }

    /**
     * Retrieve a document from a SearchAsyncClient index.
     */
    public static void getDocument() {
        searchAsyncClient = createSearchAsyncClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.getDocument#String-Class
        Hotel hotel = searchAsyncClient.getDocument("100", Hotel.class).block();
        if (hotel != null) {
            System.out.printf("Retrieved Hotel %s%n", hotel.getHotelId());
        }
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.getDocument#String-Class
    }

    /**
     * Search documents in a SearchAsyncClient index.
     */
    public static void searchDocuments() {
        searchAsyncClient = createSearchAsyncClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.searchDocuments#String
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelId", "8");
        searchDocument.put("description", "budget");
        searchDocument.put("descriptionFr", "motel");

        SearchDocument searchDocument1 = new SearchDocument();
        searchDocument1.put("hotelId", "9");
        searchDocument1.put("description", "budget");
        searchDocument1.put("descriptionFr", "motel");

        List<SearchDocument> searchDocuments = new ArrayList<>();
        searchDocuments.add(searchDocument);
        searchDocuments.add(searchDocument1);
        searchAsyncClient.uploadDocuments(searchDocuments);

        SearchPagedFlux results = searchAsyncClient.search("SearchText");
        results.getTotalCount().subscribe(total -> System.out.printf("There are %s results", total));
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.searchDocuments#String
    }

    /**
     * Make a suggestion query to a SearchAsyncClient index.
     */
    public static void suggestDocuments() {
        searchAsyncClient = createSearchAsyncClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.suggestDocuments#String-String
        SuggestPagedFlux results = searchAsyncClient.suggest("searchText", "sg");
        results.subscribe(item -> {
            System.out.printf("The text '%s' was found.%n", item.getText());
        });
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.suggestDocuments#String-String
    }

    /**
     * Autocomplete a query in a SearchAsyncClient index.
     */
    public static void autocompleteDocuments() {
        searchAsyncClient = createSearchAsyncClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.autocomplete#String-String
        AutocompletePagedFlux results = searchAsyncClient.autocomplete("searchText", "sg");
        results.subscribe(item -> {
            System.out.printf("The text '%s' was found.%n", item.getText());
        });
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.autocomplete#String-String
    }

}
