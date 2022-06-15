// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.redis;

import com.azure.core.credential.AzureKeyCredential;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

public final class JedisRedisCheckpointStoreBuilder {
    private JedisPool pool;

    /**
     * Sets the JedisPool field
     * @param jedisPool, a JedisPool object initialized using hostname and password of the Azure Redis Cache
     * @return JedisRedisCheckpointStoreBuilder object
     */
    public JedisRedisCheckpointStoreBuilder jedisPool(JedisPool jedisPool){
        this.pool = jedisPool;
        return this;
    }
    /**
     * Builds an instance of the JedisRedisCheckpointStore class
     * @return JedisRedisCheckpointStore object
     */
    public JedisRedisCheckpointStore build(){

        return new JedisRedisCheckpointStore(pool);
    }

}
