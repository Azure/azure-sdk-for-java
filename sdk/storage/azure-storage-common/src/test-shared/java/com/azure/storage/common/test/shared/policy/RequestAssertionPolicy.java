// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Predicate;

public final class RequestAssertionPolicy implements HttpPipelinePolicy {

    private final Predicate<HttpRequest> requestPredicate;
    private final String message;

    public RequestAssertionPolicy(Predicate<HttpRequest> requestPredicate, String message) {
        this.requestPredicate = Objects.requireNonNull(requestPredicate);
        this.message = Objects.requireNonNull(message);
    }


    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!requestPredicate.test(context.getHttpRequest())) {
            return Mono.error(new IllegalStateException(message));
        }
        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if (!requestPredicate.test(context.getHttpRequest())) {
            throw new IllegalStateException(message);
        }
        return next.processSync();
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_CALL;
    }
}
