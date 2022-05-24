// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

/**
 * A type that invokes next policy in the pipeline.
 */
public class HttpPipelineNextPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(HttpPipelineNextPolicy.class);
    private final HttpPipeline pipeline;
    private final HttpPipelineCallContext context;
    private int currentPolicyIndex;
    private final boolean originatedFromSyncContext;

    /**
     * Package Private ctr.
     * <p>
     * Creates HttpPipelineNextPolicy.
     *
     * @param pipeline the pipeline
     * @param context the request-response context
     */
    HttpPipelineNextPolicy(final HttpPipeline pipeline, HttpPipelineCallContext context) {
        this.pipeline = pipeline;
        this.context = context;
        this.currentPolicyIndex = -1;
        originatedFromSyncContext = false;
    }

    /**
     * Package Private ctr.
     * <p>
     * Creates HttpPipelineNextPolicy.
     *
     * @param pipeline the pipeline
     * @param context the request-response context
     * @param originatedFromSyncContext boolean to indicate if the pipeline originated from sync pipeline
     */
    HttpPipelineNextPolicy(final HttpPipeline pipeline, HttpPipelineCallContext context,
                           boolean originatedFromSyncContext) {
        this.pipeline = pipeline;
        this.context = context;
        this.currentPolicyIndex = -1;
        this.originatedFromSyncContext = originatedFromSyncContext;
    }

    /**
     * Invokes the next {@link HttpPipelinePolicy}.
     *
     * @return A publisher which upon subscription invokes next policy and emits response from the policy.
     */
    public Mono<HttpResponse> process() {
        if (originatedFromSyncContext) {
            return Mono.fromCallable(() ->
                this.pipeline.getPolicy(this.currentPolicyIndex).processSync(this.context, this));

        } else {
            final int size = this.pipeline.getPolicyCount();
            if (this.currentPolicyIndex > size) {
                return Mono.error(new IllegalStateException("There is no more policies to execute."));
            }

            this.currentPolicyIndex++;
            if (this.currentPolicyIndex == size) {
                return
                    this.pipeline.getHttpClient().send(this.context.getHttpRequest(), this.context.getContext());
            } else {
                return this.pipeline.getPolicy(this.currentPolicyIndex).process(this.context, this);
            }
        }
    }

    /**
     * Invokes the next {@link HttpPipelinePolicy} synchronously.
     *
     * @return A publisher which upon subscription invokes next policy and emits response from the policy.
     */
    public HttpResponse processSync() {
        final int size = this.pipeline.getPolicyCount();
        this.currentPolicyIndex++;
        if (this.currentPolicyIndex == size) {
            return this.pipeline.getHttpClient().sendSync(this.context.getHttpRequest(), this.context.getContext());
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
        HttpPipelineNextPolicy cloned = new HttpPipelineNextPolicy(this.pipeline, this.context);
        cloned.currentPolicyIndex = this.currentPolicyIndex;
        return cloned;
    }
}
