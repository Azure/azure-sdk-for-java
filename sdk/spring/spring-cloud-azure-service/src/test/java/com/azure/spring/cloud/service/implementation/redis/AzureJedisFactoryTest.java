// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.redis;

import org.apache.commons.pool2.PooledObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AzureJedisFactoryTest {

    @Test
    @SuppressWarnings("unchecked")
    void testActivateObjectJedis() {

        JedisClientConfig jedisClientConfig = mock(JedisClientConfig.class);
        PooledObject<Jedis> pooledJedis = mock(PooledObject.class);
        HostAndPort hostAndPort = mock(HostAndPort.class);
        Jedis jedis = mock(Jedis.class);

        when(pooledJedis.getObject()).thenReturn(jedis);
        when(jedis.getDB()).thenReturn(7);
        when(jedis.select(7)).thenReturn("7 selected");
        when(jedisClientConfig.getDatabase()).thenReturn(7);

        AzureJedisFactory testTarget = new AzureJedisFactory(hostAndPort, jedisClientConfig);
        testTarget.activateObject(pooledJedis);

        verify(jedis, times(1)).getDB();
        verify(jedis, times(0)).select(7);
        verify(jedisClientConfig, times(1)).getDatabase();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testDestroyObject() {
        JedisClientConfig jedisClientConfig = mock(JedisClientConfig.class);
        PooledObject<Jedis> pooledJedis = mock(PooledObject.class);
        HostAndPort hostAndPort = mock(HostAndPort.class);
        Jedis jedis = mock(Jedis.class);

        when(pooledJedis.getObject()).thenReturn(jedis);
        when(jedis.isConnected()).thenReturn(true);

        AzureJedisFactory testTarget = new AzureJedisFactory(hostAndPort, jedisClientConfig);
        testTarget.destroyObject(pooledJedis);

        verify(jedis, times(1)).isConnected();
        verify(jedis, times(1)).close();

    }

    @Test
    @SuppressWarnings("unchecked")
    void testValidateObjectFalse() {
        JedisClientConfig jedisClientConfig = mock(JedisClientConfig.class);
        PooledObject<Jedis> pooledJedis = mock(PooledObject.class);
        HostAndPort hostAndPort = mock(HostAndPort.class);
        Jedis jedis = mock(Jedis.class);

        when(pooledJedis.getObject()).thenReturn(jedis);
        when(jedis.isConnected()).thenReturn(true);

        AzureJedisFactory testTarget = new AzureJedisFactory(hostAndPort, jedisClientConfig);
        boolean result = testTarget.validateObject(pooledJedis);

        Assertions.assertFalse(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testValidateObjectTrue() {
        JedisClientConfig jedisClientConfig = mock(JedisClientConfig.class);
        PooledObject<Jedis> pooledJedis = mock(PooledObject.class);
        HostAndPort hostAndPort = mock(HostAndPort.class);
        when(hostAndPort.getHost()).thenReturn("mock-host");
        when(hostAndPort.getPort()).thenReturn(1233);

        Jedis jedis = mock(Jedis.class, Mockito.RETURNS_DEEP_STUBS);
        when(pooledJedis.getObject()).thenReturn(jedis);
        when(jedis.getClient().getHost()).thenReturn("mock-host");
        when(jedis.getClient().getPort()).thenReturn(1233);
        when(jedis.isConnected()).thenReturn(true);
        when(jedis.ping()).thenReturn("PONG");

        AzureJedisFactory testTarget = new AzureJedisFactory(hostAndPort, jedisClientConfig);
        boolean result = testTarget.validateObject(pooledJedis);
        Assertions.assertTrue(result);

    }

    @Test
    void testMakeObject() {
        JedisClientConfig jedisClientConfig = mock(JedisClientConfig.class);
        HostAndPort hostAndPort = mock(HostAndPort.class);
        AzureJedisFactory testTarget = new AzureJedisFactory(hostAndPort, jedisClientConfig);

        try (MockedConstruction<Jedis> jedisMockedConstruction = mockConstruction(Jedis.class,
            (jedisMocker, context) -> {
                when(jedisMocker.isConnected()).thenReturn(true);
                when(jedisMocker.get("fake-key")).thenReturn("fake-value");

                Assertions.assertNotNull(testTarget.makeObject());
                Assertions.assertEquals(jedisMocker, testTarget.makeObject().getObject());
                Assertions.assertEquals("fake-value", testTarget.makeObject().getObject().get("fake-key"));
            })) {
            Assertions.assertEquals(0, jedisMockedConstruction.constructed().size());
        }
    }
}
