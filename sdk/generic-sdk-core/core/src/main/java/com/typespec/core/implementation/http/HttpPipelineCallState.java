// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http;

import com.typespec.core.http.models.HttpRequest;
import com.typespec.core.http.pipeline.HttpPipeline;
import com.typespec.core.http.pipeline.HttpPipelinePolicy;
import com.typespec.core.util.ClientLogger;

/**
 * Represents a class responsible for maintaining information related to request-specific context and pipeline data.
 */
public class HttpPipelineCallState {
    private static final ClientLogger LOGGER = new ClientLogger(HttpPipelineCallState.class);
    private final HttpPipeline pipeline;
    private final HttpRequest request;
    private int currentPolicyIndex;

    /**
     * Constructor to create HttpPipelineCallState.
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @param request The request.
     */
    public HttpPipelineCallState(HttpPipeline pipeline, HttpRequest request) {
        this.pipeline = pipeline;
        this.request = request;
        this.currentPolicyIndex = -1;
    }

    /**
     * Retrieves the next policy on the pipeline.
     *
     * @return A {@link HttpPipelinePolicy} next in queue in the {@link HttpPipeline}.
     * @throws IllegalStateException when there are no more policies to execute.
     */
    public HttpPipelinePolicy getNextPolicy() {
        final int size = this.pipeline.getPolicyCount();

        this.currentPolicyIndex++;

        if (this.currentPolicyIndex > size) {
            throw LOGGER.logThrowableAsError(new IllegalStateException("There is no more policies to execute."));
        } else if (this.currentPolicyIndex == size) {
            return null;
        } else {
            return this.pipeline.getPolicy(this.currentPolicyIndex);
        }
    }

    /**
     * Returns the current {@link HttpPipeline}.
     *
     * @return the current {@link HttpPipeline}.
     */
    public HttpPipeline getPipeline() {
        return this.pipeline;
    }

    /**
     * Retrieves the current policy in the pipeline.
     *
     * @return The current {@link HttpPipelinePolicy} in queue in the {@link HttpPipeline}.
     */
    public HttpPipelinePolicy getCurrentPolicy() {
        return this.pipeline.getPolicy(this.currentPolicyIndex);
    }

    /**
     * Returns the current request specific contextual data.
     *
     * @return the current request specific contextual data.
     */
    public HttpRequest getRequest() {
        return this.request;
    }

    @Override
    public HttpPipelineCallState clone() {
        HttpPipelineCallState cloned = new HttpPipelineCallState(this.pipeline, this.request);
        cloned.currentPolicyIndex = this.currentPolicyIndex;
        return cloned;
    }
}
