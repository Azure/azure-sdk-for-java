// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;

/**
 * The pipeline policy that adds a particular set of headers to HTTP requests.
 */
public class AddHeadersPolicy extends HttpPipelineSynchronousPolicy {
    private final HttpHeaders headers;

    /**
     * Creates a AddHeadersPolicy.
     *
     * @param headers The headers to add to outgoing requests.
     */
    public AddHeadersPolicy(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    protected void beforeSendingRequest(HttpPipelineCallContext context) {
        for (HttpHeader header : headers) {
            context.getHttpRequest().setHeader(header.getName(), header.getValue());
        }
    }
}
