// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.redis.passwordless.data.jedis;

import com.azure.spring.cloud.core.implementation.util.ReflectionUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.util.Pool;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class AzureJedisConnectionFactoryTest {

    private RedisStandaloneConfiguration standaloneConfig;
    private JedisClientConfiguration clientConfiguration;
    private Supplier<String> credentialSupplier;

    @Test
    void testFetchJedisConnectorWithNoPool() {
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);

        try (MockedConstruction<Jedis> jedisMockedConstruction = mockConstruction(Jedis.class,
            (jedisMocker, context) -> {
                when(jedisMocker.isConnected()).thenReturn(true);
                when(jedisMocker.get("fake-key")).thenReturn("fake-value");

                Jedis jedis = azureJedisConnectionFactory.fetchJedisConnector();

                Assertions.assertEquals(jedisMocker, jedis);
            })) {
            Assertions.assertEquals(0, jedisMockedConstruction.constructed().size());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFetchJedisConnectorWithPool() {
        clientConfiguration = mock(JedisClientConfiguration.class);
        when(clientConfiguration.isUsePooling()).thenReturn(true);
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Jedis mockJedis = mock(Jedis.class);
        Pool<Jedis> mockPool = mock(Pool.class);
        when(mockPool.getResource()).thenReturn(mockJedis);
        azureJedisConnectionFactory.setPool(mockPool);

        Jedis jedis = azureJedisConnectionFactory.fetchJedisConnector();

        Assertions.assertEquals(mockJedis, jedis);

    }

    @Test
    void testGetConnection() {
        standaloneConfig = mock(RedisStandaloneConfiguration.class);
        when(standaloneConfig.getUsername()).thenReturn("fake-userName");
        when(standaloneConfig.getPassword()).thenReturn(RedisPassword.none());

        clientConfiguration = mock(JedisClientConfiguration.class);
        when(clientConfiguration.getClientName()).thenReturn(Optional.of("fake-clientName"));
        when(clientConfiguration.isUsePooling()).thenReturn(false);

        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);

        try (MockedConstruction<Jedis> jedisMockedConstruction = mockConstruction(Jedis.class,
            (jedisMocker, context) -> {

                RedisConnection connection = azureJedisConnectionFactory.getConnection();

                Assertions.assertNotNull(connection);
                Assertions.assertEquals("fake-clientName", connection.getClientName());

            })) {
            Assertions.assertEquals(0, jedisMockedConstruction.constructed().size());
        }
    }

    @Test
    void testGetConnectionWithoutInit() {
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertThrows(IllegalStateException.class, () -> {
            azureJedisConnectionFactory.getConnection();
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAfterPropertiesSet() {
        standaloneConfig = mock(RedisStandaloneConfiguration.class);
        when(standaloneConfig.getUsername()).thenReturn("fake-userName");
        when(standaloneConfig.getPassword()).thenReturn(RedisPassword.none());

        clientConfiguration = mock(JedisClientConfiguration.class);
        when(clientConfiguration.getClientName()).thenReturn(Optional.of("fake-clientName"));
        when(clientConfiguration.isUsePooling()).thenReturn(true);
        when(clientConfiguration.getPoolConfig()).thenReturn(Optional.of(new JedisPoolConfig()));

        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);

        JedisClientConfig jedisClientConfig = (JedisClientConfig) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "jedisClientConfig", azureJedisConnectionFactory);
        Boolean initialized = (Boolean) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "initialized", azureJedisConnectionFactory);
        Pool<Jedis> pool = (Pool<Jedis>) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "pool", azureJedisConnectionFactory);

        Assertions.assertNull(jedisClientConfig);
        Assertions.assertFalse(initialized);
        Assertions.assertNull(pool);

        azureJedisConnectionFactory.afterPropertiesSet();

        jedisClientConfig = (JedisClientConfig) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "jedisClientConfig", azureJedisConnectionFactory);
        initialized = (Boolean) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "initialized", azureJedisConnectionFactory);
        pool = (Pool<Jedis>) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "pool", azureJedisConnectionFactory);
        Assertions.assertNotNull(jedisClientConfig);
        Assertions.assertTrue(initialized);
        Assertions.assertNotNull(pool);

    }

    @Test
    @SuppressWarnings("unchecked")
    void testDestroyWithPool() {

        clientConfiguration = mock(JedisClientConfiguration.class);
        when(clientConfiguration.isUsePooling()).thenReturn(true);
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Pool<Jedis> mockPool = mock(Pool.class);
        azureJedisConnectionFactory.setPool(mockPool);

        Boolean destroyed = (Boolean) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "destroyed", azureJedisConnectionFactory);
        Pool<Jedis> pool = (Pool) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "pool", azureJedisConnectionFactory);

        Assertions.assertFalse(destroyed);
        Assertions.assertEquals(mockPool, pool);

        azureJedisConnectionFactory.destroy();

        destroyed = (Boolean) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "destroyed", azureJedisConnectionFactory);
        pool = (Pool) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "pool", azureJedisConnectionFactory);

        Assertions.assertTrue(destroyed);
        Assertions.assertNull(pool);

    }

    @Test
    void testTranslateExceptionIfPossible() {
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            azureJedisConnectionFactory.translateExceptionIfPossible(new RuntimeException());
        });

    }

    @Test
    void testGetClusterConnection() {

        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            azureJedisConnectionFactory.getClusterConnection();
        });
    }

    @Test
    void testGetPoolConfig() {
        clientConfiguration = mock(JedisClientConfiguration.class);
        GenericObjectPoolConfig<Jedis> poolConfig = new GenericObjectPoolConfig<>();
        when(clientConfiguration.getPoolConfig()).thenReturn(Optional.of(poolConfig));

        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        JedisClientConfiguration jedisClientConfiguration = (JedisClientConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "clientConfiguration", azureJedisConnectionFactory);
        Assertions.assertEquals(poolConfig, jedisClientConfiguration.getPoolConfig().get());
    }

    @Test
    void testGetConvertPipelineAndTxResults() {
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertTrue(azureJedisConnectionFactory.getConvertPipelineAndTxResults());
    }

    @Test
    void testGetSentinelConnection() {
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            azureJedisConnectionFactory.getSentinelConnection();
        });
    }

    @Test
    void testGetUsePool() {
        clientConfiguration = mock(JedisClientConfiguration.class);
        when(clientConfiguration.isUsePooling()).thenReturn(true);
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertTrue(azureJedisConnectionFactory.getUsePool());

        clientConfiguration = mock(JedisClientConfiguration.class);
        when(clientConfiguration.isUsePooling()).thenReturn(false);
        azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertFalse(azureJedisConnectionFactory.getUsePool());
    }

    @Test
    void testGetDatabase() {
        standaloneConfig = mock(RedisStandaloneConfiguration.class);
        when(standaloneConfig.getDatabase()).thenReturn(7);
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        RedisStandaloneConfiguration cf = (RedisStandaloneConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "standaloneConfig", azureJedisConnectionFactory);

        Assertions.assertEquals(7, cf.getDatabase());
    }

    @Test
    void testSetConvertPipelineAndTxResults() {
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);

        azureJedisConnectionFactory.setConvertPipelineAndTxResults(false);
        Assertions.assertFalse(azureJedisConnectionFactory.getConvertPipelineAndTxResults());

        azureJedisConnectionFactory.setConvertPipelineAndTxResults(true);
        Assertions.assertTrue(azureJedisConnectionFactory.getConvertPipelineAndTxResults());
    }


    @Test
    void testIsUseSsl() {
        clientConfiguration = mock(JedisClientConfiguration.class);

        when(clientConfiguration.isUseSsl()).thenReturn(true);
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        JedisClientConfiguration cf = (JedisClientConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "clientConfiguration", azureJedisConnectionFactory);

        Assertions.assertEquals(true, cf.isUseSsl());

        when(clientConfiguration.isUseSsl()).thenReturn(false);
        azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        cf = (JedisClientConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "clientConfiguration", azureJedisConnectionFactory);

        Assertions.assertEquals(false, cf.isUseSsl());
    }

    @Test
    void testGetHostName() {
        standaloneConfig = mock(RedisStandaloneConfiguration.class);
        when(standaloneConfig.getHostName()).thenReturn("fake-host-name");
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        RedisStandaloneConfiguration cf = (RedisStandaloneConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "standaloneConfig", azureJedisConnectionFactory);

        Assertions.assertEquals("fake-host-name", cf.getHostName());
    }

    @Test
    void testGetClientName() {
        clientConfiguration = mock(JedisClientConfiguration.class);
        when(clientConfiguration.getClientName()).thenReturn(Optional.of("fake-clientName"));
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        JedisClientConfiguration jedisClientConfig = (JedisClientConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "clientConfiguration", azureJedisConnectionFactory);

        Assertions.assertEquals("fake-clientName", jedisClientConfig.getClientName().get());
    }

    @Test
    void testGetTimeout() {
        clientConfiguration = mock(JedisClientConfiguration.class);
        when(clientConfiguration.getReadTimeout()).thenReturn(Duration.ofSeconds(23));
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
    }
}
