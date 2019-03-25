// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.applicationconfig;

import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Creates a policy which puts a UUID in the request header. Azure uses
 * the request id as the unique identifier for the request.
 * Also sets 'x-ms-return-client-request-id' header to tell server to return request id in the response.
 *
 * package-private class as users do not need to see or modify the request Id headers are added to requests.
 */
class RequestIdPolicy implements HttpPipelinePolicy {
    private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";
    private static final String ECHO_REQUEST_ID_HEADER = "x-ms-return-client-request-id";

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String requestId = context.httpRequest().headers().value(REQUEST_ID_HEADER);
        if (requestId == null) {
            context.httpRequest().headers().set(REQUEST_ID_HEADER, UUID.randomUUID().toString());
        }
        context.httpRequest().headers().set(ECHO_REQUEST_ID_HEADER, "true");
        return next.process();
    }
}
