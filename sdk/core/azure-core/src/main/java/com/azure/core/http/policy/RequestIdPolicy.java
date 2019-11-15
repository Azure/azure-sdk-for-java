// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

/**
 * The pipeline policy that puts a UUID or user provided id in the request header. Azure uses the request id as
 * the unique identifier for the request.
 * @see RequestIdPolicyOptions
 */
public class RequestIdPolicy implements HttpPipelinePolicy {
    private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";

    private final RequestIdPolicyOptions requestIdPolicyOptions;

    /**
     * Creates {@link RequestIdPolicy} with default {@link ExponentialBackoff} as {@link RetryStrategy}and use
     * 'retry-after-ms' in {@link HttpResponse} header for calculating retry delay.
     */
    public RequestIdPolicy() {
        this.requestIdPolicyOptions = null;
    }

    /**
     * Creates a {@link RequestIdPolicy} with the provided {@link RequestIdPolicyOptions}.
     * User can specify {@link java.util.function.Function} to dynamically generated id for various id headers.
     *
     * @param requestIdPolicyOptions with given {@link RetryPolicyOptions}.
     * @throws NullPointerException if {@code RequestIdPolicyOptions} is {@code null}.
     */
    public RequestIdPolicy(RequestIdPolicyOptions requestIdPolicyOptions) {
        this.requestIdPolicyOptions = Objects.requireNonNull(requestIdPolicyOptions,
            "'retryPolicyOptions' cannot be null.");
    }


    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!Objects.isNull(requestIdPolicyOptions)) {
            HttpHeaders httpHeaders =  requestIdPolicyOptions.getIdHeaderSupplier().get();
            if (!Objects.isNull(httpHeaders)) {
                httpHeaders.stream().forEach(httpHeader ->
                    context.getHttpRequest().getHeaders().put(httpHeader.getName(), httpHeader.getValue())
                );
            }
        } else {
            context.getHttpRequest().getHeaders().put(REQUEST_ID_HEADER, UUID.randomUUID().toString());
        }
        return next.process();
    }
}
