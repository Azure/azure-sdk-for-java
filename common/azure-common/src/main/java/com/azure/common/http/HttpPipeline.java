// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.http;

import com.azure.common.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The http pipeline.
 */
public final class HttpPipeline {
    private final HttpClient httpClient;
    private final HttpPipelinePolicy[] pipelinePolicies;

    /**
     * Creates a HttpPipeline holding array of policies that gets applied to all request initiated through
     * {@link HttpPipeline#send(HttpPipelineCallContext)} and it's response.
     *
     * @param httpClient the http client to write request to wire and receive response from wire.
     * @param pipelinePolicies pipeline policies in the order they need to applied, a copy of this array will
     *                                  be made hence changing the original array after the creation of pipeline
     *                                  will not  mutate the pipeline
     */
    public HttpPipeline(HttpClient httpClient, HttpPipelinePolicy... pipelinePolicies) {
        Objects.requireNonNull(httpClient);
        Objects.requireNonNull(pipelinePolicies);
        this.pipelinePolicies = Arrays.copyOf(pipelinePolicies, pipelinePolicies.length);
        this.httpClient = httpClient;
    }

    /**
     * Creates a HttpPipeline holding array of policies that gets applied all request initiated through
     * {@link HttpPipeline#send(HttpPipelineCallContext)} and it's response.
     *
     * The default HttpClient {@link HttpClient#createDefault()} will be used to write request to wire and
     * receive response from wire.
     *
     * @param pipelinePolicies pipeline policies in the order they need to applied, a copy of this array will
     *                                  be made hence changing the original array after the creation of pipeline
     *                                  will not  mutate the pipeline
     */
    public HttpPipeline(HttpPipelinePolicy... pipelinePolicies) {
        this(HttpClient.createDefault(), pipelinePolicies);
    }

    /**
     * Creates a HttpPipeline holding array of policies that gets applied to all request initiated through
     * {@link HttpPipeline#send(HttpPipelineCallContext)} and it's response.
     *
     * @param httpClient the http client to write request to wire and receive response from wire.
     * @param pipelinePolicies pipeline policies in the order they need to applied, a copy of this list
     *                         will be made so changing the original list after the creation of pipeline
     *                         will not mutate the pipeline
     */
    public HttpPipeline(HttpClient httpClient, List<HttpPipelinePolicy> pipelinePolicies) {
        Objects.requireNonNull(httpClient);
        Objects.requireNonNull(pipelinePolicies);
        this.pipelinePolicies = pipelinePolicies.toArray(new HttpPipelinePolicy[0]);
        this.httpClient = httpClient;
    }

    /**
     * Creates a HttpPipeline holding array of policies that gets applied all request initiated through
     * {@link HttpPipeline#send(HttpPipelineCallContext)} and it's response.
     *
     * The default HttpClient {@link HttpClient#createDefault()} will be used to write request to wire and
     * receive response from wire.
     *
     * @param pipelinePolicies pipeline policies in the order they need to applied, a copy of this list
     *                         will be made so changing the original list after the creation of pipeline
     *                         will not mutate the pipeline
     */
    public HttpPipeline(List<HttpPipelinePolicy> pipelinePolicies) {
        this(HttpClient.createDefault(), pipelinePolicies);
    }

    /**
     * Get the policies in the pipeline.
     *
     * @return policies in the pipeline
     */
    public HttpPipelinePolicy[] pipelinePolicies() {
        return Arrays.copyOf(this.pipelinePolicies, this.pipelinePolicies.length);
    }

    /**
     * Get the {@link HttpClient} associated with the pipeline.
     *
     * @return the {@link HttpClient} associated with the pipeline
     */
    public HttpClient httpClient() {
        return this.httpClient;
    }

    /**
     * Creates a new context local to the provided http request.
     *
     * @param httpRequest the request for a context needs to be created
     * @return the request context
     */
    public HttpPipelineCallContext newContext(HttpRequest httpRequest) {
        return new HttpPipelineCallContext(httpRequest);
    }

    /**
     * Creates a new context local to the provided http request.
     *
     * @param httpRequest the request for a context needs to be created
     * @param data the data to associate with the context
     * @return the request context
     */
    public HttpPipelineCallContext newContext(HttpRequest httpRequest, ContextData data) {
        return new HttpPipelineCallContext(httpRequest, data);
    }

    /**
     * Wraps the request in a context and send it through pipeline.
     *
     * @param request the request
     * @return a publisher upon subscription flows the context through policies, sends the request and emits response upon completion
     */
    public Mono<HttpResponse> send(HttpRequest request) {
        return this.send(this.newContext(request));
    }

    /**
     * Sends the context (containing request) through pipeline.
     *
     * @param context the request context
     * @return a publisher upon subscription flows the context through policies, sends the request and emits response upon completion
     */
    public Mono<HttpResponse> send(HttpPipelineCallContext context) {
        // Return deferred to mono for complete lazy behaviour.
        //
        return Mono.defer(() -> {
            HttpPipelineNextPolicy next = new HttpPipelineNextPolicy(this, context);
            return next.process();
        });
    }
}
