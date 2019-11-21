// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

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
 * The pipeline policy that puts a UUID or user provided id in the request header. Azure uses the request id as
 * the unique identifier for the request.
 */
public class RequestIdPolicy implements HttpPipelinePolicy {
    private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";
    private final Supplier<HttpHeaders> requestIdSupplier;

    /**
     * Creates default {@link RequestIdPolicy}.
     */
    public RequestIdPolicy() {
        requestIdSupplier = null;
    }

    /**
     * Creates default {@link RequestIdPolicy} with provided {@link Supplier} to dynamically generate
     * request id for each {@link HttpRequest}.
     *
     * @param requestIdSupplier to dynamically generate to request id for each {@link HttpRequest}. {@code null} is
     * valid value. It is important to note that this {@link Supplier} should provide unique value every time
     * it is called. Example of these headers are 'x-ms-client-request-id', 'x-ms-correlation-request-id'.
     */
    public RequestIdPolicy(Supplier<HttpHeaders> requestIdSupplier) {
        this.requestIdSupplier = requestIdSupplier;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {

        if (Objects.nonNull(requestIdSupplier)) {
            HttpHeaders httpHeaders = requestIdSupplier.get();
            if (Objects.nonNull(httpHeaders) && httpHeaders.getSize() > 0) {
                httpHeaders.stream().forEach(httpHeader -> {
                    String requestIdHeaderValue = context.getHttpRequest().getHeaders().getValue(httpHeader.getName());
                    if (requestIdHeaderValue == null) {
                        context.getHttpRequest().getHeaders().put(httpHeader.getName(), httpHeader.getValue());
                    }
                });
                return next.process();
            }
        }
        
        // If we were not able to set client provided Request ID header, we will set default 'REQUEST_ID_HEADER'.
        String requestId = context.getHttpRequest().getHeaders().getValue(REQUEST_ID_HEADER);
        if (requestId == null) {
            context.getHttpRequest().getHeaders().put(REQUEST_ID_HEADER, UUID.randomUUID().toString());
        }
        return next.process();
    }
}
