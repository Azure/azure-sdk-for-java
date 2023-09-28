// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.http.policy;

import com.client.core.http.HttpHeaders;
import com.client.core.http.HttpPipelineCallContext;
import com.client.core.http.HttpPipelineNextPolicy;
import com.client.core.http.HttpPipelineNextSyncPolicy;
import com.client.core.http.HttpResponse;
import reactor.core.publisher.Mono;

/**
 * The pipeline policy that adds a particular set of headers to HTTP requests.
 */
public class AddHeadersPolicy implements HttpPipelinePolicy {
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
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        setHeaders(context.getHttpRequest().getHeaders(), headers);

        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        setHeaders(context.getHttpRequest().getHeaders(), headers);

        return next.processSync();
    }

    private static void setHeaders(HttpHeaders requestHeaders, HttpHeaders policyHeaders) {
        requestHeaders.setAllHttpHeaders(policyHeaders);
    }
}
