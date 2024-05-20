// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

/**
 * The {@code HttpPipelineSyncPolicy} class is an implementation of the {@link HttpPipelinePolicy} interface. This
 * policy represents a synchronous operation within the HTTP pipeline, meaning it doesn't perform any asynchronous or
 * synchronously blocking operations.
 *
 * <p>This class is useful when you need to perform operations in the HTTP pipeline that don't require
 * asynchronous processing or blocking. It provides hooks to perform actions before the request is sent and after the
 * response is received.</p>
 *
 * @see com.azure.core.http.policy.HttpPipelinePolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
 * @see com.azure.core.http.HttpPipelineCallContext
 */
public class HttpPipelineSyncPolicy implements HttpPipelinePolicy {
    /**
     * Creates a new instance of {@link HttpPipelineSyncPolicy}.
     */
    public HttpPipelineSyncPolicy() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return Mono.fromCallable(() -> {
            beforeSendingRequest(context);
            return next;
        }).flatMap(ignored -> next.process()).map(response -> afterReceivedResponse(context, response));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        beforeSendingRequest(context);
        HttpResponse response = next.processSync();
        return afterReceivedResponse(context, response);
    }

    /**
     * Method is invoked before the request is sent.
     *
     * @param context The request context.
     */
    protected void beforeSendingRequest(HttpPipelineCallContext context) {
        // empty by default
    }

    /**
     * Method is invoked after the response is received.
     *
     * @param context The request context.
     * @param response The response received.
     * @return The transformed response.
     */
    protected HttpResponse afterReceivedResponse(HttpPipelineCallContext context, HttpResponse response) {
        // empty by default
        return response;
    }
}
