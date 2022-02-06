// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.certificates;

import com.azure.core.util.ConfigurationBuilder;
import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.keyvault.certificates.properties.AzureKeyVaultCertificateProperties;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.service.implementation.keyvault.certificates.CertificateClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
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

    public AzureKeyVaultCertificateAutoConfiguration(AzureGlobalProperties azureGlobalProperties, ConfigurationBuilder configurationBuilder) {
        super(azureGlobalProperties, configurationBuilder.buildSection("keyvault.certificate"));
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
    CertificateClientBuilder certificateClientBuilder(CertificateClientBuilderFactory factory) {
        return factory.build(configuration);
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
