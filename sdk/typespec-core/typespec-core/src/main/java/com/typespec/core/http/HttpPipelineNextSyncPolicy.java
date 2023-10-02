// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http;

import com.typespec.core.http.policy.HttpPipelinePolicy;
import com.typespec.core.implementation.http.HttpPipelineCallState;
import com.typespec.core.implementation.http.HttpPipelineNextSyncPolicyHelper;

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
     * @return A new instance of this next pipeline sync policy.
     */
    @Override
    public HttpPipelineNextSyncPolicy clone() {
        return new HttpPipelineNextSyncPolicy(this.state.clone());
    }

    /**
     * Method to convert a {@link HttpPipelineNextSyncPolicy} to a {@link HttpPipelineNextPolicy} for supporting the
     * default implementation of
     * {@link HttpPipelinePolicy#processSync(HttpPipelineCallContext, HttpPipelineNextSyncPolicy)}.
     *
     * @return the converted {@link HttpPipelineNextSyncPolicy}.
     */
    HttpPipelineNextPolicy toAsyncPolicy() {
        return new HttpPipelineNextPolicy(this.state, true);
    }
}
