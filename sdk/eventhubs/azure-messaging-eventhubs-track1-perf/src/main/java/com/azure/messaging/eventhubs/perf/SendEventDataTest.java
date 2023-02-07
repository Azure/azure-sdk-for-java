// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.perf.test.core.EventPerfTest;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sends a number of {@link EventData} to Event Hub.
 */
public class SendEventDataTest extends EventPerfTest<EventHubsOptions> {
    private final EventHubsTestHelper<EventHubsOptions> testHelper;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private EventHubClient client;
    private CompletableFuture<EventHubClient> clientFuture;
    private Disposable subscription;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public SendEventDataTest(EventHubsOptions options) {
        super(options);

        this.testHelper = new EventHubsTestHelper<>(options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> setupAsync() {
        if (isRunning.getAndSet(true)) {
            return Mono.empty();
        }

        if (options.isSync() && client == null) {
            client = testHelper.createEventHubClient();

            subscription = Mono.fromRunnable(() -> sendEvents())
                .repeat()
                .subscribe();
        } else if (!options.isSync() && clientFuture == null) {
            clientFuture = testHelper.createEventHubClientAsync();

            subscription = Mono.defer(() -> sendEventsAsync())
                .repeat()
                .subscribe();
        }

        return Mono.empty();
    }

    @Override
    public Mono<Void> cleanupAsync() {
        if (!isRunning.getAndSet(false)) {
            return Mono.empty();
        }

        subscription.dispose();

        // Dispose of the scheduler at the very end.
        return testHelper.cleanupAsync(client, clientFuture)
            .doFinally(signal -> testHelper.close());
    }

    private void sendEvents() {
        for (final EventData event : testHelper.getEvents()) {
            try {
                client.sendSync(event);
                eventRaised();
            } catch (EventHubException e) {
                throw new RuntimeException("Unable to send event.", e);
            }
        }

        System.out.println("Batch events sent.");
    }

    private Mono<Void> sendEventsAsync() {
        return Mono.fromCompletionStage(clientFuture.thenComposeAsync(client -> {
            final CompletableFuture<?>[] completableFutures = testHelper.getEvents().stream()
                .map(event -> {
                    return client.send(event).thenRun(() -> eventRaised());
                })
                .toArray(CompletableFuture<?>[]::new);
            return CompletableFuture.allOf(completableFutures);
        }));
    }
}
