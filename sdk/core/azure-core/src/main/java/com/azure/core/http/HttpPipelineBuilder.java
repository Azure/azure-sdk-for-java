// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.http.policy.InstrumentationPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;

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
 * <!-- src_embed com.azure.core.http.HttpPipelineBuilder.noConfiguration -->
 * <pre>
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.HttpPipelineBuilder.noConfiguration -->
 *
 * <p>Create a pipeline using the default HTTP client and a retry policy</p>
 *
 * <!-- src_embed com.azure.core.http.HttpPipelineBuilder.defaultHttpClientWithRetryPolicy -->
 * <pre>
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .httpClient&#40;HttpClient.createDefault&#40;&#41;&#41;
 *     .policies&#40;new RetryPolicy&#40;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.HttpPipelineBuilder.defaultHttpClientWithRetryPolicy -->
 *
 * @see HttpPipeline
 */
public class HttpPipelineBuilder {
    private HttpClient httpClient;
    private List<HttpPipelinePolicy> pipelinePolicies;
    private ClientOptions clientOptions;
    private Tracer tracer;

    /**
     * Creates a new instance of HttpPipelineBuilder that can configure options for the {@link HttpPipeline} before
     * creating an instance of it.
     */
    public HttpPipelineBuilder() {
    }

    /**
     * Creates an {@link HttpPipeline} based on options set in the builder. Every time {@code build()} is called, a new
     * instance of {@link HttpPipeline} is created.
     * <p>
     * If HttpClient is not set then a default HttpClient is used.
     *
     * @return A HttpPipeline with the options set from the builder.
     */
    public HttpPipeline build() {
        List<HttpPipelinePolicy> policies = (pipelinePolicies == null) ? new ArrayList<>() : pipelinePolicies;

        HttpClient client;
        if (httpClient != null) {
            client = httpClient;
        } else if (clientOptions instanceof HttpClientOptions) {
            client = HttpClient.createDefault((HttpClientOptions) clientOptions);
        } else {
            client = HttpClient.createDefault();
        }

        configureTracing(policies);

        return new HttpPipeline(client, policies, tracer);
    }

    private void configureTracing(List<HttpPipelinePolicy> policies) {
        if (tracer == null) {
            TracingOptions tracingOptions = clientOptions == null ? null : clientOptions.getTracingOptions();
            tracer = TracerProvider.getDefaultProvider().createTracer("azure-core", null, null, tracingOptions);
        }

        for (HttpPipelinePolicy policy : policies) {
            if (policy instanceof InstrumentationPolicy) {
                ((InstrumentationPolicy) policy).initialize(tracer);
            }
        }
    }

    /**
     * Sets the HttpClient that the pipeline will use to send requests.
     *
     * @param httpClient The HttpClient the pipeline will use when sending requests.
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
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder policies(HttpPipelinePolicy... policies) {
        if (pipelinePolicies == null) {
            pipelinePolicies = new ArrayList<>();
        }

        this.pipelinePolicies.addAll(Arrays.asList(policies));
        return this;
    }

    /**
     * Sets the ClientOptions that will configure the pipeline.
     *
     * @param clientOptions The ClientOptions that will configure the pipeline.
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Sets the Tracer to trace logical and HTTP calls.
     *
     * @param tracer The Tracer instance.
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder tracer(Tracer tracer) {
        this.tracer = tracer;
        return this;
    }
}
