// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.autoconfigure.keyvault;

import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.spring.cloud.actuator.keyvault.KeyVaultCertificateHealthIndicator;
import com.azure.spring.cloud.autoconfigure.keyvault.certificates.AzureKeyVaultCertificateAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class of Key Vault Certificate Health
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ CertificateAsyncClient.class, HealthIndicator.class })
@ConditionalOnBean(CertificateAsyncClient.class)
@AutoConfigureAfter(AzureKeyVaultCertificateAutoConfiguration.class)
@ConditionalOnEnabledHealthIndicator("azure-keyvault-certificate")
public class KeyVaultCertificateHealthConfiguration {

    @Bean
    KeyVaultCertificateHealthIndicator keyVaultCertificateHealthIndicator(CertificateAsyncClient certificateAsyncClient) {
        return new KeyVaultCertificateHealthIndicator(certificateAsyncClient);
    }
}
