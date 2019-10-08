// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollResponse;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
 * based on status defined in {@link PollResponse}.
 *
 * @param <T> Type of poll response value
 * @see PollResponse
 */
public class Poller<T, R> {

    private final ClientLogger logger = new ClientLogger(Poller.class);
    /*
     * poll operation is a function that takes the previous PollResponse, and
     * returns a new Mono of PollResponse to represent the current state
     */
    private final Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation;

    /*
     * poll interval before next auto poll. This value will be used if the PollResponse does not include retryAfter from the service.
     */
    private final Duration pollInterval;

    /*
     * This will save last poll response.
     */
    private PollResponse<T> pollResponse;

    /*
     * This will be called when cancel operation is triggered.
     */
    private final Consumer<Poller<T, R>> cancelOperation;

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
    private final Flux<PollResponse<T>> fluxHandle;

    /*
     * Since constructor create a subscriber and start auto polling.
     * This handle will be used to dispose the subscriber when
     * client disable auto polling.
     */
    private Disposable fluxDisposable;

    private final Supplier<Mono<R>> fetchResultOperation;

    /**
     * Create a {@link Poller} instance with poll interval and poll operation. The polling starts immediately by invoking {@code pollOperation}.
     * The next poll cycle will be defined by {@code retryAfter} value in {@link PollResponse}.
     * In absence of {@code retryAfter}, the {@link Poller} will use {@code pollInterval}.
     *
     * @param pollInterval Not-null and greater than zero poll interval.
     * @param pollOperation The polling operation to be called by the {@link Poller} instance. This is a callback into the client library,
     * which must never return {@code null}, and which must always have a non-null status.
     * {@link Mono} returned from poll operation should never return {@link Mono#error(Throwable)}.If any unexpected scenario happens in poll operation,
     * it should be handled by client library and return a valid {@link PollResponse}. However if poll operation returns {@link Mono#error(Throwable)},
     * the {@link Poller} will disregard that and continue to poll.
     * @throws IllegalArgumentException if {@code pollInterval} is less than or equal to zero and if {@code pollInterval} or {@code pollOperation} are {@code null}
     */
    public Poller(Duration pollInterval, Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation) {
        this(pollInterval, pollOperation, null, null, null);
    }

    /**
     * Create a {@link Poller} instance with poll interval and poll operation. The polling starts immediately by invoking {@code pollOperation}.
     * The next poll cycle will be defined by {@code retryAfter} value in {@link PollResponse}.
     * In absence of {@code retryAfter}, the {@link Poller} will use {@code pollInterval}.
     *
     * @param pollInterval Not-null and greater than zero poll interval.
     * @param pollOperation The polling operation to be called by the {@link Poller} instance. This is a callback into the client library,
     * which must never return {@code null}, and which must always have a non-null status.
     * {@link Mono} returned from poll operation should never return {@link Mono#error(Throwable)}.If any unexpected scenario happens in poll operation,
     * it should be handled by client library and return a valid {@link PollResponse}. However if poll operation returns {@link Mono#error(Throwable)},
     * the {@link Poller} will disregard that and continue to poll.
     * @param activationOperation the operation to be called before polling begins. It can be {@code null} which will indicate to the {@link Poller}
     * that polling can begin straight away.
     * @param fetchResultOperation the operation to be called to fetch final result after polling has been completed.
     * @param cancelOperation cancel operation if cancellation is supported by the service. It can be {@code null} which will indicate to the {@link Poller}
     * that cancel operation is not supported by Azure service.
     * @throws IllegalArgumentException if {@code pollInterval} is less than or equal to zero and if {@code pollInterval} or {@code pollOperation} are {@code null}
     */
    public Poller(Duration pollInterval, Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation, Supplier<Mono<T>> activationOperation, Supplier<Mono<R>> fetchResultOperation, Consumer<Poller<T, R>> cancelOperation) {
        if (pollInterval == null || pollInterval.toNanos() <= 0) {
            logger.logExceptionAsError(new IllegalArgumentException("Null, negative or zero value for poll interval is not allowed."));
        }
        if (pollOperation == null) {
            logger.logExceptionAsError(new IllegalArgumentException("Null value for poll operation is not allowed."));
        }

        this.pollInterval = pollInterval;
        this.pollOperation = pollOperation;
        this.pollResponse = new PollResponse<>(PollResponse.OperationStatus.NOT_STARTED, null);
        this.fetchResultOperation = fetchResultOperation;

        this.fluxHandle = asyncPollRequestWithDelay()
            .flux()
            .repeat()
            .takeUntil(pollResponse -> hasCompleted())
            .share()
            .delaySubscription(activationOperation != null ? activationOperation.get() : Mono.empty());

        // auto polling start here
        this.fluxDisposable = fluxHandle.subscribe();
        this.autoPollingEnabled = true;
        this.cancelOperation = cancelOperation;
    }

