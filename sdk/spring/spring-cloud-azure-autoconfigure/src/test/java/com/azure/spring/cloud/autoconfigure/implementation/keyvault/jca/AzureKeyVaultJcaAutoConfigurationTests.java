// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultJcaProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultSslBundlesProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AzureKeyVaultJcaAutoConfigurationTests {
    private static final String ENDPOINT = "https:/%s.vault.azure.net/";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureKeyVaultJcaAutoConfiguration.class,
            AzureGlobalPropertiesAutoConfiguration.class));

    @Test
    void noJcaProviderClass() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(KeyVaultJcaProvider.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureKeyVaultJcaAutoConfiguration.class));
    }

    @Test
    void disabledJca() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.keyvault.jca.enabled=false"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureKeyVaultJcaAutoConfiguration.class));
    }

    @Test
    void keyVaultCertificates() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.keyvault.jca.endpoint=" + String.format(ENDPOINT, "mykv"),
                "spring.ssl.bundle.azure-keyvault.test.key.alias=test"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureKeyVaultJcaAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureKeyVaultJcaProperties.class);
                assertThat(context).hasSingleBean(AzureKeyVaultSslBundlesProperties.class);
                assertThat(context).hasSingleBean(AzureKeyVaultSslBundlesRegistrar.class);
            });
    }
}
