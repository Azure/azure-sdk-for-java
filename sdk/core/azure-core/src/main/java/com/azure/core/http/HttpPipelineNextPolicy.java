// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.http.HttpPipelineCallState;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * A type that invokes next policy in the pipeline.
 */
public class HttpPipelineNextPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(HttpPipelineNextPolicy.class);
    private final HttpPipelineCallState state;
    private final boolean originatedFromSyncPolicy;

    /**
     * Package Private ctr.
     *
     * Creates HttpPipelineNextPolicy.
     *
     * @param state the pipeline call state.
     */
    HttpPipelineNextPolicy(HttpPipelineCallState state) {
        this.state = state;
        this.originatedFromSyncPolicy = false;
    }

    /**
     * Package Private ctr.
     * Creates HttpPipelineNextPolicy.
     *
     * @param state the pipeline call state.
     * @param originatedFromSyncPolicy boolean to indicate if the next policy originated from sync call stack.
     */
    HttpPipelineNextPolicy(HttpPipelineCallState state, boolean originatedFromSyncPolicy) {
        this.state = state;
        this.originatedFromSyncPolicy = originatedFromSyncPolicy;
    }

    /**
     * Invokes the next {@link HttpPipelinePolicy}.
     *
     * @return A publisher which upon subscription invokes next policy and emits response from the policy.
     */
    public Mono<HttpResponse> process() {
        if (originatedFromSyncPolicy && !Schedulers.isInNonBlockingThread()) {
            // Pipeline executes in synchronous style. We most likely got here via default implementation in the
            // HttpPipelinePolicy.processSynchronously so go back to sync style here.
            // Don't do this on non-blocking threads.
            return Mono.fromCallable(() -> new HttpPipelineNextSyncPolicy(state).processSync());
        } else {
            if (originatedFromSyncPolicy) {
                LOGGER.log(LogLevel.WARNING,
                    () -> "The pipeline switched from synchronous to asynchronous. Check if "
                        + this.state.getCurrentPolicy().getClass().getSimpleName() + " does not override "
                        + "HttpPipelinePolicy.processSync");
            }

            HttpPipelinePolicy nextPolicy = state.getNextPolicy();
            if (nextPolicy == null) {
                return this.state.getPipeline()
                    .getHttpClient()
                    .send(this.state.getCallContext().getHttpRequest(), this.state.getCallContext().getContext());
            } else {
                return nextPolicy.process(this.state.getCallContext(), this);
            }
        }
    }

    /**
     * Creates a new instance of this instance.
     *
     * @return A new instance of this next pipeline policy.
     */
    @Override
    public HttpPipelineNextPolicy clone() {
        return new HttpPipelineNextPolicy(this.state.clone(), this.originatedFromSyncPolicy);
    }
}
