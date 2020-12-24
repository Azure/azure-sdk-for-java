// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.perf;

import com.microsoft.azure.eventhubs.BatchOptions;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventDataBatch;
import com.microsoft.azure.eventhubs.perf.core.EventHubsPerfStressOptions;
import com.microsoft.azure.eventhubs.perf.core.ServiceTest;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Runs the Send Events Batch Performance Test for EventHubs.
 */
public class SendEventBatchTest extends ServiceTest<EventHubsPerfStressOptions> {
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
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(Mono.fromCallable(() -> {
                eventDataBatch = eventHubClient.createBatch(batchOptions);
                EventData eventData =  EventData.create("Static Event".getBytes(StandardCharsets.UTF_8));
                for (int i = 0; i < options.getEvents(); i++) {
                    if (!eventDataBatch.tryAdd(eventData)) {
                        throw new IllegalStateException(String.format("Batch can only fit %d number of messages with "
                                + "batch size of %d ",
                            options.getCount(), options.getSize()));
                    }
                }
                return 1;
            }))
            .then();
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        eventHubClient.send(eventDataBatch);
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.fromFuture(eventHubClient.send(eventDataBatch));
    }
}
