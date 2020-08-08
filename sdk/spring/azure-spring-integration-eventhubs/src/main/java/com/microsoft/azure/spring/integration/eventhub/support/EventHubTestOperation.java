// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.eventhub.support;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventContext;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import com.microsoft.azure.spring.integration.core.api.StartPosition;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.impl.EventHubProcessor;
import com.microsoft.azure.spring.integration.eventhub.impl.EventHubTemplate;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EventHubTestOperation extends EventHubTemplate {

    private final Multimap<String, EventData> eventHubsByName = ArrayListMultimap.create();

    private final Map<String, Map<String, EventHubProcessor>> processorsByNameAndGroup =
        new ConcurrentHashMap<>();

    private final Supplier<EventContext> eventContextSupplier;

    public EventHubTestOperation(EventHubClientFactory clientFactory,
                                 Supplier<EventContext> eventContextSupplier) {
        super(clientFactory);
        this.eventContextSupplier = eventContextSupplier;
    }

    @Override
    public <U> Mono<Void> sendAsync(String eventHubName, @NonNull Message<U> message,
                                    PartitionSupplier partitionSupplier) {
        EventData azureMessage = getMessageConverter().fromMessage(message, EventData.class);

        eventHubsByName.put(eventHubName, azureMessage);
        processorsByNameAndGroup.putIfAbsent(eventHubName, new ConcurrentHashMap<>());
        processorsByNameAndGroup.get(eventHubName).values().forEach(c -> {
            EventHubProcessorSupport cs = (EventHubProcessorSupport) c;
            cs.onEvent(eventContextSupplier.get(), azureMessage);
        });

        return Mono.empty();
    }

    @Override
    protected synchronized void createEventProcessorClient(String name, String consumerGroup,
                                                           EventHubProcessor eventHubProcessor) {
        processorsByNameAndGroup.putIfAbsent(name, new ConcurrentHashMap<>());

        processorsByNameAndGroup.get(name).putIfAbsent(consumerGroup, eventHubProcessor);
    }

    @Override
    protected void startEventProcessorClient(String name, String consumerGroup) {
        if (getStartPosition() == StartPosition.EARLIEST) {
            processorsByNameAndGroup.get(name).values().forEach(c -> {
                EventHubProcessorSupport cs = (EventHubProcessorSupport) c;
                eventHubsByName.get(name).forEach(m -> cs.onEvent(eventContextSupplier.get(), m));
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

