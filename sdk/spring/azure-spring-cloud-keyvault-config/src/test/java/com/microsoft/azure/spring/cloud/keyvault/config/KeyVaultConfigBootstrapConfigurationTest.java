/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class KeyVaultConfigBootstrapConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(KeyVaultConfigBootstrapConfiguration.class));

    @Test
    public void testConfigurationDefaults() {
        contextRunner.withPropertyValues(
                "spring.cloud.azure.keyvault.config.credentials.clientId=fake-client-id",
                "spring.cloud.azure.keyvault.config.credentials.clientSecret=fake-client-secret")
                .run(context -> {
                    KeyVaultConfigProperties properties = context.getBean(KeyVaultConfigProperties.class);
                    assertEquals(null, properties.getName());
                    assertEquals(null, properties.getActiveProfile());
                    assertEquals(true, properties.isEnabled());
                    assertEquals(true, properties.isFailFast());
                });
    }

    @Test
    public void testConfigurationLoadedSuccessfully() {
        contextRunner.withPropertyValues(
                "spring.cloud.azure.keyvault.config.name=azure-app",
                "spring.cloud.azure.keyvault.config.enabled=true",
                "spring.cloud.azure.keyvault.config.failFast=false",
                "spring.cloud.azure.keyvault.config.credentials.clientId=fake-client-id",
                "spring.cloud.azure.keyvault.config.credentials.clientSecret=fake-client-secret")
                .run(context -> {
                    KeyVaultConfigProperties properties = context.getBean(KeyVaultConfigProperties.class);
                    assertEquals("azure-app", properties.getName());
                    assertEquals(true, properties.isEnabled());
                    assertEquals(false, properties.isFailFast());
                    assertEquals("fake-client-id", properties.getCredentials().getClientId());
                    assertEquals("fake-client-secret", properties.getCredentials().getClientSecret());
                });
    }

    @Test(expected = IllegalStateException.class)
    public void testConfigurationValidationErrorWithNoClientId() {
        contextRunner.withPropertyValues("spring.cloud.azure.keyvault.config.name=azure-app")
                .run(context -> context.getBean(KeyVaultConfigProperties.class));
    }
}
