// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * The pipeline policy that limits the time allowed between sending a request and receiving the response.
 */
public class TimeoutPolicy implements HttpPipelinePolicy {
    private final Duration timeoutDuration;

    private final HttpPipelineSyncPolicy inner = new HttpPipelineSyncPolicy() {
    };

    /**
     * Creates a TimeoutPolicy.
     *
     * @param timeoutDuration the timeout duration
     */
    public TimeoutPolicy(Duration timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return inner.process(context, next).timeout(this.timeoutDuration);
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        // Should we use context to transmit the timeout defined here
        // And have individual http clients apply it on each request level.

        // Is this timeout different from
        // public Response<BlockBlobItem> uploadWithResponse(BlobParallelUploadOptions options, Duration timeout,
        //     Context context)
        // Should one override the other?
        return inner.processSync(context, next);
    }
}
