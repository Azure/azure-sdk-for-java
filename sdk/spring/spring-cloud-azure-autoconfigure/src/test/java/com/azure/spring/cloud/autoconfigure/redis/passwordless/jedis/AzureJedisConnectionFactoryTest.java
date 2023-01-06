package com.azure.spring.cloud.autoconfigure.redis.passwordless.jedis;

import com.azure.spring.cloud.core.implementation.util.ReflectionUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureJedisConnectionFactoryTest {

    RedisStandaloneConfiguration standaloneConfig;
    JedisClientConfiguration clientConfiguration;
    Supplier<String> credentialSupplier;

    @Test
    void getConnectionWithoutInit() {
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertThrows(IllegalStateException.class, () -> {
            azureJedisConnectionFactory.getConnection();
        });
    }

    //
    @Test
    void getConnection() {
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        azureJedisConnectionFactory.getConnection();
    }

    @Test
    void afterPropertiesSet() {
    }

    @Test
    void postProcessConnection() {
    }

    @Test
    void createRedisPool() {
    }

    @Test
    void fetchJedisConnector() {
    }

    @Test
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
    void translateExceptionIfPossible() {
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            azureJedisConnectionFactory.translateExceptionIfPossible(new RuntimeException());
        });

    }


    @Test
    void getClusterConnection() {

        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            azureJedisConnectionFactory.getClusterConnection();
        });
    }

    @Test
    void getPoolConfig() {
        clientConfiguration = mock(JedisClientConfiguration.class);
        GenericObjectPoolConfig<Jedis> poolConfig = new GenericObjectPoolConfig<>();
        when(clientConfiguration.getPoolConfig()).thenReturn(Optional.of(poolConfig));

        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertEquals(poolConfig, azureJedisConnectionFactory.getPoolConfig());
    }

    @Test
    void getConvertPipelineAndTxResults() {
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertTrue(azureJedisConnectionFactory.getConvertPipelineAndTxResults());
    }

    @Test
    void getSentinelConnection() {
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            azureJedisConnectionFactory.getSentinelConnection();
        });
    }

    @Test
    void getUsePool() {
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
    void getDatabase() {
        standaloneConfig = mock(RedisStandaloneConfiguration.class);
        when(standaloneConfig.getDatabase()).thenReturn(7);
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertEquals(7, azureJedisConnectionFactory.getDatabase());
    }

    @Test
    void getClientConfiguration() {
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);

        JedisClientConfiguration configuration = azureJedisConnectionFactory.getClientConfiguration();
        Assertions.assertNull(configuration);

        clientConfiguration = mock(JedisClientConfiguration.class);
        azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertTrue(clientConfiguration == azureJedisConnectionFactory.getClientConfiguration());

    }

    @Test
    void setConvertPipelineAndTxResults() {
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);

        azureJedisConnectionFactory.setConvertPipelineAndTxResults(false);
        Assertions.assertFalse(azureJedisConnectionFactory.getConvertPipelineAndTxResults());

        azureJedisConnectionFactory.setConvertPipelineAndTxResults(true);
        Assertions.assertTrue(azureJedisConnectionFactory.getConvertPipelineAndTxResults());
    }

    @Test
    void getPort() {
        standaloneConfig = mock(RedisStandaloneConfiguration.class);
        when(standaloneConfig.getPort()).thenReturn(1717);
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertEquals(1717, azureJedisConnectionFactory.getPort());
    }

    @Test
    void isUseSsl() {
        clientConfiguration = mock(JedisClientConfiguration.class);

        when(clientConfiguration.isUseSsl()).thenReturn(true);
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertEquals(true, azureJedisConnectionFactory.isUseSsl());

        when(clientConfiguration.isUseSsl()).thenReturn(false);
        azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertEquals(false, azureJedisConnectionFactory.isUseSsl());
    }

    @Test
    void getHostName() {
        standaloneConfig = mock(RedisStandaloneConfiguration.class);
        when(standaloneConfig.getHostName()).thenReturn("fake-host-name");
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertEquals("fake-host-name", azureJedisConnectionFactory.getHostName());
    }

    @Test
    void getPasswordFromConfig() {
        standaloneConfig = mock(RedisStandaloneConfiguration.class);
        when(standaloneConfig.getPassword()).thenReturn(RedisPassword.of("password-from-config"));
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertEquals("password-from-config", azureJedisConnectionFactory.getPassword());
    }

    @Test
    void getPasswordFromSupplier() {
        standaloneConfig = mock(RedisStandaloneConfiguration.class);
        credentialSupplier = mock(Supplier.class);
        when(credentialSupplier.get()).thenReturn("password-from-credential-supplier");
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertEquals("password-from-credential-supplier", azureJedisConnectionFactory.getPassword());
    }

    @Test
    void getClientName() {
        clientConfiguration = mock(JedisClientConfiguration.class);
        when(clientConfiguration.getClientName()).thenReturn(Optional.of("fake-clientName"));
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertEquals("fake-clientName", azureJedisConnectionFactory.getClientName());
    }

    @Test
    void getTimeout() {
        clientConfiguration = mock(JedisClientConfiguration.class);
        when(clientConfiguration.getReadTimeout()).thenReturn(Duration.ofSeconds(23));
        AzureJedisConnectionFactory azureJedisConnectionFactory = new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, credentialSupplier);
        Assertions.assertEquals(23000, azureJedisConnectionFactory.getTimeout());
    }
}
