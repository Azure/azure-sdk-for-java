// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.redis;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.redis.passwordless.data.jedis.AzureJedisConnectionFactory;
import com.azure.spring.cloud.autoconfigure.implementation.redis.passwordless.data.jedis.AzureRedisCredentialSupplier;
import com.azure.spring.cloud.core.implementation.util.AzurePasswordlessPropertiesUtils;
import com.azure.spring.cloud.service.implementation.passwordless.AzureRedisPasswordlessProperties;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import redis.clients.jedis.Jedis;

import static com.azure.spring.cloud.autoconfigure.redis.AzureJedisPasswordlessUtil.getJedisClientConfiguration;
import static com.azure.spring.cloud.autoconfigure.redis.AzureJedisPasswordlessUtil.getStandaloneConfig;

/**
 * Azure Redis passwordless connection configuration using Jedis.
 *
 * @since 4.6.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({GenericObjectPool.class, JedisConnection.class, Jedis.class})
@ConditionalOnExpression("${spring.redis.azure.passwordless-enabled:false}")
@AutoConfigureBefore(RedisAutoConfiguration.class)
@ConditionalOnProperty(prefix = "spring.redis", name = {"host"})
@EnableConfigurationProperties(RedisProperties.class)
public class AzureJedisPasswordlessAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.redis.azure")
    AzureRedisPasswordlessProperties redisPasswordlessProperties() {
        return new AzureRedisPasswordlessProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    AzureRedisCredentialSupplier azureRedisCredentialSupplier(AzureRedisPasswordlessProperties azureRedisPasswordlessProperties, AzureGlobalProperties azureGlobalProperties) {
        return new AzureRedisCredentialSupplier(mergeAzureProperties(azureGlobalProperties, azureRedisPasswordlessProperties).toPasswordlessProperties());
    }

    @Bean
    @ConditionalOnMissingBean
    AzureJedisConnectionFactory azureRedisConnectionFactory(RedisProperties redisProperties, AzureRedisCredentialSupplier azureRedisCredentialSupplier) {
        RedisStandaloneConfiguration standaloneConfig = getStandaloneConfig(redisProperties);
        JedisClientConfiguration clientConfiguration = getJedisClientConfiguration(redisProperties);
        return new AzureJedisConnectionFactory(standaloneConfig, clientConfiguration, azureRedisCredentialSupplier);
    }

    private AzureRedisPasswordlessProperties mergeAzureProperties(AzureGlobalProperties azureGlobalProperties, AzureRedisPasswordlessProperties azurePasswordlessProperties) {
        AzureRedisPasswordlessProperties mergedProperties = new AzureRedisPasswordlessProperties();
        AzurePasswordlessPropertiesUtils.mergeAzureCommonProperties(azureGlobalProperties, azurePasswordlessProperties, mergedProperties);
        return mergedProperties;
    }

}
