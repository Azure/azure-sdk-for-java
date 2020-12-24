// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.perf;

import com.microsoft.azure.eventhubs.BatchOptions;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventDataBatch;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.perf.core.EventHubsPerfStressOptions;
import com.microsoft.azure.eventhubs.perf.core.ServiceTest;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Runs the Send Events Batch Performance Test for EventHubs.
 */
public class SendEventBatchPartitionTest extends ServiceTest<EventHubsPerfStressOptions> {
    private final BatchOptions batchOptions;
    private EventDataBatch eventDataBatch;
    private PartitionSender partitionSender;

    /**
     * Instantiates the instance of the Send Event Batch Partition test.
     *
     * @param options the options bag to use for performance testing.
     * @throws Exception when an error occurs when creating event batch.
     */
    public SendEventBatchPartitionTest(EventHubsPerfStressOptions options) throws Exception {
        super(options);
        batchOptions = new BatchOptions();
        BatchOptions batchOptions = new BatchOptions();

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
                partitionSender = eventHubClient.createPartitionSender(String.valueOf(options.getPartitionId())).get();
                eventDataBatch = partitionSender.createBatch(batchOptions);
                EventData eventData =  EventData.create(generateString(options.getMessageSize())
                    .getBytes(StandardCharsets.UTF_8));

                for (int i = 0; i < options.getEvents(); i++) {
                    if (!eventDataBatch.tryAdd(eventData)) {
                        throw new Exception(String.format("Batch can only fit %d number of messages with batch "
                                + "size of %d ", options.getCount(), options.getSize()));
                    }
                }
                return 1;
            }))
            .then();
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

    @Override
    public Mono<Void> cleanupAsync() {
        return Mono.fromCallable(() -> {
            partitionSender.close();
            return 1;
        }).then(super.cleanupAsync());
    }
}
