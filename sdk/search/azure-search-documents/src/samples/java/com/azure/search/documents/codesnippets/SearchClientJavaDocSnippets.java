// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.AutocompleteItem;
import com.azure.search.documents.models.Hotel;
import com.azure.search.documents.models.SuggestResult;
import com.azure.search.documents.util.AutocompletePagedIterable;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SuggestPagedIterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
@SuppressWarnings("unused")
public class SearchClientJavaDocSnippets {

    private static SearchClient SEARCH_CLIENT;

    private static SearchClient createSearchClientWithSearcClientBuilder() {
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.instantiationWithSearchClientBuilder
        SearchClient searchClient = new SearchClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .indexName("{indexName}")
            .buildClient();
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.instantiationWithSearchClientBuilder
        return searchClient;
    }

    /**
     * Uploading a document to a SearchClient index.
     */
    public static void uploadDocument() {
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.uploadDocument#Map-boolean
        List<Hotel> hotels = new ArrayList<>();
        hotels.add(new Hotel().setHotelId("100"));
        hotels.add(new Hotel().setHotelId("200"));
        hotels.add(new Hotel().setHotelId("300"));
        SEARCH_CLIENT.uploadDocuments(hotels);
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.uploadDocument#Map-boolean
    }

    /**
     * Merge a document in a SearchClient index.
     */
    public static void mergeDocument() {
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.mergeDocument#Map
        List<Hotel> hotels = new ArrayList<>();
        hotels.add(new Hotel().setHotelId("100"));
        hotels.add(new Hotel().setHotelId("200"));
        SEARCH_CLIENT.mergeDocuments(hotels);
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.mergeDocument#Map
    }

    /**
     * Delete a document in a SearchClient index.
     */
    public static void deleteDocument() {
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.deleteDocument#String
        SearchDocument documentId = new SearchDocument();
        documentId.put("hotelId", "100");
        SEARCH_CLIENT.deleteDocuments(Collections.singletonList(documentId));
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.deleteDocument#String
    }

    /**
     * Retrieve a document from a SearchClient index.
     */
    public static void getDocument() {
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.getDocument#String-Class
        Hotel hotel = SEARCH_CLIENT.getDocument("100", Hotel.class);
        System.out.printf("Retrieved Hotel %s%n", hotel.getHotelId());
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.getDocument#String-Class
    }

    /**
     * Search documents in a SearchClient index.
     */
    public static void searchDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.searchDocuments#String
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
        SEARCH_CLIENT.uploadDocuments(searchDocuments);

        SearchPagedIterable results = SEARCH_CLIENT.search("SearchText");
        System.out.printf("There are %s results.%n", results.getTotalCount());
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.searchDocuments#String
    }

    /**
     * Make a suggestion query to a SearchClient index.
     */
    public static void suggestDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.suggestDocuments#String-String
        SuggestPagedIterable suggestPagedIterable = SEARCH_CLIENT.suggest("searchText", "sg");
        for (SuggestResult result: suggestPagedIterable) {
            System.out.printf("The suggested text is %s", result.getText());
        }
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.suggestDocuments#String-String
    }

    /**
     * Autocomplete a query in a SearchClient index.
     */
    public static void autocompleteDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.autocomplete#String-String
        AutocompletePagedIterable autocompletePagedIterable = SEARCH_CLIENT.autocomplete("searchText", "sg");
        for (AutocompleteItem result: autocompletePagedIterable) {
            System.out.printf("The complete term is %s", result.getText());
        }
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.autocomplete#String-String
    }
}

