// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
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
 * Sample for using {@link JedisCheckpointStore} with {@link EventProcessorClient}.
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
        // To create the JedisCheckpointStore, an instance of JedisPool is required.
        // 1. Create a redis service.  The following link describes how to create one for Azure Redis Cache.
        //    https://learn.microsoft.com/azure/azure-cache-for-redis/quickstart-create-redis
        // 2. Go to your Azure Redis service.
        // 3. The host name is on the main page.  It will look similar to "{your-hostname}.redis.cache.windows.net"
        // 4. Under Settings, select Access keys.  The primary or secondary key is the password.
        HostAndPort hostAndPort = new HostAndPort("{your-hostname}.redis.cache.windows.net", 6380);
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
            .password("{your-access-key}")
            .ssl(true)
            .build();
        JedisPool jedisPool = new JedisPool(hostAndPort, clientConfig);

        // Instantiate an instance of the checkpoint store with configured JedisPool.
        CheckpointStore checkpointStore = new JedisCheckpointStore(jedisPool);

        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .connectionString("event-hub-namespace-connection-string}", "{event-hub-name}")
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .processEvent(eventContext -> onEvent(eventContext))
            .processError(errorContext -> onError(errorContext))
            .checkpointStore(checkpointStore)
            .buildEventProcessorClient();

        // Starts the event processor
        eventProcessorClient.start();

        // Perform other tasks while the event processor is processing events in the background.
        TimeUnit.MINUTES.sleep(5);

        // Stops the event processor
        eventProcessorClient.stop();
    }

    public static void onEvent(EventContext eventContext) {
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
