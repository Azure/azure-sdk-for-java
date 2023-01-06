// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.redis.passwordless.jedis;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.service.implementation.passwordless.AzureRedisPasswordlessProperties;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.function.Supplier;

import static com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils.copyPropertiesIgnoreNull;
import static com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils.copyPropertiesIgnoreTargetNonNull;

/**
 * Azure Redis passwordless connection configuration using Jedis.
 *
 * @since 4.6.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({GenericObjectPool.class, JedisConnection.class, Jedis.class})
@ConditionalOnExpression("${spring.redis.azure.passwordless-enabled:false}")
@ConditionalOnMissingBean(RedisConnectionFactory.class)
@ConditionalOnProperty(prefix = "spring.redis", name = {"host", "username"})
@EnableConfigurationProperties(RedisProperties.class)
class AzureJedisPasswordlessAutoConfiguration {

    private static final boolean COMMONS_POOL2_AVAILABLE = ClassUtils.isPresent("org.apache.commons.pool2.ObjectPool",
        AzureJedisPasswordlessAutoConfiguration.class.getClassLoader());

    private static final String AZURE_REDIS_CREDENTIAL_SUPPLIER_BEAN_NAME = "azureRedisCredentialSupplier";

    private static final int AZURE_REDIS_PORT = 6380;

    @Bean
    @ConfigurationProperties(prefix = "spring.redis.azure")
    AzureRedisPasswordlessProperties redisPasswordlessProperties() {
        return new AzureRedisPasswordlessProperties();
    }

    @Bean(name = AZURE_REDIS_CREDENTIAL_SUPPLIER_BEAN_NAME)
    @ConditionalOnMissingBean(name = AZURE_REDIS_CREDENTIAL_SUPPLIER_BEAN_NAME)
    Supplier<String> azureRedisCredentialSupplier(ObjectProvider<AzureGlobalProperties> azureGlobalProperties, AzureRedisPasswordlessProperties azureRedisPasswordlessProperties) {
        Properties properties = mergeAzureProperties(azureGlobalProperties.getIfAvailable(), azureRedisPasswordlessProperties).toProperties();
        return new AzureRedisCredentialSupplier(properties);
    }

    @Bean
    AzureJedisConnectionFactory azureRedisConnectionFactory(RedisProperties redisProperties, @Qualifier(value = AZURE_REDIS_CREDENTIAL_SUPPLIER_BEAN_NAME) Supplier<String> azureRedisCredentialSupplier) {
        RedisStandaloneConfiguration standaloneConfig = getStandaloneConfig(redisProperties);
        JedisClientConfiguration clientConfiguration = getJedisClientConfiguration(redisProperties);

        return new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, azureRedisCredentialSupplier);
    }

    private JedisClientConfiguration getJedisClientConfiguration(RedisProperties redisProperties) {

        JedisClientConfiguration.JedisClientConfigurationBuilder builder = applyProperties(redisProperties, JedisClientConfiguration.builder());
        builder.useSsl();
        RedisProperties.Pool pool = redisProperties.getJedis().getPool();

        if (isPoolEnabled(pool)) {
            applyPooling(pool, builder);
        }

        if (StringUtils.hasText(redisProperties.getUrl())) {
            customizeConfigurationFromUrl(redisProperties, builder);
        }
        return builder.build();
    }

    private JedisClientConfiguration.JedisClientConfigurationBuilder applyProperties(RedisProperties properties, JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(properties.getTimeout()).to(builder::readTimeout);
        map.from(properties.getConnectTimeout()).to(builder::connectTimeout);
        map.from(properties.getClientName()).whenHasText().to(builder::clientName);
        return builder;
    }

    private void customizeConfigurationFromUrl(RedisProperties redisProperties, JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        ConnectionInfo connectionInfo = parseUrl(redisProperties.getUrl());
        if (connectionInfo.isUseSsl()) {
            builder.useSsl();
        }
    }

    private boolean isPoolEnabled(RedisProperties.Pool pool) {
        Boolean enabled = pool.getEnabled();
        return (enabled != null) ? enabled : COMMONS_POOL2_AVAILABLE;
    }

    private void applyPooling(RedisProperties.Pool pool,
                              JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        builder.usePooling().poolConfig(jedisPoolConfig(pool));
    }

    private JedisPoolConfig jedisPoolConfig(RedisProperties.Pool pool) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(pool.getMaxActive());
        config.setMaxIdle(pool.getMaxIdle());
        config.setMinIdle(pool.getMinIdle());
        if (pool.getTimeBetweenEvictionRuns() != null) {
            config.setTimeBetweenEvictionRuns(pool.getTimeBetweenEvictionRuns());
        }
        if (pool.getMaxWait() != null) {
            config.setMaxWait(pool.getMaxWait());
        }
        return config;
    }

    private AzureRedisPasswordlessProperties mergeAzureProperties(AzureGlobalProperties azureGlobalProperties, AzureRedisPasswordlessProperties redisPasswordlessProperties) {
        AzureRedisPasswordlessProperties target = new AzureRedisPasswordlessProperties();
        copyPropertiesIgnoreNull(redisPasswordlessProperties.getScopes(), target.getScopes());
        copyPropertiesIgnoreNull(redisPasswordlessProperties.getCredential(), target.getCredential());
        copyPropertiesIgnoreNull(redisPasswordlessProperties.getProfile(), target.getProfile());
        copyPropertiesIgnoreNull(redisPasswordlessProperties.getClient(), target.getClient());
        copyPropertiesIgnoreNull(redisPasswordlessProperties.getProxy(), target.getProxy());

        if (azureGlobalProperties != null) {
            copyPropertiesIgnoreTargetNonNull(azureGlobalProperties.getProfile(), target.getProfile());
            copyPropertiesIgnoreTargetNonNull(azureGlobalProperties.getCredential(), target.getCredential());
        }

        return redisPasswordlessProperties;
    }

    private RedisStandaloneConfiguration getStandaloneConfig(RedisProperties redisProperties) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        if (StringUtils.hasText(redisProperties.getUrl())) {
            ConnectionInfo connectionInfo = parseUrl(redisProperties.getUrl());
            config.setHostName(connectionInfo.getHostName());
            config.setPort(connectionInfo.getPort());
            config.setUsername(connectionInfo.getUsername());
            config.setPassword(RedisPassword.of(connectionInfo.getPassword()));
        } else {
            config.setHostName(redisProperties.getHost());
            config.setPort(redisProperties.getPort());
            config.setUsername(redisProperties.getUsername());
            config.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }
        config.setDatabase(redisProperties.getDatabase());
        if (config.getPort() == 0) {
            config.setPort(AZURE_REDIS_PORT);
        }
        return config;
    }

    ConnectionInfo parseUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!"redis".equals(scheme) && !"rediss".equals(scheme)) {
                throw new RedisUrlSyntaxException(url);
            }
            boolean useSsl = ("rediss".equals(scheme));
            String username = null;
            String password = null;
            if (uri.getUserInfo() != null) {
                String candidate = uri.getUserInfo();
                int index = candidate.indexOf(':');
                if (index >= 0) {
                    username = candidate.substring(0, index);
                    password = candidate.substring(index + 1);
                } else {
                    password = candidate;
                }
            }
            return new ConnectionInfo(uri, useSsl, username, password);
        } catch (URISyntaxException ex) {
            throw new RedisUrlSyntaxException(url, ex);
        }
    }

    static class ConnectionInfo {

        private final URI uri;

        private final boolean useSsl;

        private final String username;

        private final String password;

        ConnectionInfo(URI uri, boolean useSsl, String username, String password) {
            this.uri = uri;
            this.useSsl = useSsl;
            this.username = username;
            this.password = password;
        }

        boolean isUseSsl() {
            return this.useSsl;
        }

        String getHostName() {
            return this.uri.getHost();
        }

        int getPort() {
            return this.uri.getPort();
        }

        String getUsername() {
            return this.username;
        }

        String getPassword() {
            return this.password;
        }

    }

}
