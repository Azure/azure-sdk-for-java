// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.security.keyvault.certificates.starter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultCertificatesEnvironmentPostProcessorTest {

    @Test
    public void overrideTrustManagerFactoryTest() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        assertFalse(KeyVaultCertificatesEnvironmentPostProcessor.overrideTrustManagerFactory(environment));

        TestPropertyValues.of("azure.keyvault.jca.overrideTrustManagerFactory=true",
                              "azure.keyvault.jca.override-trust-manager-factory=")
                          .applyTo(environment);
        assertTrue(KeyVaultCertificatesEnvironmentPostProcessor.overrideTrustManagerFactory(environment));

        TestPropertyValues.of("azure.keyvault.jca.overrideTrustManagerFactory=",
                              "azure.keyvault.jca.override-trust-manager-factory=")
                          .applyTo(environment);
        assertFalse(KeyVaultCertificatesEnvironmentPostProcessor.overrideTrustManagerFactory(environment));

        TestPropertyValues.of("azure.keyvault.jca.overrideTrustManagerFactory=",
                              "azure.keyvault.jca.override-trust-manager-factory=true")
                          .applyTo(environment);
        assertTrue(KeyVaultCertificatesEnvironmentPostProcessor.overrideTrustManagerFactory(environment));

        TestPropertyValues.of("azure.keyvault.jca.overrideTrustManagerFactory=Invalid",
                              "azure.keyvault.jca.override-trust-manager-factory=")
                          .applyTo(environment);
        assertFalse(KeyVaultCertificatesEnvironmentPostProcessor.overrideTrustManagerFactory(environment));

        TestPropertyValues.of("azure.keyvault.jca.overrideTrustManagerFactory=TRUE",
                             "azure.keyvault.jca.override-trust-manager-factory=")
                          .applyTo(environment);
        assertTrue(KeyVaultCertificatesEnvironmentPostProcessor.overrideTrustManagerFactory(environment));

        TestPropertyValues.of("azure.keyvault.jca.overrideTrustManagerFactory=Invalid",
                              "azure.keyvault.jca.override-trust-manager-factory=TRUe")
                          .applyTo(environment);
        assertTrue(KeyVaultCertificatesEnvironmentPostProcessor.overrideTrustManagerFactory(environment));
    }
}
