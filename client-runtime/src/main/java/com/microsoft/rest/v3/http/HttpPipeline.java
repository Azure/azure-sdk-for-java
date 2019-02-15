/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http;

import com.microsoft.rest.v3.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The http pipeline.
 */
public final class HttpPipeline {
    private final HttpClient httpClient;
    private final HttpPipelineOptions requestPolicyOptions;
    private final HttpPipelinePolicy[] pipelinePolicies;

    /**
     * Creates a HttpPipeline holding array of policies that gets applied to all request initiated through
     * {@link HttpPipeline#send(HttpPipelineCallContext)} and it's response.
     *
     * @param httpClient the http client to write request to wire and receive response from wire.
     * @param requestPolicyOptions optional properties that gets available in {@link HttpPipelineCallContext} for policies.
     * @param pipelinePolicies pipeline policies in the order they need to applied
     */
    public HttpPipeline(HttpClient httpClient, HttpPipelineOptions requestPolicyOptions, HttpPipelinePolicy... pipelinePolicies) {
        Objects.requireNonNull(pipelinePolicies);
        Objects.requireNonNull(httpClient);
        this.pipelinePolicies = pipelinePolicies;
        this.httpClient = httpClient;
        this.requestPolicyOptions = requestPolicyOptions;
    }

    /**
     * Creates a HttpPipeline holding array of policies that gets applied all request initiated through
     * {@link HttpPipeline#send(HttpPipelineCallContext)} and it's response.
     *
     * The default HttpClient {@link HttpClient#createDefault()} will be used to write request to wire and
     * receive response from wire.
     *
     * @param requestPolicyOptions optional properties that gets available in {@link HttpPipelineCallContext} for policies.
     * @param pipelinePolicies pipeline policies in the order they need to applied
     */
    public HttpPipeline(HttpPipelineOptions requestPolicyOptions, HttpPipelinePolicy... pipelinePolicies) {
        Objects.requireNonNull(pipelinePolicies);
        this.pipelinePolicies = pipelinePolicies;
        this.httpClient = HttpClient.createDefault();
        this.requestPolicyOptions = requestPolicyOptions;
    }

    /**
     * Creates a HttpPipeline holding array of policies that gets applied
     * to all request initiated through {@link HttpPipeline#send(HttpPipelineCallContext)}
     * and it's response.
     *
     * The default HttpClient {@link HttpClient#createDefault()} will be used to write request to wire and
     * receive response from wire.
     *
     * @param pipelinePolicies pipeline policies in the order they need to applied
     */
    public HttpPipeline(HttpPipelinePolicy... pipelinePolicies) {
        this(new HttpPipelineOptions(null), pipelinePolicies);
    }

    /**
     * Get the policies in the pipeline.
     *
     * @return policies in the pipeline
     */
    public HttpPipelinePolicy[] pipelinePolicies() {
        return this.pipelinePolicies;
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
        return new HttpPipelineCallContext(httpRequest, this.requestPolicyOptions);
    }

    /**
     * Creates a new context local to the provided http request.
     *
     * @param httpRequest the request for a context needs to be created
     * @param data the data to associate with the context
     * @return the request context
     */
    public HttpPipelineCallContext newContext(HttpRequest httpRequest, ContextData data) {
        return new HttpPipelineCallContext(httpRequest, data, this.requestPolicyOptions);
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
            NextPolicy next = new NextPolicy(this, context);
            return next.process();
        });
    }
}
