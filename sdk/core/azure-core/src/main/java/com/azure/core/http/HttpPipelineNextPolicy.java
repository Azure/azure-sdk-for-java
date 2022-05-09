// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.http.HttpPipelineCallState;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * A type that invokes next policy in the pipeline.
 */
public class HttpPipelineNextPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(HttpPipelineNextPolicy.class);
    private final HttpPipelineCallState state;
    private final boolean originatedFromSyncContext;

    /**
     * Package Private ctr.
     *
     * Creates HttpPipelineNextPolicy.
     *
     * @param state the pipeline call state.
     */
    HttpPipelineNextPolicy(HttpPipelineCallState state) {
        this.state = state;
        this.originatedFromSyncContext = false;
    }

    HttpPipelineNextPolicy(HttpPipelineCallState state, boolean originatedFromSyncContext) {
        this.state = state;
        this.originatedFromSyncContext = originatedFromSyncContext;
    }

    /**
     * Invokes the next {@link HttpPipelinePolicy}.
     *
     * @return A publisher which upon subscription invokes next policy and emits response from the policy.
     */
    public Mono<HttpResponse> process() {
        if (originatedFromSyncContext && !Schedulers.isInNonBlockingThread()) {

            // TODO (kasobol-msft) other options
            // - give up and sync over async from here
            // - move this to Schedulers.boundedElastic assuming that it's unlikely we'd be bouncing
            //   i.e. small amount of policies are not implementing sync
            // - do our best until we hit non blocking thread.

            // Pipeline executes in synchronous style. We most likely got here via default implementation in the
            // HttpPipelinePolicy.processSynchronously so go back to sync style here.
            // Don't do this on non-blocking threads.
            return Mono.fromCallable(() -> new HttpPipelineNextSyncPolicy(state).processSync());
        } else {
            if (originatedFromSyncContext) {
                LOGGER.warning("The pipeline switched from synchronous to asynchronous."
                    + " Check if all policies override HttpPipelinePolicy.processSync");
            }

            HttpPipelinePolicy nextPolicy = state.getNextPolicy();
            if (nextPolicy == null) {
                return this.state.getPipeline().getHttpClient().send(
                    this.state.getCallContext().getHttpRequest(), this.state.getCallContext().getContext());
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
        return new HttpPipelineNextPolicy(this.state.clone(), this.originatedFromSyncContext);
    }
}
