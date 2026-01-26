// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.implementation.models.AutocompletePostOptions;
import com.azure.search.documents.implementation.models.SearchPostOptions;
import com.azure.search.documents.implementation.models.SuggestPostOptions;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
        searchAsyncClient.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(Collections.singletonMap("HotelId", "100")),
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(Collections.singletonMap("HotelId", "200")),
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(Collections.singletonMap("HotelId", "300"))
        )).block();
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.uploadDocument#Map-boolean
    }

    /**
     * Merge a document in a SearchAsyncClient index.
     */
    public static void mergeDocument() {
        searchAsyncClient = createSearchAsyncClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.mergeDocument#Map
        searchAsyncClient.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.MERGE).setAdditionalProperties(Collections.singletonMap("HotelId", "100")),
            new IndexAction().setActionType(IndexActionType.MERGE).setAdditionalProperties(Collections.singletonMap("HotelId", "200"))
        )).block();
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.mergeDocument#Map
    }

    /**
     * Delete a document in a SearchAsyncClient index.
     */
    public static void deleteDocument() {
        searchAsyncClient = createSearchAsyncClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.deleteDocument#String
        searchAsyncClient.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.DELETE).setAdditionalProperties(Collections.singletonMap("HotelId", "100"))
        )).block();
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.deleteDocument#String
    }

    /**
     * Retrieve a document from a SearchAsyncClient index.
     */
    public static void getDocument() {
        searchAsyncClient = createSearchAsyncClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.getDocument#String-Class
        searchAsyncClient.getDocument("100")
            .doOnNext(document -> {
                if (document.getAdditionalProperties() != null) {
                    System.out.printf("Retrieved Hotel %s%n", document.getAdditionalProperties().get("HotelId"));
                }
            }).block();
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.getDocument#String-Class
    }

    /**
     * Search documents in a SearchAsyncClient index.
     */
    public static void searchDocuments() {
        searchAsyncClient = createSearchAsyncClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.searchDocuments#String
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("HotelId", "8");
        searchDocument.put("Description", "budget");
        searchDocument.put("DescriptionFr", "motel");

        Map<String, Object> searchDocument2 = new LinkedHashMap<>();
        searchDocument2.put("HotelId", "9");
        searchDocument2.put("Description", "budget");
        searchDocument2.put("DescriptionFr", "motel");

        searchAsyncClient.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(searchDocument),
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(searchDocument2)
        )).block();

        searchAsyncClient.search(new SearchPostOptions().setSearchText("SearchText")).byPage()
            .subscribe(page -> System.out.printf("There are %d results", page.getCount()));
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.searchDocuments#String
    }

    /**
     * Make a suggestion query to a SearchAsyncClient index.
     */
    public static void suggestDocuments() {
        searchAsyncClient = createSearchAsyncClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.suggestDocuments#String-String
        searchAsyncClient.suggestPost(new SuggestPostOptions("searchText", "sg"))
            .subscribe(results -> results.getResults()
                .forEach(item -> System.out.printf("The text '%s' was found.%n", item.getText())));
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.suggestDocuments#String-String
    }

    /**
     * Autocomplete a query in a SearchAsyncClient index.
     */
    public static void autocompleteDocuments() {
        searchAsyncClient = createSearchAsyncClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.autocomplete#String-String
        searchAsyncClient.autocompletePost(new AutocompletePostOptions("searchText", "sg"))
            .subscribe(results -> results.getResults()
                .forEach(item -> System.out.printf("The text '%s' was found.%n", item.getText())));
        // END: com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.autocomplete#String-String
    }

}
