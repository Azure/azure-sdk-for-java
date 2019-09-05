// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;


import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;
import com.azure.core.util.polling.PollResponse.OperationStatus;

/**
 * This class contains code samples for generating javadocs through doclets for {@link Poller}
 */
public final class PollerJavaDocCodeSnippets {

    private boolean debug = true;

    private Function<PollResponse<String>, Mono<PollResponse<String>>> createPollOperation(
        PollResponse<String> inProResp,
        PollResponse<String> finalPollResponse,
        long sendFinalResponseInMillis
    ) {
        return new Function<PollResponse<String>, Mono<PollResponse<String>>>() {

            // Will return success after this time.
            LocalDateTime timeToReturnFinalResponse
                = LocalDateTime.now().plus(Duration.ofMillis(sendFinalResponseInMillis));

            @Override
            public Mono<PollResponse<String>> apply(PollResponse<String> prePollResponse) {
                if (LocalDateTime.now().isBefore(timeToReturnFinalResponse)) {
                    System.out.println(" Service poll function called  returning intermediate response "
                        + inProResp.getValue());
                    return Mono.just(inProResp);
                } else {
                    System.out.println(" Service poll function called   returning final response "
                        + finalPollResponse.getValue());
                    return Mono.just(finalPollResponse);
                }
            }
        };
    }

    /**
     * Initialise
     */
    public void initialize() {
        PollResponse<String> finalPollResponse =
            new PollResponse<String>(OperationStatus.SUCCESSFULLY_COMPLETED, ("Operation Completed."));
        PollResponse<String> inProgressResp =
            new PollResponse<String>(OperationStatus.IN_PROGRESS, "Operation in progress.");

        long totalTimeoutInMillis = 1000 * 2;
        // BEGIN: com.azure.core.util.polling.poller.initialize.interval.polloperation
        // Define your custom poll operation
        Function<PollResponse<String>, Mono<PollResponse<String>>> pollOperation =
            new Function<PollResponse<String>, Mono<PollResponse<String>>>() {
                // Will return success after this time.
                LocalDateTime timeToReturnFinalResponse
                    = LocalDateTime.now().plus(Duration.ofMillis(800));
                @Override
                public Mono<PollResponse<String>> apply(PollResponse<String> prePollResponse) {
                    if (LocalDateTime.now().isBefore(timeToReturnFinalResponse)) {
                        System.out.println("returning intermediate response " + inProgressResp.getValue());
                        return Mono.just(inProgressResp);
                    } else {
                        System.out.println("returning final response " + finalPollResponse.getValue());
                        return Mono.just(finalPollResponse);
                    }
                }
            };

        //Create poller instance
        Poller<String> myPoller = new Poller<>(Duration.ofMillis(100), pollOperation);

        // Default polling will start transparently.

        // END: com.azure.core.util.polling.poller.initialize.interval.polloperation
    }

    /**
     * Initialise and subscribe snippet
     */
    public void initializeAndSubscribe() {
        PollResponse<String> finalPollResponse =
            new PollResponse<String>(OperationStatus.SUCCESSFULLY_COMPLETED, ("Operation Completed."));
        PollResponse<String> inProgressResp =
            new PollResponse<String>(OperationStatus.IN_PROGRESS, "Operation in progress.");

        long totalTimeoutInMillis = 1000 * 2;
        // BEGIN: com.azure.core.util.polling.poller.instantiationAndSubscribe
        Duration pollInterval = Duration.ofMillis(100);
        // Assumption : Poll Operation will return a String type in our example.
        Function<PollResponse<String>, Mono<PollResponse<String>>> pollOperation =
            new Function<PollResponse<String>, Mono<PollResponse<String>>>() {
                // Will return success after this time.
                LocalDateTime timeToReturnFinalResponse
                    = LocalDateTime.now().plus(Duration.ofMillis(800));

                @Override
                public Mono<PollResponse<String>> apply(PollResponse<String> prePollResponse) {
                    if (LocalDateTime.now().isBefore(timeToReturnFinalResponse)) {
                        System.out.println("returning intermediate response " + inProgressResp.getValue());
                        return Mono.just(inProgressResp);
                    } else {
                        System.out.println("returning final response " + finalPollResponse.getValue());
                        return Mono.just(finalPollResponse);
                    }
                }
            };

        //Create poller instance
        Poller<String> myPoller = new Poller<>(pollInterval, pollOperation);

        // Listen to poll responses
        myPoller.getObserver().subscribe(pr -> {
            //process poll response
            System.out.println("Got Response status,value " + pr.getStatus().toString() + " " + pr.getValue());
        });
        // Do something else

        // END: com.azure.core.util.polling.poller.instantiationAndSubscribe
    }

    /**
     * block for response
     */
    public void block() {

        Poller<String> myPoller = null;

        // BEGIN: com.azure.core.util.polling.poller.block
        PollResponse<String> myFinalResponse = myPoller.block();
        System.out.println("Polling complete final status , value=  "
            + myFinalResponse.getStatus().toString() + "," + myFinalResponse.getValue());
        // END: com.azure.core.util.polling.poller.block
    }

    /**
     * disable auto polling
     */
    public void setAutoPollingFalse() {

        Poller<String> myPoller = null;

        // BEGIN: com.azure.core.util.polling.poller.disableautopolling
        myPoller.setAutoPollingEnabled(false);
        System.out.println("Polling Enabled ?  " + myPoller.isAutoPollingEnabled());
        // END: com.azure.core.util.polling.poller.disableautopolling
    }

    /**
     * enable auto polling
     */
    public void setAutoPollingTrue() {

        Poller<String> myPoller = null;

        // BEGIN: com.azure.core.util.polling.poller.enableautopolling
        myPoller.setAutoPollingEnabled(true);
        System.out.println("Polling Enabled ?  " + myPoller.isAutoPollingEnabled());
        // END: com.azure.core.util.polling.poller.enableautopolling
    }


    /**
     * manual auto polling.
     */
    public void poll() {

        Poller<String> myPoller = null;

        // BEGIN: com.azure.core.util.polling.poller.poll-manually
        myPoller.setAutoPollingEnabled(false);
        PollResponse<String> pollResponse = null;
        // We assume that we get SUCCESSFULLY_COMPLETED status from pollOperation when polling is complete.
        while (pollResponse != null
            && pollResponse.getStatus() != OperationStatus.SUCCESSFULLY_COMPLETED) {
            pollResponse = myPoller.poll().block();
            try {
                // Ensure that you have sufficient wait in each poll() which is suitable for your application.
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Polling complete with status  " + myPoller.getStatus().toString());
        // END: com.azure.core.util.polling.poller.poll-manually
    }

    /**
     * manual auto polling. More indepth example
     */
    public void pollIndepth() {

        Poller<String> myPoller = null;

        // BEGIN: com.azure.core.util.polling.poller.poll-indepth
        // Turn off auto polling and this code will take control of polling
        myPoller.setAutoPollingEnabled(false);

        PollResponse<String> pollResponse = null;
        while (pollResponse == null
            || pollResponse.getStatus() == OperationStatus.IN_PROGRESS
            || pollResponse.getStatus() == OperationStatus.NOT_STARTED) {
            // get one poll Response at a time..
            pollResponse = myPoller.poll().block();
            System.out.println("Poll response status  " + pollResponse.getStatus().toString());
            // Ensure that you have sufficient wait in each poll() which is suitable for your application.
            try {
                // wait before next poll.
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Polling complete with status  " + myPoller.getStatus().toString());
        // END: com.azure.core.util.polling.poller.poll-indepth
    }
}
