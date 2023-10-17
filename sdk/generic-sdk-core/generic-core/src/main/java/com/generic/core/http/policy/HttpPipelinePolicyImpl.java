// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy;

import com.generic.core.http.HttpPipelineNextPolicy;
import com.generic.core.http.models.HttpPipelineCallContext;
import com.generic.core.http.models.HttpResponse;

/**
 * Represents a {@link HttpPipelinePolicy} that doesn't do any asynchronous or synchronously blocking operations.
 */
public class HttpPipelinePolicyImpl implements HttpPipelinePolicy {
    /**
     * Creates a new instance of {@link HttpPipelinePolicyImpl}.
     */
    public HttpPipelinePolicyImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final HttpResponse process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        beforeSendingRequest(context);
        HttpResponse response = next.process();
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
