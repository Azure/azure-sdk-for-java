// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import io.clientcore.core.http.pipeline.HttpRetryPolicy;

/**
 * Codesnippets for {@link HttpPipelineBuilder}.
 */
public class HttpPipelineBuilderJavaDocCodeSnippets {
    public void noConfigurationBuild() {
        // BEGIN: com.azure.core.http.HttpPipelineBuilder.noConfiguration
        HttpPipeline pipeline = new HttpPipelineBuilder().build();
        // END: com.azure.core.http.HttpPipelineBuilder.noConfiguration
    }

    public void defaultHttpClientWithRetryPolicyBuild() {
        // BEGIN: com.azure.core.http.HttpPipelineBuilder.defaultHttpClientWithRetryPolicy
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(HttpClient.createDefault())
            .policies(new RetryPolicy())
            .build();
        // END: com.azure.core.http.HttpPipelineBuilder.defaultHttpClientWithRetryPolicy
    }
}
