// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca;

import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultJcaProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultSslBundleProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultSslBundlesProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.ssl.SslBundleRegistry;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.ClassUtils;

import java.security.KeyStore;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

@Isolated("Run this by itself as it captures System.out")
@ExtendWith({ OutputCaptureExtension.class })
public class AzureKeyVaultSslBundlesRegistrarTests {

    private AzureKeyVaultJcaProperties jcaProperties;
    private AzureKeyVaultSslBundlesProperties sslBundlesProperties;
    private AzureKeyVaultSslBundlesRegistrar registrar;
    private SslBundleRegistry registry;

    @BeforeEach
    void setUp() {
        this.jcaProperties = new AzureKeyVaultJcaProperties();
        this.sslBundlesProperties = new AzureKeyVaultSslBundlesProperties();
        this.registrar = new AzureKeyVaultSslBundlesRegistrar(jcaProperties, sslBundlesProperties);
        this.registry = Mockito.mock(SslBundleRegistry.class);
        this.registrar.setResourceLoader(new DefaultResourceLoader());
    }

    @Test
    void noJcaProviderOnClassPath(CapturedOutput capturedOutput) {
        try (MockedStatic<ClassUtils> classUtils = mockStatic(ClassUtils.class)) {
            classUtils.when(() -> ClassUtils.isPresent("com.azure.security.keyvault.jca.KeyVaultJcaProvider",
                          AzureKeyVaultSslBundlesRegistrar.class.getClassLoader()))
                      .thenReturn(false);
            registrar.registerBundles(registry);
            then(registry).should(times(0)).registerBundle(anyString(), any());
            String allOutput = capturedOutput.getAll();
            String log = "Skip configuring Key Vault SSL bundles because com.azure:azure-security-keyvault-jca "
                + "doesn't exist in classpath.";
            assertTrue(allOutput.contains(log));
        }
    }

    @Test
    void notConfigureEndpointAndCertPath(CapturedOutput capturedOutput) {
        registrar.registerBundles(registry);
        then(registry).should(times(0)).registerBundle(anyString(), any());
        String allOutput = capturedOutput.getAll();
        String log = "Skip configuring Key Vault SSL bundles because spring.ssl.bundle.azure-keyvault is empty.";
        assertTrue(allOutput.contains(log));
    }

    @Test
    void configureSslBundleEndpointWithoutKeyName(CapturedOutput capturedOutput) {
        jcaProperties.setEndpoint("https://test.vault.azure.net/");

        AzureKeyVaultSslBundleProperties bundleProperties = new AzureKeyVaultSslBundleProperties();
        sslBundlesProperties.getAzureKeyvault().put("testBundle", bundleProperties);

        registrar.registerBundles(registry);
        then(registry).should(times(0)).registerBundle(anyString(), any());
        String allOutput = capturedOutput.getAll();
        String log = "Skip configuring Key Vault SSL bundle testBundle, because both spring.ssl.bundle.azure-keyvault"
            + ".testBundle.endpoint and "
            + "spring.ssl.bundle.azure-keyvault.testBundle.key.alias must be configured at the same time; or at least"
            + " one of "
            + "spring.ssl.bundle.azure-keyvault.testBundle.certificate-paths.custom and spring.ssl.bundle"
            + ".azure-keyvault.testBundle.certificate-paths.well-known "
            + "must be configured.";
        assertTrue(allOutput.contains(log));
    }

    @Test
    void registerKeyVaultCertificates(CapturedOutput capturedOutput) {
        try (MockedStatic<KeyStore> keyStoreMockedStatic = mockStatic(KeyStore.class)) {
            String bundleName = "testBundle";
            KeyStore keyStore = Mockito.mock(KeyStore.class);
            keyStoreMockedStatic.when(() -> KeyStore.getInstance("AzureKeyVault")).thenReturn(keyStore);

            jcaProperties.setEndpoint("https://test.vault.azure.net/");
            AzureKeyVaultSslBundleProperties bundleProperties = new AzureKeyVaultSslBundleProperties();
            bundleProperties.getKey().setAlias("self-signed");

            sslBundlesProperties.getAzureKeyvault().put(bundleName, bundleProperties);
            registrar.registerBundles(registry);
            then(registry).should(times(1)).registerBundle(eq(bundleName), any());
            String allOutput = capturedOutput.getAll();
            String log = "Registered Azure Key Vault SSL bundle " + bundleName + ".";
            assertTrue(allOutput.contains(log));
        }
    }

    @Test
    void registerMultipleSslBundles(CapturedOutput capturedOutput) {
        try (MockedStatic<KeyStore> keyStoreMockedStatic = mockStatic(KeyStore.class)) {
            String[] bundleNames = new String[] { "testBundle", "inheritedSslBundle", "notInheritedSslBundle",
                "localCustom", "localWellKnown", "local" };
            KeyStore keyStore = Mockito.mock(KeyStore.class);
            keyStoreMockedStatic.when(() -> KeyStore.getInstance("AzureKeyVault")).thenReturn(keyStore);

            Map<String, AzureKeyVaultSslBundleProperties> azureKeyVault = sslBundlesProperties.getAzureKeyvault();
            jcaProperties.setEndpoint("https://test.vault.azure.net/");
            AzureKeyVaultSslBundleProperties testBundle = new AzureKeyVaultSslBundleProperties();
            testBundle.getKey().setAlias("self-signed");
            azureKeyVault.put(bundleNames[0], testBundle);

            AzureKeyVaultSslBundleProperties inheritedSslBundle = new AzureKeyVaultSslBundleProperties();
            inheritedSslBundle.getKey().setAlias("tomcat");
            azureKeyVault.put(bundleNames[1], inheritedSslBundle);

            AzureKeyVaultSslBundleProperties notInheritedSslBundle = new AzureKeyVaultSslBundleProperties();
            notInheritedSslBundle.setInherit(false);
            notInheritedSslBundle.setEndpoint("https://test.vault.azure.net/");
            notInheritedSslBundle.getKey().setAlias("jetty");
            azureKeyVault.put(bundleNames[2], notInheritedSslBundle);

            AzureKeyVaultSslBundleProperties localCustom = new AzureKeyVaultSslBundleProperties();
            localCustom.getCertificatePaths().setCustom("custom");
            azureKeyVault.put(bundleNames[3], localCustom);

            AzureKeyVaultSslBundleProperties localWellKnown = new AzureKeyVaultSslBundleProperties();
            localWellKnown.getCertificatePaths().setWellKnown("wellKnown");
            azureKeyVault.put(bundleNames[4], localWellKnown);

            AzureKeyVaultSslBundleProperties local = new AzureKeyVaultSslBundleProperties();
            local.getCertificatePaths().setWellKnown("localWellKnown");
            local.getCertificatePaths().setCustom("localCustom");
            azureKeyVault.put(bundleNames[5], local);

            registrar.registerBundles(registry);
            String allOutput = capturedOutput.getAll();
            Arrays.stream(bundleNames).forEach(bundleName -> {
                then(registry).should(times(1)).registerBundle(eq(bundleName), any());
                String log = "Registered Azure Key Vault SSL bundle " + bundleName + ".";
                assertTrue(allOutput.contains(log));
            });
        }
    }
}
