// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.search.documents.indexes.SearchIndexerAsyncClient;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;

public class SearchIndexerClientBuilderJavaDocsSnippets {

    /**
     * Code snippet for creating a {@link com.azure.search.documents.indexes.SearchIndexerClient}
     */
    public void  createSearchIndexerClient() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClientBuilder.instantiation.SearchIndexerClient
        SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.search.documents.indexes.SearchIndexerClientBuilder.instantiation.SearchIndexerClient
    }

    /**
     * Code snippet for creating a {@link com.azure.search.documents.indexes.SearchIndexerAsyncClient}
     */
    public void  createSearchIndexerAsyncClient() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClientBuilder.instantiation.SearchIndexerAsyncClient
        SearchIndexerAsyncClient searchIndexerClient = new SearchIndexerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.search.documents.indexes.SearchIndexerClientBuilder.instantiation.SearchIndexerAsyncClient
    }

    /**
     * Code snippet for authenticating with Default Azure Credential.
     */
    public void createSearchIndexerClientWithDefaultAzureCredential() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClientBuilder-classLevelJavaDoc.DefaultAzureCredential
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
            .endpoint("{endpoint}")
            .credential(credential)
            .buildClient();
        // END: com.azure.search.documents.indexes.SearchIndexerClientBuilder-classLevelJavaDoc.DefaultAzureCredential
    }
}
