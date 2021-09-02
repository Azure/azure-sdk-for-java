// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.secrets;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for a {@link SecretClientBuilder} and Azure Key Vault secret clients.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SecretClientBuilder.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure.keyvault.secret", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureKeyVaultSecretProperties.class)
public class AzureKeyVaultSecretAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SecretClient azureKeyVaultSecretClient(SecretClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecretAsyncClient azureKeyVaultSecretAsyncClient(SecretClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecretClientBuilder secretClientBuilder(SecretClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecretClientBuilderFactory secretClientBuilderFactory(AzureKeyVaultSecretProperties properties) {
        return new SecretClientBuilderFactory(properties);
    }

}
