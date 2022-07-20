// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.secrets;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.common.AzureKeyVaultProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureServicePropertiesUtils;
import com.azure.spring.cloud.autoconfigure.keyvault.AzureKeyVaultConfiguration;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Key Vault Secrets support.
 *
 * @since 4.0.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnClass(SecretClientBuilder.class)
@ConditionalOnProperty(value = { "spring.cloud.azure.keyvault.secret.enabled", "spring.cloud.azure.keyvault.enabled" }, havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefixes = { "spring.cloud.azure.keyvault.secret", "spring.cloud.azure.keyvault" }, name = { "endpoint" })
@Import(AzureKeyVaultConfiguration.class)
public class AzureKeyVaultSecretAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = AzureKeyVaultSecretProperties.PREFIX)
    AzureKeyVaultSecretProperties azureKeyVaultSecretProperties(AzureKeyVaultProperties azureKeyVaultProperties) {
        return AzureServicePropertiesUtils.loadServiceCommonProperties(azureKeyVaultProperties, new AzureKeyVaultSecretProperties());
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
