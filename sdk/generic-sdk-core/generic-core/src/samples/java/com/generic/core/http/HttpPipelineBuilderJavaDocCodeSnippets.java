// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.pipeline.HttpPipeline;
import com.generic.core.http.pipeline.HttpPipelineBuilder;
import com.generic.core.http.policy.RetryPolicy;

/**
 * Codesnippets for {@link HttpPipelineBuilder}.
 */
public class HttpPipelineBuilderJavaDocCodeSnippets {
    public void noConfigurationBuild() {
        // BEGIN: com.generic.core.http.HttpPipelineBuilder.noConfiguration
        HttpPipeline pipeline = new HttpPipelineBuilder().build();
        // END: com.generic.core.http.HttpPipelineBuilder.noConfiguration
    }

    public void defaultHttpClientWithRetryPolicyBuild() {
        // BEGIN: com.generic.core.http.HttpPipelineBuilder.defaultHttpClientWithRetryPolicy
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(HttpClient.createDefault())
            .policies(new RetryPolicy())
            .build();
        // END: com.generic.core.http.HttpPipelineBuilder.defaultHttpClientWithRetryPolicy
    }
}
