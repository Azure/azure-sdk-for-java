// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.spring.cloud.autoconfigure.implementation.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultJcaProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultSslBundlesProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureGlobalPropertiesUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Key Vault JCA support.
 *  @since 5.21.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(KeyVaultJcaProvider.class)
@ConditionalOnProperty(value = "spring.cloud.azure.keyvault.jca.enabled", havingValue = "true", matchIfMissing = true)
public class AzureKeyVaultJcaAutoConfiguration extends AzureServiceConfigurationBase {

    protected AzureKeyVaultJcaAutoConfiguration(AzureGlobalProperties azureProperties) {
        super(azureProperties);
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.cloud.azure.keyvault.jca")
    AzureKeyVaultJcaProperties azureKeyVaultJcaProperties() {
        return AzureGlobalPropertiesUtils.loadProperties(getAzureGlobalProperties(), new AzureKeyVaultJcaProperties());
    }

    @Bean
    @ConfigurationProperties(prefix = AzureKeyVaultSslBundlesProperties.PREFIX)
    AzureKeyVaultSslBundlesProperties azureKeyVaultSslBundlesProperties() {
        return new AzureKeyVaultSslBundlesProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    AzureKeyVaultSslBundlesRegistrar azureKeyVaultCertificateSslBundleRegistrar(AzureKeyVaultJcaProperties jcaProperties,
                                                                                AzureKeyVaultSslBundlesProperties sslBundlesProperties) {
        return new AzureKeyVaultSslBundlesRegistrar(jcaProperties, sslBundlesProperties);
    }
}
