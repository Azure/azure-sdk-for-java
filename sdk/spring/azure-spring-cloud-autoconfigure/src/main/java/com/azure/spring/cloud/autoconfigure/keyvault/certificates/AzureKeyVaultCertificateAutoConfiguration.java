// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.certificates;

import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for a {@link CertificateClientBuilder} and Azure Key Vault secret clients.
 */
@ConditionalOnClass(CertificateClientBuilder.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure.keyvault.certificate", name = "enabled", matchIfMissing = true)
@ConditionalOnBean(AzureConfigurationProperties.class)
public class AzureKeyVaultCertificateAutoConfiguration extends AzureServiceConfigurationBase {


    public AzureKeyVaultCertificateAutoConfiguration(AzureConfigurationProperties azureProperties) {
        super(azureProperties);
    }

    @ConfigurationProperties(prefix = "spring.cloud.azure.keyvault.certificate")
    @Bean
    public AzureKeyVaultCertificateProperties keyVaultCertificateProperties() {
        return copyProperties(this.azureProperties, new AzureKeyVaultCertificateProperties());
    }

    @Bean
    @ConditionalOnMissingBean
    public CertificateClient azureKeyVaultSecretClient(CertificateClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public CertificateAsyncClient azureKeyVaultSecretAsyncClient(CertificateClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public CertificateClientBuilder certificateClientBuilder(CertificateClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CertificateClientBuilderFactory certificateClientBuilderFactory(AzureKeyVaultCertificateProperties properties) {
        return new CertificateClientBuilderFactory(properties);
    }
}
