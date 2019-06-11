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
 * This class provides implementation of long running operations. The poller starts polling <b>automatically</b> when Poller instance is created.
 * It uses {@link Flux} from reactive programming model to achieve auto polling.
 * It has function for usual operation of a poller. For example listen/observe poll responses, enable/disable auto polling,
 * manual polling, wait for polling to complete and get status of current polling.
 *
 * <p><strong>Code Samples</strong></p>
 *
 * <p><strong>Instantiating and Subscribing to Poll Response</strong></p>
 *
 * <pre>
 *     // Assumption : Our task is to create Certificate and the return CreateCertificateResponse in Poll Response. CreateCertificateResponse is user defined custom class.
 *
 * <p>     class CreateCertificateResponse {
 *              String myresponse;
 *              CreateCertificateResponse(String respone) {
 *                  this.myresponse = respone;
 *              }
 *              public String toString() {
 *                  return this.myresponse;
 *              }
 *          }
 *<p>  // 1. Create your poll Operation Function
 *
 *<p>  Function&lt;PollResponse&lt;CreateCertificateResponse&gt;, Mono&lt;PollResponse&lt;CreateCertificateResponse&gt;&gt;&gt; pollOperation = ....
 *<p>  PollerOptions pollerOptions = new PollerOptions(Duration.ofMillis(1000));
 *<p>  // 2. Create your poller instance, Replace T with your Custom Response
 *<p>  Poller&lt;CreateCertificateResponse&gt; myPoller = new Poller&lt;&gt;(pollerOptions, pollOperation);
 *     myPoller.getObserver().subscribe(pollResponse -&gt; {
 *          // process poll response here
 *          System.out.println("Got Response " + pollResponse.getStatus().toString() + " " + pollResponse.getValue());
 *          });
 *      // Do something else</pre>
 *
 * <p><strong>Wait/Block for Polling to complete</strong></p>
 *
 * <pre>
 *    PollResponse&lt;CreateCertificateResponse&gt;  myFinalResponse = myPoller.block();
 *    System.out.println(("Polling complete final status , value=  " + myFinalResponse.getStatus().toString() + "," + myFinalResponse.pr.getValue());</pre>
 *
 * <p><strong>Disable auto polling</strong></p>
 *
 * <pre>
 *     myPoller.setAutoPollingEnabled(false);</pre>
 *
 * <p><strong>Disable auto polling and manual polling</strong></p>
 * <p> We assume that we get SUCCESSFULLY_COMPLETED status from pollOperation when polling is done.
 * <pre>
 *     myPoller.setAutoPollingEnabled(false);
 *     while (myPoller.getStatus() != OperationStatus.SUCCESSFULLY_COMPLETED) {
 *           PollResponse&lt;CreateCertificateResponse> pollResponse = myPoller.poll().block(); //this will poll once
 *           // Wait in between each poll
 *           Thread.sleep(Duration.ofMillis(5000));
 *     }
 *     System.out.println(("Polling complete.");</pre>
 *
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

    private Flux<PollResponse<T>> fluxHandle;

    /*
     * Since constructor create a subscriber and start auto polling.
     * This handle will be used to dispose the subscriber when
     * client disable auto polling.
     */
    private Disposable fluxDisposable;

    /**
     * Create a Poller object. The polling starts immediately by default and it will invoke pollOperation.
     * The poll interval would defined by retryAfter value in {@link PollResponse}.
     * In absence of retryAfter, the poller will use pollInterval defined in {@link PollerOptions}.
     *
     * @param pollerOptions Not null configuration options for poller.
     * @param pollOperation to be called by poller. It should not return {@code null}. The response should always have valid {@link com.azure.core.polling.PollResponse.OperationStatus}
     * @throws NullPointerException If {@code pollerOptions} is {@code null}.
     * @throws NullPointerException If {@code pollOperation} is {@code null}.
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
     * Create a Poller object with cancel operation. The polling starts immediately by default and invoke pollOperation.
     *
     * @param pollerOptions   configuration options for poller.
     * @param pollOperation   to be called by poller. User should never return {@code null}. The response should have valid {@link com.azure.core.polling.PollResponse.OperationStatus}
     * @param cancelOperation cancel operation
     * @throws NullPointerException If {@code pollerOptions} is {@code null}.
     * @throws NullPointerException If {@code pollOperation} is {@code null}.
     */
    public Poller(PollerOptions pollerOptions, Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation, Consumer<Poller> cancelOperation) {
        this(pollerOptions, pollOperation);
        this.cancelOperation = cancelOperation;
    }

    /**
     * Calls cancelOperation function if provided.
     * It will only call cancelOperation if {@link com.azure.core.polling.PollResponse.OperationStatus} is IN_PROGRESS otherwise does nothing.
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
        cancelOperation.accept(this);
    }

    /**
     * Enable user to subscribe and listen on all the poll responses.
     * The user will start receiving PollResponse when client subscribe to this Flux.
     * The poller could still have its own auto polling in action unless user has turned off
     * auto polling.
     *
     * @return poll response as Flux that can be subscribed.
     */
    public Flux<PollResponse<T>> getObserver() {
        return fluxHandle;
    }

    /**
     * Enable user to take control of polling and trigger manual poll operation. It will perform one call to poll operation.
     * This will not turn off auto polling.
     *
     * @return a Mono of {@link PollResponse}
     */
    public Mono<PollResponse<T>> poll() {
        return pollOperation.apply(pollResponse)
            .doOnEach(pollResponseSignal -> {
                if (pollResponseSignal.get() != null) {
                    pollResponse = pollResponseSignal.get();
                }
            });
    }

    /**
     * Blocks execution and wait for polling to complete.
     * Auto polling must be turned on for poller to continuously poll in background.
     *
     * @return returns last poll response when polling is done.
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
     * Turn auto poll <strong>on or off</strong>. Once auto polling is turned off, it is <strong>user's responsibility</strong>
     * to turn it back on.
     *
     * @param autoPollingEnabled true  Ensures the polling is happening in background.
     *                           false  Ensures that polling is <strong>not</strong> happening in background.
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
     * Indicate if auto polling is on/off . By default auto polling is turned <strong>on</strong>.
     *
     * @return false if polling is stopped.
     */
    public boolean isAutoPollingEnabled() {
        return this.autoPollingEnabled;
    }

    /**
     * Current known status as a result of last poll event.
     * @return current status {@code null} if no status is available.
     */
    public PollResponse.OperationStatus getStatus() {
        return pollResponse != null ? pollResponse.getStatus() : null;
    }
}
