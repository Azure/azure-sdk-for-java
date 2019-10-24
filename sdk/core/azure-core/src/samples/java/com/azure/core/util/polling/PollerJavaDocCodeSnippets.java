// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Predicate;

/**
 * This class contains code samples for generating javadocs through doclets for {@link PollerFlux}
 */
public final class PollerJavaDocCodeSnippets {
    private final PollerFlux<String, String> pollerFlux = new PollerFlux<>(Duration.ofMillis(100),
            () -> Mono.empty(),
            (activationResponse, latestResponse) -> Mono.just(
                    new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, "Completed")),
            (activationResponse, latestResponse) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            (activationResponse, latestResponse) -> Mono.just("Final Output"));

    /**
     * Initialise and subscribe
     */
    public void initializeAndSubscribe() {
        // BEGIN: com.azure.core.util.polling.poller.instantiationAndSubscribe
        LocalDateTime timeToReturnFinalResponse = LocalDateTime.now().plus(Duration.ofMillis(800));

        // Create poller instance
        PollerFlux<String, String> poller = new PollerFlux<>(Duration.ofMillis(100),
                () -> Mono.empty(),
                // Define your custom poll operation
                (activationResponse, latestResponse) ->  {
                    if (LocalDateTime.now().isBefore(timeToReturnFinalResponse)) {
                        System.out.println("Returning intermediate response.");
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                                "Operation in progress."));
                    } else {
                        System.out.println("Returning final response.");
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                                "Operation completed."));
                    }
                },
                (activationResponse, latestResponse) -> Mono.error(new RuntimeException("Cancellation is not supported")),
                (activationResponse, latestResponse) -> Mono.just("Final Output"));

        // Listen to poll responses
        poller.subscribe(response -> {
            // Process poll response
            System.out.printf("Got response. Status: %s, Value: %s%n", response.getStatus(), response.getValue());
        });
        // Do something else

        // END: com.azure.core.util.polling.poller.instantiationAndSubscribe
    }

    /**
     * asynchronously wait poll to complete and retrieve final result from within
     * reactor operator chain.
     */
    public void getResult() {
        // BEGIN: com.azure.core.util.polling.poller.getResult
        LocalDateTime timeToReturnFinalResponse = LocalDateTime.now().plus(Duration.ofMinutes(5));

        // Create poller instance
        PollerFlux<String, String> poller = new PollerFlux<>(Duration.ofMillis(100),
                () -> Mono.empty(),
                (activationResponse, latestResponse) ->  {
                    if (LocalDateTime.now().isBefore(timeToReturnFinalResponse)) {
                        System.out.println("Returning intermediate response.");
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                                "Operation in progress."));
                    } else {
                        System.out.println("Returning final response.");
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                                "Operation completed."));
                    }
                },
                (activationResponse, latestResponse) -> Mono.just("FromServer:OperationIsCancelled"),
                (activationResponse, latestResponse) -> Mono.just("FromServer:FinalOutput"));

        poller.take(Duration.ofMinutes(30))
                .last()
                .flatMap(asyncPollResponse -> {
                    if (asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
                        // operation completed successfully, retrieving final result.
                        return asyncPollResponse
                                .getFinalResult();
                    } else {
                        return Mono.error(new RuntimeException("polling completed unsuccessfully with status:"
                                + asyncPollResponse.getStatus()));
                    }
                }).block();

        // END: com.azure.core.util.polling.poller.getResult
    }

    /**
     * block for final poll response and retrieve the final result if final response
     * indicate a successful completion of the long running operation.
     */
    public void blockAndGetResult() {
        // BEGIN: com.azure.core.util.polling.poller.blockAndGetResult
        AsyncPollResponse<String, String> terminalResponse = pollerFlux.blockLast();
        System.out.printf("Polling complete. Final Status: %s", terminalResponse.getStatus());
        if (terminalResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            String finalResult = terminalResponse.getFinalResult().block();
            System.out.printf("Polling complete. Final Status: %s", finalResult);
        }
        // END: com.azure.core.util.polling.poller.blockAndGetResult
    }

    /**
     * asynchronously poll until service return a matching status.
     */
    public void polluntil() {
        // BEGIN: com.azure.core.util.polling.poller.pollUntil
        final Predicate<AsyncPollResponse<String, String>> isComplete = response -> {
            return response.getStatus() != LongRunningOperationStatus.IN_PROGRESS
                && response.getStatus() != LongRunningOperationStatus.NOT_STARTED;
        };

        pollerFlux
            .takeUntil(isComplete)
            .subscribe(completed -> {
                System.out.println("Completed poll response, status: " + completed.getStatus());
            });
        // END: com.azure.core.util.polling.poller.pollUntil
    }

    /**
     * cancel the long running operation.
     */
    public void cancelOperation() {
        // BEGIN: com.azure.core.util.polling.poller.cancelOperation
        LocalDateTime timeToReturnFinalResponse = LocalDateTime.now().plus(Duration.ofMinutes(5));

        // Create poller instance
        PollerFlux<String, String> poller = new PollerFlux<>(Duration.ofMillis(100),
                () -> Mono.empty(),
                (activationResponse, latestResponse) ->  {
                    if (LocalDateTime.now().isBefore(timeToReturnFinalResponse)) {
                        System.out.println("Returning intermediate response.");
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                                "Operation in progress."));
                    } else {
                        System.out.println("Returning final response.");
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                                "Operation completed."));
                    }
                },
                (activationResponse, latestResponse) -> Mono.just("FromServer:OperationIsCancelled"),
                (activationResponse, latestResponse) -> Mono.just("FromServer:FinalOutput"));

        // Asynchronously wait 30 minutes to complete the polling, if not completed
        // within in the time then cancel the server operation.
        poller.take(Duration.ofMinutes(30))
                .last()
                .flatMap(asyncPollResponse -> {
                    if (!asyncPollResponse.getStatus().isComplete()) {
                        return asyncPollResponse
                                .cancelOperation()
                                .then(Mono.error(new RuntimeException("Operation is cancelled!")));
                } else {
                    return Mono.just(asyncPollResponse);
                }
            }).block();

        // END: com.azure.core.util.polling.poller.cancelOperation
    }
}
