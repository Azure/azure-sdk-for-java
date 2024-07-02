// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.autoconfigure.implementation.keyvault;

import com.azure.spring.cloud.actuator.implementation.keyvault.KeyVaultSecretHealthIndicator;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.AzureKeyVaultSecretAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class KeyVaultSecretHealthConfigurationTests {

    private static final String ENDPOINT = "https:/%s.vault.azure.net/";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues("spring.cloud.azure.keyvault.secret.endpoint=" + ENDPOINT)
        .withBean(AzureGlobalProperties.class)
        .withConfiguration(
            AutoConfigurations.of(AzureKeyVaultSecretAutoConfiguration.class,
                KeyVaultSecretHealthConfiguration.class));

    @Test
    void runShouldCreateIndicator() {
        this.contextRunner.run((context) -> assertThat(context).hasSingleBean(KeyVaultSecretHealthIndicator.class));
    }

    @Test
    void runWhenDisabledShouldNotCreateIndicator() {
        this.contextRunner.withPropertyValues("management.health.azure-keyvault-secret.enabled:false")
            .run((context) -> assertThat(context).doesNotHaveBean(KeyVaultSecretHealthIndicator.class));
    }
}
