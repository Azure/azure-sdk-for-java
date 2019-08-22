// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollResponse.OperationStatus;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class offers API that simplifies the task of executing long-running operations against Azure service.
 * The {@link Poller} consist of poll operation, cancel operation if supported by Azure service and polling interval.
 * <p>
  * It provides the following functionality:
 *
 * <ul>
 *      <li>Querying the current state of long-running operations.</li>
 *      <li>Requesting an asynchronous notification for long-running operation's state.</li>
 *      <li>Cancelling the long-running operation if cancellation is supported by the service.</li>
 *      <li>Triggering a poll operation manually.</li>
 *      <li>Enable/Disable auto-polling.</li>
 * </ul>
 *
 * <p><strong>Auto Polling</strong></p>
 * Auto-polling is enabled by-default. It means that the {@link Poller} starts polling as soon as its instance is created. The {@link Poller} will transparently call the poll operation every polling cycle
 * and track the state of the long-running operation. Azure services can return {@link PollResponse#getRetryAfter()} to override the {@code Poller.pollInterval} defined in the {@link Poller}.
 * The {@link Poller#getStatus()} represents the status returned by the successful long-running operation at the time the last auto-polling or last manual polling, whichever happened most recently.
 *
 *<p><strong>Disable Auto Polling</strong></p>
 * For those scenarios which require manual control of the polling cycle, disable auto-poling by calling {@code setAutoPollingEnabled#false} and perform manual poll
 * by invoking {@link Poller#poll()} function. It will call poll operation once and update the {@link Poller} with the latest status.
 * <p>When auto-polling is disabled, the {@link Poller} will not update its status or other information, unless manual polling is triggered by calling {@link Poller#poll()} function.
 *
 * <p>The {@link Poller} will stop polling when the long-running operation is complete or it is disabled. The polling is considered complete
 * based on status defined in {@link OperationStatus}.
 *
 * <p><strong>Code Samples</strong></p>
 *
 * <p><strong>Instantiating and Subscribing to Poll Response</strong></p>
 * {@codesnippet com.azure.core.util.polling.poller.instantiationAndSubscribe}
 *
 * <p><strong>Wait/Block for Polling to complete</strong></p>
 * {@codesnippet com.azure.core.util.polling.poller.block}
 *
 * <p><strong>Disable auto polling and polling manually</strong></p>
 * {@codesnippet com.azure.core.util.polling.poller.poll}
 *
 * @param <T> Type of poll response value
 * @see PollResponse
 * @see OperationStatus
 */
public class Poller<T> {

    private final ClientLogger logger = new ClientLogger(Poller.class);
    /*
     * poll operation is a function that takes the previous PollResponse, and
     * returns a new Mono of PollResponse to represent the current state
     */
    private final Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation;

    /*
     * poll interval before next auto poll. This value will be used if the PollResponse does not include retryAfter from the service.
     */
    private Duration pollInterval;

    /*
     * This will save last poll response.
     */
    private PollResponse<T> pollResponse;

    /*
     * This will be called when cancel operation is triggered.
     */
    private Consumer<Poller<T>> cancelOperation;

    /*
     * Indicate to poll automatically or not when poller is created.
     * default value is false;
     */
    private boolean autoPollingEnabled;

    /*
     * This handle to Flux allow us to perform polling operation in asynchronous manner.
     * This could be shared among many subscriber. One of the subscriber will be this poller itself.
     * Once subscribed, this Flux will continue to poll for status until poll operation is done/complete.
     */
    private Flux<PollResponse<T>> fluxHandle;

    /*
     * Since constructor create a subscriber and start auto polling.
     * This handle will be used to dispose the subscriber when
     * client disable auto polling.
     */
    private Disposable fluxDisposable;

    /**
     * Create a {@link Poller} instance with poll interval and poll operation. The polling starts immediately by invoking {@code pollOperation}.
     * The next poll cycle will be defined by {@code retryAfter} value in {@link PollResponse}.
     * In absence of {@code retryAfter}, the {@link Poller} will use {@code pollInterval}.
     *
     * <p><strong>Code Sample - Create poller object</strong></p>
     * {@codesnippet com.azure.core.util.polling.poller.initialize.interval.polloperation}
     *
     * @param pollInterval Not-null and greater than zero poll interval.
     * @param pollOperation The polling operation to be called by the {@link Poller} instance. This is a callback into the client library,
     * which must never return {@code null}, and which must always have a non-null {@link OperationStatus}.
     *{@link Mono} returned from poll operation should never return {@link Mono#error(Throwable)}.If any unexpected scenario happens in poll operation,
     * it should be handled by client library and return a valid {@link PollResponse}. However if poll operation returns {@link Mono#error(Throwable)},
     * the {@link Poller} will disregard that and continue to poll.
     * @throws IllegalArgumentException if {@code pollInterval} is less than or equal to zero and if {@code pollInterval} or {@code pollOperation} are {@code null}
     */
    public Poller(Duration pollInterval, Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation) {
        if (pollInterval == null || pollInterval.toNanos() <= 0) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Null, negative or zero value for poll interval is not allowed."));
        }
        if (pollOperation == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Null value for poll operation is not allowed."));
        }

        this.pollInterval = pollInterval;
        this.pollOperation = pollOperation;
        this.pollResponse = new PollResponse<>(OperationStatus.NOT_STARTED, null);

        this.fluxHandle = asyncPollRequestWithDelay()
            .flux()
            .repeat()
            .takeUntil(pollResponse -> hasCompleted())
            .share();

        // auto polling start here
        this.fluxDisposable = fluxHandle.subscribe();
        this.autoPollingEnabled = true;
    }

    /**
     * Create a {@link Poller} instance with poll interval, poll operation and cancel operation. The polling starts immediately by invoking {@code pollOperation}.
     * The next poll cycle will be defined by retryAfter value in {@link PollResponse}.
     * In absence of {@link PollResponse#getRetryAfter()}, the {@link Poller} will use {@code pollInterval}.
     *
     * @param pollInterval Not-null and greater than zero poll interval.
     * @param pollOperation The polling operation to be called by the {@link Poller} instance. This is a callback into the client library,
     * which must never return {@code null}, and which must always have a non-null {@link OperationStatus}.
     *{@link Mono} returned from poll operation should never return {@link Mono#error(Throwable)}.If any unexpected scenario happens in poll operation,
     * it should handle it and return a valid {@link PollResponse}. However if poll operation returns {@link Mono#error(Throwable)},
     * the {@link Poller} will disregard that and continue to poll.
     * @param cancelOperation cancel operation if cancellation is supported by the service. It can be {@code null} which will indicate to the {@link Poller}
     * that cancel operation is not supported by Azure service.
     * @throws IllegalArgumentException if {@code pollInterval} is less than or equal to zero and if {@code pollInterval} or {@code pollOperation} are {@code null}
     */
    public Poller(Duration pollInterval, Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation, Consumer<Poller<T>> cancelOperation) {
        this(pollInterval, pollOperation);
        this.cancelOperation = cancelOperation;
    }

    /**
     * Attempts to cancel the long-running operation that this {@link Poller} represents. This is possible only if the service supports it,
     * otherwise an {@code UnsupportedOperationException} will be thrown.
     * <p>
     * It will call cancelOperation if status is {@link OperationStatus#IN_PROGRESS} otherwise it does nothing.
     *
     * @throws UnsupportedOperationException when cancel operation is not provided.
     */
    public void cancelOperation() throws UnsupportedOperationException {
        if (this.cancelOperation == null) {
            throw new UnsupportedOperationException("Cancel operation is not supported on this service/resource.");
        }

        // We can not cancel an operation if it was never started
        // It only make sense to call cancel operation if current status IN_PROGRESS.
        if (this.pollResponse != null && this.pollResponse.getStatus() != OperationStatus.IN_PROGRESS) {
            return;
        }

        //Time to call cancel
        this.cancelOperation.accept(this);
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
     * Enable user to take control of polling and trigger manual poll operation. It will call poll operation once.
     * This will not turn off auto polling.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p><strong>Manual Polling</strong></p>
     * <p>
     * {@codesnippet com.azure.core.util.polling.poller.poll.indepth}
     *
     * @return a Mono of {@link PollResponse} This will call poll operation once. The {@link Mono} returned here could be subscribed
     * for receiving {@link PollResponse} in async manner.
     */
    public Mono<PollResponse<T>> poll() {
        return this.pollOperation.apply(this.pollResponse)
            .doOnEach(pollResponseSignal -> {
                if (pollResponseSignal.get() != null) {
                    this.pollResponse = pollResponseSignal.get();
                }
            });
    }

    /**
     * Blocks execution and wait for polling to complete. The polling is considered complete based on status defined in {@link OperationStatus}.
     * <p>It will enable auto-polling if it was disable by user.
     *
     * @return returns final {@link PollResponse} when polling is complete as defined in {@link OperationStatus}.
     */
    public PollResponse<T> block() {
        if (!isAutoPollingEnabled()) {
            setAutoPollingEnabled(true);
        }
        return this.fluxHandle.blockLast();
    }

    /**
     * Blocks indefinitely until given {@link OperationStatus} is received.
     * @param statusToBlockFor The desired {@link OperationStatus} to block for and it can be any valid {@link OperationStatus} value.
     * @return {@link PollResponse} for matching desired status.
     * @throws IllegalArgumentException If {@code statusToBlockFor} is {@code null}.
     */
    public PollResponse<T> blockUntil(OperationStatus statusToBlockFor) {
        return blockUntil(statusToBlockFor, null);
    }

    /**
     * Blocks until given {@link OperationStatus} is received or a timeout expires if provided. A {@code null} {@code timeout} will cause to block indefinitely for desired status.
     * @param statusToBlockFor The desired {@link OperationStatus} to block for and it can be any valid {@link OperationStatus} value.
     * @param timeout The time after which it will stop blocking. A {@code null} value will cause to block indefinitely. Zero or negative are not valid values.
     * @return {@link PollResponse} for matching desired status to block for.
     * @throws IllegalArgumentException if {@code timeout} is zero or negative and if {@code statusToBlockFor} is {@code null}.
     */
    public PollResponse<T> blockUntil(OperationStatus statusToBlockFor, Duration timeout) {
        if (statusToBlockFor == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Null value for status is not allowed."));
        }
        if (timeout != null && timeout.toNanos() <= 0) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Negative or zero value for timeout is not allowed."));
        }
        if (!isAutoPollingEnabled()) {
            setAutoPollingEnabled(true);
        }
        if (timeout != null) {
            return this.fluxHandle.filter(tPollResponse -> matchStatus(tPollResponse, statusToBlockFor)).blockFirst(timeout);
        } else {
            return this.fluxHandle.filter(tPollResponse -> matchStatus(tPollResponse, statusToBlockFor)).blockFirst();
        }
    }

    /*
     * Indicate that the @{link PollResponse} matches with the status to block for.
     * @param currentPollResponse The poll response which we have received from the flux.
     * @param statusToBlockFor The {@link OperationStatus} to block and it can be any valid {@link OperationStatus} value.
     * @return True if the {@link PollResponse} return status matches the status to block for.
     */
    private boolean matchStatus(PollResponse<T> currentPollResponse,  OperationStatus statusToBlockFor) {
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
     * We expect Mono from pollOperation should never return Mono.error() . If any unexpected
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

    /*
     * We will use  {@link PollResponse#getRetryAfter} if it is greater than zero otherwise use poll interval.
     */
    private Duration getCurrentDelay() {
        return (this.pollResponse != null
            && this.pollResponse.getRetryAfter() != null
            && this.pollResponse.getRetryAfter().toNanos() > 0) ? this.pollResponse.getRetryAfter() : this.pollInterval;
    }

    /**
     * Controls whether auto-polling is enabled or disabled. Refer to the {@link Poller} class-level JavaDoc for more details on auto-polling.
     *
     * <p><strong>Disable auto polling</strong></p>
     * {@codesnippet com.azure.core.util.polling.poller.disableautopolling}
     *
     * <p><strong>Enable auto polling</strong></p>
     * {@codesnippet com.azure.core.util.polling.poller.enableautopolling}
     *
     * @param autoPollingEnabled If true, auto-polling will occur transparently in the background, otherwise it requires
     *                           manual polling by the user to get the latest state.
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

    /*
     * An operation will be considered complete if it is in one of the following state:
     * <ul>
     *     <li>SUCCESSFULLY_COMPLETED</li>
     *     <li>USER_CANCELLED</li>
     *     <li>FAILED</li>
     * </ul>
     * Also see {@link OperationStatus}
     * @return true if operation is done/complete.
     */
    private boolean hasCompleted() {
        return pollResponse != null && (pollResponse.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED
            || pollResponse.getStatus() == OperationStatus.FAILED
            || pollResponse.getStatus() == OperationStatus.USER_CANCELLED);
    }

    /*
     * Determine if this poller's internal subscriber exists and active.
     */
    private boolean activeSubscriber() {
        return (this.fluxDisposable != null && !this.fluxDisposable.isDisposed());
    }

    /**
     * Indicates if auto polling is enabled. Refer to the {@link Poller} class-level JavaDoc for more details on auto-polling.
     * @return A boolean value representing if auto-polling is enabled or not..
     */
    public boolean isAutoPollingEnabled() {
        return this.autoPollingEnabled;
    }

    /**
     * Current known status as a result of last poll event or last response from a manual polling.
     *
     * @return current status or {@code null} if no status is available.
     */
    public OperationStatus getStatus() {
        return this.pollResponse != null ? this.pollResponse.getStatus() : null;
    }
}
