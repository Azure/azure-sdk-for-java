// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The HTTP pipeline that HTTP requests and responses will flow through.
 * <p>
 * The HTTP pipeline may apply a set of {@link HttpPipelinePolicy HttpPipelinePolicies} to the request before it is sent
 * and on the response as it is being returned.
 *
 * @see HttpPipelinePolicy
 */
public final class HttpPipeline {
    private final HttpClient httpClient;
    private final List<HttpPipelinePolicy> pipelinePolicies;

    /**
     * Creates an {@link HttpPipeline} holding a list of policies that gets applied to all requests initiated through
     * {@link HttpPipeline#send(HttpRequest)} and its responses.
     *
     * @param httpClient The {@link HttpClient} to write requests to the wire and receive a responses from it.
     * @param pipelinePolicies {@link HttpPipelinePolicy Pipeline policies} in the order they need to be applied, a copy
     * of this list will be made hence changing the original after the creation of the pipeline will not mutate the
     * pipeline.
     */
    HttpPipeline(HttpClient httpClient, List<HttpPipelinePolicy> pipelinePolicies) {
        Objects.requireNonNull(httpClient, "'httpClient' cannot be null.");
        Objects.requireNonNull(pipelinePolicies, "'pipelinePolicies' cannot be null.");

        this.httpClient = httpClient;

        HttpPipelinePolicy[] clonedPolicies = new HttpPipelinePolicy[pipelinePolicies.size()];

        // Set each policy's next policy.
        for (int i = pipelinePolicies.size() - 1; i >= 0; i--) {
            clonedPolicies[i] = pipelinePolicies.get(i).clone();

            if (i < pipelinePolicies.size() - 1) {
                clonedPolicies[i].setNextPolicy(clonedPolicies[i + 1]);
            }
        }

        this.pipelinePolicies = Collections.unmodifiableList(Arrays.asList(clonedPolicies));
    }

    /**
     * Get an immutable list of this pipeline's policies.
     *
     * @return An immutable list of this pipeline's policies.
     */
    public List<HttpPipelinePolicy> getPolicies() {
        return this.pipelinePolicies;
    }

    /**
     * Get the {@link HttpClient} associated with the pipeline.
     *
     * @return The {@link HttpClient} associated with the pipeline.
     */
    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * Sends the request through the pipeline.
     *
     * @param request THe HTTP request to send.
     *
     * @return An {@link Response}.
     */
    public Response<?> send(HttpRequest request) {
        HttpPipelinePolicy basePolicy = new BasePolicy();

        if (!getPolicies().isEmpty()) {
            basePolicy.setNextPolicy(this.getPolicies().get(0));
        }

        return basePolicy.process(request, this);
    }

    private static class BasePolicy extends HttpPipelinePolicy {
        @Override
        public HttpPipelinePolicy clone() {
            return new BasePolicy();
        }
    }
}
