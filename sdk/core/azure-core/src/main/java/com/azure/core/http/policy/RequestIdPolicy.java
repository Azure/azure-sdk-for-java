// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpHeader;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

/**
 * The pipeline policy that puts a UUID in the request header. The default {@link HttpHeader} name can be overwritten.
 * Azure uses the request id as the unique identifier for the request.
 */
public class RequestIdPolicy implements HttpPipelinePolicy {

    private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";
    private final String requestIdHeaderName;

    /**
     * Creates  {@link RequestIdPolicy} with provided {@code requestIdHeaderName}.
     * @param requestIdHeaderName to be used to set in {@link HttpRequest}.
     */
    public RequestIdPolicy(String requestIdHeaderName) {
        this.requestIdHeaderName = Objects.requireNonNull(requestIdHeaderName,
            "messageIdHeaderName can not be null.");
    }

    /**
     * Creates default {@link RequestIdPolicy} with default header name 'x-ms-client-request-id'.
     */
    public RequestIdPolicy() {
        requestIdHeaderName = REQUEST_ID_HEADER;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String requestId = context.getHttpRequest().getHeaders().getValue(requestIdHeaderName);
        if (requestId == null) {
            context.getHttpRequest().getHeaders().put(requestIdHeaderName, UUID.randomUUID().toString());
        }
        return next.process();
    }
}

