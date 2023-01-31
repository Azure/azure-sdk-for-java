// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Test for EventSender
 */
@Component("EventSender")
public class EventSender extends EventHubsScenario {

    private static final Random RANDOM = new Random();

    @Value("${SEND_TIMES:1000000}")
    private int sendTimes;

    @Value("${SEND_EVENTS:100}")
    private int eventsToSend;

    @Value("${PAYLOAD_SIZE_IN_BYTE:8}")
    private int payloadSize;

    @Override
    public void run() {
        final String eventHubConnStr = options.getEventhubsConnectionString();
        final String eventHub = options.getEventhubsEventHubName();

        final byte[] payload = new byte[payloadSize];
        RANDOM.nextBytes(payload);

        EventHubProducerAsyncClient client = new EventHubClientBuilder()
            .connectionString(eventHubConnStr, eventHub)
            .buildAsyncProducerClient();

        Flux.range(0, sendTimes).concatMap(i -> {
            List<EventData> eventDataList = new ArrayList<>();
            IntStream.range(0, eventsToSend).forEach(j -> {
                eventDataList.add(new EventData(payload));
            });
            return client.send(eventDataList);
        }).blockLast();

        client.close();
    }
}
