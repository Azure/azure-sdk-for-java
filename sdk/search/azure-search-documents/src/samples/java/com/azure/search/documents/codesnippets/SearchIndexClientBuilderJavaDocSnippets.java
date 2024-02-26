// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
@SuppressWarnings("unused")
public class SearchIndexClientBuilderJavaDocSnippets {

    /**
     * Create SearchIndexClient from SearchIndexClientBuilder.
     */
    public static SearchIndexClient createSearchIndexClientFromBuilder() {
        // BEGIN: com.azure.search.documents.SearchIndexClientBuilder.instantiation.SearchIndexClient
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.search.documents.SearchIndexClientBuilder.instantiation.SearchIndexClient
        return searchIndexClient;
    }


    /**
     * Create SearchIndexAsyncClient from SearchIndexClientBuilder.
     */
    public static SearchIndexAsyncClient createSearchIndexAsyncClientFromBuilder() {
        // BEGIN: com.azure.search.documents.SearchIndexClientBuilder.instantiation.SearchIndexAsyncClient
        SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.search.documents.SearchIndexClientBuilder.instantiation.SearchIndexAsyncClient
        return searchIndexAsyncClient;
    }

    /**
     * Instantiate a synchronous client using DefaultAzureCredential.
     */
    public static SearchIndexClient createSearchIndexClientWithDefaultAzureCredential() {
        // BEGIN: com.azure.search.documents.SearchIndexClientBuilder.credential
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .credential(credential)
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.search.documents.SearchIndexClientBuilder.credential
        return searchIndexClient;
    }

    /**
     * Instantiate an asynchronous client using DefaultAzureCredential.
     */
    public static SearchIndexAsyncClient createSearchIndexAsyncClientWithDefaultAzureCredential() {
        // BEGIN: com.azure.search.documents.SearchIndexClientBuilder.async.credential
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
            .credential(credential)
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.search.documents.SearchIndexClientBuilder.async.credential
        return searchIndexAsyncClient;
    }
}
