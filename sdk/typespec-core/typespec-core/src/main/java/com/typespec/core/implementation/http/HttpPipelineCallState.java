// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http;

import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpPipelineCallContext;
import com.typespec.core.http.policy.HttpPipelinePolicy;
import com.typespec.core.util.logging.ClientLogger;

/**
 * Represents a class responsible for maintaining information related to request-specific context and pipeline data.
 */
public class HttpPipelineCallState {
    private static final ClientLogger LOGGER = new ClientLogger(HttpPipelineCallState.class);
    private final HttpPipeline pipeline;
    private final HttpPipelineCallContext callContext;
    private int currentPolicyIndex;

    /**
     * Constructor to create HttpPipelineCallState.
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @param callContext The request context.
     */
    public HttpPipelineCallState(HttpPipeline pipeline, HttpPipelineCallContext callContext) {
        this.pipeline = pipeline;
        this.callContext = callContext;
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
            throw LOGGER.logExceptionAsError(new IllegalStateException("There is no more policies to execute."));
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
    public HttpPipelineCallContext getCallContext() {
        return this.callContext;
    }

    @Override
    public HttpPipelineCallState clone() {
        HttpPipelineCallState cloned = new HttpPipelineCallState(this.pipeline, this.callContext);
        cloned.currentPolicyIndex = this.currentPolicyIndex;
        return cloned;
    }
}
