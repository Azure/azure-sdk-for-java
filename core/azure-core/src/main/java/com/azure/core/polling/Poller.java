package com.azure.core.polling;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;


public class Poller<T> {

    private static final long serialversionUID = 139448132L;

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

    private Flux<PollResponse<T>> fluxAtHand;

    /**
     * Create a Poller that is configured to auto-poll.
     *
     * @param pollerOptions .
     * @param pollOperation
     **/
    public Poller(PollerOptions pollerOptions,
                  Function<PollResponse<T>,
                      PollResponse<T>> pollOperation) {
        this.pollerOptions = pollerOptions;
        this.pollOperation = pollOperation;
    }

    /**
     * @param pollerOptions
     * @param pollOperation
     * @param cancelOperation
     **/
    public Poller(PollerOptions pollerOptions,
                  Function<PollResponse<T>,
                      PollResponse<T>> pollOperation,
                  Consumer<Poller> cancelOperation) {
        this(pollerOptions, pollOperation);
        this.cancelOperation = cancelOperation;
    }

    /*public boolean isDone() {
        return pollResponse != null && pollResponse.isDone();
    }*/

    /**
     * This will cancel polling from Azure Service if supported by service
     *
     * @throws UnsupportedOperationException
     **/
    public void cancelOperation() throws UnsupportedOperationException {
        if (cancelOperation == null)
            throw new UnsupportedOperationException("Cancel operation is not supported on this service/resource.");

        //We can not cancel an operation if it was never started
        //or it is in its terminal state.
        if (pollResponse == null || pollResponse.status() != PollResponse.OperationStatus.IN_PROGRESS) {
            return;
        }
        cancelOperation.accept(this);
    }

    //TODO : Make sure we do not pool every cpu cycle. Polling must be throttle by parameter defined in PollingType i.e interval or expeonential polling

    /**
     * This will poll once. If you had stopped polling erlier, we will enable polling again.
     **/
    public Flux<PollResponse<T>> poll() {
        setStopPolling(false);
        if (fluxAtHand == null) {
            fluxAtHand = sendPollRequestWithDelay()
                .repeat(this.pollerOptions.getTimeoutInMilliSeconds() / this.pollerOptions.getPollIntervalInMillis())
                .takeUntil(pollResponse -> !isPollingStopped() && (pollResponse.status() == PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED ||
                    pollResponse.status() == PollResponse.OperationStatus.FAILED ||
                    pollResponse.status() == PollResponse.OperationStatus.USER_CANCELLED));
        }
        return fluxAtHand;
    }

    public Flux<PollResponse<T>> block() {
        return Flux.just(poll().blockLast());
    }


    /**
     * Get whether or not this PollStrategy's long running operation is done.
     *
     * @return Whether or not this PollStrategy's long running operation is done.
     */


    Mono<PollResponse<T>> sendPollRequestWithDelay() {
        return Mono.defer(() -> delayAsync().then(Mono.defer(() -> {
            if (!isPollingStopped()) {
                pollResponse = pollOperation.apply(pollResponse);
            } else {
                return Mono.empty();
            }
            return Mono.just(pollResponse);
        })));
    }

    /**
     * If this PollerOptions has a pollIntervalInMillis value, return an Mono that is delayed by the
     * number of seconds that are in the pollIntervalInMillis value. If this PollerOptions doesn't have
     * a pollIntervalInMillis value, then return an Single with no delay.
     *
     * @return A Mono with delay if this PollerOptions has a pollIntervalInMillis value.
     */
    Mono<Void> delayAsync() {
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

    private void setStopPolling(boolean stop) {
        this.autoPolling = !stop;
    }

    public boolean isPollingStopped() {
        return !this.autoPolling;
    }

    public PollResponse.OperationStatus getStatus() {
        return pollResponse != null ? pollResponse.status() : null;
    }
}
