// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.autoconfigure.keyvault;

import com.azure.spring.cloud.actuator.keyvault.KeyVaultCertificateHealthIndicator;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.keyvault.certificates.AzureKeyVaultCertificateAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class KeyVaultCertificateHealthConfigurationTests {

    private static final String ENDPOINT = "https:/%s.vault.azure.net/";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues("spring.cloud.azure.keyvault.certificate.endpoint=" + ENDPOINT)
        .withBean(AzureGlobalProperties.class)
        .withConfiguration(
            AutoConfigurations.of(AzureKeyVaultCertificateAutoConfiguration.class,
                KeyVaultCertificateHealthConfiguration.class));

    @Test
    void runShouldCreateIndicator() {
        this.contextRunner.run((context) -> assertThat(context).hasSingleBean(KeyVaultCertificateHealthIndicator.class));
    }

    @Test
    void runWhenDisabledShouldNotCreateIndicator() {
        this.contextRunner.withPropertyValues("management.health.azure-keyvault-certificate.enabled:false")
            .run((context) -> assertThat(context).doesNotHaveBean(KeyVaultCertificateHealthIndicator.class));
    }
}
