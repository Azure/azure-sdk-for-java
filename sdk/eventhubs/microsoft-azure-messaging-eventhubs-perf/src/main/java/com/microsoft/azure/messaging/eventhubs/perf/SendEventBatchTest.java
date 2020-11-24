// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.messaging.eventhubs.perf.core.EventHubsPerfStressOptions;
import com.microsoft.azure.messaging.eventhubs.perf.core.ServiceTest;
import reactor.core.publisher.Mono;

/**
 * Runs the Send Events Batch Performance Test for EventHubs.
 */
public class SendEventBatchTest extends ServiceTest<EventHubsPerfStressOptions> {
    private final EventDataBatch eventDataBatch;
    protected final PartitionSender partitionSender;


    /**
     *
     * @param options
     * @throws Exception
     */
    public SendEventBatchTest(EventHubsPerfStressOptions options) throws Exception {
        super(options);

        partitionSender = eventHubClient.createPartitionSender(String.valueOf(options.getPartitionId())).get();
        BatchOptions batchOptions = new BatchOptions();

        if (options.getBatchSize() != null) {
            batchOptions.maxMessageSize = options.getBatchSize();
        }

        if (options.getPartitionKey() != null) {
            batchOptions.partitionKey = options.getPartitionKey();
        }

        eventDataBatch = partitionSender.createBatch(batchOptions);

        for (int i = 0; i < options.getEvents(); i++) {
            if (!eventDataBatch.tryAdd(EventData.create("Static Event".getBytes()))) {
                throw new Exception(String.format("Batch can only fit %d number of messages with batch size of %d ",
                    options.getCount(), options.getSize()));
            }
        }
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        partitionSender.send(eventDataBatch);
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.fromFuture(partitionSender.send(eventDataBatch));
    }
}
