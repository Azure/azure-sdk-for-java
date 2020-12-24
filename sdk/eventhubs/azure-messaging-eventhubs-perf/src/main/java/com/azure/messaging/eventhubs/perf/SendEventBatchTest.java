// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.perf.core.EventHubsPerfStressOptions;
import com.azure.messaging.eventhubs.perf.core.ServiceTest;
import reactor.core.publisher.Mono;

/**
 * Runs the Send Events Batch Performance Test for EventHubs.
 */
public class SendEventBatchTest extends ServiceTest<EventHubsPerfStressOptions> {
    private final CreateBatchOptions createBatchOptions;
    private EventDataBatch eventDataBatch;
    private EventDataBatch eventDataBatchAsync;

    /**
     * Runs the Send Events in Batch peformance test.
     *
     * @param options The options bag to use for testing.
     * @throws IllegalStateException when the specified number of messages cannot fit in the specified batch size.
     */
    public SendEventBatchTest(EventHubsPerfStressOptions options) throws IllegalStateException {
        super(options);
        createBatchOptions = getBatchOptions(options);
//        if (createBatchOptions != null) {
//            eventDataBatch = eventHubProducerClient.createBatch(createBatchOptions);
//        } else {
//            eventDataBatch = eventHubProducerClient.createBatch();
//        }
//
//        EventData eventData = new EventData("static event");
//        for (int i = 0; i < options.getEvents(); i++) {
//            if (!eventDataBatch.tryAdd(eventData)) {
//                throw new IllegalStateException(String.format(
//                    "Batch can only fit %d number of messages with batch size of %d ",
//                    options.getCount(), options.getSize()));
//            }
//        }
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
        EventData eventData = new EventData("static event");
        for (int i = 0; i < options.getEvents(); i++) {
            if (!eventDataBatch.tryAdd(eventData)) {
                throw new IllegalStateException(String.format(
                    "Batch can only fit %d number of messages with batch size of %d ",
                    options.getCount(), options.getSize()));
            }
        }
    }

    private CreateBatchOptions getBatchOptions(EventHubsPerfStressOptions options) {
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

    // Perform the API call to be tested here
    @Override
    public void run() {
        eventHubProducerClient.send(eventDataBatch);
    }

    @Override
    public Mono<Void> runAsync() {
        return eventHubProducerAsyncClient.send(eventDataBatchAsync);
    }
}
