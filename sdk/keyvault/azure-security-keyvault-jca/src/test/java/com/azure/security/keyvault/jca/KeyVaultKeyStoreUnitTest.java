// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.implementation.KeyVaultClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.ByteArrayInputStream;
import java.security.ProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;

@ResourceLock(Resources.SYSTEM_PROPERTIES)
public class KeyVaultKeyStoreUnitTest {

    /**
     * Stores the CER test certificate (which is valid til 2120).
     */
    private static final String TEST_CERTIFICATE = "MIIDeDCCAmCgAwIBAgIQGghBu97rQJKNnUHPWU7xjDANBgkqhkiG9w0BAQsFADAk"
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
    public void testEngineStore() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        keystore.engineStore(null, null);
    }

    @Test
    public void testEngineGetCertificateAlias() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        X509Certificate certificate;
        try {
            byte[] certificateBytes = Base64.getDecoder().decode(TEST_CERTIFICATE);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateBytes));
        } catch (CertificateException e) {
            throw new ProviderException(e);
        }
        keystore.engineSetCertificateEntry("setcert", certificate);
        assertNotNull(keystore.engineGetCertificateAlias(certificate));
    }

    @Test
    public void testEngineSetCertificateEntry() {
        KeyVaultKeyStore keystore = new KeyVaultKeyStore();
        X509Certificate certificate;
        try {
            byte[] certificateBytes = Base64.getDecoder().decode(TEST_CERTIFICATE);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            certificate
                = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certificateBytes));
        } catch (CertificateException e) {
            throw new ProviderException(e);
        }

        keystore.engineSetCertificateEntry("setcert", certificate);
        assertNotNull(keystore.engineGetCertificate("setcert"));
    }

    @Test
    public void testEngineLoadParameterOverridesSystemProperties() {
        System.setProperty(KeyVaultJcaPropertyNames.CERT_PATH_WELL_KNOWN, "/ambient/well-known");
        System.setProperty(KeyVaultJcaPropertyNames.CERT_PATH_CUSTOM, "/ambient/custom");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_REFRESH_CERTIFICATES_WHEN_HAVE_UNTRUST_CERTIFICATE,
            "false");

        KeyVaultKeyStore keyStore = new KeyVaultKeyStore();
        KeyVaultLoadStoreParameter parameter
            = new KeyVaultLoadStoreParameter(null).setCertPathWellKnown("/explicit/well-known")
                .setCertPathCustom("/explicit/custom")
                .setRefreshCertificatesWhenHaveUnTrustCertificate(true);

        keyStore.engineLoad(parameter);

        assertEquals("/explicit/well-known", keyStore.certPathWellKnown);
        assertEquals("/explicit/custom", keyStore.certPathCustom);
        assertTrue(keyStore.refreshCertificatesWhenHaveUnTrustCertificate);
    }

    @Test
    public void testEngineLoadPassesExplicitParameterToKeyVaultClient() {
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_URI, "https://ambient.vault.azure.net");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter("https://explicit.vault.azure.net");
        parameter.disableAiaDownload();
        List<List<?>> constructorArguments = new ArrayList<>();

        try (MockedConstruction<KeyVaultClient> mockedConstruction = mockConstruction(KeyVaultClient.class,
            (mock, context) -> constructorArguments.add(context.arguments()))) {
            KeyVaultKeyStore keyStore = new KeyVaultKeyStore();
            keyStore.engineLoad(parameter);
            assertEquals(2, mockedConstruction.constructed().size());
        }

        assertEquals(2, constructorArguments.size());
        assertSame(parameter, constructorArguments.get(1).get(0));
        assertTrue(((KeyVaultLoadStoreParameter) constructorArguments.get(1).get(0)).isAiaDownloadDisabled());
    }

    @BeforeEach
    @AfterEach
    public void clearCertificateAliasFilterPatternProperties() {
        System.clearProperty(KeyVaultJcaPropertyNames.KEYVAULT_URI);
        System.clearProperty(KeyVaultJcaPropertyNames.CERT_PATH_WELL_KNOWN);
        System.clearProperty(KeyVaultJcaPropertyNames.CERT_PATH_CUSTOM);
        System.clearProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_REFRESH_CERTIFICATES_WHEN_HAVE_UNTRUST_CERTIFICATE);
    }

}
