// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;

public class SearchIndexAsyncClientJavaDocCodeSnippets {
    private SearchIndexAsyncClient searchIndexAsyncClient= new SearchIndexClientBuilder().buildAsyncClient();

    /**
     * Code snippet for creating a {@link SearchIndexAsyncClient}.
     */
    public void createSearchIndexAsyncClientFromBuilder() {
        // BEGIN: com.azure.search.documents.SearchIndexAsyncClient.instantiation
        SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.search.documents.SearchIndexAsyncClient.instantiation
    }
}
