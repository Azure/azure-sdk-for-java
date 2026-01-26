// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.implementation.models.AutocompletePostOptions;
import com.azure.search.documents.implementation.models.SearchPostOptions;
import com.azure.search.documents.implementation.models.SuggestPostOptions;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.LookupDocument;

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
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.uploadDocument#Map-boolean
        searchClient.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(Collections.singletonMap("HotelId", "100")),
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(Collections.singletonMap("HotelId", "200")),
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(Collections.singletonMap("HotelId", "300"))
        ));
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.uploadDocument#Map-boolean
    }

    /**
     * Merge a document in a SearchClient index.
     */
    public static void mergeDocument() {
        searchClient = createSearchClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.mergeDocument#Map
        searchClient.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.MERGE).setAdditionalProperties(Collections.singletonMap("HotelId", "100")),
            new IndexAction().setActionType(IndexActionType.MERGE).setAdditionalProperties(Collections.singletonMap("HotelId", "200"))
        ));
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.mergeDocument#Map
    }

    /**
     * Delete a document in a SearchClient index.
     */
    public static void deleteDocument() {
        searchClient = createSearchClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.deleteDocument#String
        searchClient.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(Collections.singletonMap("HotelId", "100"))
        ));
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.deleteDocument#String
    }

    /**
     * Retrieve a document from a SearchClient index.
     */
    public static void getDocument() {
        searchClient = createSearchClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.getDocument#String-Class
        LookupDocument document = searchClient.getDocument("100");
        if (document.getAdditionalProperties() != null) {
            System.out.printf("Retrieved Hotel %s%n", document.getAdditionalProperties().get("HotelId"));
        }
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.getDocument#String-Class
    }

    /**
     * Search documents in a SearchClient index.
     */
    public static void searchDocuments() {
        searchClient = createSearchClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.searchDocuments#String
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

        searchClient.search(new SearchPostOptions().setSearchText("SearchText")).streamByPage()
            .forEach(page -> System.out.printf("There are %d results.%n", page.getCount()));
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.searchDocuments#String
    }

    /**
     * Make a suggestion query to a SearchClient index.
     */
    public static void suggestDocuments() {
        searchClient = createSearchClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.suggestDocuments#String-String
        searchClient.suggestPost(new SuggestPostOptions("searchText", "sg")).getResults()
            .forEach(item -> System.out.printf("The text '%s' was found.%n", item.getText()));
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.suggestDocuments#String-String
    }

    /**
     * Autocomplete a query in a SearchClient index.
     */
    public static void autocompleteDocuments() {
        searchClient = createSearchClientWithSearchClientBuilder();
        // BEGIN: com.azure.search.documents.SearchClient-classLevelJavaDoc.autocomplete#String-String
        searchClient.autocompletePost(new AutocompletePostOptions("searchText", "sg")).getResults()
            .forEach(item -> System.out.printf("The text '%s' was found.%n", item.getText()));
        // END: com.azure.search.documents.SearchClient-classLevelJavaDoc.autocomplete#String-String
    }
}

