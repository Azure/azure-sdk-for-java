// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Supplier;

public class CustomHeadersPolicy implements HttpPipelinePolicy {
    private final Supplier<HttpHeaders> customHeaderSupplier;

    public CustomHeadersPolicy(Supplier<HttpHeaders> customHeaderSupplier) {
        this.customHeaderSupplier = Objects.requireNonNull(customHeaderSupplier, "Custom header cannot be null");
    }

    /**
     * Adds the customized headers to authenticate a request to Azure App Configuration service.
     *
     * @param context The request context
     * @param next The next HTTP pipeline policy to process the {@code context's} request after this policy
     *     completes.
     * @return A {@link Mono} representing the HTTP response that will arrive asynchronously.
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final HttpHeaders httpHeaders = customHeaderSupplier.get();

        if (Objects.nonNull(httpHeaders) && httpHeaders.getSize() > 0) {
            for (HttpHeader header : httpHeaders) {
                context.getHttpRequest().getHeaders().put(header.getName(), header.getValue());
            }
        }
        return next.process();
    }
}
