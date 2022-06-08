// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.redis;


/**
 * This class is used to create a JedisPool object using Azure Cache for Redis.
 */
public final class RedisClientConfig {
    private static final String HOSTNAME = "hostname";
    private static final String PASSWORD = "access_key";
    private static final int CONNECT_TIMEOUT_MILLS = 5000;
    private static final String CLIENTNAME = "clientName";
    private static final int OPERATION_TIMEOUT_MILLS = 1000;
    private static final int SSL_PORT = 6380;
    private static final int NON_SSL_PORT = 6379;
    private static final int POOL_MIN_IDLE = 50;
    private static final boolean POOL_BLOCK_WHEN_EXHAUSTED = true;
    private static final boolean USE_SSL = true;
    private static final int POOL_MAX_TOTAL = 200;
    private static final int POOL_MAX_IDLE = 100;

    /**
     * Constructor for the RedisClientConfig class
     */
    public RedisClientConfig() {
    }
    /**
     * This method returns the HOST_NAME.
     * @return Host Name, which is the domain assigned by the Azure Cache for Redis.
     */
    public static String getHostName() {
        return HOSTNAME;
    }
    /**
     * This method returns the PASSWORD.
     * @return Primary Key used to connect to Azure Cache for Redis
     */
    public static String getPassword() {
        return PASSWORD;
    }
    /**
     * This method returns the USE_SSL field.
     * @return True or False based on if SSL is being used or not
     */
    public boolean getUseSSL() {
        return USE_SSL;
    }
    /**
     * This method returns CONNECT_TIMEOUT_MILLS, which is in milliseconds.
     * @return The amount of time before the connection will time out.
     */
    public int getConnectTimeoutMills() {
        return CONNECT_TIMEOUT_MILLS;
    }
    /**
     * This method returns OPERATION_TIMEOUT_MILLS, which is in milliseconds.
     * @return The amount of time before an operation will time out.
     */
    public int getOperationTimeoutMills() {
        return OPERATION_TIMEOUT_MILLS;
    }
    /**
     * This method returns the CLIENT_NAME.
     * @return The name of the client
     */
    public static String getClientName() {
        return CLIENTNAME;
    }

    /**
     * Get an integer representing how many resources a pool can have.
     * @return poolMaxTotal
     */
    public int getPoolMaxTotal() {
        return POOL_MAX_TOTAL;
    }
    /**
     * Get an integer representing how many resources at max can be idle at a given time.
     * @return poolMaxIdle
     */
    public int getPoolMaxIdle() {
        return POOL_MAX_IDLE;
    }
    /**
     * Get an integer representing how many resources at minimum can be idle at a given time.
     * @return poolMinIdle
     */
    public int getPoolMinIdle() {
        return POOL_MIN_IDLE;
    }
    /** Get if the pool should be blocked or not when it's resources are exhausted.
     * @return True
     */
    public boolean getPoolBlockWhenExhausted() {
        return POOL_BLOCK_WHEN_EXHAUSTED;
    }
     /**
      * This method gets the port being used by the JedisPool
      * @return The Port being used by JedisPool object
      */
    public int getPort() {
        return getUseSSL() ? SSL_PORT : NON_SSL_PORT;
    }
}
