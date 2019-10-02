// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;


import com.azure.core.util.polling.PollResponse.OperationStatus;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Predicate;

/**
 * This class contains code samples for generating javadocs through doclets for {@link Poller}
 */
public final class PollerJavaDocCodeSnippets {
    private final Poller<String> myPoller = new Poller<>(Duration.ofMillis(100),
        response -> Mono.just(new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, "Completed")));

    /**
     * Initialise
     */
    public void initialize() {
        // BEGIN: com.azure.core.util.polling.poller.initialize.interval.polloperation
        LocalDateTime timeToReturnFinalResponse = LocalDateTime.now().plus(Duration.ofMillis(800));

        // Create poller instance
        Poller<String> poller = new Poller<>(Duration.ofMillis(100),
            // Define your custom poll operation
            prePollResponse -> {
                if (LocalDateTime.now().isBefore(timeToReturnFinalResponse)) {
                    System.out.println("Returning intermediate response.");
                    return Mono.just(new PollResponse<>(OperationStatus.IN_PROGRESS, "Operation in progress."));
                } else {
                    System.out.println("Returning final response.");
                    return Mono.just(new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, "Operation Completed."));
                }
            });

        // Default polling will start transparently.
        // END: com.azure.core.util.polling.poller.initialize.interval.polloperation
    }

    /**
     * Initialise and subscribe snippet
     */
    public void initializeAndSubscribe() {
        // BEGIN: com.azure.core.util.polling.poller.instantiationAndSubscribe
        LocalDateTime timeToReturnFinalResponse = LocalDateTime.now().plus(Duration.ofMillis(800));

        // Create poller instance
        Duration pollInterval = Duration.ofMillis(100);
        Poller<String> poller = new Poller<>(pollInterval,
            // Define your custom poll operation
            prePollResponse -> {
                if (LocalDateTime.now().isBefore(timeToReturnFinalResponse)) {
                    System.out.println("Returning intermediate response.");
                    return Mono.just(new PollResponse<>(OperationStatus.IN_PROGRESS, "Operation in progress."));
                } else {
                    System.out.println("Returning final response.");
                    return Mono.just(new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, "Operation Completed."));
                }
            });

        // Listen to poll responses
        poller.getObserver().subscribe(response -> {
            // Process poll response
            System.out.printf("Got response. Status: %s, Value: %s%n", response.getStatus(), response.getValue());
        });
        // Do something else

        // END: com.azure.core.util.polling.poller.instantiationAndSubscribe
    }

    /**
     * block for response
     */
    public void block() {
        // BEGIN: com.azure.core.util.polling.poller.block
        PollResponse<String> response = myPoller.block();
        System.out.printf("Polling complete. Status: %s, Value: %s%n", response.getStatus(), response.getValue());
        // END: com.azure.core.util.polling.poller.block
    }

    /**
     * disable auto polling
     */
    public void setAutoPollingFalse() {
        // BEGIN: com.azure.core.util.polling.poller.disableautopolling
        myPoller.setAutoPollingEnabled(false);
        System.out.println("Polling Enabled? " + myPoller.isAutoPollingEnabled());
        // END: com.azure.core.util.polling.poller.disableautopolling
    }

    /**
     * enable auto polling
     */
    public void setAutoPollingTrue() {
        // BEGIN: com.azure.core.util.polling.poller.enableautopolling
        myPoller.setAutoPollingEnabled(true);
        System.out.println("Polling Enabled? " + myPoller.isAutoPollingEnabled());
        // END: com.azure.core.util.polling.poller.enableautopolling
    }


    /**
     * manual auto polling.
     */
    public void poll() {
        // BEGIN: com.azure.core.util.polling.poller.poll-manually
        // Turns off auto polling.
        myPoller.setAutoPollingEnabled(false);

        // Continue to poll every 500ms until the response emitted from poll() is SUCCESSFULLY_COMPLETED.
        myPoller.poll()
            .repeatWhen(attemptsCompleted -> attemptsCompleted.flatMap(attempt -> Mono.delay(Duration.ofMillis(500))))
            .takeUntil(response -> response.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED)
            .subscribe(response -> {
                System.out.printf("Response status: %s. Value: %s%n", response.getStatus(), response.getValue());
            }, error -> {
                System.err.printf("Exception occurred while polling: %s%n", error);
            }, () -> {
                System.out.printf("Polling complete with status: %s%n", myPoller.getStatus());
            });
        // END: com.azure.core.util.polling.poller.poll-manually
    }

    /**
     * manual auto polling. More in-depth example
     */
    public void pollIndepth() {
        // BEGIN: com.azure.core.util.polling.poller.poll-indepth
        final Predicate<PollResponse<String>> isComplete = response -> {
            return response.getStatus() != OperationStatus.IN_PROGRESS
                && response.getStatus() != OperationStatus.NOT_STARTED;
        };

        // Turns off auto polling
        myPoller.setAutoPollingEnabled(false);

        myPoller.poll()
            .repeatWhen(attemptsCompleted -> {
                // Retry each poll operation after 500ms.
                return attemptsCompleted.flatMap(attempt -> Mono.delay(Duration.ofMillis(500)));
            })
            .takeUntil(isComplete)
            .filter(isComplete)
            .subscribe(completed -> {
                System.out.println("Completed poll response: " + completed.getStatus());
                System.out.println("Polling complete with status: " + myPoller.getStatus().toString());
            });
        // END: com.azure.core.util.polling.poller.poll-indepth
    }
}
