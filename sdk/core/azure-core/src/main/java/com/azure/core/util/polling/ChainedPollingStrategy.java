// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.rest.Response;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;

/**
 * A polling strategy that chains multiple polling strategies, finds the first strategy that can poll the current
 * long running operation, and polls with that strategy.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 *           kept
 */
public class ChainedPollingStrategy<T, U> implements PollingStrategy<T, U> {
    private final List<PollingStrategy<T, U>> pollingStrategies;
    private volatile PollingStrategy<T, U> pollableStrategy = null;

    /**
     * Creates an empty chained polling strategy.
     */
    public ChainedPollingStrategy() {
        this.pollingStrategies = new ArrayList<>();
    }

    public ChainedPollingStrategy(List<PollingStrategy<T, U>> strategies) {
        this.pollingStrategies = strategies;
    }

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        return Flux.fromIterable(pollingStrategies)
            .concatMap(strategy -> strategy.canPoll(initialResponse)
                .map(canPoll -> Tuples.of(strategy, canPoll)))
            .takeUntil(Tuple2::getT2)
            .next()
            .map(tuple2 -> {
                this.pollableStrategy = tuple2.getT1();
                return true;
            })
            .defaultIfEmpty(false);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if {@link #canPoll(Response)} is not called prior to this, or if it returns false.
     */
    @Override
    public Mono<U> getResult(PollingContext<T> context, TypeReference<U> resultType) {
        return pollableStrategy.getResult(context, resultType);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if {@link #canPoll(Response)} is not called prior to this, or if it returns false.
     */
    @Override
    public Mono<PollResponse<T>> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
                                                              TypeReference<T> pollResponseType) {
        return pollableStrategy.onInitialResponse(response, pollingContext, pollResponseType);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if {@link #canPoll(Response)} is not called prior to this, or if it returns false.
     */
    @Override
    public Mono<PollResponse<T>> poll(PollingContext<T> context, TypeReference<T> pollResponseType) {
        return pollableStrategy.poll(context, pollResponseType);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if {@link #canPoll(Response)} is not called prior to this, or if it returns false.
     */
    @Override
    public Mono<T> cancel(PollingContext<T> pollingContext, PollResponse<T> initialResponse) {
        return pollableStrategy.cancel(pollingContext, initialResponse);
    }
}
