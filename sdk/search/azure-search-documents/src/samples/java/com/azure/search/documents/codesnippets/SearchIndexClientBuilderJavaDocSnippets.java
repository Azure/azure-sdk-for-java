// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;

public class SearchIndexClientBuilderJavaDocSnippets {

    /**
     * Create SearchIndexClient from SearchIndexClientBuilder.
     */
    public void createSearchIndexClientFromBuilder() {
        // BEGIN: com.azure.search.documents.SearchIndexClientBuilder.instantiation.SearchIndexClient
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.search.documents.SearchIndexClientBuilder.instantiation.SearchIndexClient
    }


    /**
     * Create SearchIndexAsyncClient from SearchIndexClientBuilder.
     */
    public void createSearchIndexAsyncClientFromBuilder() {
        // BEGIN: com.azure.search.documents.SearchIndexClientBuilder.instantiation.SearchIndexAsyncClient
        SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.search.documents.SearchIndexClientBuilder.instantiation.SearchIndexAsyncClient
    }

    /**
     * Instantiate a synchronous client using DefaultAzureCredential.
     */
    public void createSearchIndexClientWithDefaultAzureCredential() {
            // BEGIN: com.azure.search.documents.SearchIndexClientBuilder.credential
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

            SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
                .credential(credential)
                .endpoint("{endpoint}")
                .buildClient();
            // END: com.azure.search.documents.SearchIndexClientBuilder.credential
    }

    /**
     * Instantiate an asynchronous client using DefaultAzureCredential.
     */
    public void createSearchIndexAsyncClientWithDefaultAzureCredential() {
        // BEGIN: com.azure.search.documents.SearchIndexClientBuilder.async.credential
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        SearchIndexAsyncClient searchIndexClient = new SearchIndexClientBuilder()
            .credential(credential)
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.search.documents.SearchIndexClientBuilder.async.credential
    }
}
