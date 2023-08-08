// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Sample that demonstrates the use of {@link JedisCheckpointStoreSample} for storing and updating partition
 * ownership records in Azure Redis Cache.
 */
public class JedisCheckpointStoreSample {
    private static final String EVENT_HUB_NAMESPACE = "{your-namespace}.servicebus.windows.net";
    private static final String EVENT_HUB_NAME = "{event-hub-name}";
    private static final String CONSUMER_GROUP = "$DEFAULT";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    /**
     * The main method to run the sample.
     *
     * @param args Unused arguments given to the sample
     */
    public static void main(String[] args) {

        // To create the JedisRedisCheckpointStore, an instance of JedisPool is required.
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

        System.out.println("1. Listing existing partition ownerships.");

        // listCheckpoints returns a Flux<PartitionOwnership>, this is a non-blocking call.  When the Flux is
        // constructed, it will move to the next line in the program.  For purposes of the demo, block() is chained at
        // the end to make it a synchronous call.
        List<PartitionOwnership> existingOwnerships = checkpointStore.listOwnership(EVENT_HUB_NAMESPACE, EVENT_HUB_NAME,
                CONSUMER_GROUP)
            .collectList()
            .block(TIMEOUT);

        printOwnerships(existingOwnerships);

        System.out.println("2. Updating checkpoint.");

        Checkpoint checkpoint = new Checkpoint()
            .setFullyQualifiedNamespace(EVENT_HUB_NAMESPACE)
            .setEventHubName(EVENT_HUB_NAME)
            .setConsumerGroup(CONSUMER_GROUP)
            .setPartitionId("0")
            .setSequenceNumber(2L)
            .setOffset(250L);

        checkpointStore.updateCheckpoint(checkpoint).block(TIMEOUT);

        System.out.println("3. Claiming ownership.");

        PartitionOwnership partitionOwnership = new PartitionOwnership()
            .setFullyQualifiedNamespace(EVENT_HUB_NAMESPACE)
            .setEventHubName(EVENT_HUB_NAME)
            .setConsumerGroup(CONSUMER_GROUP)
            .setPartitionId("0")
            .setOwnerId("test-owner-id")
            .setETag(UUID.randomUUID().toString())
            .setLastModifiedTime(Instant.now().getEpochSecond());

        List<PartitionOwnership> updatedOwnerships = checkpointStore.claimOwnership(
                Collections.singletonList(partitionOwnership))
            .collectList()
            .block(TIMEOUT);

        printOwnerships(updatedOwnerships);
    }

    private static void printOwnerships(List<PartitionOwnership> ownerships) {
        if (ownerships == null || ownerships.isEmpty()) {
            System.out.println("\tThere were no ownerships to print out.");
            return;
        }

        System.out.println("\tPrinting ownerships:");

        for (PartitionOwnership ownership : ownerships) {
            System.out.printf("\t- partitionId[%s] ownerId[%s] eTag[%s] lastModified[%s]%n",
                ownership.getPartitionId(), ownership.getOwnerId(), ownership.getETag(),
                ownership.getLastModifiedTime());
        }
    }
}
