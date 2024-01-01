// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.AutocompleteItem;
import com.azure.search.documents.models.Hotel;
import com.azure.search.documents.models.SuggestResult;
import com.azure.search.documents.util.AutocompletePagedFlux;
import com.azure.search.documents.util.AutocompletePagedIterable;
import com.azure.search.documents.util.SearchPagedFlux;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SuggestPagedFlux;
import com.azure.search.documents.util.SuggestPagedIterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SearchAsyncClientJavaDocSnippets {

    public static SearchAsyncClient SEARCH_ASYNC_CLIENT;

    public void createSearchAsyncClientWithSearchClientBuilder() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.instantiationWithSearchClientBuilder
        SearchAsyncClient searchAsyncClient = new SearchClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .indexName("{indexName}")
            .buildAsyncClient();
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.instantiationWithSearchClientBuilder
    }

    /**
     * Uploading a document to a SearchAsyncClient index.
     */
    public void uploadDocument() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.uploadDocument#Map-boolean
        List<Hotel> hotels = new ArrayList<>();
        hotels.add(new Hotel().setHotelId("100"));
        hotels.add(new Hotel().setHotelId("200"));
        hotels.add(new Hotel().setHotelId("300"));
        SEARCH_ASYNC_CLIENT.uploadDocuments(hotels).block();
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.uploadDocument#Map-boolean
    }

    /**
     * Merge a document in a SearchAsyncClient index.
     */
    public void mergeDocument() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.mergeDocument#Map
        List<Hotel> hotels = new ArrayList<>();
        hotels.add(new Hotel().setHotelId("100"));
        hotels.add(new Hotel().setHotelId("200"));
        SEARCH_ASYNC_CLIENT.mergeDocuments(hotels).block();
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.mergeDocument#Map
    }

    /**
     * Delete a document in a SearchAsyncClient index.
     */
    public void deleteDocument() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.deleteDocument#String
        SearchDocument documentId = new SearchDocument();
        documentId.put("hotelId", "100");
        SEARCH_ASYNC_CLIENT.deleteDocuments(Collections.singletonList(documentId));
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.deleteDocument#String
    }

    /**
     * Retrieve a document from a SearchAsyncClient index.
     */
    public void getDocument() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.getDocument#String-Class
        Hotel hotel = SEARCH_ASYNC_CLIENT.getDocument("100", Hotel.class).block();
        System.out.printf("Retrieved Hotel %s%n", hotel.getHotelId());
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.getDocument#String-Class
    }

    /**
     * Search documents in a SearchAsyncClient index.
     */
    public void searchDocuments() {
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
        SEARCH_ASYNC_CLIENT.uploadDocuments(searchDocuments);

        SearchPagedFlux results = SEARCH_ASYNC_CLIENT.search("SearchText");
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.searchDocuments#String
    }

    /**
     * Make a suggestion query to a SearchAsyncClient index.
     */
    public void suggestDocuments() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.suggestDocuments#String-String
        SuggestPagedFlux results = SEARCH_ASYNC_CLIENT.suggest("searchText", "sg");
        results.subscribe(item -> {
            System.out.printf("The text '%s' was found.\n", item.getText());
        });
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.suggestDocuments#String-String
    }

    /**
     * Autocomplete a query in a SearchAsyncClient index.
     */
    public void autocompleteDocuments() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.autocomplete#String-String
        AutocompletePagedFlux results = SEARCH_ASYNC_CLIENT.autocomplete("searchText", "sg");
        results.subscribe(item -> {
            System.out.printf("The text '%s' was found.\n", item.getText());
        });
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.autocomplete#String-String
    }

}
