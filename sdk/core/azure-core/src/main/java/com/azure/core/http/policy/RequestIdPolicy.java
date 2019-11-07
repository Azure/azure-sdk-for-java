// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * The pipeline policy that puts a UUID in the request header. Azure uses the request id as
 * the unique identifier for the request.
 */
public class RequestIdPolicy implements HttpPipelinePolicy {
    private static final String REQUEST_ID_HEADER = "client-request-id";
    private static final String LEGACY_REQUEST_ID_HEADER = "x-ms-client-request-id";

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String requestId = context.getHttpRequest().getHeaders().getValue(REQUEST_ID_HEADER);
        if (requestId == null) {
            String randomUUID = UUID.randomUUID().toString();
            context.getHttpRequest().getHeaders().put(REQUEST_ID_HEADER, randomUUID);

            //also set the legacy header for backwards compatibility.
            context.getHttpRequest().getHeaders().put(LEGACY_REQUEST_ID_HEADER, randomUUID);
        }
        return next.process();
    }
}
