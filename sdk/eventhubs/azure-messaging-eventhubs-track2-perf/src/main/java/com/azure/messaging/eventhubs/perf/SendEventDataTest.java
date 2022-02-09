// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sends a number of {@link EventData} to Event Hub.
 */
public class SendEventDataTest extends ServiceTest<EventHubsOptions> {
    private EventHubProducerClient producer;
    private EventHubProducerAsyncClient producerAsync;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public SendEventDataTest(EventHubsOptions options) {
        super(options);
    }

    @Override
    public void run() {
        if (producer == null) {
            producer = createEventHubClientBuilder()
                .buildProducerClient();
        }

        for (final EventData event : events) {
            producer.send(Collections.singleton(event));
        }
    }

    @Override
    public Mono<Void> runAsync() {
        if (producerAsync == null) {
            producerAsync = createEventHubClientBuilder().buildAsyncProducerClient();
        }

        List<Mono<Void>> sendEvents = events.stream()
            .map(eventData -> producerAsync.send(Collections.singleton(eventData)))
            .collect(Collectors.toList());

        return Mono.whenDelayError(sendEvents);
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
