// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollResponse.OperationStatus;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.core.util.polling.PollResponse.OperationStatus.FAILED;

/**
 * This class offers API that simplifies the task of executing long-running operations against an Azure service. The
 * {@link Poller} consists of a poll operation, a cancel operation, if it is supported by the Azure service, and a
 * polling interval.
 *
 * <p>
 * It provides the following functionality:
 * <ul>
 *      <li>Querying the current state of long-running operations.</li>
 *      <li>Requesting an asynchronous notification for long-running operation's state.</li>
 *      <li>Cancelling the long-running operation if cancellation is supported by the service.</li>
 *      <li>Triggering a poll operation manually.</li>
 *      <li>Enable/Disable auto-polling.</li>
 * </ul>
 *
 * <p><strong>Auto polling</strong></p>
 * Auto-polling is enabled by default. The {@link Poller} starts polling as soon as the instance is created. The
 * {@link Poller} will transparently call the poll operation every polling cycle and track the state of the
 * long-running operation. Azure services can return {@link PollResponse#getRetryAfter()} to override the
 * {@code Poller.pollInterval} defined in the {@link Poller}. {@link #getStatus()} represents the status returned by a
 * successful long-running operation at the time the last auto-polling or last manual polling, whichever happened most
 * recently.
 *
 * <p><strong>Disable auto polling</strong></p>
 * For those scenarios which require manual control of the polling cycle, disable auto-polling by calling
 * {@link #setAutoPollingEnabled(boolean) setAutoPollingEnabled(false)}. Then perform manual polling by invoking
 * {@link #poll()} function. It will call poll operation once and update {@link #getStatus()} with the latest status.
 *
 * <p>When auto-polling is disabled, the {@link Poller} will not update its status or any other information, unless
 * manual polling is triggered by calling {@link #poll()} function.
 *
 * <p>The {@link Poller} will stop polling when the long-running operation is complete or disabled. Polling is
 * considered complete based on status defined in {@link OperationStatus}.
 *
 * <p><strong>Code samples</strong></p>
 *
 * <p><strong>Instantiating and subscribing to PollResponse</strong></p>
 * {@codesnippet com.azure.core.util.polling.poller.instantiationAndSubscribe}
 *
 * <p><strong>Wait for polling to complete</strong></p>
 * {@codesnippet com.azure.core.util.polling.poller.block}
 *
 * <p><strong>Disable auto polling and poll manually</strong></p>
 * {@codesnippet com.azure.core.util.polling.poller.poll-manually}
 *
 * @param <T> Type of poll response value
 * @param <R> The final output value.
 * @see PollResponse
 * @see OperationStatus
 */
public class Poller<T, R> {

    private final ClientLogger logger = new ClientLogger(Poller.class);

    /*
     * poll operation is a function that takes the previous PollResponse, and returns a new Mono of PollResponse to
     * represent the current state
     */
    private final Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation;

    /*
     * poll interval before next auto poll. This value will be used if the PollResponse does not include retryAfter
     * from the service.
     */
    private final Duration pollInterval;

    /*
     * This will be called when cancel operation is triggered.
     */
    private final Function<Poller<T, R>, Mono<T>> cancelOperation;

    /*
     * This will be called when final result needs to be retrieved after polling has completed.
     */
    private final Supplier<Mono<R>> fetchResultOperation;

    /*
     * This handle to Flux allow us to perform polling operation in asynchronous manner.
     * This could be shared among many subscriber. One of the subscriber will be this poller itself.
     * Once subscribed, this Flux will continue to poll for status until poll operation is done/complete.
     */
    private final Flux<PollResponse<T>> fluxHandle;

    /*
     * This will save last poll response.
     */
    private volatile PollResponse<T> pollResponse = new PollResponse<>(OperationStatus.NOT_STARTED, null);

    /*
     * Since constructor create a subscriber and start auto polling. This handle will be used to dispose the subscriber
     * when client disable auto polling.
     */
    private Disposable fluxDisposable;

