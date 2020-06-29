// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;

public class SearchIndexClientJavaDocCodeSnippets {
    private SearchIndexClient searchIndexClient = new SearchIndexClientBuilder().buildClient();
    /**
     * Code snippet for creating a {@link SearchIndexClient}.
     */
    public void createSearchIndexClientFromBuilder() {
        // BEGIN: com.azure.search.documents.SearchIndexClient.instantiation
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.search.documents.SearchIndexClient.instantiation
    }
}
