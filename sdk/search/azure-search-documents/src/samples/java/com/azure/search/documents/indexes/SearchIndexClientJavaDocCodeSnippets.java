// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;

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

    /**
     * Code snippet for creating a {@link SearchIndexClient} with pipeline.
     */
    public void createSearchIndexClientWithPipeline() {
        // BEGIN: com.azure.search.documents.SearchIndexClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .pipeline(pipeline)
            .buildClient();
        // END: com.azure.search.documents.SearchIndexClient.pipeline.instantiation
    }
}
