// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.HttpPipelineCallState;

/**
 * A type that invokes next policy in the pipeline.
 */
public class HttpPipelineNextPolicy {
    private final HttpPipelineCallState state;

    /**
     * Package-private constructor. Creates an HttpPipelineNextPolicy instance.
     *
     * @param state The pipeline call state.
     */
    HttpPipelineNextPolicy(HttpPipelineCallState state) {
        this.state = state;
    }

    /**
     * Invokes the next {@link HttpPipelinePolicy}.
     *
     * @return The response.
     */
    public Response<?> process() {
        HttpPipelinePolicy nextPolicy = state.getNextPolicy();

        if (nextPolicy == null) {
            return this.state.getPipeline().getHttpClient().send(this.state.getHttpRequest());
        } else {
            return nextPolicy.process(this.state.getHttpRequest(), this);
        }
    }

    /**
     * Creates a new instance of this instance.
     *
     * @return A new instance of this next pipeline policy.
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public HttpPipelineNextPolicy clone() {
        return new HttpPipelineNextPolicy(this.state.clone());
    }
}
