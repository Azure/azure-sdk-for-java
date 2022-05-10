// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;

public class HttpPipelineCallState {
    private static final ClientLogger LOGGER = new ClientLogger(HttpPipelineCallState.class);
    private final HttpPipeline pipeline;
    private final HttpPipelineCallContext callContext;
    private int currentPolicyIndex;

    public HttpPipelineCallState(HttpPipeline pipeline, HttpPipelineCallContext callContext) {
        this.pipeline = pipeline;
        this.callContext = callContext;
        this.currentPolicyIndex = -1;
    }

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

    public HttpPipeline getPipeline() {
        return pipeline;
    }

    public HttpPipelineCallContext getCallContext() {
        return callContext;
    }

    @Override
    public HttpPipelineCallState clone() {
        HttpPipelineCallState cloned = new HttpPipelineCallState(this.pipeline, this.callContext);
        cloned.currentPolicyIndex = this.currentPolicyIndex;
        return cloned;
    }
}
