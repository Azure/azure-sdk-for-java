// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubException;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

/**
 * Sends a number of {@link EventData} to Event Hub.
 */
public class SendEventDataTest extends ServiceTest<EventHubsOptions> {

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public SendEventDataTest(EventHubsOptions options) {
        super(options);
    }

    @Override
    public void run() {
        if (client == null) {
            client = createEventHubClient(options);
        }

        for (int i = 0; i < events.size(); i++) {
            final EventData event = events.get(i);

            try {
                client.sendSync(event);
            } catch (EventHubException e) {
                throw new RuntimeException("Unable to send event at index: " + i, e);
            }
        }
    }

    @Override
    public Mono<Void> runAsync() {
        if (clientFuture == null) {
            clientFuture = createEventHubClientAsync(options);
        }

        return Mono.fromCompletionStage(clientFuture.thenComposeAsync(client -> {
            final CompletableFuture<?>[] completableFutures = events.stream()
                .map(client::send)
                .toArray(CompletableFuture<?>[]::new);
            return CompletableFuture.allOf(completableFutures);
        }));
    }
}
