// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.redis;

import com.azure.core.credential.AzureKeyCredential;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
final class JedisRedisCheckpointStoreBuilder {
    private String hostname = "";
    private String key;
    private JedisPool jedisPool;
    private int operationTimeout = 1000;
    private int connectionTimeout = 5 * operationTimeout;
    private int maxPoolConnections = 200;
    private int maxPoolIdleConnections = 100;
    private int minPoolIdleConnections = 50;
    private int maxWait = operationTimeout;

    /**
     * Sets the hostname for the Azure Redis Cache associated with the CheckpointStore
     * @param endpoint a String representing the hostname for Azure Redis Cache
     * @return a JedisRedisCheckpointStoreBuilder instance
     */
    public JedisRedisCheckpointStoreBuilder endpoint(String endpoint) {
        this.hostname = endpoint;
        return this;
    }
    /**
     * Sets the password for the Azure Redis Cache associated with the CheckpointStore
     * @param key an AzureKeyCredential representing the key for Azure Redis Cache
     * @return a JedisRedisCheckpointStoreBuilder instance
     */
    public JedisRedisCheckpointStoreBuilder credential(AzureKeyCredential key) {
        this.key = key.getKey();
        return this;
    }
    /**
     * Builds an instance of the JedisRedisCheckpointStore class
     * @return JedisRedisCheckpointStore object
     */
    public JedisRedisCheckpointStore build() {
        boolean blockPoolWhenExhausted = true;
        String clientName = "clientName";
        boolean useSsl = true;
        int sslPort = 6380;
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxPoolConnections);
        poolConfig.setMaxIdle(maxPoolIdleConnections);
        poolConfig.setBlockWhenExhausted(blockPoolWhenExhausted);
        poolConfig.setMaxWaitMillis(maxWait);
        poolConfig.setMinIdle(minPoolIdleConnections);
        jedisPool = new JedisPool(poolConfig, hostname, sslPort, connectionTimeout, operationTimeout, key, Protocol.DEFAULT_DATABASE, clientName, useSsl, null, null, null);
        return new JedisRedisCheckpointStore(jedisPool);
    }
    /**
     * Sets the timeout value for JedisPool operations
     * @param operationTimeout an int representing the operation timeout value for the JedisPool
     * @return a JedisRedisCheckpointStoreBuilder instance
     */
    public JedisRedisCheckpointStoreBuilder operationTimeout(int operationTimeout) {
        this.operationTimeout = operationTimeout;
        connectionTimeout = 5 * operationTimeout;
        maxWait = operationTimeout;
        return this;
    }
    /**
     * Sets the maximum number of connections that the JedisPool object can create
     * @param maxPoolConnections an int representing maximum connections the JedisPool can have
     * @return a JedisRedisCheckpointStoreBuilder instance
     */
    public JedisRedisCheckpointStoreBuilder maxPoolConnections(int maxPoolConnections) {
        this.maxPoolConnections = maxPoolConnections;
        return this;
    }
    /**
     * Sets the maximum number of idle connections that the JedisPool can have.
     * @param maxPoolIdleConnections an int representing maximum idle connections the JedisPool can have
     * @return a JedisRedisCheckpointStoreBuilder instance
     */
    public JedisRedisCheckpointStoreBuilder maxPoolIdleConnections(int maxPoolIdleConnections) {
        this.maxPoolIdleConnections = maxPoolIdleConnections;
        return this;
    }
    /**
     * Sets the minimum number of idle connections that the JedisPool can have.
     * @param minPoolIdleConnections an int representing the minimum number of connections that can be idle in a pool
     * @return a JedisRedisCheckpointStoreBuilder instance
     */
    public JedisRedisCheckpointStoreBuilder minPoolIdleConnections(int minPoolIdleConnections) {
        this.minPoolIdleConnections = minPoolIdleConnections;
        return this;
    }
}
