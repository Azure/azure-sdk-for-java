// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.certificates;

import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for a {@link CertificateClientBuilder} and Azure Key Vault secret clients.
 */
@ConditionalOnClass(CertificateClientBuilder.class)
@ConditionalOnExpression("${spring.cloud.azure.keyvault.certificate.enabled:true}")
@ConditionalOnProperty("spring.cloud.azure.keyvault.certificate.vault-url")
public class AzureKeyVaultCertificateAutoConfiguration extends AzureServiceConfigurationBase {


    public AzureKeyVaultCertificateAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @ConfigurationProperties(prefix = "spring.cloud.azure.keyvault.certificate")
    @Bean
    public AzureKeyVaultCertificateProperties azureKeyVaultCertificateProperties() {
        return loadProperties(this.azureGlobalProperties, new AzureKeyVaultCertificateProperties());
    }

    @Bean
    @ConditionalOnMissingBean
    public CertificateClient azureKeyVaultCertificateClient(CertificateClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public CertificateAsyncClient azureKeyVaultCertificateAsyncClient(CertificateClientBuilder builder) {
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
