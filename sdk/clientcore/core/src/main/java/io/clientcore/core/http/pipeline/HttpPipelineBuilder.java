// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.util.configuration.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link HttpPipeline},
 * calling {@link HttpPipelineBuilder#build() build} constructs an instance of the pipeline.
 *
 * <p>A pipeline is configured with a HttpClient that sends the request, if no client is set a default is used.
 * A pipeline may be configured with a list of policies that are applied to each request.</p>
 *
 * <p><strong>Code Samples</strong></p>
 *
 * <p>Create a pipeline without configuration</p>
 *
 * <!-- src_embed io.clientcore.core.http.HttpPipelineBuilder.noConfiguration -->
 * <pre>
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.http.HttpPipelineBuilder.noConfiguration -->
 *
 * <p>Create a pipeline using the default HTTP client and a retry policy</p>
 *
 * <!-- src_embed io.clientcore.core.http.HttpPipelineBuilder.defaultHttpClientWithRetryPolicy -->
 * <pre>
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .httpClient&#40;HttpClient.getNewInstance&#40;&#41;&#41;
 *     .policies&#40;new HttpRetryPolicy&#40;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.http.HttpPipelineBuilder.defaultHttpClientWithRetryPolicy -->
 *
 * @see HttpPipeline
 */
public class HttpPipelineBuilder {
    private HttpClient httpClient;
    private List<HttpPipelinePolicy> pipelinePolicies;

    /**
     * Creates a new instance of HttpPipelineBuilder that can configure options for the {@link HttpPipeline} before
     * creating an instance of it.
     */
    public HttpPipelineBuilder() {
    }

    /**
     * Creates an {@link HttpPipeline} based on options set in the builder. Every time {@code build()} is called, a new
     * instance of {@link HttpPipeline} is created.
     *
     * <p>If HttpClient is not set then a default HttpClient is used.
     *
     * @return A HttpPipeline with the options set from the builder.
     */
    public HttpPipeline build() {
        List<HttpPipelinePolicy> policies = (pipelinePolicies == null) ? new ArrayList<>() : pipelinePolicies;
        HttpClient client;

        if (httpClient != null) {
            client = httpClient;
        } else {
            if (Configuration.getGlobalConfiguration().get("ENABLE_HTTP_CLIENT_SHARING", Boolean.TRUE)) {
                client = HttpClient.getSharedInstance();
            } else {
                client = HttpClient.getNewInstance();
            }
        }

        return new HttpPipeline(client, policies);
    }

    /**
     * Sets the HttpClient that the pipeline will use to send requests.
     *
     * @param httpClient The HttpClient the pipeline will use when sending requests.
     *
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;

        return this;
    }

    /**
     * Adds {@link HttpPipelinePolicy policies} to the set of policies that the pipeline will use when sending
     * requests.
     *
     * @param policies Policies to add to the policy set.
     *
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
