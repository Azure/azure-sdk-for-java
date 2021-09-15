// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.secrets;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Auto-configuration for a {@link SecretClientBuilder} and Azure Key Vault secret clients.
 */
@ConditionalOnClass(SecretClientBuilder.class)
@ConditionalOnExpression("${spring.cloud.azure.keyvault.secret.enabled:true}")
@ConditionalOnProperty("spring.cloud.azure.keyvault.secret.vault-url")
public class AzureKeyVaultSecretAutoConfiguration extends AzureServiceConfigurationBase {


    public AzureKeyVaultSecretAutoConfiguration(AzureConfigurationProperties azureProperties) {
        super(azureProperties);
    }

    @Bean
    @ConfigurationProperties(prefix = AzureKeyVaultSecretProperties.PREFIX)
    public AzureKeyVaultSecretProperties azureKeyVaultSecretProperties() {
        return loadProperties(this.azureProperties, new AzureKeyVaultSecretProperties());
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
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecretClientBuilderFactory secretClientBuilderFactory(AzureKeyVaultSecretProperties properties) {
        return new SecretClientBuilderFactory(properties);
    }

}
