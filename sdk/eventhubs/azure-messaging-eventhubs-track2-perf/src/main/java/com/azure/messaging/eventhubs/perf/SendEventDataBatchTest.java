// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import reactor.core.publisher.Mono;

/**
 * Sends an event data batch with {@link EventHubsOptions#getCount()} number of events in the batch.
 */
public class SendEventDataBatchTest extends ServiceTest<EventHubsOptions> {
    private EventHubProducerClient producer;
    private EventHubProducerAsyncClient producerAsync;

    /**
     * Creates an instance of performance test. )O
     *
     * @param options the options configured for the test.
     */
    public SendEventDataBatchTest(EventHubsOptions options) {
        super(options);
    }

    @Override
    public void run() {
        if (producer == null) {
            producer = createEventHubClientBuilder()
                .buildProducerClient();
        }

        final EventDataBatch batch = producer.createBatch();
        addEvents(batch, options.getCount());

        producer.send(batch);
    }

    @Override
    public Mono<Void> runAsync() {
        if (producerAsync == null) {
            producerAsync = createEventHubClientBuilder().buildAsyncProducerClient();
        }

        return producerAsync.createBatch().flatMap(batch -> {
            addEvents(batch, options.getCount());
            return producerAsync.send(batch);
        }).then();
    }

    @Override
    public Mono<Void> cleanupAsync() {
        if (producer != null) {
            producer.close();
        }
        if (producerAsync != null) {
            producerAsync.close();
        }

        return super.cleanupAsync();
    }
}
