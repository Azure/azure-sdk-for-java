// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A polling strategy that chains multiple polling strategies, finds the first strategy that can poll the current
 * long-running operation, and polls with that strategy.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 * kept
 */
public final class ChainedPollingStrategy<T, U> implements PollingStrategy<T, U> {
    private static final ClientLogger LOGGER = new ClientLogger(ChainedPollingStrategy.class);

    private final List<PollingStrategy<T, U>> pollingStrategies;
    private PollingStrategy<T, U> pollableStrategy = null;

    /**
     * Creates a chained polling strategy with a list of polling strategies.
     *
     * @param strategies the list of polling strategies
     * @throws NullPointerException If {@code strategies} is null.
     * @throws IllegalArgumentException If {@code strategies} is an empty list.
     */
    public ChainedPollingStrategy(List<PollingStrategy<T, U>> strategies) {
        Objects.requireNonNull(strategies, "'strategies' cannot be null.");
        if (strategies.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'strategies' cannot be empty."));
        }
        this.pollingStrategies = Collections.unmodifiableList(strategies);
    }

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        // Find the first strategy that can poll in series so that
        // pollableStrategy is only set once
        return Flux.fromIterable(pollingStrategies)
            .concatMap(strategy -> strategy.canPoll(initialResponse)
                .map(canPoll -> Tuples.of(strategy, canPoll)))
            .takeUntil(Tuple2::getT2)
            .last()
            .map(tuple2 -> {
                this.pollableStrategy = tuple2.getT1();
                return true;
            })
            .defaultIfEmpty(false);
    }

    @Override
    public boolean canPollSync(Response<?> initialResponse) {
        for (PollingStrategy<T, U> pollingStrategy : pollingStrategies) {
            if (pollingStrategy.canPollSync(initialResponse)) {
                this.pollableStrategy = pollingStrategy;
                return true;
            }
        }

        return false;
    }

    /**
     * Parses the response from the final GET call into the result type of the long-running operation.
     *
     * @param pollingContext the {@link PollingContext} for the current polling operation
     * @param resultType the {@link TypeReference} of the final result object to deserialize into, or BinaryData if raw
     * response body should be kept.
     * @return a publisher emitting the final result
     * @throws NullPointerException if {@link #canPoll(Response)} is not called prior to this, or if it returns false.
     */
    @Override
    public Mono<U> getResult(PollingContext<T> pollingContext, TypeReference<U> resultType) {
        return pollableStrategy.getResult(pollingContext, resultType);
    }

    /**
     * Parses the response from the final GET call into the result type of the long-running operation.
     *
     * @param pollingContext the {@link PollingContext} for the current polling operation
     * @param resultType the {@link TypeReference} of the final result object to deserialize into, or BinaryData if raw
     * response body should be kept.
     * @return a publisher emitting the final result
     * @throws NullPointerException if {@link #canPollSync(Response)} is not called prior to this, or if it returns
     * false.
     */
    @Override
    public U getResultSync(PollingContext<T> pollingContext, TypeReference<U> resultType) {
        return pollableStrategy.getResultSync(pollingContext, resultType);
    }

    /**
     * Parses the initial response into a {@link LongRunningOperationStatus}, and stores information useful for polling
     * in the {@link PollingContext}. If the result is anything other than
     * {@link LongRunningOperationStatus#IN_PROGRESS}, the long-running operation will be terminated and none of the
     * other methods will be invoked.
     *
     * @param response the response from the initial method call to activate the long-running operation
     * @param pollingContext the {@link PollingContext} for the current polling operation
     * @param pollResponseType the {@link TypeReference} of the response type from a polling call, or BinaryData if raw
     * response body should be kept. This should match the generic parameter {@link U}.
     * @return a publisher emitting the poll response containing the status and the response content
     * @throws NullPointerException if {@link #canPoll(Response)} is not called prior to this, or if it returns false.
     */
    @Override
    public Mono<PollResponse<T>> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
        TypeReference<T> pollResponseType) {
        return pollableStrategy.onInitialResponse(response, pollingContext, pollResponseType);
    }

    /**
     * Parses the initial response into a {@link LongRunningOperationStatus}, and stores information useful for polling
     * in the {@link PollingContext}. If the result is anything other than
     * {@link LongRunningOperationStatus#IN_PROGRESS}, the long-running operation will be terminated and none of the
     * other methods will be invoked.
     *
     * @param response the response from the initial method call to activate the long-running operation
     * @param pollingContext the {@link PollingContext} for the current polling operation
     * @param pollResponseType the {@link TypeReference} of the response type from a polling call, or BinaryData if raw
     * response body should be kept. This should match the generic parameter {@link U}.
     * @return a poll response containing the status and the response content
     * @throws NullPointerException if {@link #canPollSync(Response)} is not called prior to this, or if it returns
     * false.
     */
    @Override
    public PollResponse<T> onInitialResponseSync(Response<?> response, PollingContext<T> pollingContext, TypeReference<T> pollResponseType) {
        return pollableStrategy.onInitialResponseSync(response, pollingContext, pollResponseType);
    }

    /**
     * Parses the response from the polling URL into a {@link PollResponse}, and stores information useful for further
     * polling and final response in the {@link PollingContext}. The result must have the
     * {@link LongRunningOperationStatus} specified, and the entire polling response content as a {@link BinaryData}.
     *
     * @param pollingContext the {@link PollingContext} for the current polling operation
     * @param pollResponseType the {@link TypeReference} of the response type from a polling call, or BinaryData if raw
     * response body should be kept. This should match the generic parameter {@link U}.
     * @return a publisher emitting the poll response containing the status and the response content
     * @throws NullPointerException if {@link #canPoll(Response)} is not called prior to this, or if it returns false.
     */
    @Override
    public Mono<PollResponse<T>> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType) {
        return pollableStrategy.poll(pollingContext, pollResponseType);
    }

    /**
     * Parses the response from the polling URL into a {@link PollResponse}, and stores information useful for further
     * polling and final response in the {@link PollingContext}. The result must have the
     * {@link LongRunningOperationStatus} specified, and the entire polling response content as a {@link BinaryData}.
     *
     * @param pollingContext the {@link PollingContext} for the current polling operation
     * @param pollResponseType the {@link TypeReference} of the response type from a polling call, or BinaryData if raw
     * response body should be kept. This should match the generic parameter {@link U}.
     * @return a poll response containing the status and the response content
     * @throws NullPointerException if {@link #canPollSync(Response)} is not called prior to this, or if it returns
     * false.
     */
    @Override
    public PollResponse<T> pollSync(PollingContext<T> pollingContext, TypeReference<T> pollResponseType) {
        return pollableStrategy.pollSync(pollingContext, pollResponseType);
    }

    /**
     * Cancels the long-running operation if service supports cancellation. If service does not support cancellation
     * then the implementer should return Mono.error with an error message indicating absence of cancellation.
     * <p>
     * Implementing this method is optional - by default, cancellation will not be supported unless overridden.
     *
     * @param pollingContext the {@link PollingContext} for the current polling operation, or null if the polling has
     * started in a {@link SyncPoller}
     * @param initialResponse the response from the initial operation
     * @return a publisher emitting the cancellation response content
     * @throws NullPointerException if {@link #canPoll(Response)} is not called prior to this, or if it returns false.
     */
    @Override
    public Mono<T> cancel(PollingContext<T> pollingContext, PollResponse<T> initialResponse) {
        return pollableStrategy.cancel(pollingContext, initialResponse);
    }

    /**
     * Cancels the long-running operation if service supports cancellation. If service does not support cancellation
     * then the implementer should throw an exception with an error message indicating absence of cancellation.
     * <p>
     * Implementing this method is optional - by default, cancellation will not be supported unless overridden.
     *
     * @param pollingContext the {@link PollingContext} for the current polling operation, or null if the polling has
     * started in a {@link SyncPoller}
     * @param initialResponse the response from the initial operation
     * @return the cancellation response content
     * @throws NullPointerException if {@link #canPollSync(Response)} is not called prior to this, or if it returns
     * false.
     */
    @Override
    public T cancelSync(PollingContext<T> pollingContext, PollResponse<T> initialResponse) {
        return pollableStrategy.cancelSync(pollingContext, initialResponse);
    }
}
