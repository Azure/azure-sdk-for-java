// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.util.polling.strategy.PollingStrategy;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.function.Supplier;

/**
 *
 * @param <U> The type of the final result of long running operation.
 */
public final class PollerFactory<U> {

    private final HttpPipeline httpPipeline;

    private Duration pollInterval;

    public PollerFactory(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        pollInterval = Duration.ofSeconds(10);
    }

    public PollerFactory<U> setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
        return this;
    }

    // TODO: exception types?
    public PollerFlux<PollResult, U> createPoller(PollingStrategy strategy,
            Supplier<Mono<? extends Response<?>>> activation, Type resultType) {
        return new PollerFlux<>(
            pollInterval,
            ctx -> activation.get()
                .flatMap(r -> {
                    if (!strategy.canPoll(r)) {
                        return Mono.<PollResult>error(new RuntimeException("Cannot poll with strategy " + strategy));
                    }
                    return Mono.just(strategy.onActivationResponse(r, ctx));
                }),
            ctx -> {
                HttpRequest request = new HttpRequest(HttpMethod.GET, strategy.getPollingUrl(ctx));
                return httpPipeline.send(request/*TODO: context? */).flatMap(r -> strategy.onPollingResponse(r, ctx))
                    .map(pollResult -> new PollResponse<>(pollResult.getStatus(), pollResult));
            },
            (ctx, pr) -> Mono.error(new RuntimeException("Cancellation is not supported.")),
            ctx -> {
                String finalResultUrl = strategy.getFinalGetUrl(ctx);
                if (finalResultUrl == null) {
                    return strategy.getFinalResult(null, ctx, resultType);
                } else {
                    HttpRequest request = new HttpRequest(HttpMethod.GET, finalResultUrl);
                    return httpPipeline.send(request/*TODO: context? */).flatMap(res ->
                            strategy.getFinalResult(res, ctx, resultType));
                }
            });
    }
}
