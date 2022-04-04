// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.certificates;

import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.certificates.properties.AzureKeyVaultCertificateProperties;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.service.implementation.keyvault.certificates.CertificateClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Key Vault Certificate support.
 *
 * @since 4.0.0
 */
@ConditionalOnClass(CertificateClientBuilder.class)
@ConditionalOnExpression("${spring.cloud.azure.keyvault.certificate.enabled:true}")
@ConditionalOnProperty("spring.cloud.azure.keyvault.certificate.endpoint")
public class AzureKeyVaultCertificateAutoConfiguration extends AzureServiceConfigurationBase {

    AzureKeyVaultCertificateAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @ConfigurationProperties(prefix = "spring.cloud.azure.keyvault.certificate")
    @Bean
    AzureKeyVaultCertificateProperties azureKeyVaultCertificateProperties() {
        return loadProperties(getAzureGlobalProperties(), new AzureKeyVaultCertificateProperties());
    }

    /**
     * Autoconfigure the {@link CertificateClient} instance.
     * @param builder the {@link CertificateClientBuilder} to build the instance.
     * @return the certificate client instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public CertificateClient azureKeyVaultCertificateClient(CertificateClientBuilder builder) {
        return builder.buildClient();
    }

    /**
     * Autoconfigure the {@link CertificateAsyncClient} instance.
     * @param builder the {@link CertificateClientBuilder} to build the instance.
     * @return the certificate async client instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public CertificateAsyncClient azureKeyVaultCertificateAsyncClient(CertificateClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    CertificateClientBuilder certificateClientBuilder(CertificateClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    CertificateClientBuilderFactory certificateClientBuilderFactory(
        AzureKeyVaultCertificateProperties properties,
        ObjectProvider<AzureServiceClientBuilderCustomizer<CertificateClientBuilder>> customizers) {
        CertificateClientBuilderFactory factory = new CertificateClientBuilderFactory(properties);

        factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_KEY_VAULT_CERTIFICATES);
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }

}
