// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.security.ProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The JUnit tests for the KeyVaultKeyStore class.
 */
@Disabled
public class KeyVaultKeyStoreTest {

    /**
     * Stores the CER test certificate (which is valid til 2120).
     */
    private static final String TEST_CERTIFICATE
        = "MIIDeDCCAmCgAwIBAgIQGghBu97rQJKNnUHPWU7xjDANBgkqhkiG9w0BAQsFADAk"
        + "MSIwIAYDVQQDExlodW5kcmVkLXllYXJzLmV4YW1wbGUuY29tMCAXDTIwMDkwMjE3"
        + "NDUyNFoYDzIxMjAwOTAyMTc1NTI0WjAkMSIwIAYDVQQDExlodW5kcmVkLXllYXJz"
        + "LmV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuU14"
        + "btkN5wmcO2WKXqm1NUKXzi79EtqiFFkrLgPAwj5NNwMw2Akm3GpdEpwkJ8/q3l7d"
        + "frDEVOO9gwZbz7xppyqutjxjllw8CCgjFdfK02btz56CGgh3X25ZZtzPbuMZJM0j"
        + "o4mVEdaFNJ0eUeMppS0DcbbuTWCF7Jf1gvr8GVqx+E0IJUFkE+D4kdTbnJSaeK0A"
        + "KEt94z88MPX18h8ud14uRVmUCYVZrZeswdE2tO1BpazrXELHuXCtrjGxsDDjDzeP"
        + "98aFI9kblkqoJS4TsmloLEjwZLm80cyJDEmpXXMtR7C0FFXFI1BAtIa4mxSgBLsT"
        + "L4GVPEGNANR8COYkHQIDAQABo4GjMIGgMA4GA1UdDwEB/wQEAwIFoDAJBgNVHRME"
        + "AjAAMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjAkBgNVHREEHTAbghlo"
        + "dW5kcmVkLXllYXJzLmV4YW1wbGUuY29tMB8GA1UdIwQYMBaAFOGTt4H3ho30O4e+"
        + "hebwJjm2VMvIMB0GA1UdDgQWBBThk7eB94aN9DuHvoXm8CY5tlTLyDANBgkqhkiG"
        + "9w0BAQsFAAOCAQEAGp8mCioVCmM+kZv6r+K2j2uog1k4HBwN1NfRoSsibDB8+QXF"
        + "bmNf3M0imiuR/KJgODyuROwaa/AalxNFMOP8XTL2YmP7XsddBs9ONHHQXKjY/Ojl"
        + "PsIPR7vZjwYPfEB+XEKl2fOIxDQQ921POBV7M6DdTC49T5X+FsLR1AIIfinVetT9"
        + "QmNuvzulBX0T0rea/qpcPK4HTj7ToyImOaf8sXRv2s2ODLUrKWu5hhTNH2l6RIkQ"
        + "U/aIAdQRfDaSE9jhtcVu5d5kCgBs7nz5AzeCisDPo5zIt4Mxej3iVaAJ79oEbHOE"
        + "p192KLXLV/pscA4Wgb+PJ8AAEa5B6xq8p9JO+Q==";

    @Test
    public void testEngineGetCertificate() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
        keystore.engineLoad(parameter);
        assertNull(keystore.engineGetCertificate("myalias"));
    }

    @Test
    public void testEngineGetCertificateAlias() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
        keystore.engineLoad(parameter);
        assertNull(keystore.engineGetCertificateAlias(null));
    }

    @Test
    public void testEngineGetCertificateChain() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
        keystore.engineLoad(parameter);
        assertNull(keystore.engineGetCertificateChain("myalias"));
    }

    @Test
    public void testEngineIsCertificateEntry() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
        keystore.engineLoad(parameter);
        assertFalse(keystore.engineIsCertificateEntry("myalias"));
    }

    @Test
    public void testEngineSetCertificateEntry() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
        keystore.engineLoad(parameter);

        X509Certificate certificate;

        try {
            byte[] certificateBytes = Base64.getDecoder().decode(TEST_CERTIFICATE);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateBytes));
        } catch (CertificateException e) {
            throw new ProviderException(e);
        }

        keystore.engineSetCertificateEntry("setcert", certificate);
        assertNotNull(keystore.engineGetCertificate("setcert"));
    }

    @Test
    public void testEngineGetKey() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
        keystore.engineLoad(parameter);
        assertNull(keystore.engineGetKey("myalias", null));
    }

    @Test
    public void testEngineIsKeyEntry() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
        keystore.engineLoad(parameter);
        assertFalse(keystore.engineIsKeyEntry("myalias"));
    }

    @Test
    public void testEngineSetKeyEntry() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        keystore.engineSetKeyEntry("myalias", null, null);
    }

    @Test
    public void testEngineSetKeyEntry2() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        keystore.engineSetKeyEntry("myalias", null, null, null);
    }

    @Test
    public void testEngineAliases() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
        keystore.engineLoad(parameter);
        assertTrue(keystore.engineAliases().hasMoreElements());
    }

    @Test
    public void testEngineContainsAlias() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
        keystore.engineLoad(parameter);
        assertFalse(keystore.engineContainsAlias("myalias"));
    }

    @Test
    public void testEngineGetCreationDate() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        assertNotNull(keystore.engineGetCreationDate("myalias"));
    }

    @Test
    public void testEngineDeleteEntry() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        keystore.engineDeleteEntry("myalias");
    }

    @Test
    public void testEngineSize() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        assertTrue(keystore.engineSize() >= 0);
    }

    @Test
    public void testEngineStore() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        keystore.engineStore(null, null);
    }
}
