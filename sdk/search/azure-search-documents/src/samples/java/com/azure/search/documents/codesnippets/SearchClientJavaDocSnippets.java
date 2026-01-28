// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.models.AutocompleteOptions;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.LookupDocument;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SuggestOptions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class SearchClientJavaDocSnippets {

    private static SearchClient searchClient;

    private static SearchClient createSearchClientWithSearchClientBuilder() {
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
        searchClient = createSearchClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.indexDocuments#IndexDocumentsBatch-upload
        searchClient.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(Collections.singletonMap("HotelId", "100")),
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(Collections.singletonMap("HotelId", "200")),
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(Collections.singletonMap("HotelId", "300"))
        ));
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.indexDocuments#IndexDocumentsBatch-upload
    }

    /**
     * Merge a document in a SearchClient index.
     */
    public static void mergeDocument() {
        searchClient = createSearchClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.indexDocuments#IndexDocumentsBatch-merge
        searchClient.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.MERGE).setAdditionalProperties(Collections.singletonMap("HotelId", "100")),
            new IndexAction().setActionType(IndexActionType.MERGE).setAdditionalProperties(Collections.singletonMap("HotelId", "200"))
        ));
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.indexDocuments#IndexDocumentsBatch-merge
    }

    /**
     * Delete a document in a SearchClient index.
     */
    public static void deleteDocument() {
        searchClient = createSearchClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.indexDocuments#IndexDocumentsBatch-delete
        searchClient.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(Collections.singletonMap("HotelId", "100"))
        ));
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.indexDocuments#IndexDocumentsBatch-delete
    }

    /**
     * Retrieve a document from a SearchClient index.
     */
    public static void getDocument() {
        searchClient = createSearchClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.getDocument#String
        LookupDocument document = searchClient.getDocument("100");
        if (document.getAdditionalProperties() != null) {
            System.out.printf("Retrieved Hotel %s%n", document.getAdditionalProperties().get("HotelId"));
        }
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.getDocument#String
    }

    /**
     * Search documents in a SearchClient index.
     */
    public static void searchDocuments() {
        searchClient = createSearchClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.search#SearchOptions
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("HotelId", "8");
        searchDocument.put("Description", "budget");
        searchDocument.put("DescriptionFr", "motel");

        Map<String, Object> searchDocument2 = new LinkedHashMap<>();
        searchDocument2.put("HotelId", "9");
        searchDocument2.put("Description", "budget");
        searchDocument2.put("DescriptionFr", "motel");

        searchClient.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(searchDocument),
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(searchDocument2)));

        searchClient.search(new SearchOptions().setSearchText("SearchText")).streamByPage()
            .forEach(page -> System.out.printf("There are %d results.%n", page.getCount()));
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.search#SearchOptions
    }

    /**
     * Make a suggestion query to a SearchClient index.
     */
    public static void suggestDocuments() {
        searchClient = createSearchClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.suggest#SuggestOptions
        searchClient.suggest(new SuggestOptions("searchText", "sg")).getResults()
            .forEach(item -> System.out.printf("The text '%s' was found.%n", item.getText()));
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.suggest#SuggestOptions
    }

    /**
     * Autocomplete a query in a SearchClient index.
     */
    public static void autocompleteDocuments() {
        searchClient = createSearchClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.autocomplete#AutocompleteOptions
        searchClient.autocomplete(new AutocompleteOptions("searchText", "sg")).getResults()
            .forEach(item -> System.out.printf("The text '%s' was found.%n", item.getText()));
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.autocomplete#AutocompleteOptions
    }
}

