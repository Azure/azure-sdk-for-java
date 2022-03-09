// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.security.keyvault.certificates.starter;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Security;

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

    @Test
    public void insertProviderTest() throws KeyStoreException {
        // Simulated users added KeyVaultJcaProvider to java.security
        Security.removeProvider("AzureKeyVault");
        Security.insertProviderAt(new KeyVaultJcaProvider(), 11);
        ConfigurableEnvironment environment = new StandardEnvironment();
        KeyVaultCertificatesEnvironmentPostProcessor postProcessor = new KeyVaultCertificatesEnvironmentPostProcessor();
        postProcessor.postProcessEnvironment(environment, null);
        KeyStore keyStore = KeyStore.getInstance("DKS");
        assertTrue(keyStore.getProvider() instanceof KeyVaultJcaProvider);
    }

    @Test
    public void insertProviderDefaultFileTest() throws KeyStoreException {
        // Simulated default java.security
        Security.removeProvider("AzureKeyVault");
        ConfigurableEnvironment environment = new StandardEnvironment();
        KeyVaultCertificatesEnvironmentPostProcessor postProcessor = new KeyVaultCertificatesEnvironmentPostProcessor();
        postProcessor.postProcessEnvironment(environment, null);
        KeyStore keyStore = KeyStore.getInstance("DKS");
        assertTrue(keyStore.getProvider() instanceof KeyVaultJcaProvider);
    }

}
