// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

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
    HttpPipeline(HttpClient httpClient, List<HttpPipelinePolicy> pipelinePolicies) {
        Objects.requireNonNull(httpClient);
        Objects.requireNonNull(pipelinePolicies);
        this.httpClient = httpClient;
        this.pipelinePolicies = pipelinePolicies.toArray(new HttpPipelinePolicy[0]);
    }

    /**
     * Get the policy at the passed index in the pipeline.
     * @param index index of the the policy to retrieve.
     * @return the policy stored at that index.
     */
    public HttpPipelinePolicy getPolicy(final int index) {
        return this.pipelinePolicies[index];
    }

    /**
     * Get the count of policies in the pipeline.
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
    public HttpClient httpClient() {
        return this.httpClient;
    }

    /**
     * Wraps the request in a context and send it through pipeline.
     *
     * @param request the request
     * @return a publisher upon subscription flows the context through policies, sends the request and emits response upon completion
     */
    public Mono<HttpResponse> send(HttpRequest request) {
        return this.send(new HttpPipelineCallContext(request));
    }

    /**
     * Wraps the request in a context with additional metadata and sends it through the pipeline.
     * @param request the request
     * @param data additional metadata to pass along in the request
     * @return a publisher upon subscription flows the context through policies, sends the request and emits response upon completion
     */
    public Mono<HttpResponse> send(HttpRequest request, Context data) {
        return this.send(new HttpPipelineCallContext(request, data));
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
