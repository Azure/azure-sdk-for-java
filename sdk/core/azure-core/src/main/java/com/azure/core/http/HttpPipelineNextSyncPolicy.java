// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.http.HttpPipelineCallState;
import com.azure.core.implementation.http.HttpPipelineNextSyncPolicyHelper;
import com.azure.core.util.Context;

/**
 * <p>A class that invokes the next policy in the HTTP pipeline in a synchronous manner.</p>
 *
 * <p>This class encapsulates the state of the HTTP pipeline call and provides a method to process the next policy in
 * the pipeline synchronously.</p>
 *
 * <p>It provides methods to process the next policy and clone the current instance of the next pipeline policy.</p>
 *
 * <p>This class is useful when you want to send an HTTP request through the HTTP pipeline and need to process the
 * next policy in the pipeline in a synchronous manner.</p>
 *
 * @see HttpPipelinePolicy
 * @see HttpPipelineCallState
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
            return this.state.getPipeline()
                .getHttpClient()
                .sendSync(this.state.getCallContext().getHttpRequest(), this.state.getCallContext().getContext());
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
     * Creates a new instance at the same pipeline position with an independent call context.
     * The supplied request and context values are not deep-copied. Callers running chains concurrently should
     * supply independent requests and ensure that shared context values support concurrent access.
     *
     * @param request The request for the cloned chain.
     * @param context The context data for the cloned chain.
     * @return A new instance with its own call context.
     * @throws NullPointerException If {@code request} or {@code context} is null.
     */
    public HttpPipelineNextSyncPolicy clone(HttpRequest request, Context context) {
        return new HttpPipelineNextSyncPolicy(this.state.clone(new HttpPipelineCallContext(request, context)));
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
