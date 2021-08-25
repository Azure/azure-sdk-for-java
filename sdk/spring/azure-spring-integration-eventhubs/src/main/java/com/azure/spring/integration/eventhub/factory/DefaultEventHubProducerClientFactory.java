package com.azure.spring.integration.eventhub.factory;

import com.azure.core.util.ClientOptions;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.spring.cloud.context.core.util.Memoizer;
import com.azure.spring.integration.eventhub.api.ProducerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.azure.spring.cloud.context.core.util.Constants.SPRING_EVENT_HUB_APPLICATION_ID;

public class DefaultEventHubProducerClientFactory implements ProducerFactory, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEventHubProducerClientFactory.class);

    private final Map<String, EventHubProducerAsyncClient> producerClientMap = new ConcurrentHashMap<>();

    private final String eventHubConnectionString;

    private final Function<String, EventHubProducerAsyncClient> producerClientCreator =
        Memoizer.memoize(producerClientMap, this::createProducerClient);

    public DefaultEventHubProducerClientFactory(@NonNull String eventHubConnectionString) {
        this.eventHubConnectionString = eventHubConnectionString;
    }

    private EventHubProducerAsyncClient createProducerClient(String eventHubName) {
        return new EventHubClientBuilder()
            .connectionString(eventHubConnectionString, eventHubName)
            .clientOptions(new ClientOptions().setApplicationId(SPRING_EVENT_HUB_APPLICATION_ID))
            .buildAsyncProducerClient();
    }

    @Override
    public void destroy() {
        producerClientMap.values().forEach(producerClient -> {
            try {
                producerClient.close();
            } catch (Exception ex) {
                LOGGER.warn("Failed to clean event hub client factory", ex);
            }
        });
    }

    @Override
    public EventHubProducerAsyncClient getOrCreateProducerClient(String eventHubName) {
        return this.producerClientCreator.apply(eventHubName);
    }
}
