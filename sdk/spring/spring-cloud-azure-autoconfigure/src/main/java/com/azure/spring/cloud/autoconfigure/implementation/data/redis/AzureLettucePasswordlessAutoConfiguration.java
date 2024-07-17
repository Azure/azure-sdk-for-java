// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.redis;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.passwordless.properties.AzureRedisPasswordlessProperties;
import com.azure.spring.cloud.autoconfigure.implementation.data.redis.lettuce.AzureRedisCredentials;
import com.azure.spring.cloud.core.implementation.util.AzurePasswordlessPropertiesUtils;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisCredentials;
import io.lettuce.core.RedisCredentialsProvider;
import io.lettuce.core.protocol.ProtocolVersion;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnection;
import org.springframework.data.redis.connection.lettuce.RedisCredentialsProviderFactory;
import reactor.core.publisher.Mono;


/**
 * Azure Redis passwordless connection configuration using Lettuce.
 *
 * @since 5.13.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({LettuceConnection.class, RedisCredentials.class})
@ConditionalOnExpression("${spring.data.redis.azure.passwordless-enabled:false}")
@AutoConfigureBefore(RedisAutoConfiguration.class)
@ConditionalOnProperty(prefix = "spring.data.redis", name = {"host"})
@EnableConfigurationProperties(RedisProperties.class)
public class AzureLettucePasswordlessAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.data.redis.azure")
    AzureRedisPasswordlessProperties redisPasswordlessProperties() {
        return new AzureRedisPasswordlessProperties();
    }

    @Bean(name = "azureRedisCredentials")
    @ConditionalOnMissingBean
    AzureRedisCredentials azureRedisCredentials(RedisProperties redisProperties,
                                                AzureRedisPasswordlessProperties azureRedisPasswordlessProperties,
                                                AzureGlobalProperties azureGlobalProperties) {
        return new AzureRedisCredentials(redisProperties.getUsername(),
            mergeAzureProperties(azureGlobalProperties, azureRedisPasswordlessProperties));
    }

    @Bean(name = "azureLettuceClientConfigurationBuilderCustomizer")
    @ConditionalOnMissingBean
    LettuceClientConfigurationBuilderCustomizer azureLettuceClientConfigurationBuilderCustomizer(AzureRedisCredentials azureRedisCredentials) {
        return builder -> builder.redisCredentialsProviderFactory(new RedisCredentialsProviderFactory() {

            @Override
            public RedisCredentialsProvider createCredentialsProvider(RedisConfiguration redisConfiguration) {
                return () -> Mono.just(azureRedisCredentials);
            }

            @Override
            public RedisCredentialsProvider createSentinelCredentialsProvider(RedisSentinelConfiguration redisConfiguration) {
                return () -> Mono.just(azureRedisCredentials);
            }
        }).clientOptions(ClientOptions.builder().protocolVersion(ProtocolVersion.RESP2).build());
    }

    private AzureRedisPasswordlessProperties mergeAzureProperties(AzureGlobalProperties azureGlobalProperties,
                                                                  AzureRedisPasswordlessProperties azurePasswordlessProperties) {
        AzureRedisPasswordlessProperties mergedProperties = new AzureRedisPasswordlessProperties();
        AzurePasswordlessPropertiesUtils.mergeAzureCommonProperties(azureGlobalProperties, azurePasswordlessProperties, mergedProperties);
        return mergedProperties;
    }

}
