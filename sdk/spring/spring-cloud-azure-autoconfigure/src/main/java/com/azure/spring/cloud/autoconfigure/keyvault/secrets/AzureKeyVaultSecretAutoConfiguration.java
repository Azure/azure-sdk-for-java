// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.secrets;

import com.azure.core.util.ConfigurationBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.service.implementation.keyvault.secrets.SecretClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.CONFIGURATION_BUILDER_BEAN_NAME;

/**
 * Auto-configuration for a {@link SecretClientBuilder} and Azure Key Vault secret clients.
 *
 * @since 4.0.0
 */
@ConditionalOnClass(SecretClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.keyvault.secret.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.keyvault.secret", name = "endpoint")
public class AzureKeyVaultSecretAutoConfiguration extends AzureServiceConfigurationBase {

    public AzureKeyVaultSecretAutoConfiguration(AzureGlobalProperties azureGlobalProperties,
                                                @Qualifier(CONFIGURATION_BUILDER_BEAN_NAME) ConfigurationBuilder configurationBuilder) {
        super(azureGlobalProperties, configurationBuilder.buildSection("keyvault.secret"));
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
    SecretClientBuilder secretClientBuilder(SecretClientBuilderFactory factory) {
        return factory.build(configuration);
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
