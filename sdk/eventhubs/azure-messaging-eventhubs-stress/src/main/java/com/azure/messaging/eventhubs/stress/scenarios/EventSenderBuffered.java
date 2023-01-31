// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubBufferedProducerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Test for EventSenderBuffered
 */
@Component("EventSenderBuffered")
public class EventSenderBuffered extends EventHubsScenario {
    private static final ClientLogger LOGGER = new ClientLogger(EventSenderBuffered.class);

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

        final EventHubBufferedProducerAsyncClient sender = new EventHubBufferedProducerClientBuilder()
            .connectionString(eventHubConnStr, eventHub)
            .onSendBatchFailed(context -> {
                final String numberOfEvents;

                if (context.getEvents() == null) {
                    numberOfEvents = "N/A";
                } else {
                    final Stream<EventData> stream = StreamSupport.stream(context.getEvents().spliterator(),
                        false);
                    numberOfEvents = String.valueOf(stream.count());
                }

                LOGGER.warning("partitionId[{}] # events[{}] Unable to publish events. ",
                    context.getPartitionId(), numberOfEvents, context.getThrowable());
            })
            .onSendBatchSucceeded(context -> {
                LOGGER.verbose("Send success.");
            })
            .buildAsyncClient();

        final byte[] payload = new byte[payloadSize];
        RANDOM.nextBytes(payload);

        Flux.range(0, sendTimes).concatMap(i -> {
            List<EventData> eventDataList = new ArrayList<>();
            IntStream.range(0, eventsToSend).forEach(j -> {
                eventDataList.add(new EventData(payload));
            });

            return sender.enqueueEvents(eventDataList);
        }).blockLast();

        sender.close();
    }
}
