// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;

public class AzureJedisPool extends JedisPool {

    public AzureJedisPool(final GenericObjectPoolConfig<Jedis> poolConfig, final HostAndPort hostAndPort,
                          final JedisClientConfig clientConfig) {
        super(poolConfig, new AzureJedisFactory(hostAndPort, clientConfig));
    }
}
