// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;


import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * This class contains code samples for generating javadocs through doclets for {@link Poller}
 */
public final class PollerJavaDocCodeSnippets {

    private boolean debug = true;

    private Function<PollResponse<MyResponse>, Mono<PollResponse<MyResponse>>> createPollOperation(
        PollResponse<MyResponse> inProResp,
        PollResponse<MyResponse> finalPollResponse,
        long sendFinalResponseInMillis
    ) {
        return new Function<PollResponse<MyResponse>, Mono<PollResponse<MyResponse>>>() {

            // Will return success after this time.
            LocalDateTime timeToReturnFinalResponse
                = LocalDateTime.now().plus(Duration.ofMillis(sendFinalResponseInMillis));

            @Override
            public Mono<PollResponse<MyResponse>> apply(PollResponse<MyResponse> prePollResponse) {
                if (LocalDateTime.now().isBefore(timeToReturnFinalResponse)) {
                    System.out.println(" Service poll function called  returning intermediate response "
                        + inProResp.getValue().response);
                    return Mono.just(inProResp);
                } else {
                    System.out.println(" Service poll function called   returning final response "
                        + finalPollResponse.getValue().response);
                    return Mono.just(finalPollResponse);
                }
            }
        };
    }

    /**
     * Initialise and subscribe snippet
     */
    public void initialiseAndSubscribe() {
        PollResponse<MyResponse> finalPollResponse =
            new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new MyResponse("Created : Cert A"));
        PollResponse<MyResponse> inProgressResp =
            new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new MyResponse("Starting : Cert A"));

        long totalTimeoutInMillis = 1000 * 2;
        // BEGIN: com.azure.core.util.polling.poller.instantiationAndSubscribe
        Duration pollInterval = Duration.ofMillis(100);
        // Define my custom poll Operation. Assumption : Poll Operation will return user defined custom class MyResponse.
        Function<PollResponse<MyResponse>, Mono<PollResponse<MyResponse>>> pollOperation =
            new Function<PollResponse<MyResponse>, Mono<PollResponse<MyResponse>>>() {
                // Will return success after this time.
                LocalDateTime timeToReturnFinalResponse
                    = LocalDateTime.now().plus(Duration.ofMillis(800));

                @Override
                public Mono<PollResponse<MyResponse>> apply(PollResponse<MyResponse> prePollResponse) {
                    if (LocalDateTime.now().isBefore(timeToReturnFinalResponse)) {
                        System.out.println("returning intermediate response " + inProgressResp.getValue().response);
                        return Mono.just(inProgressResp);
                    } else {
                        System.out.println("returning final response " + finalPollResponse.getValue().response);
                        return Mono.just(finalPollResponse);
                    }
                }
            };

        //Create poller instance
        Poller<MyResponse> myPoller = new Poller<>(pollInterval, pollOperation);

        // Listen to poll responses
        myPoller.getObserver().subscribe(pr -> {
            //process poll response
            System.out.println("Got Response status,value " + pr.getStatus().toString() + " " + pr.getValue().response);
        });
        // Do something else

        // END: com.azure.core.util.polling.poller.instantiationAndSubscribe
    }

    /**
     * block for response
     */
    public void block() {

        Poller<MyResponse> myPoller = null;

        // BEGIN: com.azure.core.util.polling.poller.block
        PollResponse<MyResponse> myFinalResponse = myPoller.block();
        System.out.println("Polling complete final status , value=  "
            + myFinalResponse.getStatus().toString() + "," + myFinalResponse.getValue());
        // END: com.azure.core.util.polling.poller.block
    }

    /**
     * disable auto polling
     */
    public void setAutoPollingFalse() {

        Poller<MyResponse> myPoller = null;

        // BEGIN: com.azure.core.util.polling.poller.disableautopolling
        myPoller.setAutoPollingEnabled(false);
        System.out.println("Polling Enabled ?  " + myPoller.isAutoPollingEnabled());
        // END: com.azure.core.util.polling.poller.disableautopolling
    }

    /**
     * enable auto polling
     */
    public void setAutoPollingTrue() {

        Poller<MyResponse> myPoller = null;

        // BEGIN: com.azure.core.util.polling.poller.enableautopolling
        myPoller.setAutoPollingEnabled(true);
        System.out.println("Polling Enabled ?  " + myPoller.isAutoPollingEnabled());
        // END: com.azure.core.util.polling.poller.enableautopolling
    }


    /**
     * manual auto polling.
     */
    public void poll() {

        Poller<MyResponse> myPoller = null;

        // BEGIN: com.azure.core.util.polling.poller.poll
        myPoller.setAutoPollingEnabled(false);
        PollResponse<MyResponse> pollResponse = null;
        // We assume that we get SUCCESSFULLY_COMPLETED status from pollOperation when polling is done.
        while (pollResponse == null || !pollResponse.isDone()) {
            pollResponse = myPoller.poll().block();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Polling complete with status  " + myPoller.getStatus().toString());
        // END: com.azure.core.util.polling.poller.poll
    }

    /**
     * manual auto polling. More indepth example
     */
    public void pollIndepth() {

        Poller<MyResponse> myPoller = null;

        // BEGIN: com.azure.core.util.polling.poller.poll.indepth
        // Turn off auto polling and we will take control of polling
        myPoller.setAutoPollingEnabled(false);

        PollResponse<MyResponse> pollResponse = null;
        while (pollResponse == null || !pollResponse.isDone()) {
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
        // END: com.azure.core.util.polling.poller.poll.indepth
    }

    class MyResponse {
        String response;

        MyResponse(String response) {
            this.response = response;
        }

        public String toString() {
            return response;
        }
    }
}
