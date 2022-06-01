// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.redis;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPoolConfig;

/**
 * This class is used to create a JedisPool object using Azure Cache for Redis.
 */
public final class RedisClientConfig {
    private RedisClientConfig() {
    }
    public static final String HOST_NAME = "hostname";
    public static final String PASSWORD = "access_key";
    public static final boolean USE_SSL = true;
    public static final int SSL_PORT = 6380;
    public static final int NON_SSL_PORT = 6379;
    public static final int CONNECT_TIMEOUT_MILLS = 5000;
    public static final int OPERATION_TIMEOUT_MILLS = 1000;
    public static final String CLIENT_NAME = "clientName";

    public static final GenericObjectPoolConfig POOL_CONFIG = createPoolConfig();
    public static final int POOL_MAX_TOTAL = 200;
    public static final int POOL_MAX_IDLE = 100;
    public static final int POOL_MIN_IDLE = 50;
    public static final boolean POOL_BLOCK_WHEN_EXHAUSTED = true;
    public static final int POOL_MAX_WAIT_MILLIS = OPERATION_TIMEOUT_MILLS;
    public static final int RECONNECT_MAX_ATTEMPTS = 3;

    public static final RedisClientConfig INSTANCE = new RedisClientConfig();

    private static JedisPoolConfig createPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        poolConfig.setMaxTotal(POOL_MAX_TOTAL);
        poolConfig.setMaxIdle(POOL_MAX_IDLE);
        poolConfig.setBlockWhenExhausted(POOL_BLOCK_WHEN_EXHAUSTED);
        poolConfig.setMaxWaitMillis(POOL_MAX_WAIT_MILLIS);
        poolConfig.setMinIdle(POOL_MIN_IDLE);
        return poolConfig;
    }

     /**
      * This function gets the port being used by the JedisPool
      * @return The Port being used by JedisPool object
      */
    public static int getPort() {
        return USE_SSL ? SSL_PORT : NON_SSL_PORT;
    }
     /**
      *
      * @return INSTANCE of the JedisPool class
      */
    public static RedisClientConfig getInstance() {
        return INSTANCE;
    }
}
