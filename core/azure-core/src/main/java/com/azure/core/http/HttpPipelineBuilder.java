// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link HttpPipeline},
 * calling {@link HttpPipelineBuilder#build() build} constructs an instance of the pipeline.
 *
 * <p>A pipeline uses a HttpClient to send requests, if no client is configured a default client will be used.
 * A pipeline may also contain a list of policies that are applied to each service request that is sent.</p>
 *
 * <p><strong>Code Samples</strong></p>
 *
 * <p>Create a pipeline without configuration</p>
 *
 * <pre>
 * HttpPipeline.builder()
 *     .build();
 * </pre>
 *
 * <p>Create a pipeline using the default HTTP client and a retry policy</p>
 *
 * <pre>
 * HttpPipeline.builder()
 *     .httpClient(HttpClient.createDefault())
 *     .policies(new RetryPolicy())
 *     .build();
 * </pre>
 *
 * @see HttpPipeline
 */
public class HttpPipelineBuilder {
    private HttpClient httpClient;
    private List<HttpPipelinePolicy> pipelinePolicies;


    HttpPipelineBuilder() {
    }

    /**
     * Creates a {@link HttpPipeline} based on options set in the Builder. Every time {@code build()} is
     * called, a new instance of {@link HttpPipeline} is created.
     *
     * If HttpClient is not set then the {@link HttpClient#createDefault() default HttpClient} is used.
     *
     * @return A HttpPipeline with the options set from the builder.
     */
    public HttpPipeline build() {
        List<HttpPipelinePolicy> policies = (pipelinePolicies == null) ? new ArrayList<>() : pipelinePolicies;
        HttpClient client = (httpClient == null) ? HttpClient.createDefault() : httpClient;

        return new HttpPipeline(client, policies);
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
     * @param policies Policies to add to the policy set.
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder policies(HttpPipelinePolicy... policies) {
        if (pipelinePolicies == null) {
            pipelinePolicies = new ArrayList<>();
        }

        this.pipelinePolicies.addAll(Arrays.asList(policies));
        return this;
    }
}