    /*
     * Indicate to poll automatically or not when poller is created.
     * default value is false;
     */
    private boolean autoPollingEnabled;

    /**
     * Creates a {@link Poller} instance with poll interval and poll operation. The polling starts immediately by
     * invoking {@code pollOperation}. The next poll cycle will be defined by {@code retryAfter} value in
     * {@link PollResponse}. In absence of {@code retryAfter}, the {@link Poller} will use {@code pollInterval}.
     *
     * <p><strong>Create poller object</strong></p>
     * {@codesnippet com.azure.core.util.polling.poller.initialize.interval.polloperation}
     *
     * @param pollInterval Non null and greater than zero poll interval.
     * @param pollOperation The polling operation to be called by the {@link Poller} instance. This must never return
     *     {@code null} and always have a non-null {@link OperationStatus}. {@link Mono} returned from poll operation
     *     should never return {@link Mono#error(Throwable)}. If an unexpected scenario happens during the poll
     *     operation, it should be handled by the client library and return a valid {@link PollResponse}. However if
     *     the poll operation returns {@link Mono#error(Throwable)}, the {@link Poller} will disregard it and continue
     *     to poll.
     * @param fetchResultOperation The operation to be called to fetch final result after polling has been completed.
     * @throws IllegalArgumentException if {@code pollInterval} is less than or equal to zero.
     * @throws NullPointerException if {@code pollInterval} or {@code pollOperation} is {@code null}.
     */
    public Poller(Duration pollInterval, Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation,
                  Supplier<Mono<R>> fetchResultOperation) {
        this(pollInterval, pollOperation, fetchResultOperation, null, null);
    }

    /**
     * Creates a {@link Poller} instance with poll interval, poll operation, and optional cancel operation. Polling
     * starts immediately by invoking {@code pollOperation}. The next poll cycle will be defined by retryAfter value in
     * {@link PollResponse}. In absence of {@link PollResponse#getRetryAfter()}, the {@link Poller} will use
     * {@code pollInterval}.
     *
     * @param pollInterval Not-null and greater than zero poll interval.
     * @param pollOperation The polling operation to be called by the {@link Poller} instance. This must never return
     *     {@code null} and always have a non-null {@link OperationStatus}. {@link Mono} returned from poll operation
     *     should never return {@link Mono#error(Throwable)}. If an unexpected scenario happens during the poll
     *     operation, it should be handled by the client library and return a valid {@link PollResponse}. However if
     *     the poll operation returns {@link Mono#error(Throwable)}, the {@link Poller} will disregard it and continue
     *     to poll.
     * @param activationOperation The activation operation to be called by the {@link Poller} instance before
     *     calling {@code pollOperation}. It can be {@code null} which will indicate to the {@link Poller} that
     *     {@code pollOperation} can be called straight away.
     * @param fetchResultOperation The operation to be called to fetch final result after polling has been completed.
     * @param cancelOperation Cancel operation if cancellation is supported by the service. If it is {@code null}, then
     *     the cancel operation is not supported.
     * @throws IllegalArgumentException if {@code pollInterval} is less than or equal to zero and if
     *      {@code pollInterval} or {@code pollOperation} are {@code null}
     */
    public Poller(Duration pollInterval, Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation,
                  Supplier<Mono<R>> fetchResultOperation, Supplier<Mono<T>> activationOperation,
                  Function<Poller<T, R>, Mono<T>> cancelOperation) {

        Objects.requireNonNull(pollInterval, "'pollInterval' cannot be null.");
        Objects.requireNonNull(fetchResultOperation, "'fetchResultOperation' cannot be null.");
        if (pollInterval.compareTo(Duration.ZERO) <= 0) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for 'pollInterval' is not allowed."));
        }

        this.pollInterval = pollInterval;
        this.fetchResultOperation = fetchResultOperation;
        this.pollOperation = Objects.requireNonNull(pollOperation, "'pollOperation' cannot be null.");

