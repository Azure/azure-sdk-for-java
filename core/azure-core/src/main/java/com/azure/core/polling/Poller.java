// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.polling;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This is implementation for Long Running Operations.
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

    private Flux<PollResponse<T>> fluxHandle;

    /*
     * Since constructor create a subscriber and start auto polling.
     * This handle will be used to dispose the subscriber when
     * client disable auto polling.
     */
    private Disposable fluxDisposable;

    /**
     * Create a Poller object. Auto polling is turned on by default.
     * @param pollerOptions configuration options for poller.
     * @param pollOperation This is the operation to be called.
     */
    public Poller(PollerOptions pollerOptions, Function<PollResponse<T>, PollResponse<T>> pollOperation) {

        this.pollerOptions = pollerOptions;
        this.pollOperation = pollOperation;
        fluxHandle = createFlux();

        // auto polling  start here
        pollResponse = new PollResponse<>(PollResponse.OperationStatus.NOT_STARTED, null);
        setAutoPollingEnabled(true);
    }

    /**
     * Constructor
     * @param pollerOptions poller options
     * @param pollOperation poller operation
     * @param cancelOperation cancel operation
     */
    public Poller(PollerOptions pollerOptions, Function<PollResponse<T>, PollResponse<T>> pollOperation, Consumer<Poller> cancelOperation) {
        this(pollerOptions, pollOperation);
        this.cancelOperation = cancelOperation;
    }

    /*
     * We will maintain one instance of fluxHandle per poller.
     * This handle will be set to null when autopolling is disabled.
     */
    private Flux<PollResponse<T>> createFlux() {
        if (fluxHandle == null) {
            fluxHandle = sendPollRequestWithDelay()
                .flux()
                .repeat()
                .takeUntil(pollResponse -> pollResponse.isDone());
        }
        return fluxHandle;
    }

    /**
     * This will cancel polling from Azure Service if supported by service
     * @throws UnsupportedOperationException when cancel operation is not suported.
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
     * Enable client to subscribe and receive all the responses.
     * The client will start receiving PollResponse When client subscribe to this Flux.
     * The poller still have its own default polling in action unless, client has  turned off
     * auto polling.
     * It is recommended to turn off Auto polling when client want to subscribe to this Flux.
     *
     * @return Returns poll response as Flux that can be subscribed.
     */
    public Flux<PollResponse<T>> getObserver() {
         createFlux();
         return fluxHandle;
    }

    /**
     * Calls poll operation once in sync.
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
        return getObserver().blockLast();
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
    private Mono<PollResponse<T>> sendPollRequestWithDelay() {
        return Mono.defer(() -> delayAsync().then(Mono.defer(() -> {
            if (!isTerminalState()) {
                updatePollOperationSync();
            } else if (!isTerminalState()) {
                return Mono.empty();
            }
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
        if (this.pollerOptions.pollInterval().toNanos() > 0) {
            result = result.delaySubscription(this.pollerOptions.pollInterval());
        }
        return result;
    }

    /**
     *
     * @param  autoPollingEnabled  true : Ensures the polling is happening in background.
     *                             false : Ensures that polling is not happening in background.
     */
    public void setAutoPollingEnabled(boolean autoPollingEnabled) {
    	
    	// setting same auto polling status would not require any action.
    	
    	if (this.autoPollingEnabled == autoPollingEnabled ) {
    		return;
    	}

    	this.autoPollingEnabled = autoPollingEnabled;
        if (this.autoPollingEnabled) {
            if (fluxHandle != null && !activeSubscriber()) {
                fluxDisposable = fluxHandle.subscribe(pr ->  pollResponse = pr );
            }
        } else {
            if (activeSubscriber()) {
                fluxDisposable.dispose();
            }
            fluxHandle = null;
        }
    }

    /*
     * Determine if subscriber exists and  still active.
     */
    private boolean activeSubscriber() {
    	return (fluxDisposable != null && fluxDisposable.isDisposed());
    }

    /**
     * @return true if polling is stopped.
     */
    public boolean isAutoPollingEnabled() {
        return this.autoPollingEnabled;
    }

    /**
     * @return status . Null if no status is available.
     */
    public PollResponse.OperationStatus getStatus() {
        return pollResponse != null ? pollResponse.getStatus() : null;
    }
}
