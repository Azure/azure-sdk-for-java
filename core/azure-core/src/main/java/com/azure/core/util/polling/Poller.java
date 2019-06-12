// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import reactor.core.Disposable;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class provides implementation of long running operations. The poller starts polling <b>automatically</b> when Poller instance is created.
 * It uses {@link Flux} from reactive programming model to achieve auto polling.
 * It has function for usual operation of a poller. For example listen/observe poll responses, enable/disable auto polling,
 * manual polling, wait for polling to complete and get status of current polling.
 *
 * <p><strong>Code Samples</strong></p>
 *
 * <p><strong>Instantiating and Subscribing to Poll Response</strong></p>
 *
 * {@codesnippet com.azure.core.util.polling.poller.instantiationAndSubscribe}
 *
 * <p><strong>Wait/Block for Polling to complete</strong></p>
 * {@codesnippet com.azure.core.util.polling.poller.block}
 *
 * <p><strong>Disable auto polling and polling manually</strong></p>
 * {@codesnippet com.azure.core.util.polling.poller.poll}
 *
 * <p><strong>Implementation of Long Running Operations</strong></p>
 *
 * @param <T> type of poll response value
 *
 * @see PollResponse
 * @see com.azure.core.util.polling.PollResponse.OperationStatus
 */

public class Poller<T> {

    /*
     * pollOperation is a Function that takes the previous PollResponse, and
     * returns a new Mono of PollResponse to represent the current state
     */
    private final Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation;

    /*
     * poll interval before next auto poll.
     */
    private Duration pollInterval;

    /*
     * This will save last poll response.
     */
    private PollResponse<T> pollResponse;

    /*
     * This will be called when cancel operation is triggered.
     */
    private Consumer<Poller> cancelOperation;

    /*
     * Indicate to poll automatically or not when poller is created.
     * default value is false;
     */
    private boolean autoPollingEnabled;

    /* hold Flux of PollResponse<T>*/
    private Flux<PollResponse<T>> fluxHandle;

    /*
     * Since constructor create a subscriber and start auto polling.
     * This handle will be used to dispose the subscriber when
     * client disable auto polling.
     */
    private Disposable fluxDisposable;

    /**
     * Create a Poller object with poll interval and poll operation. The polling starts immediately by default and it will invoke pollOperation.
     * The next poll cycle will be defined by retryAfter value in {@link PollResponse}.
     * In absence of retryAfter, the poller will use pollInterval.
     *
     * @param pollInterval Not null  and greater than zero poll interval. It ensure that polling happens only once in given pollInterval.
     * @param pollOperation to be called by poller. It should not return {@code null}. The response should always have valid {@link com.azure.core.util.polling.PollResponse.OperationStatus}
     * @throws NullPointerException If {@code pollerOptions} is {@code null} for {@code  pollInterval pollOperation}.
     * @throws IllegalArgumentException if {@code  pollInterval} is negative or zero
     */
    public Poller(Duration pollInterval, Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation) {

        Objects.requireNonNull(pollInterval, "The poll interval input parameter cannot be null.");
        if (pollInterval.toNanos() <= 0) {
            throw new IllegalArgumentException("Negative or zero poll interval not allowed.");
        }
        Objects.requireNonNull(pollOperation, "The poll operation input parameter cannot be null.");

        this.pollInterval = pollInterval;
        this.pollOperation = pollOperation;
        this.pollResponse = new PollResponse<>(PollResponse.OperationStatus.NOT_STARTED, null);

        this.fluxHandle = asyncPollRequestWithDelay()
            .flux()
            .repeat()
            .takeUntil(pollResponse -> pollResponse != null && pollResponse.isDone())
            .share();

        // auto polling start here
        this.fluxDisposable = fluxHandle.subscribe();
        this.autoPollingEnabled = true;
    }

    /**
     * Create a Poller object with cancel operation. The polling starts immediately by default and invoke pollOperation.
     *
     * @param pollInterval Not null poll interval. It ensure that polling happens only once in given pollInterval.
     * @param pollOperation   to be called by poller. User should never return {@code null}. The response should have valid {@link com.azure.core.util.polling.PollResponse.OperationStatus}
     * @param cancelOperation cancel operation
     * @throws NullPointerException If {@code pollerOptions pollOperation} is {@code null}.
     */
    public Poller(Duration pollInterval, Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation, Consumer<Poller> cancelOperation) {
        this(pollInterval, pollOperation);
        this.cancelOperation = cancelOperation;
    }

