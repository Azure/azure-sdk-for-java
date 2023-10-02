// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.polling;

import com.typespec.core.http.rest.Response;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.serializer.TypeReference;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A synchronous polling strategy that chains multiple synchronous polling strategies, finds the first strategy that can
 * poll the current long-running operation, and polls with that strategy.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 * kept
 */
public final class SyncChainedPollingStrategy<T, U> implements SyncPollingStrategy<T, U> {
    private static final ClientLogger LOGGER = new ClientLogger(SyncChainedPollingStrategy.class);

    private final List<SyncPollingStrategy<T, U>> pollingStrategies;
    private SyncPollingStrategy<T, U> pollableStrategy = null;

    /**
     * Creates a synchronous chained polling strategy with a list of polling strategies.
     *
     * @param strategies the list of synchronous polling strategies
     * @throws NullPointerException If {@code strategies} is null.
     * @throws IllegalArgumentException If {@code strategies} is an empty list.
     */
    public SyncChainedPollingStrategy(List<SyncPollingStrategy<T, U>> strategies) {
        Objects.requireNonNull(strategies, "'strategies' cannot be null.");
        if (strategies.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'strategies' cannot be empty."));
        }
        this.pollingStrategies = Collections.unmodifiableList(strategies);
    }

    @Override
    public boolean canPoll(Response<?> initialResponse) {
        // Find the first strategy that can poll in series so that
        // pollableStrategy is only set once
        for (SyncPollingStrategy<T, U> strategy : pollingStrategies) {
            if (strategy.canPoll(initialResponse)) {
                this.pollableStrategy = strategy;
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if {@link #canPoll(Response)} is not called prior to this, or if it returns false.
     */
    @Override
    public U getResult(PollingContext<T> context, TypeReference<U> resultType) {
        return pollableStrategy.getResult(context, resultType);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if {@link #canPoll(Response)} is not called prior to this, or if it returns false.
     */
    @Override
    public PollResponse<T> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
        TypeReference<T> pollResponseType) {
        return pollableStrategy.onInitialResponse(response, pollingContext, pollResponseType);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if {@link #canPoll(Response)} is not called prior to this, or if it returns false.
     */
    @Override
    public PollResponse<T> poll(PollingContext<T> context, TypeReference<T> pollResponseType) {
        return pollableStrategy.poll(context, pollResponseType);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if {@link #canPoll(Response)} is not called prior to this, or if it returns false.
     */
    @Override
    public T cancel(PollingContext<T> pollingContext, PollResponse<T> initialResponse) {
        return pollableStrategy.cancel(pollingContext, initialResponse);
    }
}
