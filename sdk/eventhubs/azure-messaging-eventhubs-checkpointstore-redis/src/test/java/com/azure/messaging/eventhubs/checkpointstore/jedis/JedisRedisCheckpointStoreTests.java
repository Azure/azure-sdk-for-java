// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.jedis;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import redis.clients.jedis.JedisPool;

/**
 * Unit tests for {@link  JedisRedisCheckpointStore}
 */
public class JedisRedisCheckpointStoreTests {
    @Test
    public void testListCheckpoints() {
        JedisPool jedisPool = Mockito.mock(JedisPool.class);
        JedisRedisCheckpointStore checkpointStore = new JedisRedisCheckpointStore(jedisPool);
    }

}
