// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.autoconfigure.implementation.keyvault;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.spring.cloud.actuator.implementation.keyvault.KeyVaultSecretHealthIndicator;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.AzureKeyVaultSecretAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class of Key Vault Secret Health
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ SecretAsyncClient.class, HealthIndicator.class })
@ConditionalOnBean(SecretAsyncClient.class)
@AutoConfigureAfter(AzureKeyVaultSecretAutoConfiguration.class)
@ConditionalOnEnabledHealthIndicator("azure-keyvault-secret")
public class KeyVaultSecretHealthConfiguration {

    @Bean
    KeyVaultSecretHealthIndicator keyVaultSecretHealthIndicator(SecretAsyncClient secretAsyncClient) {
        return new KeyVaultSecretHealthIndicator(secretAsyncClient);
    }
}
