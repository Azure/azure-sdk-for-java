// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Predicate;

/**
 * This class contains code samples for generating javadocs through doclets for {@link PollerFlux}
 */
public final class PollerJavaDocCodeSnippets {
    private final PollerFlux<String, String> pollerFlux = new PollerFlux<>(Duration.ofMillis(100),
        (context) -> Mono.empty(),
        (context) -> Mono.just(
                new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, "Completed")),
        (activationResponse, context) -> Mono.error(new RuntimeException("Cancellation is not supported")),
        (context) -> Mono.just("Final Output"));

    /**
     * Instantiating and subscribing to PollerFlux.
     */
    public void initializeAndSubscribe() {
        // BEGIN: com.azure.core.util.polling.poller.instantiationAndSubscribe
        LocalDateTime timeToReturnFinalResponse = LocalDateTime.now().plus(Duration.ofMillis(800));

        // Create poller instance
        PollerFlux<String, String> poller = new PollerFlux<>(Duration.ofMillis(100),
            (context) -> Mono.empty(),
            // Define your custom poll operation
            (context) ->  {
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
            (activationResponse, context) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            (context) -> Mono.just("Final Output"));

        // Listen to poll responses
        poller.subscribe(response -> {
            // Process poll response
            System.out.printf("Got response. Status: %s, Value: %s%n", response.getStatus(), response.getValue());
        });
        // Do something else

        // END: com.azure.core.util.polling.poller.instantiationAndSubscribe
    }

    /**
     * Asynchronously wait for polling to complete and then retrieve the final result.
     */
    public void getResult() {
        // BEGIN: com.azure.core.util.polling.poller.getResult
        LocalDateTime timeToReturnFinalResponse = LocalDateTime.now().plus(Duration.ofMinutes(5));

        // Create poller instance
        PollerFlux<String, String> poller = new PollerFlux<>(Duration.ofMillis(100),
            (context) -> Mono.empty(),
            (context) ->  {
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
            (activationResponse, context) -> Mono.just("FromServer:OperationIsCancelled"),
            (context) -> Mono.just("FromServer:FinalOutput"));

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
     * Block for polling to complete and then retrieve the final result.
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
     * Asynchronously poll until poller receives matching status.
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
     * Asynchronously cancel the long running operation.
     */
    public void cancelOperation() {
        // BEGIN: com.azure.core.util.polling.poller.cancelOperation
        LocalDateTime timeToReturnFinalResponse = LocalDateTime.now().plus(Duration.ofMinutes(5));

        // Create poller instance
        PollerFlux<String, String> poller = new PollerFlux<>(Duration.ofMillis(100),
            (context) -> Mono.empty(),
            (context) ->  {
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
            (activationResponse, context) -> Mono.just("FromServer:OperationIsCancelled"),
            (context) -> Mono.just("FromServer:FinalOutput"));

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

    /**
     * Instantiating and subscribing to PollerFlux created from a
     * {@link PollingStrategy}.
     */
    public void initializeAndSubscribeWithPollingStrategy() {
        // BEGIN: com.azure.core.util.polling.poller.instantiationAndSubscribeWithPollingStrategy
        // Create poller instance
        PollerFlux<BinaryData, String> poller = PollerFlux.create(
            Duration.ofMillis(100),
            // pass in your custom activation operation
            () -> Mono.just(new SimpleResponse<Void>(new HttpRequest(
                HttpMethod.POST,
                "http://httpbin.org"),
                202,
                new HttpHeaders().set("Operation-Location", "http://httpbin.org"),
                null)),
            new OperationResourcePollingStrategy<>(
                new HttpPipelineBuilder().build(),
                Context.NONE),
            TypeReference.createInstance(BinaryData.class),
            TypeReference.createInstance(String.class));

        // Listen to poll responses
        poller.subscribe(response -> {
            // Process poll response
            System.out.printf("Got response. Status: %s, Value: %s%n", response.getStatus(), response.getValue());
        });
        // Do something else

        // END: com.azure.core.util.polling.poller.instantiationAndSubscribeWithPollingStrategy
    }

    /**
     * Instantiating and subscribing to PollerFlux created from a custom modified
     * {@link PollingStrategy}.
     */
    public void initializeAndSubscribeWithCustomPollingStrategy() {
        // BEGIN: com.azure.core.util.polling.poller.initializeAndSubscribeWithCustomPollingStrategy

        // Create custom polling strategy based on OperationResourcePollingStrategy
        PollingStrategy<BinaryData, String> strategy = new OperationResourcePollingStrategy<>(
                new HttpPipelineBuilder().build(),
                Context.NONE) {
            // override any interface method to customize the polling behavior
            @Override
            public Mono<PollResponse<BinaryData>> poll(PollingContext<BinaryData> context,
                                                       TypeReference<BinaryData> pollResponseType) {
                return Mono.just(new PollResponse<>(
                    LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    BinaryData.fromString("")));
            }
        };

        // Create poller instance
        PollerFlux<BinaryData, String> poller = PollerFlux.create(
            Duration.ofMillis(100),
            // pass in your custom activation operation
            () -> Mono.just(new SimpleResponse<Void>(new HttpRequest(
                HttpMethod.POST,
                "http://httpbin.org"),
                202,
                new HttpHeaders().set("Operation-Location", "http://httpbin.org"),
                null)),
            strategy,
            TypeReference.createInstance(BinaryData.class),
            TypeReference.createInstance(String.class));

        // Listen to poll responses
        poller.subscribe(response -> {
            // Process poll response
            System.out.printf("Got response. Status: %s, Value: %s%n", response.getStatus(), response.getValue());
        });
        // Do something else

        // END: com.azure.core.util.polling.poller.initializeAndSubscribeWithCustomPollingStrategy
    }
}
