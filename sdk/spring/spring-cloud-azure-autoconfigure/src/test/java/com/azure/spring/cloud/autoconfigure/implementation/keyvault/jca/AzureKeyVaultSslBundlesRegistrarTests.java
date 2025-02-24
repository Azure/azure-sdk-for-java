// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca;

import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultJcaConnectionProperties;
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
            String log = "Skip configuring Key Vault SSL bundles because 'com.azure:azure-security-keyvault-jca' "
                + "doesn't exist in classpath.";
            assertTrue(allOutput.contains(log));
        }
    }

    @Test
    void notConfigureEndpointAndCertPath(CapturedOutput capturedOutput) {
        registrar.registerBundles(registry);
        then(registry).should(times(0)).registerBundle(anyString(), any());
        String allOutput = capturedOutput.getAll();
        String log = "Skip configuring Key Vault SSL bundles because 'spring.ssl.bundle.azure-keyvault' is empty.";
        assertTrue(allOutput.contains(log));
    }

    @Test
    void configureSslBundleEndpointWithoutKeyName(CapturedOutput capturedOutput) {
        AzureKeyVaultSslBundleProperties bundleProperties = new AzureKeyVaultSslBundleProperties();
        sslBundlesProperties.getAzureKeyvault().put("testBundle", bundleProperties);

        registrar.registerBundles(registry);
        then(registry).should(times(0)).registerBundle(anyString(), any());
        String allOutput = capturedOutput.getAll();
        String log = "Skip configuring Key Vault SSL bundle 'testBundle'. At least configure the 'keyvault-ref' of the truststore; "
            + "or one of 'certificate-paths.custom' and 'certificate-paths.well-known' must be configured.";
        assertTrue(allOutput.contains(log));
    }

    @Test
    void registerKeyVaultSslBundle(CapturedOutput capturedOutput) {
        try (MockedStatic<KeyStore> keyStoreMockedStatic = mockStatic(KeyStore.class)) {
            String bundleName = "testBundle";
            KeyStore keyStore = Mockito.mock(KeyStore.class);
            keyStoreMockedStatic.when(() -> KeyStore.getInstance("AzureKeyVault")).thenReturn(keyStore);

            String keyvaultName = "keyvault1";
            AzureKeyVaultJcaConnectionProperties connectionProperties = new AzureKeyVaultJcaConnectionProperties();
            connectionProperties.setEndpoint("https://test.vault.azure.net/");
            jcaProperties.getConnections().put(keyvaultName, connectionProperties);
            AzureKeyVaultSslBundleProperties bundleProperties = new AzureKeyVaultSslBundleProperties();
            bundleProperties.getTruststore().setKeyvaultRef(keyvaultName);

            sslBundlesProperties.getAzureKeyvault().put(bundleName, bundleProperties);
            registrar.registerBundles(registry);
            then(registry).should(times(1)).registerBundle(eq(bundleName), any());
            String allOutput = capturedOutput.getAll();
            String keystoreLog = "The keystore parameter of Key Vault SSL bundle 'testBundle' is null.";
            String registerLog = "Registered Azure Key Vault SSL bundle '" + bundleName + "'.";
            assertTrue(allOutput.contains(keystoreLog) || allOutput.contains(registerLog));
        }
    }

    @Test
    void registerMultipleSslBundles(CapturedOutput capturedOutput) {
        try (MockedStatic<KeyStore> keyStoreMockedStatic = mockStatic(KeyStore.class)) {
            String[] bundleNames = new String[] {
                "oneKeyVault",
                "twoKeyVault",
                "localCustom",
                "localWellKnown",
                "local"
            };
            KeyStore keyStore = Mockito.mock(KeyStore.class);
            keyStoreMockedStatic.when(() -> KeyStore.getInstance("AzureKeyVault")).thenReturn(keyStore);

            Map<String, AzureKeyVaultSslBundleProperties> azureKeyVault = sslBundlesProperties.getAzureKeyvault();

            int index = 0;
            AzureKeyVaultSslBundleProperties oneKeyVault = getBundlePropertiesWithOneKeyVault();
            azureKeyVault.put(bundleNames[index++], oneKeyVault);

            AzureKeyVaultSslBundleProperties twoKeyVault = getBundlePropertiesWithOneKeyVault();
            azureKeyVault.put(bundleNames[index++], twoKeyVault);

            AzureKeyVaultSslBundleProperties localCustom = getBundlePropertiesWithLocalCustom();
            azureKeyVault.put(bundleNames[index++], localCustom);

            AzureKeyVaultSslBundleProperties localWellKnown = getBundlePropertiesWithLocalWellKnown();
            azureKeyVault.put(bundleNames[index++], localWellKnown);

            AzureKeyVaultSslBundleProperties local = getBundlePropertiesWithLocal();
            azureKeyVault.put(bundleNames[index], local);

            registrar.registerBundles(registry);
            String allOutput = capturedOutput.getAll();
            Arrays.stream(bundleNames).forEach(bundleName -> {
                then(registry).should(times(1)).registerBundle(eq(bundleName), any());
                String log = "Registered Azure Key Vault SSL bundle '" + bundleName + "'.";
                assertTrue(allOutput.contains(log));
            });
        }
    }

    private AzureKeyVaultSslBundleProperties getBundlePropertiesWithLocal() {
        AzureKeyVaultSslBundleProperties bundleProperties = new AzureKeyVaultSslBundleProperties();
        bundleProperties.getCertificatePaths().setWellKnown("classpath:keyvault/certificate-paths/well-known");
        bundleProperties.getCertificatePaths().setWellKnown("classpath:keyvault/certificate-paths/well-known");
        return bundleProperties;
    }

    private AzureKeyVaultSslBundleProperties getBundlePropertiesWithLocalWellKnown() {
        AzureKeyVaultSslBundleProperties bundleProperties = new AzureKeyVaultSslBundleProperties();
        bundleProperties.getCertificatePaths().setWellKnown("classpath:keyvault/certificate-paths/well-known");
        return bundleProperties;
    }

    private AzureKeyVaultSslBundleProperties getBundlePropertiesWithLocalCustom() {
        AzureKeyVaultSslBundleProperties bundleProperties = new AzureKeyVaultSslBundleProperties();
        bundleProperties.getCertificatePaths().setCustom("classpath:keyvault/certificate-paths/custom");
        return bundleProperties;
    }

    private AzureKeyVaultSslBundleProperties getBundlePropertiesWithWtoKeyVault() {
        String keyvault1 = "keyvault1";
        AzureKeyVaultJcaConnectionProperties connection1Properties = new AzureKeyVaultJcaConnectionProperties();
        connection1Properties.setEndpoint("https://test1.vault.azure.net/");
        jcaProperties.getConnections().put(keyvault1, connection1Properties);

        String keyvault2 = "keyvault2";
        AzureKeyVaultJcaConnectionProperties connection2Properties = new AzureKeyVaultJcaConnectionProperties();
        connection2Properties.setEndpoint("https://test2.vault.azure.net/");
        jcaProperties.getConnections().put(keyvault2, connection2Properties);

        AzureKeyVaultSslBundleProperties bundleProperties = new AzureKeyVaultSslBundleProperties();
        bundleProperties.getKeystore().setKeyvaultRef(keyvault1);
        bundleProperties.getTruststore().setKeyvaultRef(keyvault2);
        return bundleProperties;
    }

    private AzureKeyVaultSslBundleProperties getBundlePropertiesWithOneKeyVault() {
        String keyvault1 = "keyvault1";
        AzureKeyVaultJcaConnectionProperties connectionProperties = new AzureKeyVaultJcaConnectionProperties();
        connectionProperties.setEndpoint("https://test1.vault.azure.net/");
        jcaProperties.getConnections().put(keyvault1, connectionProperties);
        AzureKeyVaultSslBundleProperties bundleProperties = new AzureKeyVaultSslBundleProperties();
        bundleProperties.setKeyvaultRef(keyvault1);
        return bundleProperties;
    }
}
