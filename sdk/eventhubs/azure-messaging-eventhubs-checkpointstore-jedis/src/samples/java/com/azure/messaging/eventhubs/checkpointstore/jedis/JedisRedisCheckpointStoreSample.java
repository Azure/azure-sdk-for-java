// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Sample that demonstrates the use of {@link JedisRedisCheckpointStoreSample} for storing and updating partition
 * ownership records using Redis cache.
 */
public class JedisRedisCheckpointStoreSample {
    private static final String FULLY_QUALIFIED_NAMESPACE = "example-eh-namespace.servicebus.windows.net";
    private static final String EVENT_HUB_NAME = "my-event-hub";
    private static final String CONSUMER_GROUP = "my-consumer-group";

    /**
     * Maximum time to wait for operation to complete.
     */
    private static final Duration TIMEOUT = Duration.ofMinutes(2);

    /**
     * The main method to run the sample.
     *
     * @param args Unused arguments given to the sample
     */
    public static void main(String[] args) {
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
            .password("<YOUR_REDIS_PRIMARY_ACCESS_KEY>")
            .ssl(true)
            .build();

        String redisHostName = "<YOUR_REDIS_HOST_NAME>.redis.cache.windows.net";
        HostAndPort hostAndPort = new HostAndPort(redisHostName, 6380);
        JedisPool jedisPool = new JedisPool(hostAndPort, clientConfig);

        JedisRedisCheckpointStore jedisRedisCheckpointStore = new JedisRedisCheckpointStore(jedisPool);

        System.out.println("1. Printing existing partition ownership information.");

        // block() turns the Flux<PartitionOwnership> into a blocking, synchronous call for the sake of this demo.
        // In practise, customers can chain the Flux with other reactive operators to transform the results.
        List<PartitionOwnership> ownershipInformation = jedisRedisCheckpointStore
            .listOwnership(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP)
            .collectList()
            .block(TIMEOUT);

        ownershipInformation.forEach(info -> printPartitionOwnership(info));

        System.out.println("2. Updating sample checkpoint.");

        Checkpoint checkpoint = new Checkpoint()
            .setFullyQualifiedNamespace(FULLY_QUALIFIED_NAMESPACE)
            .setConsumerGroup(CONSUMER_GROUP)
            .setEventHubName(EVENT_HUB_NAME)
            .setPartitionId("0")
            .setSequenceNumber(2L)
            .setOffset(250L);

        jedisRedisCheckpointStore.updateCheckpoint(checkpoint).block(TIMEOUT);

        System.out.println("3. Claiming checkpoint.");
        PartitionOwnership partitionOwnership = new PartitionOwnership()
            .setFullyQualifiedNamespace(FULLY_QUALIFIED_NAMESPACE)
            .setConsumerGroup(CONSUMER_GROUP)
            .setEventHubName(EVENT_HUB_NAME)
            .setPartitionId("0");

        List<PartitionOwnership> claimedPartitions = jedisRedisCheckpointStore
            .claimOwnership(Collections.singletonList(partitionOwnership))
            .collectList()
            .block(TIMEOUT);

        claimedPartitions.forEach(info -> {
            System.out.printf("Successfully claimed partitionId: %s%n", info.getPartitionId());
        });
    }

    private static void printPartitionOwnership(PartitionOwnership partitionOwnership) {
        String po =
            new StringJoiner(",")
                .add("pid=" + partitionOwnership.getPartitionId())
                .add("ownerId=" + partitionOwnership.getOwnerId())
                .add("cg=" + partitionOwnership.getConsumerGroup())
                .add("eh=" + partitionOwnership.getEventHubName())
                .add("etag=" + partitionOwnership.getETag())
                .add("lastModified=" + partitionOwnership.getLastModifiedTime())
                .toString();
        System.out.println(po);
    }
}
