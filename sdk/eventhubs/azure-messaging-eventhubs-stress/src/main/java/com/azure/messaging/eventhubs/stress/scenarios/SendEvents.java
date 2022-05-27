// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.stress.util.Constants;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service("SendEvents")
public class SendEvents extends EventHubsScenario {

    private final int sendTimes = 1000;
    private final int eventNumber =  500;

    @Override
    public void run() {
        final String eventHubConnStr = options.get(Constants.EVENT_HUBS_CONNECTION_STRING);
        final String eventHub = options.get(Constants.EVENT_HUB_NAME);

        EventHubProducerAsyncClient client = new EventHubClientBuilder()
                .connectionString(eventHubConnStr, eventHub)
                .buildAsyncProducerClient();

        Flux.range(0, sendTimes).concatMap(i -> {
            List<EventData> eventDataList = new ArrayList<>();
            IntStream.range(0, eventNumber).forEach(j -> {
                eventDataList.add(new EventData("A"));
            });
            return client.send(eventDataList);
        }).subscribe();
    }
}
