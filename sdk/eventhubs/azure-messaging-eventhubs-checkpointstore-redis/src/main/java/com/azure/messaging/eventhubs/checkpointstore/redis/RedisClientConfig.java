// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.redis;
import redis.clients.jedis.JedisPoolConfig;

/**
 * This class is used to create a JedisPool object using Azure Cache for Redis.
 */
public final class RedisClientConfig {
    private RedisClientConfig() {
    }
    private final String hostName = "hostname";

    /**
     * This method returns the HOST_NAME.
     * @return Host Name, which is the domain assigned by the Azure Cache for Redis.
     */
    public String getHostName() {
        return hostName;
    }
    private final String password = "access_key";

    /**
     * This method returns the PASSWORD.
     * @return Primary Key used to connect to Azure Cache for Redis
     */
    public String getPassword() {
        return password;
    }
    private final boolean useSsl = true;

    /**
     * This method returns the USE_SSL field.
     * @return True or False based on if SSL is being used or not
     */
    public boolean getUseSSL() {
        return useSsl;
    }
    private final int sslPort = 6380;
    private final int nonSslPort = 6379;
    private final int connectTimeoutMills = 5000;

    /**
     * This method returns CONNECT_TIMEOUT_MILLS, which is in milliseconds.
     * @return The amount of time before the connection will time out.
     */
    public int getConnectTimeoutMills() {
        return connectTimeoutMills;
    }
    private static final int OPERATION_TIMEOUT_MILLS = 1000;
    /**
     * This method returns OPERATION_TIMEOUT_MILLS, which is in milliseconds.
     * @return The amount of time before an operation will time out.
     */
    public static int getOperationTimeoutMills() {
        return OPERATION_TIMEOUT_MILLS;
    }
    private final String clientName = "clientName";

    /**
     * This method returns the CLIENT_NAME.
     * @return The name of the client
     */
    public String getClientName() {
        return clientName;
    }
    private final JedisPoolConfig POOL_CONFIG = createPoolConfig();

    /**
     * This method gets the JedisPoolConfig object.
     * @return A JedisPoolConfig object
     */
    public JedisPoolConfig getPoolConfig() {
        return POOL_CONFIG;
    }
    private static final int POOL_MAX_TOTAL = 200;
    private static final int POOL_MAX_IDLE = 100;
    private static final int POOL_MIN_IDLE = 50;
    private static final boolean POOL_BLOCK_WHEN_EXHAUSTED = true;
    private static final int POOL_MAX_WAIT_MILLIS = OPERATION_TIMEOUT_MILLS;
    private final int RECONNECT_MAX_ATTEMPTS = 3;

    private static final RedisClientConfig INSTANCE = new RedisClientConfig();

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
      * This method gets the port being used by the JedisPool
      * @return The Port being used by JedisPool object
      */
    public int getPort() {
        return useSsl ? sslPort : nonSslPort;
    }
     /**
      * This method returns an instance of RedisClientConfig.
      * @return INSTANCE of the JedisPool class
      */
    public static RedisClientConfig getInstance() {
        return INSTANCE;
    }
}
