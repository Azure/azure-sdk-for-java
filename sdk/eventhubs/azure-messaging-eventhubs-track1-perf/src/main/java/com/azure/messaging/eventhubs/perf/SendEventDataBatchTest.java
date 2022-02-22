// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventhubs.EventDataBatch;
import com.microsoft.azure.eventhubs.EventHubException;
import reactor.core.publisher.Mono;

/**
 * Sends an event data batch with {@link EventHubsOptions#getCount()} number of events in the batch.
 */
public class SendEventDataBatchTest extends ServiceTest<EventHubsOptions> {

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public SendEventDataBatchTest(EventHubsOptions options) {
        super(options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int runBatch() {
        final EventDataBatch batch = createEventDataBatch(client, options.getCount());
        try {
            client.sendSync(batch);
        } catch (EventHubException e) {
            throw new RuntimeException("Unable to send EventDataBatch.", e);
        }
        return options.getCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Integer> runBatchAsync() {
        return Mono.fromCompletionStage(clientFuture
            .thenComposeAsync(client -> {
                final EventDataBatch batch = createEventDataBatch(client, options.getCount());
                return client.send(batch);
            })).then(Mono.just(options.getCount()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> setupAsync() {
        if (options.isSync() && client == null) {
            client = createEventHubClient();
        } else if (!options.isSync() && clientFuture == null) {
            clientFuture = createEventHubClientAsync();
        }

        return super.setupAsync();
    }
}
