// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.http.polling;

import com.azure.v2.core.implementation.polling.PollContextRequiredException;
import com.azure.v2.core.implementation.polling.PollingUtils;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.v2.core.implementation.polling.PollingUtils.validatePollInterval;
import static com.azure.v2.core.implementation.polling.PollingUtils.validateTimeout;

/**
 * Simple implementation of {@link Poller}.
 *
 * <p>This class provides a simple implementation of the {@link Poller} interface for long-running operations.
 * It provides synchronous polling with a regular interval.</p>
 *
 * <p>Long-running operations are operations such as the creation or deletion of a resource, which take a significant
 * amount of time to complete. These operations are typically handled asynchronously, with the client initiating the
 * operation and then polling the service at intervals to determine whether the operation has completed.</p>
 *
 * <p><strong>Code Sample: Using a SimplePoller to poll until the operation is successfully completed</strong></p>
 *
 * @param <T> The type of poll response value
 * @param <U> The type of the final result of the long running operation
 *
 * @see com.azure.v2.core.http.polling
 * @see Poller
 * @see PollResponse
 * @see LongRunningOperationStatus
 */
final class SimplePoller<T, U> implements Poller<T, U> {
    private static final ClientLogger LOGGER = new ClientLogger(SimplePoller.class);
    private final Function<PollingContext<T>, PollResponse<T>> pollOperation;
    private final BiFunction<PollingContext<T>, PollResponse<T>, T> cancelOperation;
    private final Function<PollingContext<T>, U> fetchResultOperation;
    private final PollResponse<T> activationResponse;
    private final PollingContext<T> pollingContext = new PollingContext<>();
    private final Semaphore pollingSemaphore = new Semaphore(1);
    private volatile PollingContext<T> terminalPollContext;
    private volatile Duration pollInterval;

    /**
     * Creates SimpleSyncPoller.
     *
     * @param pollInterval the polling interval.
     * @param syncActivationOperation the operation to synchronously activate (start) the long-running operation, this
     * operation will be called with a new {@link PollingContext}.
     * @param pollOperation the operation to poll the current state of long-running operation, this parameter is
     * required and the operation will be called with current {@link PollingContext}.
     * @param cancelOperation a {@link Function} that represents the operation to cancel the long-running operation if
     * service supports cancellation, this parameter is required and if service does not support cancellation then the
     * implementer should throw an error with an error message indicating absence of cancellation support, the operation
     * will be called with current {@link PollingContext}.
     * @param fetchResultOperation a {@link Function} that represents the  operation to retrieve final result of the
     * long-running operation if service support it, this parameter is required and operation will be called current
     * {@link PollingContext}, if service does not have an api to fetch final result and if final result is same as
     * final poll response value then implementer can choose to simply return value from provided final poll response.
     */
    SimplePoller(Duration pollInterval, Function<PollingContext<T>, PollResponse<T>> syncActivationOperation,
        Function<PollingContext<T>, PollResponse<T>> pollOperation,
        BiFunction<PollingContext<T>, PollResponse<T>, T> cancelOperation,
        Function<PollingContext<T>, U> fetchResultOperation) {
        Objects.requireNonNull(pollInterval, "'pollInterval' cannot be null.");
        if (pollInterval.isNegative() || pollInterval.isZero()) {
            throw LOGGER.throwableAtWarning()
                .log("Negative or zero value for 'defaultPollInterval' is not allowed.", IllegalArgumentException::new);
        }
        this.pollInterval = pollInterval;
        Objects.requireNonNull(syncActivationOperation, "'syncActivationOperation' cannot be null.");
        this.pollOperation = Objects.requireNonNull(pollOperation, "'pollOperation' cannot be null.");
        this.cancelOperation = Objects.requireNonNull(cancelOperation, "'cancelOperation' cannot be null.");
        this.fetchResultOperation
            = Objects.requireNonNull(fetchResultOperation, "'fetchResultOperation' cannot be null.");
        this.activationResponse = syncActivationOperation.apply(this.pollingContext);
        this.pollingContext.setOnetimeActivationResponse(this.activationResponse);
        this.pollingContext.setLatestResponse(this.activationResponse);
        if (this.activationResponse.getStatus().isComplete()) {
            this.terminalPollContext = this.pollingContext;
        }
    }

