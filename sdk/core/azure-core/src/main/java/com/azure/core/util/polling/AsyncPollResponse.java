// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * AsyncPollResponse represents an event emitted by the {@link PollerFlux} that asynchronously polls
 * a long-running operation (LRO). An AsyncPollResponse event provides information such as the current
 * {@link LongRunningOperationStatus status} of the LRO, any {@link #getValue value} returned
 * in the poll, as well as other useful information provided by the service.
 * AsyncPollResponse also exposes {@link #cancelOperation} method to cancel the long-running operation
 * from reactor operator chain and {@link #getFinalResult()} method that returns final result of
 * the long-running operation.
 *
 * @param <T> The type of poll response value.
 * @param <U> The type of the final result of long-running operation.
 */
public final class AsyncPollResponse<T, U> {
    private final ClientLogger logger = new ClientLogger(AsyncPollResponse.class);
    private final PollingContext<T> pollingContext;
    private final BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancellationOperation;
    private final Function<PollingContext<T>, Mono<U>> fetchResultOperation;
    private final PollResponse<T> pollResponse;

    /**
     * Creates AsyncPollResponse.
     *
     * @param pollingContext the polling context
     * @param cancellationOperation the cancellation operation if supported by the service
     * @param fetchResultOperation the operation to fetch final result of long-running operation, if supported
     *                             by the service
     */
    AsyncPollResponse(PollingContext<T> pollingContext,
                      BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancellationOperation,
                      Function<PollingContext<T>, Mono<U>> fetchResultOperation) {
        this.pollingContext = Objects.requireNonNull(pollingContext,
                "'pollingContext' cannot be null.");
        this.cancellationOperation = Objects.requireNonNull(cancellationOperation,
                "'cancellationOperation' cannot be null.");
        this.fetchResultOperation = Objects.requireNonNull(fetchResultOperation,
                "'fetchResultOperation' cannot be null.");
        this.pollResponse = this.pollingContext.getLatestResponse();
    }

    /**
     * Represents the status of the long-running operation at the time the last polling operation finished successfully.
     * @return A {@link LongRunningOperationStatus} representing the result of the poll operation.
     */
    public LongRunningOperationStatus getStatus() {
        return pollResponse.getStatus();
    }

    /**
     * The value returned as a result of the last successful poll operation. This can be any custom user defined object,
     * or null if no value was returned from the service.
     *
     * @return T result of poll operation.
     */
    public T getValue() {
        return pollResponse.getValue();
    }

    /**
     * @return a Mono, upon subscription it cancels the remote long-running operation if cancellation
     * is supported by the service.
     */
    public Mono<T> cancelOperation() {
        return Mono.defer(() -> {
            try {
                return this.cancellationOperation
                        .apply(this.pollingContext, this.pollingContext.getActivationResponse());
            } catch (RuntimeException re) {
                return FluxUtil.monoError(logger, re);
            }
        });
    }

    /**
     * @return a Mono, upon subscription it fetches the final result of long-running operation if it
     * is supported by the service. If the long-running operation is not completed, then an empty
     * Mono will be returned.
     */
    public Mono<U> getFinalResult() {
        return Mono.defer(() -> {
            if (!this.pollResponse.getStatus().isComplete()) {
                return Mono.empty();
            } else {
                try {
                    return this.fetchResultOperation
                            .apply(this.pollingContext);
                } catch (RuntimeException re) {
                    return FluxUtil.monoError(logger, re);
                }
            }
        });
    }

    /**
     * Returns the delay the service has requested until the next polling operation is performed. A null or negative
     * value will be taken to mean that the Poller should determine on its own when the next poll operation is
     * to occur.
     *
     * @return Duration How long to wait before next retry.
     */
    Duration getRetryAfter() {
        return pollResponse.getRetryAfter();
    }
}