    /**
     * Calls Cancel Operation function if provided.
     * It will only call cancelOperation if {@link com.azure.core.util.polling.PollResponse.OperationStatus} is IN_PROGRESS otherwise it does nothing.
     *
     * @throws UnsupportedOperationException when cancel operation is not provided.
     */
    public void cancelOperation() throws UnsupportedOperationException {
        if (cancelOperation == null) {
            throw new UnsupportedOperationException("Cancel operation is not supported on this service/resource.");
        }

        // We can not cancel an operation if it was never started
        // It only make sense to call cancel operation if current status IN_PROGRESS.
        if (pollResponse != null && pollResponse.getStatus() != PollResponse.OperationStatus.IN_PROGRESS) {
            return;
        }
        //Time to call cancel
        this.cancelOperation.accept(this);
    }

    /**
     * Enable user to subscribe to {@link Flux} and listen to every {@link PollResponse} asynchronously.
     * The user will start receiving PollResponse when client subscribe to this Flux.
     * The poller could still have its own auto polling in action unless user has turned off
     * auto polling.
     *
     * @return poll response as Flux that can be subscribed.
     */
    public Flux<PollResponse<T>> getObserver() {
        return this.fluxHandle;
    }

    /**
     * Enable user to take control of polling and trigger manual poll operation. It will perform one call to poll operation.
     * This will not turn off auto polling.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p><strong>Manual Polling</strong></p>

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
     * Blocks execution and wait for polling to complete. The polling is complete/done based on status defined in {@link com.azure.core.util.polling.PollResponse.OperationStatus}.
     * Auto polling must be turned on for poller to continuously poll in background.
     *
     * @return returns final poll response when polling is done.
     */
    public PollResponse<T> block() {
        return this.fluxHandle.blockLast();
    }

    /*
     * This function will apply delay and call poll operation function async.
     * @return mono of poll response
     */
    private Mono<PollResponse<T>> asyncPollRequestWithDelay() {
        return  Mono.defer(() -> this.pollOperation.apply(this.pollResponse)
            .delaySubscription(getCurrentDelay())
            .doOnEach(pollResponseSignal -> {
                if (pollResponseSignal.get() != null) {
                    this.pollResponse = pollResponseSignal.get();
                }
            }));
            
    }

    private Duration getCurrentDelay() {
        return (this.pollResponse != null && this.pollResponse.getRetryAfter() != null) ? this.pollResponse.getRetryAfter() : this.pollInterval;
    }

    /**
     * Provide control to turn auto polling <strong>on or off</strong>. Once auto polling is turned off, it is <strong>user's responsibility</strong>
     * to turn it back on.
     *
     * <p><strong>Disable auto polling</strong></p>
     * {@codesnippet com.azure.core.util.polling.poller.disableautopolling}
     *
     * <p><strong>Enable auto polling</strong></p>
     * {@codesnippet com.azure.core.util.polling.poller.enableautopolling}
     *
     * @param autoPollingEnabled true  Ensures the polling is happening in background.
     * false Ensures that polling is <strong>not</strong> happening in background.
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
     * Determine if this poller's internal subscriber exists and  still active.
     */
    private boolean activeSubscriber() {
        return (this.fluxDisposable != null && !this.fluxDisposable.isDisposed());
    }

    /**
     * Indicate if auto polling is on/off . By default auto polling is turned <strong>on</strong>.
     *
     * @return false if polling is stopped. true if auto polling is enabled.
     */
    public boolean isAutoPollingEnabled() {
        return this.autoPollingEnabled;
    }

    /**
     * Current known status as a result of last poll event. If auto polling was disabled by user, this will represent
     * old response when auto polling was enabled.
     * @return current status {@code null} if no status is available.
     */
    public PollResponse.OperationStatus getStatus() {
        return this.pollResponse != null ? this.pollResponse.getStatus() : null;
    }
}
