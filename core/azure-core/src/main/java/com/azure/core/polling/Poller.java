// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.polling;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class provides implementation of long running operations. The poller starts polling by default in background.
 * It has function for normal operation of poller. For example listen to poll responses, stop auto polling,
 * manual polling, wait for polling to complete and get status of current polling.
 * <p>
 *     Since auto polling is turned on by default. If some scenario requires to disable this feature.
 *     It can be done by calling setAutoPollingEnabled(false) function.
 *
 * <p><strong>Implementation of Long Running Operations</strong></p>
 *
 * @param <T>
 */

public class Poller<T> {

    /*
     * pollOperation is a Function that takes the previous PollResponse, and
     * returns a new PollResponse to represent the current state
     */
    private Function<PollResponse<T>, PollResponse<T>> pollOperation;

    /*
     * Various configuration options to create poller object.
     */
    private PollerOptions pollerOptions;

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

    /*
     * Indicate if cancel is initiated.
     */
    private boolean cancelInitiated;

    private Flux<PollResponse<T>> backgroundFluxHandle;

    /*
     * Since constructor create a subscriber and start auto polling.
     * This handle will be used to dispose the subscriber when
     * client disable auto polling.
     */
    private Disposable backgroundFluxDisposable;

    /**
     * Create a Poller object. Auto polling is turned on by default. The background thread will start immediately
     * and invoke pollOperation.
     * @param pollerOptions configuration options for poller.
     * @param pollOperation to be called by poller. User should never return {@code null}. The response should have valid {@link com.azure.core.polling.PollResponse.OperationStatus}
     */
    public Poller(PollerOptions pollerOptions, Function<PollResponse<T>, PollResponse<T>> pollOperation) {

        this.pollerOptions = pollerOptions;
        this.pollOperation = pollOperation;
        backgroundFluxHandle = createBackgroundFlux();
        pollResponse = new PollResponse<>(PollResponse.OperationStatus.NOT_STARTED, null);

        // auto polling start here
        setAutoPollingEnabled(true);
    }

    /**
     * Create a Poller object. Auto polling is turned on by default. The background thread will start immediately
     * and invoke pollOperation.
     * @param pollerOptions configuration options for poller.
     * @param pollOperation to be called by poller. User should never return {@code null}. The response should have valid {@link com.azure.core.polling.PollResponse.OperationStatus}
     * @param cancelOperation cancel operation
     */
    public Poller(PollerOptions pollerOptions, Function<PollResponse<T>, PollResponse<T>> pollOperation, Consumer<Poller> cancelOperation) {
        this(pollerOptions, pollOperation);
        this.cancelOperation = cancelOperation;
    }

    /**
     * This will call cancelOperation function if provided. Once cancelOperation is triggered successfully, it can not be called again.
     * This is to avoid unintentional calls to cancelOperation.
     * It will not call cancelOperation if operation status is not started/Cancelled/Failed/successfully completed.
     *
     * @see com.azure.core.polling.PollResponse.OperationStatus
     * @throws UnsupportedOperationException when cancel operation is not provided.
     */
    public void cancelOperation() throws UnsupportedOperationException {
        if (cancelOperation == null) {
            throw new UnsupportedOperationException("Cancel operation is not supported on this service/resource.");
        }

        // We can not cancel an operation if it was never started
        // or it is in its terminal state.
        // Check cancelInitiated: to protect against multiple time call to cancel operation
        if (cancelInitiated || (pollResponse != null && pollResponse.getStatus() != PollResponse.OperationStatus.IN_PROGRESS)) {
            return;
        }
        //Time to call cancel
        cancelOperation.accept(this);
        cancelInitiated = true;
    }
    /**
     * Enable user to subscribe and receive all the responses.
     * The user will start receiving PollResponse When client subscribe to this Flux.
     * This Flux will not trigger a call to PollOperation.
     * The poller still have its own default polling in action unless, user has  turned off
     * auto polling.
     *
     * @return poll response as Flux that can be subscribed.
     */
    public Flux<PollResponse<T>> getObserver() {
        return listenPollRequestWithDelay()
            .flux()
            .repeat()
            .takeUntil(pollResponse -> pollResponse.isDone());
    }

