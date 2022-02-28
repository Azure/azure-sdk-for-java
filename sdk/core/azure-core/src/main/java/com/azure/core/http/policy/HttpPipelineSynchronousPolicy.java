// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

/**
 * Represents a {@link HttpPipelinePolicy} that doesn't do any asynchronous or synchronously blocking operations.
 */
public class HttpPipelineSynchronousPolicy implements HttpPipelinePolicy {

    /**
     * {@inheritDoc}
     */
    // TODO (kasobol-msft) ideally this should be marked final but then it breaks revapi.
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return Mono.defer(() -> {
            beforeSendingRequest(context);
            return next.process();
        }).map(response -> afterReceivedResponse(context, response));
    }

    /**
     * {@inheritDoc}
     */
    // TODO (kasobol-msft) ideally this should be marked final but then it breaks revapi.
    @Override
    public HttpResponse processSynchronously(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        beforeSendingRequest(context);
        HttpResponse response = next.processSynchronously();
        return afterReceivedResponse(context, response);
    }

    /**
     * Method is invoked before the request is sent.
     * @param context The request context.
     */
    protected void beforeSendingRequest(HttpPipelineCallContext context) {
        // empty by default
    }

    /**
     * Method is invoked after the response is received.
     * @param context The request context.
     * @param response The response received.
     * @return The transformed response.
     */
    protected HttpResponse afterReceivedResponse(HttpPipelineCallContext context, HttpResponse response) {
        // empty by default
        return response;
    }
}
