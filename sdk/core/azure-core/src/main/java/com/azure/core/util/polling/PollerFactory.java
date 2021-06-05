// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.strategy.PollingStrategy;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.function.Supplier;

/**
 *
 * @param <U> The type of the final result of long running operation.
 */
public final class PollerFactory<U> {

    private final SerializerAdapter serializerAdapter;
    private final HttpPipeline httpPipeline;

    private Duration pollInterval;

    public PollerFactory(SerializerAdapter serializerAdapter,
                         HttpPipeline httpPipeline) {
        this.serializerAdapter = serializerAdapter;
        this.httpPipeline = httpPipeline;
        pollInterval = Duration.ofSeconds(10);
    }

    public PollerFactory<U> setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
        return this;
    }

    public PollerFlux<PollResult, U> createPoller(PollingStrategy strategy,
            Supplier<Mono<? extends Response<?>>> activation, Type resultType) {
        return new PollerFlux<>(
                pollInterval,
                ctx -> activation.get()
                    .flatMap(r -> {
                        if (!strategy.canPoll(r)) {
                            return Mono.<PollResult>error(new RuntimeException("Cannot poll with strategy " + strategy));
                        }

                        return Mono.just(strategy.parseInitialResponse(r, ctx));
                    }),
                ctx -> {
                    HttpRequest request = new HttpRequest(HttpMethod.GET, strategy.getPollingUrl(ctx));
                    return httpPipeline.send(request/*TODO: context? */)
                            .flatMap(r -> r.getBodyAsString()
                                .map(body -> {
                                    PollResult result = strategy.parsePollingResponse(r, body, ctx);
                                    return new PollResponse<>(result.getStatus(), result);
                                }));
                },
                (ctx, pr) -> Mono.error(new RuntimeException("Cancellation is not supported.")),
                ctx -> {
                    String finalResultUrl = strategy.getFinalResultUrl(ctx);
                    if (finalResultUrl == null) {
                        return Mono.error(new RuntimeException("Cannot find final result URL"));
                    }
                    HttpRequest request = new HttpRequest(HttpMethod.GET, strategy.getFinalResultUrl(ctx));
                    return httpPipeline.send(request/*TODO: context? */).flatMap(res -> {
                        if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, resultType)) {
                            return (Mono<U>) BinaryData.fromFlux(res.getBody());
                        } else {
                            return res.getBodyAsString().flatMap(body -> Mono.fromCallable(() ->
                                    serializerAdapter.deserialize(body, resultType, SerializerEncoding.JSON)));
                        }
                    });
                });
    }
}