    /**
     * Create a {@link Poller} instance with poll interval, poll operation and cancel operation. The polling starts immediately by invoking {@code pollOperation}.
     * The next poll cycle will be defined by retryAfter value in {@link PollResponse}.
     * In absence of {@link PollResponse#getRetryAfter()}, the {@link Poller} will use {@code pollInterval}.
     *
     * @param pollInterval Not-null and greater than zero poll interval.
     * @param pollOperation The polling operation to be called by the {@link Poller} instance. This is a callback into the client library,
     * which must never return {@code null}, and which must always have a non-null status.
     *{@link Mono} returned from poll operation should never return {@link Mono#error(Throwable)}.If any unexpected scenario happens in poll operation,
     * it should handle it and return a valid {@link PollResponse}. However if poll operation returns {@link Mono#error(Throwable)},
     * the {@link Poller} will disregard that and continue to poll.
     * @param fetchResultOperation the operation to be called to fetch final result after polling has been completed.
     * @param cancelOperation cancel operation if cancellation is supported by the service. It can be {@code null} which will indicate to the {@link Poller}
     * that cancel operation is not supported by Azure service.
     * @throws IllegalArgumentException if {@code pollInterval} is less than or equal to zero and if {@code pollInterval} or {@code pollOperation} are {@code null}
     */
    public Poller(Duration pollInterval, Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation, Supplier<Mono<R>> fetchResultOperation, Consumer<Poller<T, R>> cancelOperation) {
        this(pollInterval, pollOperation, null, fetchResultOperation, cancelOperation);
    }

    /**
     * Create a {@link Poller} instance with poll interval, poll operation and cancel operation. The polling starts immediately by invoking {@code pollOperation}.
     * The next poll cycle will be defined by retryAfter value in {@link PollResponse}.
     * In absence of {@link PollResponse#getRetryAfter()}, the {@link Poller} will use {@code pollInterval}.
     *
     * @param pollInterval Not-null and greater than zero poll interval.
     * @param pollOperation The polling operation to be called by the {@link Poller} instance. This is a callback into the client library,
     * which must never return {@code null}, and which must always have a non-null status.
     * {@link Mono} returned from poll operation should never return {@link Mono#error(Throwable)}.If any unexpected scenario happens in poll operation,
     * it should handle it and return a valid {@link PollResponse}. However if poll operation returns {@link Mono#error(Throwable)},
     * the {@link Poller} will disregard that and continue to poll.
     * @param fetchResultOperation the operation to be called to fetch final result after polling has been completed.
     * @param activationOperation the operation to be called before polling begins. It can be {@code null} which will indicate to the {@link Poller}
     * that polling can begin straight away.
     * @throws IllegalArgumentException if {@code pollInterval} is less than or equal to zero and if {@code pollInterval} or {@code pollOperation} are {@code null}
     */
    public Poller(Duration pollInterval, Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation, Supplier<Mono<R>> fetchResultOperation, Supplier<Mono<T>> activationOperation) {
        this(pollInterval, pollOperation, activationOperation, fetchResultOperation, null);
    }

