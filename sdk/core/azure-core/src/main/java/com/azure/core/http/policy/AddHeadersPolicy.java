// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * The pipeline policy that adds a particular set of headers to HTTP requests.
 */
public class AddHeadersPolicy implements HttpPipelinePolicy {
    private final Supplier<HttpHeaders> headersSupplier;

    /**
     * Creates a AddHeadersPolicy provided {@link Supplier}.
     *
     * @param headersSupplier The {@link Supplier} to provide headers to add to outgoing requests.
     */
    public AddHeadersPolicy(Supplier<HttpHeaders> headersSupplier) {
        this.headersSupplier = Objects.requireNonNull(headersSupplier, "headersSuppliercan not be null.");
    }

    /**
     * Creates a AddHeadersPolicy.
     *
     * @param headers The headers to add to outgoing requests.
     */
    public AddHeadersPolicy(HttpHeaders headers) {
        this.headersSupplier = () -> headers;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpHeaders headers = headersSupplier.get();
        for (HttpHeader header : headers) {
            context.getHttpRequest().setHeader(header.getName(), header.getValue());
        }
        return next.process();
    }
}
