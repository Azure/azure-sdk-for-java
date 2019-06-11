// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.polling;

import reactor.core.Disposable;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class provides implementation of long running operations. The poller starts polling <b>automatically in background</b>.
 * It uses {@link Flux} from reactive programming model and subscribe to achieve auto polling.
 * It has function for usual operation of a poller. For example listen to poll responses, enable/disable auto polling,
 * manual polling, wait for polling to complete and get status of current polling.
 * <p>
 * Since auto polling is turned <b>on</b> by default. If some scenario requires to disable this feature.
 * It can be done by following code
 * <pre><code>
 *     myPollerInstance.setAutoPollingEnabled(false)
 * </code></pre>
 * <p><strong>Implementation of Long Running Operations</strong></p>
 *
 * @param <T> type of poll response value
 *
 * @see PollerOptions
 * @see PollResponse
 * @see com.azure.core.polling.PollResponse.OperationStatus
 */

public class Poller<T> {

    /*
     * pollOperation is a Function that takes the previous PollResponse, and
     * returns a new Mono of PollResponse to represent the current state
     */
    private final Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation;

    /*
     * Various configuration options to create poller object.
     */
    private final PollerOptions pollerOptions;

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
     * Create a Poller object. The polling starts immediately by default and invoke pollOperation.
     * The poll interval would defined by retryAfter value in {@link PollResponse}.
     * In absence of retryAfter, the poller will use pollInterval defined in {@link PollerOptions}.
     *
     * @param pollerOptions Not null configuration options for poller.
     * @param pollOperation to be called by poller. It should not return {@code null}. The response should always have valid {@link com.azure.core.polling.PollResponse.OperationStatus}
     */
    public Poller(PollerOptions pollerOptions, Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation) {

        Objects.requireNonNull(pollerOptions, "The PollerOptions input parameter cannot be null.");
        Objects.requireNonNull(pollOperation, "The poll operation input parameter cannot be null.");

        this.pollerOptions = pollerOptions;
        this.pollOperation = pollOperation;
        pollResponse = new PollResponse<>(PollResponse.OperationStatus.NOT_STARTED, null);

        fluxHandle = asyncPollRequestWithDelay()
            .flux()
            .repeat()
            .takeUntil(pollResponse -> pollResponse != null && pollResponse.isDone())
            .share();

        // auto polling start here
        fluxDisposable = fluxHandle.subscribe();
        autoPollingEnabled = true;
    }

    /**
     * Create a Poller object. The polling starts immediately by default and invoke pollOperation.
     *
     * @param pollerOptions   configuration options for poller.
     * @param pollOperation   to be called by poller. User should never return {@code null}. The response should have valid {@link com.azure.core.polling.PollResponse.OperationStatus}
     * @param cancelOperation cancel operation
     */
    public Poller(PollerOptions pollerOptions, Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation, Consumer<Poller> cancelOperation) {
        this(pollerOptions, pollOperation);
        this.cancelOperation = cancelOperation;
    }

    /**
     * This will call cancelOperation function if provided. Once cancelOperation is triggered successfully, it can not be called again.
     * This is to avoid unintentional calls to cancelOperation.
     * It will only call cancelOperation if {@link com.azure.core.polling.PollResponse.OperationStatus} is IN_PROGRESS.
     *
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
     * Enable user to subscribe and listen on all the poll responses.
     * The user will start receiving PollResponse when client subscribe to this Flux.
     * The poller still have its own auto polling in action unless user has turned off
     * auto polling.
     *
     * @return poll response as Flux that can be subscribed.
     */
    public Flux<PollResponse<T>> getObserver() {
        return fluxHandle;
    }

    /**
     * This is function for manual poll. It will perform one poll.
     *
     * @return a Mono of {@link PollResponse}
     */
    public Mono<PollResponse<T>> poll() {
        return pollOperation.apply(pollResponse)
            .doOnEach(pollResponseSignal -> {
                if (pollResponseSignal.get() != null) pollResponse = pollResponseSignal.get();
            });
    }

    /**
     * This will block till poll operation is complete.
     *
     * @return returns last poll response.
     */
    public PollResponse<T> block() {
        return fluxHandle.blockLast();
    }

    /*
     * This function will apply delay and call poll operation function async.
     * @return mono of poll response
     */
    private Mono<PollResponse<T>> asyncPollRequestWithDelay() {
        return  Mono.defer(() -> pollOperation.apply(pollResponse)
            .delaySubscription(getCurrentDelay())
            .doOnEach(pollResponseSignal -> {
                if (pollResponseSignal.get() != null) {
                    pollResponse = pollResponseSignal.get();
                }
            }));
    }

    private Duration getCurrentDelay() {
        return ((pollResponse != null && pollResponse.getRetryAfter() != null) ? pollResponse.getRetryAfter() : this.pollerOptions.pollInterval());
    }

    /**
     * Function to tun auto poll on or off. Once auto polling is turned off, it is user's responsibility
     * to turn it back on.
     *
     * @param autoPollingEnabled true  Ensures the polling is happening in background.
     *                           false  Ensures that polling is not happening in background.
     */
    public final void setAutoPollingEnabled(boolean autoPollingEnabled) {

        this.autoPollingEnabled = autoPollingEnabled;
        if (this.autoPollingEnabled) {
            if (!activeSubscriber()) {
                fluxDisposable = fluxHandle.subscribe(pr -> pollResponse = pr);
            }
        } else {
            if (activeSubscriber()) {
                fluxDisposable.dispose();
            }
        }
    }

    /*
     * Determine if this poller's internal subscriber exists and  still active.
     */
    private boolean activeSubscriber() {
        return (fluxDisposable != null && !fluxDisposable.isDisposed());
    }

    /**
     * Indicate if auto polling is on/off . By default auto polling is turned on.
     *
     * @return false if polling is stopped.
     */
    public boolean isAutoPollingEnabled() {
        return this.autoPollingEnabled;
    }

    /**
     * Current known status as a result of last poll event.
     * @return status {@code null} if no status is available.
     */
    public PollResponse.OperationStatus getStatus() {
        return pollResponse != null ? pollResponse.getStatus() : null;
    }
}
