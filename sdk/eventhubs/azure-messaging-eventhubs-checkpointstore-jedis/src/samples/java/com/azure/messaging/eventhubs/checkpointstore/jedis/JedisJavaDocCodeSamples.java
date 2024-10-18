// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;

/**
 * Javadoc samples for {@link JedisCheckpointStore}.
 */
public class JedisJavaDocCodeSamples {
    /**
     * Demonstrates how to instantiate the checkpoint store.
     */
    public void instantiation() {
        // BEGIN: com.azure.messaging.eventhubs.jedischeckpointstore.instantiation
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
            .password("<YOUR_REDIS_PRIMARY_ACCESS_KEY>")
            .ssl(true)
            .build();

        String redisHostName = "<YOUR_REDIS_HOST_NAME>.redis.cache.windows.net";
        HostAndPort hostAndPort = new HostAndPort(redisHostName, 6380);
        JedisPool jedisPool = new JedisPool(hostAndPort, clientConfig);

        CheckpointStore checkpointStore = new JedisCheckpointStore(jedisPool);

        // DefaultAzureCredential tries to resolve the best credential to use based on your environment.
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        EventProcessorClient processorClient = new EventProcessorClientBuilder()
            .checkpointStore(checkpointStore)
            .fullyQualifiedNamespace("<YOUR_EVENT_HUB_FULLY_QUALIFIED_NAMESPACE>")
            .eventHubName("<YOUR_EVENT_HUB_NAME>")
            .credential(credential)
            .consumerGroup("<YOUR_CONSUMER_GROUP_NAME>")
            .processEvent(eventContext -> {
                System.out.println("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
                    + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
            })
            .processError(context -> {
                System.out.println("Error occurred while processing events " + context.getThrowable().getMessage());
            })
            .buildEventProcessorClient();
        // END: com.azure.messaging.eventhubs.jedischeckpointstore.instantiation
    }
}
