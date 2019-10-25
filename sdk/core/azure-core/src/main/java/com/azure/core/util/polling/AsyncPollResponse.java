// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

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
    private final PollResponse<T> activationResponse;
    private final BiFunction<PollResponse<T>, PollResponse<T>, Mono<T>> cancellationOperation;
    private final BiFunction<PollResponse<T>, PollResponse<T>, Mono<U>> fetchResultOperation;
    private final PollResponse<T> pollResponse;

    /**
     * Creates AsyncPollResponse.
     *
     * @param activationResponse the response from activation operation
     * @param pollResponse the {@link PollResponse} this type composes
     * @param cancellationOperation the cancellation operation if supported by the service
     * @param fetchResultOperation the operation to fetch final result of long-running operation, if supported
     *                             by the service
     */
    AsyncPollResponse(PollResponse<T> activationResponse,
                      PollResponse<T> pollResponse,
                      BiFunction<PollResponse<T>, PollResponse<T>, Mono<T>> cancellationOperation,
                      BiFunction<PollResponse<T>, PollResponse<T>, Mono<U>> fetchResultOperation) {
        this.activationResponse = Objects.requireNonNull(activationResponse,
                "'activationResponse' cannot be null.");
        this.pollResponse = Objects.requireNonNull(pollResponse,
                "'pollResponse' cannot be null.");
        this.cancellationOperation = Objects.requireNonNull(cancellationOperation,
                "'cancellationOperation' cannot be null.");
        this.fetchResultOperation = Objects.requireNonNull(fetchResultOperation,
                "'fetchResultOperation' cannot be null.");
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
     * Returns the delay the service has requested until the next polling operation is performed. A null or negative
     * value will be taken to mean that the Poller should determine on its own when the next poll operation is
     * to occur.
     * Note: package private
     *
     * @return Duration How long to wait before next retry.
     */
    Duration getRetryAfter() {
        return pollResponse.getRetryAfter();
    }

    /**
     * A map of properties provided by the service that will be made available into the next poll operation.
     *
     * @return Map of properties that were returned from the service, and which will be made available into the next
     *     poll operation.
     */
    public Map<Object, Object> getProperties() {
        return pollResponse.getProperties();
    }

    /**
     * @return a Mono, upon subscription it cancels the remote long-running operation if cancellation
     * is supported by the service.
     */
    public Mono<T> cancelOperation() {
        return Mono.defer(() -> {
            try {
                return this.cancellationOperation
                        .apply(this.activationResponse, this.pollResponse);
            } catch (Throwable throwable) {
                return Mono.error(throwable);
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
                            .apply(this.activationResponse, this.pollResponse);
                } catch (Throwable throwable) {
                    return Mono.error(throwable);
                }
            }
        });
    }
}

