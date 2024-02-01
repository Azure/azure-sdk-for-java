// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
// BEGIN: DefaultAzureCredentialImports
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
// END: DefaultAzureCredentialImports
import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;

@SuppressWarnings("unused")
public class SearchClientBuilderJavaDocSnippets {

    /**
     * Code snippet for creating {@link SearchClient} using a {@link SearchClientBuilder}.
     */
    public SearchClient createSearchClientFromBuilder() {
        // BEGIN: com.azure.search.documents.SearchClientBuilder-classLevelJavaDoc.instantiation.SearchClient
        SearchClient searchClient = new SearchClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .indexName("{indexName}")
            .buildClient();
        // END: com.azure.search.documents.SearchClientBuilder-classLevelJavaDoc.instantiation.SearchClient
        return searchClient;
    }

    /**
     * Code snippet for creating a {@link SearchAsyncClient}.
     */
    public SearchAsyncClient createSearchAsyncClientFromBuilder() {
        // BEGIN: com.azure.search.documents.SearchClientBuilder-classLevelJavaDoc.instantiation.SearchAsyncClient
        SearchAsyncClient searchAsyncClient = new SearchClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .indexName("{indexName}")
            .buildAsyncClient();
        // END: com.azure.search.documents.SearchClientBuilder-classLevelJavaDoc.instantiation.SearchAsyncClient
        return searchAsyncClient;
    }

    /**
     * Instantiate a synchronous client using DefaultAzureCredential.
     */
    public SearchClient createSearchClientWithDefaultAzureCredential() {
        // BEGIN: com.azure.search.documents.SearchClientBuilder-classLevelJavaDoc.credential
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        SearchClient searchClient = new SearchClientBuilder()
            .credential(credential)
            .endpoint("{endpoint}")
            .indexName("{indexName}")
            .buildClient();
        // END: com.azure.search.documents.SearchClientBuilder-classLevelJavaDoc.credential
        return searchClient;
    }

    /**
     * Instantiate an asynchronous client using DefaultAzureCredential.
     */
    public SearchAsyncClient createSearchAsyncClientWithDefaultAzureCredential() {
        // BEGIN: com.azure.search.documents.SearchClientBuilder-classLevelJavaDoc.async.credential
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        SearchAsyncClient searchAsyncClient = new SearchClientBuilder()
            .credential(credential)
            .endpoint("{endpoint}")
            .indexName("{indexName}")
            .buildAsyncClient();
        // END: com.azure.search.documents.SearchClientBuilder-classLevelJavaDoc.async.credential
        return searchAsyncClient;
    }


}
