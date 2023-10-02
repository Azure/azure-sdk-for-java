// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.polling;

import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.polling.implementation.PollContextRequiredException;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Simple implementation of {@link SyncPoller}.
 *
 * @param <T> The type of poll response value
 * @param <U> The type of the final result of the long running operation
 */
final class SimpleSyncPoller<T, U> implements SyncPoller<T, U> {
    private static final ClientLogger LOGGER = new ClientLogger(SimpleSyncPoller.class);
    private final Function<PollingContext<T>, PollResponse<T>> pollOperation;
    private final BiFunction<PollingContext<T>, PollResponse<T>, T> cancelOperation;
    private final Function<PollingContext<T>, U> fetchResultOperation;
    private final PollResponse<T> activationResponse;
    private final PollingContext<T> pollingContext = new PollingContext<>();
    private volatile PollingContext<T> terminalPollContext;
    private volatile Duration pollInterval;

    /**
     * Creates SimpleSyncPoller.
     *
     * @param pollInterval the polling interval.
     * @param syncActivationOperation the operation to synchronously activate (start) the long running operation,
     *     this operation will be called with a new {@link PollingContext}.
     * @param pollOperation the operation to poll the current state of long running operation, this parameter
     *     is required and the operation will be called with current {@link PollingContext}.
     * @param cancelOperation a {@link Function} that represents the operation to cancel the long running operation
     *     if service supports cancellation, this parameter is required and if service does not support cancellation
     *     then the implementer should return Mono.error with an error message indicating absence of cancellation
     *     support, the operation will be called with current {@link PollingContext}.
     * @param fetchResultOperation a {@link Function} that represents the  operation to retrieve final result of
     *     the long running operation if service support it, this parameter is required and operation will be called
     *     current {@link PollingContext}, if service does not have an api to fetch final result and if final result
     *     is same as final poll response value then implementer can choose to simply return value from provided
     *     final poll response.
     */
    SimpleSyncPoller(Duration pollInterval,
                     Function<PollingContext<T>, PollResponse<T>> syncActivationOperation,
                     Function<PollingContext<T>, PollResponse<T>> pollOperation,
                     BiFunction<PollingContext<T>, PollResponse<T>, T> cancelOperation,
                     Function<PollingContext<T>, U> fetchResultOperation) {
        Objects.requireNonNull(pollInterval, "'pollInterval' cannot be null.");
        if (pollInterval.isNegative() || pollInterval.isZero()) {
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for 'defaultPollInterval' is not allowed."));
        }
        this.pollInterval = pollInterval;
        Objects.requireNonNull(syncActivationOperation, "'syncActivationOperation' cannot be null.");
        this.pollOperation = Objects.requireNonNull(pollOperation, "'pollOperation' cannot be null.");
        this.cancelOperation = Objects.requireNonNull(cancelOperation, "'cancelOperation' cannot be null.");
        this.fetchResultOperation = Objects.requireNonNull(fetchResultOperation,
            "'fetchResultOperation' cannot be null.");
        this.activationResponse = syncActivationOperation.apply(this.pollingContext);
        this.pollingContext.setOnetimeActivationResponse(this.activationResponse);
        this.pollingContext.setLatestResponse(this.activationResponse);
        if (this.activationResponse.getStatus().isComplete()) {
            this.terminalPollContext = this.pollingContext;
        }
    }

    @Override
    public synchronized PollResponse<T> poll() {
        PollResponse<T> response = this.pollOperation
            .apply(this.pollingContext);
        this.pollingContext.setLatestResponse(response);
        if (response.getStatus().isComplete()) {
            this.terminalPollContext = this.pollingContext.copy();
        }
        return response;
    }

    @Override
    public PollResponse<T> waitForCompletion() {
        return waitForCompletionHelper(null);
    }

    @Override
    public PollResponse<T> waitForCompletion(Duration timeout) {
        Objects.requireNonNull(timeout, "'timeout' cannot be null.");
        if (timeout.isNegative() || timeout.isZero()) {
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for timeout is not allowed."));
        }
        return waitForCompletionHelper(timeout);
    }

    @Override
    public PollResponse<T> waitUntil(LongRunningOperationStatus statusToWaitFor) {
        Objects.requireNonNull(statusToWaitFor, "'statusToWaitFor' cannot be null.");
        return waitUntilHelper(null, statusToWaitFor);
    }

    @Override
    public PollResponse<T> waitUntil(Duration timeout, LongRunningOperationStatus statusToWaitFor) {
        Objects.requireNonNull(timeout, "'timeout' cannot be null.");
        if (timeout.isNegative() || timeout.isZero()) {
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for timeout is not allowed."));
        }
        Objects.requireNonNull(statusToWaitFor, "'statusToWaitFor' cannot be null.");
        return waitUntilHelper(timeout, statusToWaitFor);
    }


    private PollResponse<T> waitUntilHelper(Duration timeout, LongRunningOperationStatus statusToWaitFor) {
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null
            && currentTerminalPollContext.getLatestResponse().getStatus() == statusToWaitFor) {
            return currentTerminalPollContext.getLatestResponse();
        } else {
            PollingContext<T> context = this.pollingContext.copy();
            PollResponse<T> pollResponse = PollingUtil.pollingLoop(context, timeout, statusToWaitFor, pollOperation, pollInterval);
            if (!pollResponse.getStatus().equals(statusToWaitFor)) {
                throw LOGGER.logExceptionAsError(new NoSuchElementException("Polling completed without"
                    + " receiving the given status '" + statusToWaitFor + "'."));
            }
            if (pollResponse.getStatus().isComplete()) {
                this.terminalPollContext = context;
            }
            return pollResponse;
        }
    }

    private PollResponse<T> waitForCompletionHelper(Duration timeout) {
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null) {
            return currentTerminalPollContext.getLatestResponse();
        } else {
            PollingContext<T> context = this.pollingContext.copy();
            PollResponse<T> pollResponse = PollingUtil.pollingLoop(context, timeout, null, pollOperation, pollInterval);
            this.terminalPollContext = context;
            return pollResponse;
        }
    }

    @Override
    public U getFinalResult() {
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null) {
            return this.fetchResultOperation
                .apply(currentTerminalPollContext);
        } else {
            PollingContext<T> context = this.pollingContext.copy();
            PollingUtil.pollingLoop(context, null, null, pollOperation, pollInterval);
            this.terminalPollContext = context;
            return getFinalResult();
        }
    }

    @Override
    public void cancelOperation() {
        PollingContext<T> context1 = this.pollingContext.copy();
        if (context1.getActivationResponse() == context1.getLatestResponse()) {
            this.cancelOperation.apply(context1, context1.getActivationResponse());
        } else {
            try {
                this.cancelOperation.apply(null, this.activationResponse);
            } catch (PollContextRequiredException crp) {
                PollingContext<T> context2 = this.pollingContext.copy();
                PollingUtil.pollingLoop(pollingContext, null, null, pollOperation, pollInterval);
                this.cancelOperation
                    .apply(context2, this.activationResponse);
            }
        }
    }

    @Override
    public SyncPoller<T, U> setPollInterval(Duration pollInterval) {
        Objects.requireNonNull(pollInterval, "'pollInterval' cannot be null.");
        if (pollInterval.isNegative() || pollInterval.isZero()) {
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for 'pollInterval' is not allowed."));
        }
        this.pollInterval = pollInterval;
        return this;
    }
}
