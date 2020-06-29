// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;

public class SearchClientJavaDocCodeSnippets {
    private SearchClient searchClient= new SearchClientBuilder().buildClient();

    /**
     * Code snippet for creating a {@link SearchClient}.
     */
    public void createSearchClientFromBuilder() {
        // BEGIN: com.azure.search.documents.SearchClient.instantiation
        SearchClient searchClient = new SearchClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.search.documents.SearchClient.instantiation
    }

    /**
     * Code snippet for creating a {@link SearchClient} with pipeline.
     */
    public void createSearchClientWithPipeline() {
        // BEGIN: com.azure.search.documents.SearchClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        SearchClient searchClient = new SearchClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .pipeline(pipeline)
            .buildClient();
        // END: com.azure.search.documents.SearchClient.pipeline.instantiation
    }
}
