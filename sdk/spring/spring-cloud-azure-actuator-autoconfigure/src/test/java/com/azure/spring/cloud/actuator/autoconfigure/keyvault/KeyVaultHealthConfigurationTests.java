// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.autoconfigure.keyvault;

import com.azure.spring.cloud.actuator.keyvault.KeyVaultSecretHealthIndicator;
import com.azure.spring.cloud.autoconfigure.keyvault.secrets.AzureKeyVaultSecretAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.properties.AzureGlobalProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class KeyVaultHealthConfigurationTests {

    private static final String ENDPOINT = "https:/%s.vault.azure.net/";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues("spring.cloud.azure.keyvault.secret.endpoint=" + ENDPOINT)
        .withBean(AzureGlobalProperties.class)
        .withConfiguration(AutoConfigurations.of(AzureKeyVaultSecretAutoConfiguration.class, KeyVaultHealthConfiguration.class));

    @Test
    void runShouldCreateIndicator() {
        this.contextRunner.run((context) -> assertThat(context).hasSingleBean(KeyVaultSecretHealthIndicator.class));
    }

    @Test
    void runWhenDisabledShouldNotCreateIndicator() {
        this.contextRunner.withPropertyValues("management.health.azure-keyvault.enabled:false")
            .run((context) -> assertThat(context).doesNotHaveBean(KeyVaultSecretHealthIndicator.class));
    }
}
