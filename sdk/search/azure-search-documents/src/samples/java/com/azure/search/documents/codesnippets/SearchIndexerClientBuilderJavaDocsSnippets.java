// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.search.documents.indexes.SearchIndexerAsyncClient;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
@SuppressWarnings("unused")
public class SearchIndexerClientBuilderJavaDocsSnippets {

    /**
     * Code snippet for creating a {@link com.azure.search.documents.indexes.SearchIndexerClient}
     */
    public SearchIndexerClient createSearchIndexerClient() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClientBuilder.instantiation.SearchIndexerClient
        SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.search.documents.indexes.SearchIndexerClientBuilder.instantiation.SearchIndexerClient
        return searchIndexerClient;
    }

    /**
     * Code snippet for creating a {@link com.azure.search.documents.indexes.SearchIndexerAsyncClient}
     */
    public SearchIndexerAsyncClient createSearchIndexerAsyncClient() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClientBuilder.instantiation.SearchIndexerAsyncClient
        SearchIndexerAsyncClient searchIndexerAsyncClient = new SearchIndexerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.search.documents.indexes.SearchIndexerClientBuilder.instantiation.SearchIndexerAsyncClient
        return searchIndexerAsyncClient;
    }

    /**
     * Code snippet for authenticating a synchronous client with Default Azure Credential.
     */
    public SearchIndexerClient createSearchIndexerClientWithDefaultAzureCredential() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClientBuilder-classLevelJavaDoc.DefaultAzureCredential
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
            .endpoint("{endpoint}")
            .credential(credential)
            .buildClient();
        // END: com.azure.search.documents.indexes.SearchIndexerClientBuilder-classLevelJavaDoc.DefaultAzureCredential
        return searchIndexerClient;
    }

    /**
     * Code snippet for authenticating an asynchronous client with Default Azure Credential.
     */
    public SearchIndexerAsyncClient createSearchIndexerAsyncClientWithDefaultAzureCredential() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClientBuilder-classLevelJavaDoc.async.DefaultAzureCredential
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        SearchIndexerAsyncClient searchIndexerAsyncClient = new SearchIndexerClientBuilder()
            .endpoint("{endpoint}")
            .credential(credential)
            .buildAsyncClient();
        // END: com.azure.search.documents.indexes.SearchIndexerClientBuilder-classLevelJavaDoc.async.DefaultAzureCredential
        return searchIndexerAsyncClient;
    }
}
