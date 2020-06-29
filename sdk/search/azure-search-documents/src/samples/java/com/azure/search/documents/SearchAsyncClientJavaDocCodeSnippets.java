// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;

public class SearchAsyncClientJavaDocCodeSnippets {
    private SearchAsyncClient searchAsyncClient= new SearchClientBuilder().buildAsyncClient();

    /**
     * Code snippet for creating a {@link SearchAsyncClient}.
     */
    public void createSearchAsyncClientFromBuilder() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.instantiation
        SearchAsyncClient searchAsyncClient = new SearchClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.search.documents.SearchAsyncClient.instantiation
    }
}
