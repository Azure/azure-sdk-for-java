// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.redis;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;
import com.azure.identity.extensions.implementation.credential.provider.TokenCredentialProvider;
import com.azure.identity.extensions.implementation.credential.provider.TokenCredentialProviders;
import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.passwordless.properties.AzureRedisPasswordlessProperties;
import com.azure.spring.cloud.autoconfigure.implementation.data.redis.lettuce.AzureRedisCredentials;
import com.azure.spring.cloud.core.implementation.util.AzurePasswordlessPropertiesUtils;
import com.azure.spring.cloud.core.properties.PasswordlessProperties;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisCredentials;
import io.lettuce.core.RedisCredentialsProvider;
import io.lettuce.core.protocol.ProtocolVersion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnection;
import org.springframework.data.redis.connection.lettuce.RedisCredentialsProviderFactory;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Properties;

import static com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider.PASSWORDLESS_TOKEN_CREDENTIAL_BEAN_NAME;


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

    private static final Log LOGGER = LogFactory.getLog(AzureLettucePasswordlessAutoConfiguration.class);

    private final GenericApplicationContext applicationContext;

    AzureLettucePasswordlessAutoConfiguration(GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.data.redis.azure")
    AzureRedisPasswordlessProperties redisPasswordlessProperties() {
        return new AzureRedisPasswordlessProperties();
    }

    @Bean(name = "azureRedisCredentials")
    @ConditionalOnMissingBean
    @DependsOn("springTokenCredentialProviderContextProvider")
    AzureRedisCredentials azureRedisCredentials(RedisProperties redisProperties,
                                                AzureRedisPasswordlessProperties azureRedisPasswordlessProperties,
                                                AzureGlobalProperties azureGlobalProperties) {
        Properties properties = azureRedisPasswordlessProperties.toPasswordlessProperties();
        enhancePasswordlessProperties(properties, azureRedisPasswordlessProperties);
        TokenCredentialProvider provider = TokenCredentialProviders.createInstance(new TokenCredentialProviderOptions(properties));
        return new AzureRedisCredentials(redisProperties.getUsername(), provider,
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

    private void enhancePasswordlessProperties(Properties properties, PasswordlessProperties passwordlessProperties) {
        if (!passwordlessProperties.isPasswordlessEnabled()) {
            if (!passwordlessProperties.isPasswordlessEnabled()) {
                LOGGER.debug("Feature passwordless authentication is not enabled(spring.data.redis.azure.passwordless-enabled=false), "
                    + "skip enhancing Redis properties.");
                return;
            }
        }

        String tokenCredentialBeanName = passwordlessProperties.getCredential().getTokenCredentialBeanName();
        if (StringUtils.hasText(tokenCredentialBeanName)) {
            AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.setProperty(properties, tokenCredentialBeanName);
        } else {
            TokenCredentialProvider tokenCredentialProvider = TokenCredentialProvider.createDefault(new TokenCredentialProviderOptions(properties));
            TokenCredential tokenCredential = tokenCredentialProvider.get();

            tokenCredentialBeanName = PASSWORDLESS_TOKEN_CREDENTIAL_BEAN_NAME + ".spring.data.redis.azure";
            AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.setProperty(properties, tokenCredentialBeanName);
            applicationContext.registerBean(tokenCredentialBeanName, TokenCredential.class, () -> tokenCredential);
        }

        AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.setProperty(properties, SpringTokenCredentialProvider.class.getName());
        AuthProperty.AUTHORITY_HOST.setProperty(properties, passwordlessProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint());
    }
}
