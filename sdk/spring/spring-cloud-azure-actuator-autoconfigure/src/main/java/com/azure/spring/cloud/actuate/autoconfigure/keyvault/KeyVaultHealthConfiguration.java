// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.autoconfigure.keyvault;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.spring.cloud.actuate.keyvault.KeyVaultSecretHealthIndicator;
import com.azure.spring.cloud.autoconfigure.keyvault.secrets.AzureKeyVaultSecretAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * configuration class of KeyVaultHealth
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ SecretClient.class, HealthIndicator.class })
@ConditionalOnBean(SecretAsyncClient.class)
@AutoConfigureAfter(AzureKeyVaultSecretAutoConfiguration.class)
@ConditionalOnEnabledHealthIndicator("azure-keyvault")
public class KeyVaultHealthConfiguration {

    @Bean
    KeyVaultSecretHealthIndicator keyVaultHealthIndicator(SecretAsyncClient secretAsyncClient) {
        return new KeyVaultSecretHealthIndicator(secretAsyncClient);
    }
}
