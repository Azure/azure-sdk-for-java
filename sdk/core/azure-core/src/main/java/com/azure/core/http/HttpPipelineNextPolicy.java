// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * A type that invokes next policy in the pipeline.
 */
public class HttpPipelineNextPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(HttpPipelineNextPolicy.class);
    private final HttpPipeline pipeline;
    private final HttpPipelineCallContext context;
    private final boolean isSync;
    private int currentPolicyIndex;

    /**
     * Package Private ctr.
     *
     * Creates HttpPipelineNextPolicy.
     *
     * @param pipeline the pipeline
     * @param context the request-response context
     * @param isSync whether pipeline is invoked synchronously or not.
     */
    HttpPipelineNextPolicy(final HttpPipeline pipeline, HttpPipelineCallContext context, boolean isSync) {
        this.pipeline = pipeline;
        this.context = context;
        this.isSync = isSync;
        this.currentPolicyIndex = -1;
    }

    /**
     * Invokes the next {@link HttpPipelinePolicy}.
     *
     * @return A publisher which upon subscription invokes next policy and emits response from the policy.
     */
    public Mono<HttpResponse> process() {
        if (isSync && !Schedulers.isInNonBlockingThread()) {
            // Pipeline executes in synchronous style. We most likely got here via default implementation in the
            // HttpPipelinePolicy.processSynchronously so go back to sync style here.
            // Don't do this on non-blocking threads.
            return Mono.fromCallable(this::processSync);
        } else {
            if (isSync) {
                LOGGER.warning("The pipeline switched from synchronous to asynchronous."
                    + " Check if all policies override HttpPipelinePolicy.processSync");
            }
            final int size = this.pipeline.getPolicyCount();
            if (this.currentPolicyIndex > size) {
                return Mono.error(new IllegalStateException("There is no more policies to execute."));
            }

            this.currentPolicyIndex++;
            if (this.currentPolicyIndex == size) {
                return this.pipeline.getHttpClient().send(this.context.getHttpRequest(), this.context.getContext());
            } else {
                return this.pipeline.getPolicy(this.currentPolicyIndex).process(this.context, this);
            }
        }
    }

    /**
     * Invokes the next {@link HttpPipelinePolicy}.
     *
     * @return A publisher which upon subscription invokes next policy and emits response from the policy.
     */
    public HttpResponse processSync() {
        if (!isSync) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "Must not use HttpPipelineNextPolicy.processSync in asynchronous HttpPipeline invocation."));
        }
        final int size = this.pipeline.getPolicyCount();
        if (this.currentPolicyIndex > size) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("There is no more policies to execute."));
        }

        this.currentPolicyIndex++;
        if (this.currentPolicyIndex == size) {
            return this.pipeline.getHttpClient().sendSync(
                this.context.getHttpRequest(), this.context.getContext());
        } else {
            return this.pipeline.getPolicy(this.currentPolicyIndex).processSync(this.context, this);
        }
    }

    /**
     * Creates a new instance of this instance.
     *
     * @return A new instance of this next pipeline policy.
     */
    @Override
    public HttpPipelineNextPolicy clone() {
        HttpPipelineNextPolicy cloned = new HttpPipelineNextPolicy(this.pipeline, this.context, this.isSync);
        cloned.currentPolicyIndex = this.currentPolicyIndex;
        return cloned;
    }
}
