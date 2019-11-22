// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The pipeline policy that adds request id header in {@link HttpRequest} once. These id does not change
 * when request is retried. Azure uses the request id as the unique identifier for the request.
 * Example of these headers are 'x-ms-client-request-id' and 'x-ms-correlation-request-id'.
 */
public class RequestIdPolicy implements HttpPipelinePolicy {
    private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";
    private final Supplier<HttpHeaders> requestIdSupplier;

    /**
     * Creates default {@link RequestIdPolicy}.
     */
    public RequestIdPolicy() {
        requestIdSupplier = () -> new HttpHeaders().put(REQUEST_ID_HEADER, UUID.randomUUID().toString());
    }

    /**
     * Creates {@link RequestIdPolicy} with provided {@link Supplier} to dynamically generate request id for each
     * {@link HttpRequest}.
     *
     * @param requestIdSupplier to dynamically generate to request id for each {@link HttpRequest}. It is suggested
     * that this {@link Supplier} provides unique value every time it is called.
     * Example of these headers are 'x-ms-client-request-id', 'x-ms-correlation-request-id'.
     *
     * @throws NullPointerException when {@code requestIdSupplier} is {@code null}.
     */
    public RequestIdPolicy(Supplier<HttpHeaders> requestIdSupplier) {
        this.requestIdSupplier = Objects.requireNonNull(requestIdSupplier, "'requestIdSupplier' must not be null");
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {

        HttpHeaders httpHeaders = requestIdSupplier.get();
        if (Objects.nonNull(httpHeaders) && httpHeaders.getSize() > 0) {
            for (HttpHeader header : httpHeaders) {
                String requestIdHeaderValue = context.getHttpRequest().getHeaders().getValue(header.getName());
                if (requestIdHeaderValue == null) {
                    context.getHttpRequest().getHeaders().put(header.getName(), header.getValue());
                }
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