        // When the first item is emitted, we set the poll response to it. So the first invocation of pollOperation can
        // leverage this value.
        final Mono<T> onActivation = activationOperation == null
            ? Mono.empty()
            : activationOperation.get().map(response -> {
                this.pollResponse = new PollResponse<>(OperationStatus.NOT_STARTED, response);
                return response;
            })
            .doOnError(ex -> this.pollResponse = new PollResponse<>(FAILED, null))
            .onErrorReturn(null);


        this.fluxHandle = asyncPollRequestWithDelay()
            .flux()
            .repeat()
            .takeUntil(pollResponse -> isComplete())
            .share()
            .delaySubscription(onActivation);

        // auto polling start here
        this.fluxDisposable = fluxHandle.subscribe();
        this.autoPollingEnabled = true;
        this.cancelOperation = cancelOperation;
    }

    /**
     * Create a {@link Poller} instance with poll interval, poll operation and cancel operation. The polling starts
     * immediately by invoking {@code pollOperation}. The next poll cycle will be defined by retryAfter value
     * in {@link PollResponse}. In absence of {@link PollResponse#getRetryAfter()}, the {@link Poller}
     * will use {@code pollInterval}.
     *
     * @param pollInterval Not-null and greater than zero poll interval.
     * @param pollOperation The polling operation to be called by the {@link Poller} instance. This is a callback into
     *     the client library, which must never return {@code null}, and which must always have a non-null
     *     {@link OperationStatus}. {@link Mono} returned from poll operation should never return
     *     {@link Mono#error(Throwable)}. If any unexpected scenario happens in poll operation, it should be handled by
     *     client library and return a valid {@link PollResponse}. However if poll operation returns
     *     {@link Mono#error(Throwable)}, the {@link Poller} will disregard that and continue to poll.
     * @param fetchResultOperation The operation to be called to fetch final result after polling has been completed.
     * @param cancelOperation cancel operation if cancellation is supported by the service. It can be {@code null}
     *      which will indicate to the {@link Poller} that cancel operation is not supported by Azure service.
     * @throws IllegalArgumentException if {@code pollInterval} is less than or equal to zero and if
     * {@code pollInterval} or {@code pollOperation} are {@code null}
     */
    public Poller(Duration pollInterval, Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation,
                  Supplier<Mono<R>> fetchResultOperation, Function<Poller<T, R>, Mono<T>> cancelOperation) {
        this(pollInterval, pollOperation, fetchResultOperation, null, cancelOperation);
    }

    /**
     * Attempts to cancel the long-running operation that this {@link Poller} represents. This is possible only if the
     * service supports it, otherwise an {@code UnsupportedOperationException} will be thrown.
     * <p>
     * It will call cancelOperation if status is {@link OperationStatus#IN_PROGRESS} otherwise it does nothing.
     *
     * @throws UnsupportedOperationException when the cancel operation is not supported by the Azure service.
     * @return A {@link Mono} containing the poller response.
     */
    public Mono<T> cancelOperation() throws UnsupportedOperationException {
        if (this.cancelOperation == null) {
            return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(
                "Cancel operation is not supported on this service/resource.")));
        }

        // We can not cancel an operation if it was never started
        // It only make sense to call cancel operation if current status IN_PROGRESS.

        final PollResponse<T> response = this.pollResponse;
        if (response != null && response.getStatus() != OperationStatus.IN_PROGRESS) {
            return Mono.empty();
        }

        //Time to call cancel
        return this.cancelOperation.apply(this);
    }

    /**
     * Returns the final result if the polling operation has been completed.
     * @return A {@link Mono} containing the final output.
     */
    public Mono<R> result() {
        if (getStatus() == null || !getStatus().equals(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED)) {
            return Mono.error(new IllegalAccessException("The poll operation has not successfully completed."));
        }
        return fetchResultOperation.get();
    }

    /**
     * This method returns a {@link Flux} that can be subscribed to, enabling a subscriber to receive notification of
     * every {@link PollResponse}, as it is received.
     *
     * @return A {@link Flux} that can be subscribed to receive poll responses as the long-running operation executes.
     */
    public Flux<PollResponse<T>> getObserver() {
        return this.fluxHandle;
    }

    /**
     * Enables user to take control of polling and trigger manual poll operation. It will call poll operation once.
     * This will not turn off auto polling.
     *
     * <p><strong>Manual polling</strong></p>
     * <p>
     * {@codesnippet com.azure.core.util.polling.poller.poll-indepth}
     *
     * @return A {@link Mono} that returns {@link PollResponse}. This will call poll operation once.
     */
    public Mono<PollResponse<T>> poll() {
        return pollOperation.apply(this.pollResponse)
            .doOnEach(pollResponseSignal -> {
                if (pollResponseSignal.get() != null) {
                    this.pollResponse = pollResponseSignal.get();
                }
            });
    }

    /**
     * Blocks execution and wait for polling to complete. The polling is considered complete based on the status defined
     * in {@link OperationStatus}.
     *
     * <p>It will enable auto-polling if it was disabled by the user.
     *
     * @return The final output once Polling completes.
     */
    public R block() {
        if (!isAutoPollingEnabled()) {
            setAutoPollingEnabled(true);
        }
        this.fluxHandle.blockLast();
        return result().block();
    }

    /**
     * Blocks execution and wait for polling to complete. The polling is considered complete based on status defined in
     * {@link OperationStatus}.
     * <p>It will enable auto-polling if it was disable by user.
     *
     * @param timeout The duration for which execution is blocked and waits for polling to complete.
     * @return The final output once Polling completes.
     */
    public R block(Duration timeout) {
        if (!isAutoPollingEnabled()) {
            setAutoPollingEnabled(true);
        }
        this.fluxHandle.blockLast(timeout);
        return result().block();
    }

    /**
     * Blocks indefinitely until given {@link OperationStatus} is received.
     *
     * @param statusToBlockFor The desired {@link OperationStatus} to block for.
     * @return {@link PollResponse} whose {@link PollResponse#getStatus()} matches {@code statusToBlockFor}.
     * @throws IllegalArgumentException If {@code statusToBlockFor} is {@code null}.
     */
    public PollResponse<T> blockUntil(OperationStatus statusToBlockFor) {
        return blockUntil(statusToBlockFor, null);
    }

    /**
     * Blocks until given {@code statusToBlockFor} is received or the {@code timeout} elapses. If a {@code null}
     * {@code timeout} is given, it will block indefinitely.
     *
     * @param statusToBlockFor The desired {@link OperationStatus} to block for and it can be any valid
     *     {@link OperationStatus} value.
     * @param timeout The time after which it will stop blocking. A {@code null} value will cause to block
     *     indefinitely. Zero or negative are not valid values.
     * @return {@link PollResponse} for matching desired status to block for.
     * @throws IllegalArgumentException if {@code timeout} is zero or negative and if {@code statusToBlockFor} is
     *     {@code null}.
     */
    public PollResponse<T> blockUntil(OperationStatus statusToBlockFor, Duration timeout) {
        if (statusToBlockFor == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Null value for status is not allowed."));
        }
        if (timeout != null && timeout.toNanos() <= 0) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for timeout is not allowed."));
        }
        if (!isAutoPollingEnabled()) {
            setAutoPollingEnabled(true);
        }
        if (timeout != null) {
            return this.fluxHandle
                .filter(tPollResponse -> matchStatus(tPollResponse, statusToBlockFor)).blockFirst(timeout);
        } else {
            return this.fluxHandle
                .filter(tPollResponse -> matchStatus(tPollResponse, statusToBlockFor)).blockFirst();
        }
    }

    /*
     * Indicate that the {@link PollResponse} matches with the status to block for.
     * @param currentPollResponse The poll response which we have received from the flux.
     * @param statusToBlockFor The {@link OperationStatus} to block and it can be any valid {@link OperationStatus}
     * value.
     * @return True if the {@link PollResponse} return status matches the status to block for.
     */
    private boolean matchStatus(PollResponse<T> currentPollResponse, OperationStatus statusToBlockFor) {
        // perform validation
        if (currentPollResponse == null || statusToBlockFor == null) {
            return false;
        }
        if (statusToBlockFor == currentPollResponse.getStatus()) {
            return true;
        }
        return false;
    }

    /*
     * This function will apply delay and call poll operation function async.
     * We expect Mono from pollOperation should never return Mono.error(). If any unexpected
     * scenario happens in pollOperation, it should catch it and return a valid PollResponse.
     * This is because poller does not know what to do in case on Mono.error.
     * This function will return empty mono in case of Mono.error() returned by poll operation.
     *
     * @return mono of poll response
     */
    private Mono<PollResponse<T>> asyncPollRequestWithDelay() {
        return Mono.defer(() -> this.pollOperation.apply(this.pollResponse)
            .delaySubscription(getCurrentDelay())
            .onErrorResume(throwable -> {
                // We should never get here and since we want to continue polling
                //Log the error
                return Mono.empty();
            })
            .doOnEach(pollResponseSignal -> {
                if (pollResponseSignal.get() != null) {
                    this.pollResponse = pollResponseSignal.get();
                }
            }));
    }

    /**
     * We will use {@link PollResponse#getRetryAfter()} if it is greater than zero otherwise use poll interval.
     */
    private Duration getCurrentDelay() {
        final PollResponse<T> current = pollResponse;

        return (current != null
            && current.getRetryAfter() != null
            && current.getRetryAfter().compareTo(Duration.ZERO) > 0) ? current.getRetryAfter() : pollInterval;
    }

    /**
     * Controls whether auto-polling is enabled or disabled. Refer to the {@link Poller} class-level JavaDoc for more
     * details on auto-polling.
     *
     * <p><strong>Disable auto polling</strong></p>
     * {@codesnippet com.azure.core.util.polling.poller.disableautopolling}
     *
     * <p><strong>Enable auto polling</strong></p>
     * {@codesnippet com.azure.core.util.polling.poller.enableautopolling}
     *
     * @param autoPollingEnabled If true, auto-polling will occur transparently in the background, otherwise it
     *     requires manual polling by the user to get the latest state.
     */
    public final void setAutoPollingEnabled(boolean autoPollingEnabled) {
        this.autoPollingEnabled = autoPollingEnabled;
        if (this.autoPollingEnabled) {
            if (!activeSubscriber()) {
                this.fluxDisposable = this.fluxHandle.subscribe(pr -> this.pollResponse = pr);
            }
        } else {
            if (activeSubscriber()) {
                this.fluxDisposable.dispose();
            }
        }
    }

    /**
     * An operation will be considered complete if it is in some custom complete state or in one of the following state:
     * <ul>
     *     <li>SUCCESSFULLY_COMPLETED</li>
     *     <li>USER_CANCELLED</li>
     *     <li>FAILED</li>
     * </ul>
     * Also see {@link OperationStatus}
     * @return true if operation is done/complete.
     */
    public boolean isComplete() {
        final PollResponse<T> current = this.pollResponse;
        return current != null && current.getStatus().isComplete();
    }

    /**
     * Get the last poll response.
     * @return the last poll response.
     */
    public PollResponse<T> getLastPollResponse() {
        return this.pollResponse;
    }

    /*
     * Determine if this poller's internal subscriber exists and active.
     */
    private boolean activeSubscriber() {
        return (this.fluxDisposable != null && !this.fluxDisposable.isDisposed());
    }

    /**
     * Indicates if auto polling is enabled. Refer to the {@link Poller} class-level JavaDoc for more details on
     * auto-polling.
     *
     * @return {@code true} if auto-polling is enabled and {@code false} otherwise.
     */
    public boolean isAutoPollingEnabled() {
        return this.autoPollingEnabled;
    }

    /**
     * Current known status as a result of last poll event or last response from a manual polling.
     *
     * @return Current status or {@code null} if no status is available.
     */
    public OperationStatus getStatus() {
        final PollResponse<T> current = this.pollResponse;
        return current != null ? current.getStatus() : null;
    }
}
