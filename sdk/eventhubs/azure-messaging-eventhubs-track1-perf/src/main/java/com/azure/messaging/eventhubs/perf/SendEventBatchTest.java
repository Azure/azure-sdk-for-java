// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.perf.core.EventHubsPerfStressOptions;
import com.azure.messaging.eventhubs.perf.core.ServiceBatchTest;
import com.azure.messaging.eventhubs.perf.core.Util;
import com.microsoft.azure.eventhubs.BatchOptions;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventDataBatch;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Runs the Send Events Batch Performance Test for EventHubs.
 */
public class SendEventBatchTest extends ServiceBatchTest<EventHubsPerfStressOptions> {
    private final BatchOptions batchOptions;
    private EventDataBatch eventDataBatch;

    /**
     * Instantiates the instance of the Send Event Batch test.
     *
     * @param options the options bag to use for performance testing.
     * @throws Exception when an error occurs when creating event batch.
     */
    public SendEventBatchTest(EventHubsPerfStressOptions options) throws Exception {
        super(options);

        batchOptions = new BatchOptions();

        if (options.getBatchSize() != null) {
            batchOptions.maxMessageSize = options.getBatchSize();
        }

        if (options.getPartitionKey() != null) {
            batchOptions.partitionKey = options.getPartitionKey();
        }
    }

    @Override
    public int runBatch() {
        eventHubClient.send(eventDataBatch);
        return options.getEvents();
    }

    @Override
    public Mono<Integer> runBatchAsync() {
        return Mono.fromFuture(eventHubClient.send(eventDataBatch))
            .then(Mono.defer(() -> Mono.just(options.getEvents())));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(Mono.fromCallable(() -> {
                eventDataBatch = eventHubClient.createBatch(batchOptions);
                EventData eventData =  EventData.create(Util.generateString(options.getMessageSize())
                    .getBytes(StandardCharsets.UTF_8));
                for (int i = 0; i < options.getEvents(); i++) {
                    if (!eventDataBatch.tryAdd(eventData)) {
                        throw new IllegalStateException(String.format("Batch can only fit %d number of messages with "
                                + "batch size of %d ",
                            options.getCount(), options.getSize()));
                    }
                }
                return Mono.empty();
            }))
            .then();
    }
}
