// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.autoconfigure.keyvault;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.spring.cloud.actuate.keyvault.KeyVaultSecretHealthIndicator;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.keyvault.secrets.AzureKeyVaultSecretAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * configuration class of KeyVaultHealth
 */
@Configuration
@ConditionalOnClass({ SecretClient.class, HealthIndicator.class })
@ConditionalOnBean(SecretAsyncClient.class)
@AutoConfigureAfter(AzureKeyVaultSecretAutoConfiguration.class)
@ConditionalOnEnabledHealthIndicator("azure-key-vault")
@ConditionalOnProperty(value = "spring.cloud.azure.keyvault.secret.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.keyvault.secret", name = "endpoint")
public class KeyVaultHealthConfiguration {

    @Bean
    KeyVaultSecretHealthIndicator keyVaultHealthIndicator(SecretAsyncClient secretAsyncClient) {
        return new KeyVaultSecretHealthIndicator(secretAsyncClient);
    }
}
