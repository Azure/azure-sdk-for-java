// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.util.Collections;
import java.util.StringJoiner;

/**
 * Sample that demonstrates the use of {@link JedisRedisCheckpointStoreSample} for storing and updating partition ownership records
 * in Azure Redis Cache.
 */
public class JedisRedisCheckpointStoreSample {
    private static final int PORT = 6380; //default SSL port for Azure Redis Cache
    private static final String HOST_NAME = ""; // For Azure Redis Cache, this will look like '....redis.cache.windows.net'
    private static final String PASSWORD = ""; //Primary Key used for connecting to Azure Redis Cache
    private static final JedisPoolConfig POOL_CONFIG = new JedisPoolConfig();
    private static final JedisPool JEDIS_POOL = new JedisPool(POOL_CONFIG, HOST_NAME, PORT, 1000, 1000, PASSWORD, Protocol.DEFAULT_DATABASE, "clientname", true, null, null, null);

    /**
     * The main method to run the sample.
     *
     * @param args Unused arguments given to the sample
     * @throws Exception an Exception will be thrown in case of errors while running the sample
     */
    public static void main(String[] args) throws Exception {

        // Instantiating the JedisRedisCheckpointStore
        JedisRedisCheckpointStore jedisRedisCheckpointStore = new JedisRedisCheckpointStore(JEDIS_POOL);

        jedisRedisCheckpointStore.listOwnership("namespace", "abc", "xyz")
            .subscribe(JedisRedisCheckpointStoreSample::printPartitionOwnership);

        System.out.println("Updating checkpoint");
        Checkpoint checkpoint = new Checkpoint()
            .setFullyQualifiedNamespace("namespace")
            .setConsumerGroup("xyz")
            .setEventHubName("abc")
            .setPartitionId("0")
            .setSequenceNumber(2L)
            .setOffset(250L);
        jedisRedisCheckpointStore.updateCheckpoint(checkpoint)
            .subscribe(eTag -> System.out.println(eTag), error -> System.out.println(error.getMessage()));

        System.out.println("Claiming checkpoint");
        PartitionOwnership partitionOwnership = new PartitionOwnership()
            .setFullyQualifiedNamespace("namespace")
            .setConsumerGroup("xyz")
            .setEventHubName("abc")
            .setPartitionId("0");
        jedisRedisCheckpointStore.claimOwnership(Collections.singletonList(partitionOwnership))
            .subscribe(ownership -> System.out.println(ownership.getOwnerId()), error -> System.out.println(error.getMessage())); // This should print null for the first time a partition is claimed

    }

    static void printPartitionOwnership(PartitionOwnership partitionOwnership) {
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
