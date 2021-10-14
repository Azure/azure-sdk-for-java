// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.core.util.ClientOptions;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.core.util.Memoizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.azure.spring.core.ApplicationId.AZURE_SPRING_EVENT_HUB;
import static com.azure.spring.core.ApplicationId.VERSION;


/**
 * Default implementation of {@link EventHubClientFactory}.
 *
 * @author Warren Zhu
 * @author Xiaolu Dai
 */
public class DefaultEventHubClientFactory implements EventHubClientFactory, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEventHubClientFactory.class);

    // Maps used for cache and clean up clients
    // (eventHubName, consumerGroup) -> consumerClient
    private final Map<Tuple2<String, String>, EventHubConsumerAsyncClient> consumerClientMap = new ConcurrentHashMap<>();
    // eventHubName -> producerClient
    private final Map<String, EventHubProducerAsyncClient> producerClientMap = new ConcurrentHashMap<>();
    // (eventHubName, consumerGroup) -> eventProcessorClient
    private final Map<Tuple2<String, String>, EventProcessorClient> processorClientMap = new ConcurrentHashMap<>();
    // Memoized functional client creator
    private final BiFunction<String, String, EventHubConsumerAsyncClient> eventHubConsumerClientCreator =
        Memoizer.memoize(consumerClientMap, this::createEventHubClient);
    private final Function<String, EventHubProducerAsyncClient> producerClientCreator =
        Memoizer.memoize(producerClientMap, this::createProducerClient);

    // TODO (xiada) this will share credential across different event hubs, but they could have different credentials
    private final EventHubSharedAuthenticationClientBuilder eventHubServiceClientBuilder;
    private EventProcessorSharedAuthenticationClientBuilder eventProcessorServiceClientBuilder;

    public DefaultEventHubClientFactory(EventHubSharedAuthenticationClientBuilder eventHubClientBuilder) {
        this.eventHubServiceClientBuilder = eventHubClientBuilder;
    }

    public void setEventProcessorServiceClientBuilder(EventProcessorSharedAuthenticationClientBuilder eventProcessorServiceClientBuilder) {
        this.eventProcessorServiceClientBuilder = eventProcessorServiceClientBuilder;
    }

    private EventHubConsumerAsyncClient createEventHubClient(String eventHubName, String consumerGroup) {
        return eventHubServiceClientBuilder
            .eventHubName(eventHubName)
            .consumerGroup(consumerGroup)
            //TODO (xiada) the client options here
            .clientOptions(new ClientOptions().setApplicationId(AZURE_SPRING_EVENT_HUB + VERSION))
            .buildAsyncConsumerClient();
    }

    private EventHubProducerAsyncClient createProducerClient(String eventHubName) {
        return eventHubServiceClientBuilder
            .eventHubName(eventHubName)
            //TODO (xiada) the client options here
            .clientOptions(new ClientOptions().setApplicationId(AZURE_SPRING_EVENT_HUB + VERSION))
            .buildAsyncProducerClient();
    }

    private EventProcessorClient createEventProcessorClientInternal(String eventHubName,
                                                                    String consumerGroup,
                                                                    EventHubProcessor eventHubProcessor) {
        if (this.eventProcessorServiceClientBuilder == null) {
            throw new IllegalStateException("Event processor is not configured so no EventProcessorClient could be created");
        }

        return eventProcessorServiceClientBuilder
            .eventHubName(eventHubName)
            .consumerGroup(consumerGroup)
            .processPartitionInitialization(eventHubProcessor::onInitialize)
            .processPartitionClose(eventHubProcessor::onClose)
            .processEvent(eventHubProcessor::onEvent)
            .processError(eventHubProcessor::onError)
            .buildEventProcessorClient();
    }

    private <K, V> void close(Map<K, V> map, Consumer<V> close) {
        map.values().forEach(it -> {
            try {
                close.accept(it);
            } catch (Exception ex) {
                LOGGER.warn("Failed to clean event hub client factory", ex);
            }
        });
    }

    @Override
    public void destroy() {
        close(consumerClientMap, EventHubConsumerAsyncClient::close);
        close(producerClientMap, EventHubProducerAsyncClient::close);
        close(processorClientMap, EventProcessorClient::stop);
    }

    @Override
    public EventHubConsumerAsyncClient getOrCreateConsumerClient(String eventHubName, String consumerGroup) {
        return this.eventHubConsumerClientCreator.apply(eventHubName, consumerGroup);
    }

    @Override
    public EventHubProducerAsyncClient getOrCreateProducerClient(String eventHubName) {
        return this.producerClientCreator.apply(eventHubName);
    }

    @Override
    public EventProcessorClient createEventProcessorClient(String eventHubName, String consumerGroup,
                                                           EventHubProcessor processor) {
        return processorClientMap.computeIfAbsent(Tuples.of(eventHubName, consumerGroup), (t) ->
            createEventProcessorClientInternal(eventHubName, consumerGroup, processor));
    }

    @Override
    public Optional<EventProcessorClient> getEventProcessorClient(String eventHubName, String consumerGroup) {
        return Optional.ofNullable(this.processorClientMap.get(Tuples.of(eventHubName, consumerGroup)));
    }

    @Override
    public EventProcessorClient removeEventProcessorClient(String eventHubName, String consumerGroup) {
        return this.processorClientMap.remove(Tuples.of(eventHubName, consumerGroup));
    }
}