    @Override
    public PollResponse<T> poll() {
        try {
            pollingSemaphore.acquire();
        } catch (InterruptedException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }

        try {
            PollResponse<T> response = this.pollOperation.apply(this.pollingContext);
            this.pollingContext.setLatestResponse(response);
            if (response.getStatus().isComplete()) {
                this.terminalPollContext = this.pollingContext.copy();
            }
            return response;
        } finally {
            pollingSemaphore.release();
        }
    }

    @Override
    public PollResponse<T> waitForCompletion() {
        return waitForCompletionHelper(null);
    }

    @Override
    public PollResponse<T> waitForCompletion(Duration timeout) {
        validateTimeout(timeout, LOGGER);

        return waitForCompletionHelper(timeout);
    }

    @Override
    public PollResponse<T> waitUntil(LongRunningOperationStatus statusToWaitFor) {
        Objects.requireNonNull(statusToWaitFor, "'statusToWaitFor' cannot be null.");
        return waitUntilHelper(null, statusToWaitFor);
    }

    @Override
    public PollResponse<T> waitUntil(Duration timeout, LongRunningOperationStatus statusToWaitFor) {
        validateTimeout(timeout, LOGGER);

        Objects.requireNonNull(statusToWaitFor, "'statusToWaitFor' cannot be null.");
        return waitUntilHelper(timeout, statusToWaitFor);
    }

    private PollResponse<T> waitUntilHelper(Duration timeout, LongRunningOperationStatus statusToWaitFor) {
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null) {
            // If the terminal poll context is not null, then the operation has already completed.
            // Don't attempt to waitUntil status as it will never happen.
            return currentTerminalPollContext.getLatestResponse();
        } else {
            PollingContext<T> context = this.pollingContext.copy();
            PollResponse<T> pollResponse
                = PollingUtils.pollingLoop(context, timeout, statusToWaitFor, pollOperation, pollInterval, true);

            if (pollResponse.getStatus().isComplete()) {
                this.terminalPollContext = context;
            }
            return pollResponse;
        }
    }

    private PollResponse<T> waitForCompletionHelper(Duration timeout) {
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null) {
            // If the terminal poll context is not null, then the operation has already completed.
            return currentTerminalPollContext.getLatestResponse();
        }

        PollingContext<T> context = this.pollingContext.copy();
        PollResponse<T> pollResponse
            = PollingUtils.pollingLoop(context, timeout, null, pollOperation, pollInterval, false);
        this.terminalPollContext = context;
        return pollResponse;
    }

    @Override
    public U getFinalResult() {
        return getFinalResultHelper(null);
    }

    @Override
    public U getFinalResult(Duration timeout) {
        validateTimeout(timeout, LOGGER);
        return getFinalResultHelper(timeout);
    }

    private U getFinalResultHelper(Duration timeout) {
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null) {
            // If the terminal poll context is not null, then the operation has already completed.
            return this.fetchResultOperation.apply(currentTerminalPollContext);
        } else {
            PollingContext<T> context = this.pollingContext.copy();
            PollingUtils.pollingLoop(context, timeout, null, pollOperation, pollInterval, false);
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
                PollingUtils.pollingLoop(pollingContext, null, null, pollOperation, pollInterval, false);
                this.cancelOperation.apply(context2, this.activationResponse);
            }
        }
    }

    @Override
    public Poller<T, U> setPollInterval(Duration pollInterval) {
        this.pollInterval = validatePollInterval(pollInterval, LOGGER);
        return this;
    }
}
