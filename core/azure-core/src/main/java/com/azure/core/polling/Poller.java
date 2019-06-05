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
 * This is implementation for Long Running Operations.
 **/
public class Poller<T> {

    /*pollOperation is a Function that takes the previous PollResponse, and
    returns a new PollResponse to represent the current state*/
    private Function<PollResponse<T>, PollResponse<T>> pollOperation;

    /*Various configuration options to create poller object.*/
    private PollerOptions pollerOptions;

    /*This will save last poll response.*/
    private PollResponse<T> pollResponse;

    /*This will be called when cancel operation is triggered.*/
    private Consumer<Poller> cancelOperation;

    /* Indicate to poll automatically or not when poller is created.
     * default value is false;*/
    private boolean autoPolling = true;

    /*Indicate if cancel is initiated.*/
    private boolean cancelInitiated;

    private Flux<PollResponse<T>> fluxHandle;

    /*Since constructor create a subscriber and start autopoll.
     * This subscriber will be duplicate when client call subscriber.
     * Thus this handle will be used to dispose the subscriber when
     * client invoke poll function*/
    private Disposable fluxDisposable;

    /**
     * Create a Poller that is configured to auto-poll.
     *
     * @param pollerOptions configuration options for poller.
     * @param pollOperation This is the operation to be called.
     **/
    public Poller(PollerOptions pollerOptions,
                  Function<PollResponse<T>,
                      PollResponse<T>> pollOperation) {
        this.pollerOptions = pollerOptions;
        this.pollOperation = pollOperation;
        fluxHandle = createFlux();
        //autopolling  start here
        setStopPolling(false);
        fluxDisposable = fluxHandle.subscribe(ps -> pollResponse = ps);
    }

    /**
     * Constructor
     *
     * @param pollerOptions   poller options
     * @param pollOperation   poller operation
     * @param cancelOperation cancel operation
     **/
    public Poller(PollerOptions pollerOptions,
                  Function<PollResponse<T>,
                      PollResponse<T>> pollOperation,
                  Consumer<Poller> cancelOperation) {
        this(pollerOptions, pollOperation);
        this.cancelOperation = cancelOperation;
    }

    private Flux<PollResponse<T>> createFlux() {
        return sendPollRequestWithDelay()
            .repeat(repeatPollTimes())
            .takeUntil(pollResponse -> !isPollingStopped() && (pollResponse.status() == PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED
                || pollResponse.status() == PollResponse.OperationStatus.FAILED
                || pollResponse.status() == PollResponse.OperationStatus.USER_CANCELLED));
    }

    /*Calculate how many time we should repeat poll*/
    private int repeatPollTimes() {
        // Poller options will ensure that TimeoutInMilliSeconds > 0
        if (this.pollerOptions.getPollIntervalInMillis() > 0) {
            return this.pollerOptions.getTimeoutInMilliSeconds() / this.pollerOptions.getPollIntervalInMillis();
        } else {
            // Poller Options validation will ensure that poll interval is > 0
            // We should never reach here. This is extra safety guard.
            return this.pollerOptions.getTimeoutInMilliSeconds();
        }
    }

    /**
     * This will cancel polling from Azure Service if supported by service
     *
     * @throws UnsupportedOperationException when cancel operation is not suported.
     **/
    public void cancelOperation() throws UnsupportedOperationException {
        if (cancelOperation == null) {
            throw new UnsupportedOperationException("Cancel operation is not supported on this service/resource.");
        }
        //We can not cancel an operation if it was never started
        //or it is in its terminal state.
        //Check cancelInitiated: to protect against multiple time call to cancel operation
        if (cancelInitiated
            || (pollResponse != null && pollResponse.status() != PollResponse.OperationStatus.IN_PROGRESS)) {
            return;
        }
        //Time to call cancel
        cancelOperation.accept(this);
        cancelInitiated = true;
    }

    /**
     * This will poll and send PollResponse. If you had stopped polling erlier, we will enable polling again.
     *
     * @return Return poll response as Flux
     **/
    public Flux<PollResponse<T>> poll() {
        setStopPolling(false);
        if (fluxDisposable != null && !fluxDisposable.isDisposed()) {
            fluxDisposable.dispose();
        }
        return fluxHandle;
    }

    /**
     * This will block till poll operation is complete
     *
     * @return returns poll response
     **/
    public PollResponse<T> block() {
        return poll().blockLast();
    }

    /**
     * Get whether or not this PollStrategy's long running operation is done.
     *
     * @return Whether or not this PollStrategy's long running operation is done.
     */
    private Mono<PollResponse<T>> sendPollRequestWithDelay() {
        return Mono.defer(() -> delayAsync().then(Mono.defer(() -> {
            if (!isPollingStopped() && !isTerminalState()) {
                pollResponse = pollOperation.apply(pollResponse);
            } else if (!isTerminalState()) {
                return Mono.empty();
            }
            return Mono.just(pollResponse);
        })));
    }

    /*Helper function*/
    private boolean isTerminalState() {
        return pollResponse != null && (
            pollResponse.status() == PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED
                || pollResponse.status() == PollResponse.OperationStatus.FAILED
                || pollResponse.status() == PollResponse.OperationStatus.USER_CANCELLED);

    }

    /**
     * If this PollerOptions has a pollIntervalInMillis value, return an Mono that is delayed by the
     * number of seconds that are in the pollIntervalInMillis value. If this PollerOptions doesn't have
     * a pollIntervalInMillis value, then return an Single with no delay.
     *
     * @return A Mono with delay if this PollerOptions has a pollIntervalInMillis value.
     */
    private Mono<Void> delayAsync() {
        Mono<Void> result = Mono.empty();
        if (this.pollerOptions.getPollIntervalInMillis() > 0) {
            result = result.delaySubscription(Duration.ofMillis(
                (long) (this.pollerOptions.getPollIntervalInMillis() * this.pollerOptions.getPollIntervalGrowthFactor())
            ));
        }
        return result;
    }

    /**
     * This will stop polling
     **/
    public void stopPolling() {
        setStopPolling(true);
    }

    /**
     * Enable auto polling
     **/
    public void enablePolling() {
        setStopPolling(false);
    }

    private void setStopPolling(boolean stop) {
        this.autoPolling = !stop;
    }

    /**
     * @return true if polling is stopped.
     **/
    public boolean isPollingStopped() {
        return !this.autoPolling;
    }

    /**
     * @return status . Null if no status is available.
     **/
    public PollResponse.OperationStatus getStatus() {
        return pollResponse != null ? pollResponse.status() : null;
    }
}
