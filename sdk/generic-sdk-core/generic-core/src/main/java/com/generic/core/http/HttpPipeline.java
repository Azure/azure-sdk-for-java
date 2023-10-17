// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.models.HttpPipelineCallContext;
import com.generic.core.http.policy.HttpPipelinePolicy;
import com.generic.core.implementation.serializer.http.HttpPipelineCallState;
import com.generic.core.models.Context;

import java.util.List;
import java.util.Objects;

/**
 * The HTTP pipeline that HTTP requests and responses will flow through.
 * <p>
 * The HTTP pipeline may apply a set of {@link HttpPipelinePolicy HttpPipelinePolicies} to the request before it is
 * sent and on the response as it is being returned.
 *
 * @see HttpPipelinePolicy
 */
public final class HttpPipeline {
    private final HttpClient httpClient;
    private final HttpPipelinePolicy[] pipelinePolicies;

    /**
     * Creates a HttpPipeline holding array of policies that gets applied to all request initiated through {@link
     * HttpPipeline#send(HttpRequest, Context)} and it's response.
     *
     * @param httpClient       the http client to write request to wire and receive response from wire.
     * @param pipelinePolicies pipeline policies in the order they need to be applied, a copy of this array will be made
     *                         hence changing the original array after the creation of pipeline will not  mutate the pipeline
     */
    HttpPipeline(HttpClient httpClient, List<HttpPipelinePolicy> pipelinePolicies) {
        Objects.requireNonNull(httpClient, "'httpClient' cannot be null.");
        Objects.requireNonNull(pipelinePolicies, "'pipelinePolicies' cannot be null.");
        this.httpClient = httpClient;
        this.pipelinePolicies = pipelinePolicies.toArray(new HttpPipelinePolicy[0]);
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
     * Wraps the request in a context with additional metadata and sends it through the pipeline.
     *
     * @param request THe HTTP request to send.
     * @param data Additional metadata to pass along with the request.
     * @return A publisher upon subscription flows the context through policies, sends the request, and emits response
     * upon completion.
     */
    public HttpResponse send(HttpRequest request, Context data) {
        HttpPipelineNextPolicy next = new HttpPipelineNextPolicy(
            new HttpPipelineCallState(this, new HttpPipelineCallContext(request, data)));
        return next.process();
    }
}
