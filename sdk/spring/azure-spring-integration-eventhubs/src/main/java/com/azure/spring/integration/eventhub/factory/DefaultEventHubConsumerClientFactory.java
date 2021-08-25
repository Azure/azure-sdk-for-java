package com.azure.spring.integration.eventhub.factory;

import com.azure.core.util.ClientOptions;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.spring.cloud.context.core.util.Memoizer;
import com.azure.spring.cloud.context.core.util.Tuple;
import com.azure.spring.integration.eventhub.api.ConsumerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import static com.azure.spring.cloud.context.core.util.Constants.SPRING_EVENT_HUB_APPLICATION_ID;

public class DefaultEventHubConsumerClientFactory implements ConsumerFactory, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEventHubConsumerClientFactory.class);

    private final Map<Tuple<String, String>, EventHubConsumerAsyncClient> consumerClientMap = new ConcurrentHashMap<>();

    private final String eventHubConnectionString;

    private final BiFunction<String, String, EventHubConsumerAsyncClient> eventHubConsumerClientCreator =
        Memoizer.memoize(consumerClientMap, this::createEventHubConsumerClient);


    public DefaultEventHubConsumerClientFactory(@NonNull String eventHubConnectionString) {
        this.eventHubConnectionString = eventHubConnectionString;
    }

    private EventHubConsumerAsyncClient createEventHubConsumerClient(String eventHubName, String consumerGroup) {
        return new EventHubClientBuilder()
            .connectionString(eventHubConnectionString, eventHubName)
            .consumerGroup(consumerGroup)
            .clientOptions(new ClientOptions().setApplicationId(SPRING_EVENT_HUB_APPLICATION_ID))
            .buildAsyncConsumerClient();
    }

    @Override
    public void destroy() {
        consumerClientMap.values().forEach(consumerClient -> {
            try {
                consumerClient.close();
            } catch (Exception ex) {
                LOGGER.warn("Failed to clean event hub client factory", ex);
            }
        });
    }

    @Override
    public EventHubConsumerAsyncClient getOrCreateConsumerClient(String eventHubName, String consumerGroup) {
        return this.eventHubConsumerClientCreator.apply(eventHubName, consumerGroup);
    }

}