    /**
     * Attempts to cancel the long-running operation that this {@link Poller} represents. This is possible only if the service supports it,
     * otherwise an {@code UnsupportedOperationException} will be thrown.
     * <p>
     * It will call cancelOperation if status is 'In Progress' otherwise it does nothing.
     *
     * @throws UnsupportedOperationException when cancel operation is not provided.
     */
    public void cancelOperation() throws UnsupportedOperationException {
        if (this.cancelOperation == null) {
            logger.logExceptionAsError(new UnsupportedOperationException("Cancel operation is not supported on this service/resource."));
        }

        // We can not cancel an operation if it was never started
        // It only make sense to call cancel operation if current status IN_PROGRESS.
        if (this.pollResponse != null && this.pollResponse.getStatus() != PollResponse.OperationStatus.IN_PROGRESS) {
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
     * Blocks execution and wait for polling to complete. The polling is considered complete based on status defined in {@link PollResponse}.
     * <p>It will enable auto-polling if it was disable by user.
     *
     * @return returns final {@link PollResponse} when polling is complete.
     */
    public R block() {
        if (!isAutoPollingEnabled()) {
            setAutoPollingEnabled(true);
        }
        this.fluxHandle.blockLast();
        return result().block();
    }

    public Mono<R> result() {
        if (!getStatus().equals(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED)) {
            return Mono.error(new IllegalAccessException("The poll operation has not successfully completed."));
        }
        return fetchResultOperation.get();
    }

    /**
     * Blocks execution and wait for polling to complete. The polling is considered complete based on status defined in {@link PollResponse}.
     * <p>It will enable auto-polling if it was disable by user.
     *
     * @param timeout the duration for which the excecution is blocked and waits for polling to complete.
     * @return returns final {@link PollResponse} when polling is complete.
     */
    public R block(Duration timeout) {
        if (!isAutoPollingEnabled()) {
            setAutoPollingEnabled(true);
        }
        this.fluxHandle.blockLast(timeout);
        return result().block();
    }

    /**
     * Blocks indefinitely until given {@code statusToBlockFor} is received.
     * @param statusToBlockFor The desired status to block for.
     * @return {@link PollResponse} for matching desired status.
     * @throws IllegalArgumentException If {@code statusToBlockFor} is {@code null}.
     */
    public PollResponse<T> blockUntil(PollResponse.OperationStatus statusToBlockFor) {
        return blockUntil(statusToBlockFor, null);
    }

    /**
     * Blocks until given {@code statusToBlockFor} is received or a timeout expires if provided. A {@code null} {@code timeout} will cause to block indefinitely for desired status.
     * @param statusToBlockFor The desired status to block for.
     * @param timeout The time after which it will stop blocking. A {@code null} value will cause to block indefinitely. Zero or negative are not valid values.
     * @return {@link PollResponse} for matching desired status to block for.
     * @throws IllegalArgumentException if {@code timeout} is zero or negative and if {@code statusToBlockFor} is {@code null}.
     */
    public PollResponse<T> blockUntil(PollResponse.OperationStatus statusToBlockFor, Duration timeout) {
        if (statusToBlockFor == null) {
            logger.logExceptionAsError(new IllegalArgumentException("Null value for status is not allowed."));
        }
        if (timeout != null && timeout.toNanos() <= 0) {
            logger.logExceptionAsError(new IllegalArgumentException("Negative or zero value for timeout is not allowed."));
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
    private boolean matchStatus(PollResponse<T> currentPollResponse,  PollResponse.OperationStatus statusToBlockFor) {
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
        return pollResponse != null && (pollResponse.getStatus() == PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED
            || pollResponse.getStatus() == PollResponse.OperationStatus.FAILED
            || pollResponse.getStatus() == PollResponse.OperationStatus.USER_CANCELLED);
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
    public PollResponse.OperationStatus getStatus() {
        return this.pollResponse != null ? this.pollResponse.getStatus() : null;
    }
}
