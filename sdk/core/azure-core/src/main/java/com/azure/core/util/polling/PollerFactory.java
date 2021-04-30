// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Supplier;

/**
 *
 * @param <U> The type of the final result of long running operation.
 */
public final class PollerFactory<U> {

    PollerFlux<PollResult, U> createOperationResourcePoller(HttpPipeline httpPipeline, Supplier<Mono<Response<?>>> activation) {
        return new PollerFlux<>(Duration.ofSeconds(30),
                ctx -> activation.get()
                    .flatMap(r -> {
                        HttpHeader operationLocation = r.getHeaders().get("Operation-Location");
                        if (operationLocation == null) {
                            return Mono.error(new RuntimeException("Operation-Location header not found"));
                        } else {
                            PollResult result = new PollResult().setNextPollUrl(operationLocation.getValue());
                            if (r.getStatusCode() / 100 != 2) {
                                return Mono.error(new RuntimeException("Operation cancelled or failed"))
                            }
                            return Mono.just(result);
                        }
                    }),
                ctx -> {
                    HttpRequest request = new HttpRequest(HttpMethod.GET, ctx.getLatestResponse().getValue().getNextPollUrl());
                    return httpPipeline.send(request/*TODO: context? */)
                            .
                },
                null,
                null);
    }
}
