// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.secrets;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.service.keyvault.secrets.SecretClientBuilderFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for a {@link SecretClientBuilder} and Azure Key Vault secret clients.
 */
@ConditionalOnClass(SecretClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.keyvault.secret.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.keyvault.secret", name = "endpoint")
public class AzureKeyVaultSecretAutoConfiguration extends AzureServiceConfigurationBase {


    public AzureKeyVaultSecretAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(prefix = AzureKeyVaultSecretProperties.PREFIX)
    public AzureKeyVaultSecretProperties azureKeyVaultSecretProperties() {
        return loadProperties(this.azureGlobalProperties, new AzureKeyVaultSecretProperties());
    }

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
        factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_KEY_VAULT);
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecretClientBuilderFactory secretClientBuilderFactory(AzureKeyVaultSecretProperties properties) {
        return new SecretClientBuilderFactory(properties);
    }

}
