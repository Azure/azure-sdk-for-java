// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeUnit;

/**
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {

    /**
     * Code sample for creating a synchronous Event Hub producer.
     */
    public void createJedis() {
        // BEGIN: readme-sample-createJedis
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
            .password("<YOUR_REDIS_PRIMARY_ACCESS_KEY>")
            .ssl(true)
            .build();

        String redisHostName = "<YOUR_REDIS_HOST_NAME>.redis.cache.windows.net";
        HostAndPort hostAndPort = new HostAndPort(redisHostName, 6380);
        JedisPool jedisPool = new JedisPool(hostAndPort, clientConfig);

        // Do things with JedisPool.

        // Finally, dispose of resource
        jedisPool.close();
        // END: readme-sample-createJedis
    }

    /**
     * Code sample for creating an EventProcessorClient with Redis checkpoint library.
     */
    public void createCheckpointStore() throws InterruptedException {
        // BEGIN: readme-sample-createCheckpointStore
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
            .checkpointStore(new JedisRedisCheckpointStore(jedisPool))
            .processEvent(eventContext -> {
                System.out.println("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
                    + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
            })
            .processError(context -> {
                System.out.println("Error occurred while processing events " + context.getThrowable().getMessage());
            })
            .buildEventProcessorClient();

        // This will start the processor. It will start processing events from all partitions.
        eventProcessorClient.start();

        // (for demo purposes only - adding sleep to wait for receiving events)
        // Your application will probably keep the eventProcessorClient alive until the program ends.
        TimeUnit.SECONDS.sleep(2);

        // When the user wishes to stop processing events, they can call `stop()`.
        eventProcessorClient.stop();

        // Dispose of JedisPool resource.
        jedisPool.close();
        // END: readme-sample-createCheckpointStore
    }
}
