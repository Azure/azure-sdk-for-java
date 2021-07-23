// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.support;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.integration.core.api.PartitionSupplier;
import com.azure.spring.integration.core.api.StartPosition;
import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.azure.spring.integration.eventhub.impl.EventHubProcessor;
import com.azure.spring.integration.eventhub.impl.EventHubTemplate;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A test implementation of {@link EventHubTemplate}. This is used for testing.
 */
public class EventHubTestOperation extends EventHubTemplate {

    private final Map<String, List<EventData>> eventHubsByName = new HashMap<>();

    private final Map<String, Map<String, EventHubProcessor>> processorsByNameAndGroup =
        new ConcurrentHashMap<>();

    private final Supplier<EventContext> eventContextSupplier;

    public EventHubTestOperation(EventHubClientFactory clientFactory,
                                 Supplier<EventContext> eventContextSupplier) {
        super(clientFactory);
        this.eventContextSupplier = eventContextSupplier;
    }

    @Override
    public <U> Mono<Void> sendAsync(String eventHubName,
                                    @NonNull Message<U> message,
                                    PartitionSupplier partitionSupplier) {
        EventData azureMessage = getMessageConverter().fromMessage(message, EventData.class);

        if (eventHubsByName.containsKey(eventHubName)) {
            eventHubsByName.get(eventHubName).add(azureMessage);
        } else {
            List<EventData> eventDataList = new ArrayList<>(1);
            eventDataList.add(azureMessage);
            eventHubsByName.put(eventHubName, eventDataList);
        }
        processorsByNameAndGroup.putIfAbsent(eventHubName, new ConcurrentHashMap<>());
        processorsByNameAndGroup.get(eventHubName).values().forEach(c -> {
            EventHubProcessorSupport cs = (EventHubProcessorSupport) c;
            cs.onEvent(eventContextSupplier.get(), azureMessage);
        });

        return Mono.empty();
    }

    @Override
    protected synchronized void createEventProcessorClient(String name,
                                                           String consumerGroup,
                                                           EventHubProcessor eventHubProcessor) {
        processorsByNameAndGroup.putIfAbsent(name, new ConcurrentHashMap<>());

        processorsByNameAndGroup.get(name).putIfAbsent(consumerGroup, eventHubProcessor);
    }

    @Override
    protected void startEventProcessorClient(String name, String consumerGroup) {
        if (getStartPosition() == StartPosition.EARLIEST) {
            processorsByNameAndGroup.get(name).values().forEach(c -> {
                EventHubProcessorSupport cs = (EventHubProcessorSupport) c;
                if (eventHubsByName.containsKey(name)) {
                    eventHubsByName.get(name).forEach(m -> cs.onEvent(eventContextSupplier.get(), m));
                }
            });
        }
    }

    @Override
    protected void stopEventProcessorClient(String name, String consumerGroup) {
        processorsByNameAndGroup.get(name).remove(consumerGroup);
    }

    @Override
    public EventHubProcessor createEventProcessor(Consumer<Message<?>> consumer, Class<?> messagePayloadType) {
        return new EventHubProcessorSupport(consumer, messagePayloadType, getCheckpointConfig(), getMessageConverter());
    }
}

