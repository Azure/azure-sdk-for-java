// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Service("EventSender")
public class EventSender extends EventHubsScenario {

    private static final int SEND_TIMES = 10000;
    private static final int EVENT_NUMBER = 500;
    private static final int PAYLOAD_SIZE = 4 * 1024;

    @Override
    public void run() {
        final String eventHubConnStr = options.getEventhubsConnectionString();
        final String eventHub = options.getEventhubsEventHubName();

        final byte[] payload = new byte[PAYLOAD_SIZE];
        (new Random()).nextBytes(payload);

        EventHubProducerAsyncClient client = new EventHubClientBuilder()
            .connectionString(eventHubConnStr, eventHub)
            .buildAsyncProducerClient();

        Flux.range(0, SEND_TIMES).concatMap(i -> {
            List<EventData> eventDataList = new ArrayList<>();
            IntStream.range(0, EVENT_NUMBER).forEach(j -> {
                eventDataList.add(new EventData(payload));
            });
            return client.send(eventDataList);
        }).subscribe();
    }
}
