// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca;

import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultJcaProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultSslBundleProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.ssl.SslBundleRegistry;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.ClassUtils;

import java.security.KeyStore;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

@Isolated("Run this by itself as it captures System.out")
@ExtendWith({OutputCaptureExtension.class})
class AzureKeyVaultSslBundleRegistrarTests {

    @Test
    void noJcaProviderOnClassPath(CapturedOutput capturedOutput) {
        AzureKeyVaultSslBundleRegistrar registrar = new AzureKeyVaultSslBundleRegistrar(new AzureKeyVaultJcaProperties(), new AzureKeyVaultSslBundleProperties());
        registrar.setResourceLoader(new DefaultResourceLoader());
        SslBundleRegistry registry = Mockito.mock(SslBundleRegistry.class);

        try (MockedStatic<ClassUtils> classUtils = mockStatic(ClassUtils.class)) {
            classUtils.when(() -> ClassUtils.isPresent("com.azure.security.keyvault.jca.KeyVaultJcaProvider",
                    AzureKeyVaultSslBundleRegistrar.class.getClassLoader()))
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
    void notConfigureSslBundlesAzureKeyvault(CapturedOutput capturedOutput) {
        AzureKeyVaultSslBundleRegistrar registrar = new AzureKeyVaultSslBundleRegistrar(new AzureKeyVaultJcaProperties(), new AzureKeyVaultSslBundleProperties());
        registrar.setResourceLoader(new DefaultResourceLoader());
        SslBundleRegistry registry = Mockito.mock(SslBundleRegistry.class);

        registrar.registerBundles(registry);
        then(registry).should(times(0)).registerBundle(anyString(), any());
        String allOutput = capturedOutput.getAll();
        String log = "Skip configuring Key Vault SSL bundles because 'spring.ssl.bundle.azure-keyvault' is empty.";
        assertTrue(allOutput.contains(log));
    }

    @Test
    void notConfigureEndpointOrSslBundleProperties(CapturedOutput capturedOutput) {
        AzureKeyVaultSslBundleProperties sslBundleProperties = new AzureKeyVaultSslBundleProperties();
        AzureKeyVaultSslBundleRegistrar registrar = new AzureKeyVaultSslBundleRegistrar(new AzureKeyVaultJcaProperties(), sslBundleProperties);
        registrar.setResourceLoader(new DefaultResourceLoader());
        SslBundleRegistry registry = Mockito.mock(SslBundleRegistry.class);

        sslBundleProperties.getKeyvault().put("testBundle", new AzureKeyVaultSslBundleProperties.KeyVaultSslBundleProperties());

        registrar.registerBundles(registry);
        then(registry).should(times(0)).registerBundle(anyString(), any());
        String allOutput = capturedOutput.getAll();
        String log = "Skip configuring Key Vault SSL bundle 'testBundle'. Consider configuring 'keyvault-ref', "
            + "'certificate-paths.custom' or 'certificate-paths.well-known' properties of the keystore or "
            + "truststore.";
        assertTrue(allOutput.contains(log));
    }

    @Test
    void registerKeyVaultSslBundle(CapturedOutput capturedOutput) {
        AzureKeyVaultJcaProperties jcaProperties = new AzureKeyVaultJcaProperties();
        AzureKeyVaultSslBundleProperties sslBundleProperties = new AzureKeyVaultSslBundleProperties();
        AzureKeyVaultSslBundleRegistrar registrar = new AzureKeyVaultSslBundleRegistrar(jcaProperties, sslBundleProperties);
        registrar.setResourceLoader(new DefaultResourceLoader());
        SslBundleRegistry registry = Mockito.mock(SslBundleRegistry.class);

        try (MockedStatic<KeyStore> keyStoreMockedStatic = mockStatic(KeyStore.class)) {
            String bundleName = "testBundle";
            KeyStore keyStore = Mockito.mock(KeyStore.class);
            keyStoreMockedStatic.when(() -> KeyStore.getInstance("AzureKeyVault")).thenReturn(keyStore);

            String keyvaultName = "keyvault1";
            AzureKeyVaultJcaProperties.JcaVaultProperties jcaVaultProperties = new AzureKeyVaultJcaProperties.JcaVaultProperties();
            jcaVaultProperties.setEndpoint("https://test.vault.azure.net/");
            jcaProperties.getVaults().put(keyvaultName, jcaVaultProperties);
            AzureKeyVaultSslBundleProperties.KeyVaultSslBundleProperties bundleProperties = new AzureKeyVaultSslBundleProperties.KeyVaultSslBundleProperties();
            bundleProperties.getTruststore().setKeyvaultRef(keyvaultName);

            sslBundleProperties.getKeyvault().put(bundleName, bundleProperties);
            registrar.registerBundles(registry);
            then(registry).should(times(1)).registerBundle(eq(bundleName), any());
            String allOutput = capturedOutput.getAll();
            String keystoreLog = "The keystore parameter of Key Vault SSL bundle 'testBundle' is null.";
            String registerLog = "Registered Azure Key Vault SSL bundle '" + bundleName + "'.";
            assertTrue(allOutput.contains(keystoreLog) || allOutput.contains(registerLog));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "test-client-id" })
    void registerKeyVaultSslBundleUsingManagedIdentity(String clientId, CapturedOutput capturedOutput) {
        AzureKeyVaultJcaProperties jcaProperties = new AzureKeyVaultJcaProperties();
        AzureKeyVaultSslBundleProperties sslBundleProperties = new AzureKeyVaultSslBundleProperties();
        AzureKeyVaultSslBundleRegistrar registrar = new AzureKeyVaultSslBundleRegistrar(jcaProperties, sslBundleProperties);
        registrar.setResourceLoader(new DefaultResourceLoader());
        SslBundleRegistry registry = Mockito.mock(SslBundleRegistry.class);

        try (MockedStatic<KeyStore> keyStoreMockedStatic = mockStatic(KeyStore.class)) {
            String bundleName = "tlsServerBundle";
            KeyStore keyStore = Mockito.mock(KeyStore.class);
            keyStoreMockedStatic.when(() -> KeyStore.getInstance("AzureKeyVault")).thenReturn(keyStore);

            String keyvaultName = "keyvault1";
            AzureKeyVaultJcaProperties.JcaVaultProperties jcaVaultProperties = new AzureKeyVaultJcaProperties.JcaVaultProperties();
            jcaVaultProperties.setEndpoint("https://test.vault.azure.net/");

            jcaVaultProperties.getCredential().setManagedIdentityEnabled(true);
            jcaVaultProperties.getCredential().setClientId(clientId);

            jcaProperties.getVaults().put(keyvaultName, jcaVaultProperties);
            AzureKeyVaultSslBundleProperties.KeyVaultSslBundleProperties bundleProperties = new AzureKeyVaultSslBundleProperties.KeyVaultSslBundleProperties();
            bundleProperties.getKey().setAlias("server");
            bundleProperties.getKeystore().setKeyvaultRef(keyvaultName);
            bundleProperties.getTruststore().setKeyvaultRef(keyvaultName);

            sslBundleProperties.getKeyvault().put(bundleName, bundleProperties);
            registrar.registerBundles(registry);
            then(registry).should(times(1)).registerBundle(eq(bundleName), any());
            String allOutput = capturedOutput.getAll();
            String registerLog = "Registered Azure Key Vault SSL bundle '" + bundleName + "'.";
            assertTrue(allOutput.contains(registerLog));
        }
    }

    @Test
    void registerMultipleSslBundles(CapturedOutput capturedOutput) {
        AzureKeyVaultJcaProperties jcaProperties = new AzureKeyVaultJcaProperties();
        AzureKeyVaultSslBundleProperties sslBundleProperties = new AzureKeyVaultSslBundleProperties();
        AzureKeyVaultSslBundleRegistrar registrar = new AzureKeyVaultSslBundleRegistrar(jcaProperties, sslBundleProperties);
        registrar.setResourceLoader(new DefaultResourceLoader());
        SslBundleRegistry registry = Mockito.mock(SslBundleRegistry.class);

        try (MockedStatic<KeyStore> keyStoreMockedStatic = mockStatic(KeyStore.class)) {
            KeyStore keyStore = Mockito.mock(KeyStore.class);
            keyStoreMockedStatic.when(() -> KeyStore.getInstance("AzureKeyVault")).thenReturn(keyStore);

            AzureKeyVaultJcaProperties.JcaVaultProperties kv1 = new AzureKeyVaultJcaProperties.JcaVaultProperties();
            kv1.setEndpoint("https://test1.vault.azure.net/");
            jcaProperties.getVaults().put("keyvault1", kv1);

            AzureKeyVaultJcaProperties.JcaVaultProperties kv2 = new AzureKeyVaultJcaProperties.JcaVaultProperties();
            kv2.setEndpoint("https://test2.vault.azure.net/");
            jcaProperties.getVaults().put("keyvault2", kv2);

            // bundle for using kv1 for keystore
            AzureKeyVaultSslBundleProperties.KeyVaultSslBundleProperties kvBundle1 = new AzureKeyVaultSslBundleProperties.KeyVaultSslBundleProperties();
            kvBundle1.getKeystore().setKeyvaultRef("keyvault1");
            sslBundleProperties.getKeyvault().put("kvBundle1", kvBundle1);


            // bundle for using kv1 for keystore and kv2 for truststore
            AzureKeyVaultSslBundleProperties.KeyVaultSslBundleProperties kvBundle2 = new AzureKeyVaultSslBundleProperties.KeyVaultSslBundleProperties();
            kvBundle2.getKeystore().setKeyvaultRef("keyvault1");
            kvBundle2.getTruststore().setKeyvaultRef("keyvault2");
            sslBundleProperties.getKeyvault().put("kvBundle2", kvBundle2);

            // bundle for local trust store with custom certs
            AzureKeyVaultSslBundleProperties.KeyVaultSslBundleProperties localCustomTrustStoreBundle = new AzureKeyVaultSslBundleProperties.KeyVaultSslBundleProperties();
            localCustomTrustStoreBundle.getTruststore().getCertificatePaths().setCustom("classpath:keyvault/certificate-paths/custom");
            sslBundleProperties.getKeyvault().put("localCustom", localCustomTrustStoreBundle);

            // bundle for local trust store with well-known certs
            AzureKeyVaultSslBundleProperties.KeyVaultSslBundleProperties localWellKnownBundle = new AzureKeyVaultSslBundleProperties.KeyVaultSslBundleProperties();
            localWellKnownBundle.getTruststore().getCertificatePaths().setWellKnown("classpath:keyvault/certificate-paths/well-known");
            sslBundleProperties.getKeyvault().put("localWellKnown", localWellKnownBundle);

            registrar.registerBundles(registry);
            String allOutput = capturedOutput.getAll();
            Arrays.stream(sslBundleProperties.getKeyvault().keySet().toArray(new String[0])).forEach(bundleName -> {
                then(registry).should(times(1)).registerBundle(eq(bundleName), any());
                String log = "Registered Azure Key Vault SSL bundle '" + bundleName + "'.";
                assertTrue(allOutput.contains(log));
            });
        }
    }
}
