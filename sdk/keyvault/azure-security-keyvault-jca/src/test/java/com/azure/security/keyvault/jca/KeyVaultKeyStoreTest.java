// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * The JUnit tests for the KeyVaultKeyStore class.
 */
@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class KeyVaultKeyStoreTest {

    private static String certificateName;

    private static KeyVaultKeyStore keystore;

    @BeforeAll
    public static void setEnvironmentProperty() {
        PropertyConvertorUtils.putEnvironmentPropertyToSystemPropertyForKeyVaultJca();
        keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getenv("AZURE_KEYVAULT_ENDPOINT"),
            System.getenv("AZURE_KEYVAULT_TENANT_ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT_ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT_SECRET"));
        certificateName = System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME");
        keystore.engineLoad(parameter);
    }

    @Test
    public void testEngineGetCertificate() {
        assertNotNull(keystore.engineGetCertificate(certificateName));
    }


    @Test
    public void testEngineGetCertificateChain() {
        assertNotNull(keystore.engineGetCertificateChain(certificateName));
    }

    @Test
    public void testEngineGetKey() {
        assertNotNull(keystore.engineGetKey(certificateName, null));
    }


    @Test
    public void testEngineSetKeyEntry() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        keystore.engineSetKeyEntry(certificateName, null, null);
    }

    @Test
    public void testEngineAliases() {
        assertTrue(keystore.engineAliases().hasMoreElements());
    }


    @Test
    public void testEngineGetCreationDate() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        assertNotNull(keystore.engineGetCreationDate(certificateName));
    }

    @Test
    public void testEngineDeleteEntry() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        assertTrue(keystore.engineContainsAlias(certificateName));
        keystore.engineDeleteEntry(certificateName);
        assertFalse(keystore.engineContainsAlias(certificateName));
    }

    @Test
    public void testEngineSize() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        assertTrue(keystore.engineSize() >= 0);
    }

    @Test
    public void testRefreshEngineGetCertificate() throws Exception {
        System.setProperty("azure.keyvault.jca.refresh-certificates-when-have-un-trust-certificate", "true");
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);
        KeyStore ks = PropertyConvertorUtils.getKeyVaultKeyStore();
        Certificate certificate = ks.getCertificate(certificateName);
        ks.deleteEntry(certificateName);
        Thread.sleep(10);
        assertEquals(ks.getCertificateAlias(certificate), certificateName);
    }

    @Test
    public void testNotRefreshEngineGetCertificate() throws Exception {
        System.setProperty("azure.keyvault.jca.refresh-certificates-when-have-un-trust-certificate", "false");
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);
        KeyStore ks = PropertyConvertorUtils.getKeyVaultKeyStore();
        Certificate certificate = ks.getCertificate(certificateName);
        ks.deleteEntry(certificateName);
        assertNull(ks.getCertificateAlias(certificate));
    }

}
