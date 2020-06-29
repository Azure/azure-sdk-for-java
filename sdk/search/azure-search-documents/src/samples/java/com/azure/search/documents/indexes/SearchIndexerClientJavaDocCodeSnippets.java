// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;

public class SearchIndexerClientJavaDocCodeSnippets {
    private SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder().buildClient();
    /**
     * Code snippet for creating a {@link SearchIndexerClient}.
     */
    public void createSearchIndexerClientFromBuilder() {
        // BEGIN: com.azure.search.documents.SearchIndexerClient.instantiation
        SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.search.documents.SearchIndexerClient.instantiation
    }

    /**
     * Code snippet for creating a {@link SearchIndexerClient} with pipeline.
     */
    public void createSearchIndexerClientWithPipeline() {
        // BEGIN: com.azure.search.documents.SearchIndexerClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        SearchIndexerClient searchIndexClient = new SearchIndexerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .pipeline(pipeline)
            .buildClient();
        // END: com.azure.search.documents.SearchIndexerClient.pipeline.instantiation
    }
}
