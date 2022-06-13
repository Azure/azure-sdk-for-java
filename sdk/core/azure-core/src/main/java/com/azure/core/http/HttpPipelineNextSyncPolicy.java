// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.http.HttpPipelineCallState;
import com.azure.core.implementation.http.HttpPipelineNextSyncPolicyHelper;

/**
 * A type that invokes next policy in the pipeline.
 */
public class HttpPipelineNextSyncPolicy {
    private final HttpPipelineCallState state;

    static {
        HttpPipelineNextSyncPolicyHelper.setAccessor(HttpPipelineNextSyncPolicy::toAsyncPolicy);
    }

    /**
     * Package Private ctr.
     *
     * Creates HttpPipelineNextPolicy.
     *
     * @param state the pipeline call state.
     */
    HttpPipelineNextSyncPolicy(HttpPipelineCallState state) {
        this.state = state;
    }

    /**
     * Invokes the next {@link HttpPipelinePolicy}.
     *
     * @return The response.
     */
    public HttpResponse processSync() {
        HttpPipelinePolicy nextPolicy = state.getNextPolicy();
        if (nextPolicy == null) {
            return this.state.getPipeline().getHttpClient().sendSync(
                this.state.getCallContext().getHttpRequest(), this.state.getCallContext().getContext());
        } else {
            return nextPolicy.processSync(this.state.getCallContext(), this);
        }
    }

    /**
     * Creates a new instance of this instance.
     *
     * @return A new instance of this next pipeline policy.
     */
    @Override
    public HttpPipelineNextSyncPolicy clone() {
        return new HttpPipelineNextSyncPolicy(this.state.clone());
    }

    HttpPipelineNextPolicy toAsyncPolicy() {
        return new HttpPipelineNextPolicy(this.state, true);
    }
}
