// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.http.HttpPipelineCallState;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * <p>The HTTP pipeline through which HTTP requests and responses flow.</p>
 *
 * <p>This class encapsulates the HTTP pipeline that applies a set of {@link HttpPipelinePolicy HttpPipelinePolicies}
 * to the request before it is sent and on the response as it is being returned.</p>
 *
 * <p>It provides methods to get the policy at a specific index in the pipeline, get the count of policies in the
 * pipeline, get the associated {@link HttpClient}, and send the HTTP request through the pipeline.</p>
 *
 * <p>This class is useful when you want to send an HTTP request and apply a set of policies to the request and
 * response.</p>
 *
 * @see HttpPipelinePolicy
 * @see HttpClient
 */
public final class HttpPipeline {
    private final HttpClient httpClient;
    private final HttpPipelinePolicy[] pipelinePolicies;

    private final Tracer tracer;

    /**
     * Creates a HttpPipeline holding array of policies that gets applied to all request initiated through {@link
     * HttpPipeline#send(HttpPipelineCallContext)} and it's response.
     *
     * @param httpClient the http client to write request to wire and receive response from wire.
     * @param pipelinePolicies pipeline policies in the order they need to be applied, a copy of this array will be made
     * hence changing the original array after the creation of pipeline will not  mutate the pipeline
     */
    HttpPipeline(HttpClient httpClient, List<HttpPipelinePolicy> pipelinePolicies, Tracer tracer) {
        Objects.requireNonNull(httpClient, "'httpClient' cannot be null.");
        Objects.requireNonNull(pipelinePolicies, "'pipelinePolicies' cannot be null.");
        this.httpClient = httpClient;
        this.pipelinePolicies = pipelinePolicies.toArray(new HttpPipelinePolicy[0]);
        this.tracer = tracer;
    }

    /**
     * Get the policy at the passed index in the pipeline.
     *
     * @param index index of the policy to retrieve.
     * @return the policy stored at that index.
     */
    public HttpPipelinePolicy getPolicy(final int index) {
        return this.pipelinePolicies[index];
    }

    /**
     * Get the count of policies in the pipeline.
     *
     * @return count of policies.
     */
    public int getPolicyCount() {
        return this.pipelinePolicies.length;
    }

    /**
     * Get the {@link HttpClient} associated with the pipeline.
     *
     * @return the {@link HttpClient} associated with the pipeline
     */
    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * Get the {@link Tracer} associated with the pipeline.
     *
     * @return the {@link Tracer} associated with the pipeline
     */
    public Tracer getTracer() {
        return tracer;
    }

    /**
     * Wraps the {@code request} in a context and sends it through pipeline.
     *
     * @param request The HTTP request to send.
     * @return A publisher upon subscription flows the context through policies, sends the request, and emits response
     * upon completion.
     */
    public Mono<HttpResponse> send(HttpRequest request) {
        return this.send(new HttpPipelineCallContext(request));
    }

    /**
     * Wraps the request in a context with additional metadata and sends it through the pipeline.
     *
     * @param request THe HTTP request to send.
     * @param data Additional metadata to pass along with the request.
     * @return A publisher upon subscription flows the context through policies, sends the request, and emits response
     * upon completion.
     */
    public Mono<HttpResponse> send(HttpRequest request, Context data) {
        return this.send(new HttpPipelineCallContext(request, data));
    }

    /**
     * Sends the context (containing an HTTP request) through pipeline.
     *
     * @param context The request context.
     * @return A publisher upon subscription flows the context through policies, sends the request and emits response
     * upon completion.
     */
    public Mono<HttpResponse> send(HttpPipelineCallContext context) {
        // Return deferred to mono for complete lazy behaviour.
        return Mono.defer(() -> {
            HttpPipelineNextPolicy next = new HttpPipelineNextPolicy(new HttpPipelineCallState(this, context));
            return next.process();
        });
    }

    /**
     * Wraps the request in a context with additional metadata and sends it through the pipeline.
     *
     * @param request THe HTTP request to send.
     * @param data Additional metadata to pass along with the request.
     * @return A publisher upon subscription flows the context through policies, sends the request, and emits response
     * upon completion.
     */
    public HttpResponse sendSync(HttpRequest request, Context data) {
        HttpPipelineNextSyncPolicy next = new HttpPipelineNextSyncPolicy(
            new HttpPipelineCallState(this, new HttpPipelineCallContext(request, data)));
        return next.processSync();
    }
}
