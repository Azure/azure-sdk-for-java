// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Sample for using {@link JedisRedisCheckpointStore} with {@link EventProcessorClient}.
 */
public class EventProcessorJedisRedisCheckpointStoreSample {
    private static final int PORT = 6380; //default SSL port for Azure Redis Cache
    private static final String HOST_NAME = "t"; // For Azure Redis Cache, this will look like '....redis.cache.windows.net'
    private static final String PASSWORD = ""; //Primary Key used for connecting to Azure Redis Cache
    private static final String EH_CONNECTION_STRING = ""; //Connection String for Event Hubs
    private static final JedisPoolConfig POOL_CONFIG = new JedisPoolConfig();
    private static final JedisPool JEDIS_POOL = new JedisPool(POOL_CONFIG, HOST_NAME, PORT, 1000, 1000, PASSWORD, Protocol.DEFAULT_DATABASE, "clientname", true, null, null, null);

    public static final Consumer<EventContext> PARTITION_PROCESSOR = eventContext -> {
        System.out.printf("Processing event from partition %s with sequence number %d %n",
                eventContext.getPartitionContext().getPartitionId(), eventContext.getEventData().getSequenceNumber());
        if (eventContext.getEventData().getSequenceNumber() % 10 == 0) {
            eventContext.updateCheckpoint();
        }
    };

    public static final Consumer<ErrorContext> ERROR_HANDLER = errorContext -> {
        System.out.printf("Error occurred in partition processor for partition %s, %s.%n",
                errorContext.getPartitionContext().getPartitionId(),
                errorContext.getThrowable());
    };

    /**
     * The main method to run the sample.
     *
     * @param args Unused arguments given to the sample
     * @throws Exception an Exception will be thrown in case of errors while running the sample
     */
    public static void main(String[] args) throws Exception {

        JedisRedisCheckpointStore jedisRedisCheckpointStore = new JedisRedisCheckpointStore(JEDIS_POOL);
        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
                .connectionString(EH_CONNECTION_STRING)
                .consumerGroup("") // add Consumer Group name here
                .processEvent(PARTITION_PROCESSOR)
                .processError(ERROR_HANDLER)
                .checkpointStore(jedisRedisCheckpointStore)
                .buildEventProcessorClient();

        // Starts the event processor
        eventProcessorClient.start();

        // Perform other tasks while the event processor is processing events in the background.
        TimeUnit.MINUTES.sleep(5);

        // Stops the event processor
        eventProcessorClient.stop();
    }
}