    /*
     * We will maintain one instance of backgroundFluxHandle per poller.
     * This handle will be set to null when autopolling is disabled.
     */
    private Flux<PollResponse<T>> createBackgroundFlux() {
        if (backgroundFluxHandle == null) {
            backgroundFluxHandle = sendPollRequestWithDelayBackground()
                .flux()
                .repeat()
                .takeUntil(pollResponse -> pollResponse.isDone());
        }
        return backgroundFluxHandle;
    }

    /**
     * Calls poll operation once in sync if Pool operation is not completed.
     * @return a Mono of {@link PollResponse}
     */
    public Mono<PollResponse<T>> poll() {

        if (!isTerminalState()) {
            updatePollOperationSync();
        }
        return Mono.just(pollResponse);
    }

    /**
     * This will block till poll operation is complete
     * @return returns poll response
     */
    public PollResponse<T> block() {
        return backgroundFluxHandle.blockLast();
    }

    /*
     * Calls poll operation function and update pollResponse.
     */
    private  void updatePollOperationSync() {
        pollResponse = pollOperation.apply(pollResponse);
    }

    /*
     * Get whether or not this PollStrategy's long running operation is done.
     * @return Whether or not this PollStrategy's long running operation is done.
     */
    private Mono<PollResponse<T>> sendPollRequestWithDelayBackground() {
        return Mono.defer(() -> delayAsync().then(Mono.defer(() -> {
            if (!isTerminalState()) {
                updatePollOperationSync();
            } else if (!isTerminalState()) {
                return Mono.empty();
            }
            return Mono.just(pollResponse);
        })));
    }

    private Mono<PollResponse<T>> listenPollRequestWithDelay() {
        return Mono.defer(() -> delayAsync().then(Mono.defer(() -> {
            return Mono.just(pollResponse);
        })));
    }

    /*
     * Helper function
     */
    private boolean isTerminalState() {
        return pollResponse != null && pollResponse.isDone();
    }

    /*
     * If this PollerOptions has a pollIntervalInMillis value, return an Mono that is delayed by the
     * number of seconds that are in the pollIntervalInMillis value. If this PollerOptions doesn't have
     * a pollIntervalInMillis value, then return an Single with no delay.
     *
     * @return A Mono with delay if this PollerOptions has a pollIntervalInMillis value.
     */
    private Mono<Void> delayAsync() {
        Mono<Void> result = Mono.empty();
        if (getCurrentDelay().toNanos() > 0) {
            result = result.delaySubscription(this.pollerOptions.pollInterval());
        }
        return result;
    }

    private Duration getCurrentDelay() {
        return ((pollResponse != null && pollResponse.getRetryAfter() != null) ?  pollResponse.getRetryAfter() :  this.pollerOptions.pollInterval());
    }

    /**
     * Function to tun auto poll on or off. Once auto polling is turned off, it is user's responsibility
     *  to turn it back on.
     *
     * @param  autoPollingEnabled  true  Ensures the polling is happening in background.
     *                             false  Ensures that polling is not happening in background.
     */
    public void setAutoPollingEnabled(boolean autoPollingEnabled) {
        // setting same auto polling status would not require any action.
        if (this.autoPollingEnabled == autoPollingEnabled) {
            return;
        }

        this.autoPollingEnabled = autoPollingEnabled;
        if (this.autoPollingEnabled) {

            if (backgroundFluxHandle == null) {
                backgroundFluxHandle = createBackgroundFlux();
            }
            if (!activeSubscriber()) {
                backgroundFluxDisposable = backgroundFluxHandle.subscribe(pr ->  pollResponse = pr);
            }
        } else {
            if (activeSubscriber()) {
                backgroundFluxDisposable.dispose();
            }
            backgroundFluxHandle = null;
        }
    }

    /*
     * Determine if subscriber exists and  still active.
     */
    private boolean activeSubscriber() {
        return (backgroundFluxHandle != null && backgroundFluxDisposable != null && !backgroundFluxDisposable.isDisposed());
    }

    /**
     * @return true if polling is stopped.
     */
    public boolean isAutoPollingEnabled() {
        return this.autoPollingEnabled;
    }

    /**
     * @return status {@code null} if no status is available.
     */
    public PollResponse.OperationStatus getStatus() {
        return pollResponse != null ? pollResponse.getStatus() : null;
    }
}
