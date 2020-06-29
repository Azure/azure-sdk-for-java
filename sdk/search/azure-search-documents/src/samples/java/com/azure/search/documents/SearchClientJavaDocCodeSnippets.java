// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;

public class SearchClientJavaDocCodeSnippets {
    private SearchClient searchClient = new SearchClientBuilder().buildClient();

    /**
     * Code snippet for creating a {@link SearchClient}.
     */
    public void createSearchClientFromBuilder() {
        // BEGIN: com.azure.search.documents.SearchClient.instantiation
        SearchClient searchClient = new SearchClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .indexName("{indexName}")
            .buildClient();
        // END: com.azure.search.documents.SearchClient.instantiation
    }
}
