// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeUnit;

/**
 * Sample for using {@link JedisRedisCheckpointStore} with {@link EventProcessorClient}.
 */
public class EventProcessorClientJedisSample {
    /**
     * The main method to run the sample.
     *
     * @param args Unused arguments given to the sample
     *
     * @throws Exception an Exception will be thrown in case of errors while running the sample
     */
    public static void main(String[] args) throws Exception {
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
            .password("<YOUR_REDIS_PRIMARY_ACCESS_KEY>")
            .ssl(true)
            .build();

        String redisHostName = "<YOUR_REDIS_HOST_NAME>.redis.cache.windows.net";
        HostAndPort hostAndPort = new HostAndPort(redisHostName, 6380);
        JedisPool jedisPool = new JedisPool(hostAndPort, clientConfig);

        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .consumerGroup("<< CONSUMER GROUP NAME >>")
            .connectionString("<< EVENT HUB NAMESPACE CONNECTION STRING >>")
            .eventHubName("<< EVENT HUB NAME >>")
            .processEvent(eventContext -> onEvent(eventContext))
            .processError(errorContext -> onError(errorContext))
            .checkpointStore(new JedisRedisCheckpointStore(jedisPool))
            .buildEventProcessorClient();

        // Starts the event processor
        eventProcessorClient.start();

        // Perform other tasks while the event processor is processing events in the background.
        TimeUnit.MINUTES.sleep(5);

        // Stops the event processor
        eventProcessorClient.stop();
    }

    private static void onEvent(EventContext eventContext) {
        System.out.printf("Processing event from partition %s with sequence number %d %n",
            eventContext.getPartitionContext().getPartitionId(), eventContext.getEventData().getSequenceNumber());
        if (eventContext.getEventData().getSequenceNumber() % 10 == 0) {
            eventContext.updateCheckpoint();
        }
    }

    private static void onError(ErrorContext errorContext) {
        System.out.printf("Error occurred in partition processor for partition %s, %s.%n",
            errorContext.getPartitionContext().getPartitionId(),
            errorContext.getThrowable());
    }
}
