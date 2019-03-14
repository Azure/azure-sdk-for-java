/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.http;

import com.azure.common.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

/**
 * A type that invokes next policy in the pipeline.
 */
public class HttpPipelineNextPolicy {
    private final HttpPipeline pipeline;
    private final HttpPipelineCallContext context;
    private int currentPolicyIndex;

    /**
     * Package Private ctr.
     *
     * Creates HttpPipelineNextPolicy.
     *
     * @param pipeline the pipeline
     * @param context the request-response context
     */
    HttpPipelineNextPolicy(final HttpPipeline pipeline, HttpPipelineCallContext context) {
        this.pipeline = pipeline;
        this.context = context;
        this.currentPolicyIndex = -1;
    }

    /**
     * Invokes the next {@link HttpPipelinePolicy}.
     *
     * @return a publisher upon subscription invokes next policy and emits response from the policy.
     */
    public Mono<HttpResponse> process() {
        final int size = this.pipeline.pipelinePolicies().length;
        if (this.currentPolicyIndex > size) {
            return Mono.error(new IllegalStateException("There is no more policies to execute."));
        } else {
            this.currentPolicyIndex++;
            if (this.currentPolicyIndex == size) {
                return this.pipeline.httpClient().send(this.context.httpRequest());
            } else {
                return this.pipeline.pipelinePolicies()[this.currentPolicyIndex].process(this.context, this);
            }
        }
    }

    @Override
    public HttpPipelineNextPolicy clone() {
        HttpPipelineNextPolicy cloned = new HttpPipelineNextPolicy(this.pipeline, this.context);
        cloned.currentPolicyIndex = this.currentPolicyIndex;
        return cloned;
    }
}
