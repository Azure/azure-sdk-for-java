// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.redis.passwordless.data.jedis;

import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.lang.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.util.Pool;

class AzureJedisConnection extends JedisConnection {

    protected AzureJedisConnection(Jedis jedis,
                                   @Nullable Pool<Jedis> pool,
                                   JedisClientConfig nodeConfig,
                                   JedisClientConfig sentinelConfig) {
        super(jedis, pool, nodeConfig, sentinelConfig);
    }
}
