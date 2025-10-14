// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.http.polling;

import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A polling strategy that chains multiple polling strategies, finds the first strategy that can
 * poll the current long-running operation, and polls with that strategy.
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
            throw LOGGER.throwableAtError().log("'strategies' cannot be empty.", IllegalArgumentException::new);
        }
        this.pollingStrategies = Collections.unmodifiableList(strategies);
    }

    @Override
    public boolean canPoll(Response<T> initialResponse) {
        // Find the first strategy that can poll in series so that
        // pollableStrategy is only set once
        for (PollingStrategy<T, U> strategy : pollingStrategies) {
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
    public U getResult(PollingContext<T> context, Type resultType) {
        return pollableStrategy.getResult(context, resultType);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if {@link #canPoll(Response)} is not called prior to this, or if it returns false.
     */
    @Override
    public PollResponse<T> onInitialResponse(Response<T> response, PollingContext<T> pollingContext,
        Type pollResponseType) {
        return pollableStrategy.onInitialResponse(response, pollingContext, pollResponseType);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if {@link #canPoll(Response)} is not called prior to this, or if it returns false.
     */
    @Override
    public PollResponse<T> poll(PollingContext<T> context, Type pollResponseType) {
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
