// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.redis;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.DefaultJedisSocketFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisSocketFactory;
import redis.clients.jedis.exceptions.JedisException;

/**
 * PoolableObjectFactory custom impl for Azure Redis.
 */
public class AzureJedisFactory implements PooledObjectFactory<Jedis> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureJedisFactory.class);

    private final JedisSocketFactory jedisSocketFactory;

    private final JedisClientConfig clientConfig;

    protected AzureJedisFactory(HostAndPort hostAndPort, JedisClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.jedisSocketFactory = new DefaultJedisSocketFactory(hostAndPort, this.clientConfig);
    }


    @Override
    public void activateObject(PooledObject<Jedis> pooledJedis) {
        final Jedis jedis = pooledJedis.getObject();
        if (jedis.getDB() != clientConfig.getDatabase()) {
            jedis.select(clientConfig.getDatabase());
        }
    }

    @Override
    public void destroyObject(PooledObject<Jedis> pooledJedis) {
        final Jedis jedis = pooledJedis.getObject();
        if (jedis.isConnected()) {
            try {
                // need a proper test, probably with mock
                if (!jedis.isBroken()) {
                    jedis.quit();
                }
            } catch (RuntimeException e) {
                LOGGER.warn("Error while QUIT", e);
            }
            try {
                jedis.close();
            } catch (RuntimeException e) {
                LOGGER.warn("Error while close", e);
            }
        }
    }

    @Override
    public PooledObject<Jedis> makeObject() {
        Jedis jedis = null;
        try {
            jedis = new Jedis(jedisSocketFactory, clientConfig);
            jedis.connect();
            return new DefaultPooledObject<>(jedis);
        } catch (JedisException jedisException) {
            if (jedis != null) {
                try {
                    jedis.quit();
                } catch (RuntimeException e) {
                    LOGGER.warn("Error while QUIT", e);
                }
                try {
                    jedis.close();
                } catch (RuntimeException e) {
                    LOGGER.warn("Error while close", e);
                }
            }
            throw jedisException;
        }
    }

    @Override
    public void passivateObject(PooledObject<Jedis> pooledObject) throws Exception {

    }

    @Override
    public boolean validateObject(PooledObject<Jedis> pooledJedis) {
        final Jedis jedis = pooledJedis.getObject();
        try {
            String host = jedisSocketFactory.getHost();
            int port = jedisSocketFactory.getPort();

            String connectionHost = jedis.getClient().getHost();
            int connectionPort = jedis.getClient().getPort();

            return host.equals(connectionHost)
                    && port == connectionPort && jedis.isConnected()
                    && jedis.ping().equals("PONG");
        } catch (final Exception e) {
            LOGGER.error("Error while validating pooled Jedis object.", e);
            return false;
        }
    }

}
