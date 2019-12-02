// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The pipeline policy that puts a UUID in the request header. Azure uses the request id as
 * the unique identifier for the request.
 */
public class RequestIdPolicy implements HttpPipelinePolicy {
    private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";
    private final Supplier<HttpHeader> requestIdSupplier;


    /**
     * Creates a RequestIdPolicy provided {@link Supplier}.
     *
     * @param headerSupplier The {@link Supplier} to provide header to add to outgoing requests.
     */
    public RequestIdPolicy(Supplier<HttpHeader> headerSupplier) {
        this.requestIdSupplier = Objects.requireNonNull(headerSupplier, "headerSupplier not be null.");
    }

    /**
     * Creates default {@link RequestIdPolicy}.
     */
    public RequestIdPolicy() {
        requestIdSupplier = () -> new HttpHeader(REQUEST_ID_HEADER, UUID.randomUUID().toString());
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpHeader httpHeader = requestIdSupplier.get();
        if (Objects.nonNull(httpHeader)) {
                String requestIdHeaderValue = context.getHttpRequest().getHeaders().getValue(httpHeader.getName());
                if (requestIdHeaderValue == null) {
                    context.getHttpRequest().getHeaders().put(httpHeader.getName(), httpHeader.getValue());
                }
            return next.process();
        }

        // If we were not able to set client provided Request ID header, we will set default 'REQUEST_ID_HEADER'.
        String requestId = context.getHttpRequest().getHeaders().getValue(REQUEST_ID_HEADER);
        if (requestId == null) {
            context.getHttpRequest().getHeaders().put(REQUEST_ID_HEADER, UUID.randomUUID().toString());
        }
        return next.process();
    }
}

