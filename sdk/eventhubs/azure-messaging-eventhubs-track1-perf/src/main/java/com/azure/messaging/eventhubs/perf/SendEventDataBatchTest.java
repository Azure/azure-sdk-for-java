// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.perf.test.core.EventPerfTest;
import com.microsoft.azure.eventhubs.EventDataBatch;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sends an event data batch with {@link EventHubsOptions#getCount()} number of events in the batch.
 */
public class SendEventDataBatchTest extends EventPerfTest<EventHubsOptions> {
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
    public SendEventDataBatchTest(EventHubsOptions options) {
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
            this.client = testHelper.createEventHubClient();
            this.subscription = Mono.fromRunnable(() -> sendEvents())
                .repeat(() -> isRunning.get())
                .subscribe();
        } else if (!options.isSync() && clientFuture == null) {
            this.clientFuture = testHelper.createEventHubClientAsync();

            this.subscription = Mono.defer(() ->sendEventsAsync())
                .repeat(() -> isRunning.get())
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
        final EventDataBatch batch = testHelper.createEventDataBatch(client, options.getCount());
        try {
            client.sendSync(batch);
            eventRaised();
        } catch (EventHubException e) {
            throw new RuntimeException("Unable to send EventDataBatch.", e);
        }
    }

    private Mono<Void> sendEventsAsync() {
        final CompletableFuture<Void> sendBatch = clientFuture.thenComposeAsync(client -> {
            final EventDataBatch batch = testHelper.createEventDataBatch(client, options.getCount());
            return client.send(batch);
        });

        return Mono.fromCompletionStage(sendBatch.thenRun(() -> eventRaised()));
    }
}
