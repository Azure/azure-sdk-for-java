package com.azure.messaging.eventhubs.perf;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.perf.core.EventHubsPerfOptions;
import com.azure.messaging.eventhubs.perf.core.ServiceBatchTest;
import com.azure.messaging.eventhubs.perf.core.Util;
import reactor.core.publisher.Mono;

/**
 * Runs the Send Events Batch Performance Test for EventHubs.
 */
public class SendEventBatchTest extends ServiceBatchTest<EventHubsPerfOptions> {
    private final CreateBatchOptions createBatchOptions;
    private EventDataBatch eventDataBatch;
    private EventDataBatch eventDataBatchAsync;

    /**
     * Runs the Send Events in Batch peformance test.
     *
     * @param options The options bag to use for testing.
     * @throws IllegalStateException when the specified number of messages cannot fit in the specified batch size.
     */
    public SendEventBatchTest(EventHubsPerfOptions options) throws IllegalStateException {
        super(options);
        createBatchOptions = getBatchOptions(options);
    }

    @Override
    public int runBatch() {
        eventHubProducerClient.send(eventDataBatch);
        return options.getEvents();
    }

    @Override
    public Mono<Integer> runBatchAsync() {
        return eventHubProducerAsyncClient.send(eventDataBatchAsync)
            .then(Mono.defer(() -> Mono.just(options.getEvents())));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(Mono.fromCallable(() -> {
                if (createBatchOptions != null) {
                    eventDataBatch = eventHubProducerClient.createBatch(createBatchOptions);
                } else {
                    eventDataBatch = eventHubProducerClient.createBatch();
                }
                addEventsToBatch(eventDataBatch);
                return 1;
            })).then(Mono.defer(() -> createBatchOptions != null
                ? eventHubProducerAsyncClient.createBatch(createBatchOptions)
                : eventHubProducerAsyncClient.createBatch()
            )).map(eventBatch -> {
                addEventsToBatch(eventBatch);
                eventDataBatchAsync = eventBatch;
                return Mono.empty();
            }).then();
    }

    private void addEventsToBatch(EventDataBatch eventDataBatch) {
        EventData eventData = new EventData(Util.generateString(options.getMessageSize()));
        for (int i = 0; i < options.getEvents(); i++) {
            if (!eventDataBatch.tryAdd(eventData)) {
                throw new IllegalStateException(String.format(
                    "Batch can only fit %d number of messages with batch size of %d ",
                    options.getCount(), options.getSize()));
            }
        }
    }

    private CreateBatchOptions getBatchOptions(EventHubsPerfOptions options) {
        CreateBatchOptions createBatchOptions = new CreateBatchOptions();
        boolean returnBatchOptions = false;
        if (options.getBatchSize() != null) {
            createBatchOptions.setMaximumSizeInBytes(options.getBatchSize());
            returnBatchOptions = true;
        }
        if (options.getPartitionId() != null) {
            createBatchOptions.setPartitionId(String.valueOf(options.getBatchSize()));
            returnBatchOptions = true;
        }

        if (options.getPartitionKey() != null) {
            createBatchOptions.setPartitionKey(options.getPartitionKey());
            returnBatchOptions = true;
        }

        if (returnBatchOptions) {
            return createBatchOptions;
        }
        return null;
    }
}
