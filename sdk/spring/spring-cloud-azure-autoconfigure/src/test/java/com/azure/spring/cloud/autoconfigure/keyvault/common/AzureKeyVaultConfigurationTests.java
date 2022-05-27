// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.common;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.common.AzureKeyVaultProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AzureKeyVaultConfigurationTests {

    private static final String ENDPOINT = "https:/%s.vault.azure.net/";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
        .withConfiguration(AutoConfigurations.of(AzureKeyVaultConfiguration.class));

    @Test
    void configurationPropertiesShouldBind() {
        String endpoint = String.format(ENDPOINT, "mykv");
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.keyvault.endpoint=" + endpoint
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureKeyVaultProperties.class);
                AzureKeyVaultProperties properties = context.getBean(AzureKeyVaultProperties.class);
                assertEquals(endpoint, properties.getEndpoint());
            });
    }

}
