// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Predicate;

public class ResponseAssertionPolicy implements HttpPipelinePolicy {

    private final Predicate<HttpResponse> responsePredicate;
    private final String message;

    public ResponseAssertionPolicy(Predicate<HttpResponse> responsePredicate, String message) {
        this.responsePredicate = Objects.requireNonNull(responsePredicate);
        this.message = Objects.requireNonNull(message);
    }


    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().flatMap(response -> {
            if (!responsePredicate.test(response)) {
                return Mono.error(new IllegalStateException(message));
            }
            return Mono.just(response);
        });


    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        HttpResponse response = next.processSync();
        if (!responsePredicate.test(response)) {
            throw new IllegalStateException(message);
        }
        return response;
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_CALL;
    }
}
