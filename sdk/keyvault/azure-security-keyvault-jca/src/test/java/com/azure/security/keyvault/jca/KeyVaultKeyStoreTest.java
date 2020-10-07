// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import static java.util.logging.Level.ALL;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The JUnit tests for the KeyVaultKeyStore class.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
 */
public class KeyVaultKeyStoreTest {

    /**
     * Test engineGetCertificate method.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testEngineGetCertificate() throws Exception {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
                System.getProperty("azure.keyvault.uri"),
                System.getProperty("azure.tenant.id"),
                System.getProperty("azure.client.id"),
                System.getProperty("azure.client.secret"));
        keystore.engineLoad(parameter);
        assertNull(keystore.engineGetCertificate("myalias"));
    }

    /**
     * Test engineGetCertificateAlias method.
     *
     * @throws Exception when an error occurs.
     */
    @Test
    public void testEngineGetCertificateAlias() throws Exception {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
                System.getProperty("azure.keyvault.uri"),
                System.getProperty("azure.tenant.id"),
                System.getProperty("azure.client.id"),
                System.getProperty("azure.client.secret"));
        keystore.engineLoad(parameter);
        assertNull(keystore.engineGetCertificateAlias(null));
    }

    /**
     * Test engineGetCertificateChain method.
     *
     * @throws Exception when an error occurs.
     */
    @Test
    public void testEngineGetCertificateChain() throws Exception {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
                System.getProperty("azure.keyvault.uri"),
                System.getProperty("azure.tenant.id"),
                System.getProperty("azure.client.id"),
                System.getProperty("azure.client.secret"));
        keystore.engineLoad(parameter);
        assertNull(keystore.engineGetCertificateChain("myalias"));
    }

    /**
     * Test engineIsCertificateEntry method.
     *
     * @throws Exception when an error occurs.
     */
    @Test
    public void testEngineIsCertificateEntry() throws Exception {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
                System.getProperty("azure.keyvault.uri"),
                System.getProperty("azure.tenant.id"),
                System.getProperty("azure.client.id"),
                System.getProperty("azure.client.secret"));
        keystore.engineLoad(parameter);
        assertFalse(keystore.engineIsCertificateEntry("myalias"));
    }

    /**
     * Test engineSetCertificateEntry method.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testEngineSetCertificateEntry() throws Exception {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        keystore.engineSetCertificateEntry("myalias", null);
    }

    /**
     * Test engineGetKey method.
     *
     * @throws Exception when an error occurs.
     */
    @Test
    public void testEngineGetKey() throws Exception {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
                System.getProperty("azure.keyvault.uri"),
                System.getProperty("azure.tenant.id"),
                System.getProperty("azure.client.id"),
                System.getProperty("azure.client.secret"));
        keystore.engineLoad(parameter);
        assertNull(keystore.engineGetKey("myalias", null));
    }

    /**
     * Test engineIsKeyEntry method.
     *
     * @throws Exception when an error occurs.
     */
    @Test
    public void testEngineIsKeyEntry() throws Exception {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
                System.getProperty("azure.keyvault.uri"),
                System.getProperty("azure.tenant.id"),
                System.getProperty("azure.client.id"),
                System.getProperty("azure.client.secret"));
        keystore.engineLoad(parameter);
        assertFalse(keystore.engineIsKeyEntry("myalias"));
    }

    /**
     * Test engineSetKeyEntry method.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testEngineSetKeyEntry() throws Exception {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        keystore.engineSetKeyEntry("myalias", null, null);
    }

    /**
     * Test engineSetKeyEntry method.
     *
     * @throws Exception when an error occurs.
     */
    @Test
    public void testEngineSetKeyEntry2() throws Exception {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        keystore.engineSetKeyEntry("myalias", null, null, null);
    }

    /**
     * Test engineAliases method.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testEngineAliases() throws Exception {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
                System.getProperty("azure.keyvault.uri"),
                System.getProperty("azure.tenant.id"),
                System.getProperty("azure.client.id"),
                System.getProperty("azure.client.secret"));
        keystore.engineLoad(parameter);
        assertTrue(keystore.engineAliases().hasMoreElements());
    }

    /**
     * Test engineContainsAlias method.
     *
     * @throws Exception when an error occurs.
     */
    @Test
    public void testEngineContainsAlias() throws Exception {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
                System.getProperty("azure.keyvault.uri"),
                System.getProperty("azure.tenant.id"),
                System.getProperty("azure.client.id"),
                System.getProperty("azure.client.secret"));
        keystore.engineLoad(parameter);
        assertFalse(keystore.engineContainsAlias("myalias"));
    }

    /**
     * Test engineGetCretionDate method.
     */
    @Test
    public void testEngineGetCreationDate() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        assertNotNull(keystore.engineGetCreationDate("myalias"));
    }

    /**
     * Test engineDeleteEntry method.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testEngineDeleteEntry() throws Exception {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        keystore.engineDeleteEntry("myalias");
    }

    /**
     * Test engineSize method.
     */
    @Test
    public void testEngineSize() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        assertTrue(keystore.engineSize() >= 0);
    }

    /**
     * Test engineStore method.
     *
     * @throws Exception when an error occurs.
     */
    @Test
    public void testEngineStore() throws Exception {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        keystore.engineStore(null, null);
    }
}
