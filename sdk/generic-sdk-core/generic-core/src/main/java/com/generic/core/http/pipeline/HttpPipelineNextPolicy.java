// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.pipeline;

import com.generic.core.http.models.HttpResponse;
import com.generic.core.implementation.http.HttpPipelineCallState;

/**
 * A type that invokes next policy in the pipeline.
 */
public class HttpPipelineNextPolicy {
    private final HttpPipelineCallState state;

    /**
     * Package Private ctr.
     * Creates HttpPipelineNextPolicy.
     *
     * @param state the pipeline call state.
     */
    HttpPipelineNextPolicy(HttpPipelineCallState state) {
        this.state = state;
    }

    /**
     * Invokes the next {@link HttpPipelinePolicy}.
     *
     * @return The response.
     */
    public HttpResponse process() {
        HttpPipelinePolicy nextPolicy = state.getNextPolicy();
        if (nextPolicy == null) {
            return this.state.getPipeline().getHttpClient().send(
                this.state.getCallContext().getHttpRequest(), this.state.getCallContext().getContext());
        } else {
            return nextPolicy.process(this.state.getCallContext(), this);
        }
    }

    /**
     * Creates a new instance of this instance.
     *
     * @return A new instance of this next pipeline policy.
     */
    @Override
    public HttpPipelineNextPolicy clone() {
        return new HttpPipelineNextPolicy(this.state.clone());
    }

}
