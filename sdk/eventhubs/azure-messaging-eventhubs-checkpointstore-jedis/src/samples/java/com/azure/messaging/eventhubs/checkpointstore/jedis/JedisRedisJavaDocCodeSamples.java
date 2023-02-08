// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.messaging.eventhubs.CheckpointStore;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;

/**
 * Javadoc samples for {@link JedisCheckpointStore}.
 */
public class JedisRedisJavaDocCodeSamples {
    /**
     * Demonstrates how to instantiate the checkpoint store.
     */
    public void instantiation() {
        // BEGIN: com.azure.messaging.eventhubs.jedisredischeckpointstore.instantiation
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
            .password("<YOUR_REDIS_PRIMARY_ACCESS_KEY>")
            .ssl(true)
            .build();

        String redisHostName = "<YOUR_REDIS_HOST_NAME>.redis.cache.windows.net";
        HostAndPort hostAndPort = new HostAndPort(redisHostName, 6380);
        JedisPool jedisPool = new JedisPool(hostAndPort, clientConfig);

        CheckpointStore checkpointStore = new JedisCheckpointStore(jedisPool);
        // END: com.azure.messaging.eventhubs.jedisredischeckpointstore.instantiation
    }
}
