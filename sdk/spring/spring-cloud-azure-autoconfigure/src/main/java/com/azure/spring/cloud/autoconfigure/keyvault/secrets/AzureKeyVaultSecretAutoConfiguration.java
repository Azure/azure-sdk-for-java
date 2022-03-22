// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.secrets;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Key Vault Secrets support.
 *
 * @since 4.0.0
 */
@ConditionalOnClass(SecretClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.keyvault.secret.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.keyvault.secret", name = "endpoint")
public class AzureKeyVaultSecretAutoConfiguration extends AzureServiceConfigurationBase {


    AzureKeyVaultSecretAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(prefix = AzureKeyVaultSecretProperties.PREFIX)
    AzureKeyVaultSecretProperties azureKeyVaultSecretProperties() {
        return loadProperties(getAzureGlobalProperties(), new AzureKeyVaultSecretProperties());
    }

    /**
     * Autoconfigure the {@link SecretClient} instance.
     * @param builder the {@link SecretClientBuilder} to build the instance.
     * @return the secret client instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public SecretClient azureKeyVaultSecretClient(SecretClientBuilder builder) {
        return builder.buildClient();
    }

    /**
     * Autoconfigure the {@link SecretAsyncClient} instance.
     * @param builder the {@link SecretClientBuilder} to build the instance.
     * @return the secret async client instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public SecretAsyncClient azureKeyVaultSecretAsyncClient(SecretClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    SecretClientBuilder secretClientBuilder(SecretClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    SecretClientBuilderFactory secretClientBuilderFactory(
        AzureKeyVaultSecretProperties properties,
        ObjectProvider<AzureServiceClientBuilderCustomizer<SecretClientBuilder>> customizers) {
        SecretClientBuilderFactory factory = new SecretClientBuilderFactory(properties);

        factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_KEY_VAULT_SECRETS);
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }

}
