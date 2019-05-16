// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link HttpPipeline},
 * calling {@link HttpPipelineBuilder#build() build} constructs an instance of the pipeline.
 *
 * <pre>
 * HttpPipeline.builder()
 *     .httpClient(httpClient)
 *     .addPolicy(httpPipelinePolicy)
 *     .build();
 * </pre>
 *
 * <pre>
 * HttpPipeline.builder()
 *     .httpClient(httpClient)
 *     .setPolicies(httpPipelinePolicies)
 *     .build();
 * </pre>
 *
 * @see HttpPipeline
 */
public class HttpPipelineBuilder {
    private HttpClient httpClient;
    private List<HttpPipelinePolicy> pipelinePolicies = new ArrayList<>();


    HttpPipelineBuilder() {
    }

    /**
     * Creates a {@link HttpPipeline} based on options set in the Builder. Every time {@code build()} is
     * called, a new instance of {@link HttpPipeline} is created.
     *
     * If HttpClient is not set then the default HttpClient is used.
     *
     * @return A HttpPipeline with the options set from the builder.
     */
    public HttpPipeline build() {
        if (httpClient == null) {
            return new HttpPipeline(HttpClient.createDefault(), pipelinePolicies);
        } else {
            return new HttpPipeline(httpClient, pipelinePolicies);
        }
    }

    /**
     * Sets the HttpClient for the pipeline instance.
     *
     * @param httpClient The HttpClient the pipeline will use when sending requests.
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a {@link HttpPipelinePolicy} to the list of policies that the pipeline will use.
     *
     * @param policy Policy to add to the policy set.
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder addPolicy(HttpPipelinePolicy policy) {
        this.pipelinePolicies.add(policy);
        return this;
    }

    /**
     * Sets the policies that the pipeline will use.
     *
     * @param policies List of policies
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder setPolicies(List<HttpPipelinePolicy> policies) {
        this.pipelinePolicies = new ArrayList<>(policies);
        return this;
    }
}
