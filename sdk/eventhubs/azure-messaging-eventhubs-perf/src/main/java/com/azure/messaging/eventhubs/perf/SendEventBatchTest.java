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
    private final EventDataBatch eventDataBatch;

    /**
     *
     * @param options
     * @throws Exception
     */
    public SendEventBatchTest(EventHubsPerfStressOptions options) throws Exception {
        super(options);
        if (options.getBatchSize() != null) {
            CreateBatchOptions batchOptions = new CreateBatchOptions()
                .setMaximumSizeInBytes(Long.valueOf(options.getSize()).intValue());
            eventDataBatch = eventHubProducerClient.createBatch(batchOptions);
        } else {
            eventDataBatch = eventHubProducerClient.createBatch();
        }

        for (int i = 0; i < options.getEvents(); i++) {
            if (!eventDataBatch.tryAdd(new EventData("static event"))) {
                throw new Exception(String.format("Batch can only fit %d number of messages with batch size of %d ",
                    options.getCount(), options.getSize()));
            }
        }
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        eventHubProducerClient.send(eventDataBatch);
    }

    @Override
    public Mono<Void> runAsync() {
        return eventHubProducerAsyncClient.send(eventDataBatch);
    }
}
