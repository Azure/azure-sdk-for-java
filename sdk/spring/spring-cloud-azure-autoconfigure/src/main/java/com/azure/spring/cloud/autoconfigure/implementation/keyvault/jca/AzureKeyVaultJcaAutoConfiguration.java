// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultJcaProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultSslBundleProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Key Vault JCA support.
 *  @since 5.21.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ KeyVaultJcaProvider.class, SslBundle.class })
@EnableConfigurationProperties({AzureKeyVaultJcaProperties.class, AzureKeyVaultSslBundleProperties.class})
@ConditionalOnProperty(value = "spring.cloud.azure.keyvault.jca.enabled", havingValue = "true", matchIfMissing = true)
public class AzureKeyVaultJcaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    AzureKeyVaultSslBundleRegistrar azureKeyVaultSslBundleRegistrar(AzureKeyVaultJcaProperties jcaProperties,
                                                                    AzureKeyVaultSslBundleProperties sslBundlesProperties) {
        return new AzureKeyVaultSslBundleRegistrar(jcaProperties, sslBundlesProperties);
    }
}
