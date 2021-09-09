// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.ProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The JUnit test for the KeyVaultCertificates.
 */
@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class KeyVaultCertificatesTest {

    private static String certificateName;

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

    @BeforeAll
    public static void setEnvironmentProperty() {
        PropertyConvertorUtils.putEnvironmentPropertyToSystemPropertyForKeyVaultJca();
        PropertyConvertorUtils.addKeyVaultJcaProvider();
        certificateName = System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME");
    }

    @Test
    public void testGetKeyStore() throws Exception {
        KeyStore keyStore = PropertyConvertorUtils.getKeyVaultKeyStore();
        assertNotNull(keyStore.getCertificate(certificateName));
        assertTrue(keyStore.containsAlias(certificateName));
        X509Certificate certificate = getTestCertificate();

        keyStore.setCertificateEntry("setcert", certificate);
        assertNotNull(keyStore.getCertificateAlias(certificate), "setcert");
    }

    private X509Certificate getTestCertificate() {
        X509Certificate certificate;

        try {
            byte[] certificateBytes = Base64.getDecoder().decode(TEST_CERTIFICATE);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateBytes));
        } catch (CertificateException e) {
            throw new ProviderException(e);
        }
        return certificate;
    }

    @Test
    public void testCertificatesRefreshInterval() throws Exception {
        System.setProperty("azure.keyvault.jca.certificates-refresh-interval", "1000");
        KeyStore keyStore = PropertyConvertorUtils.getKeyVaultKeyStore();
        assertNotNull(keyStore.getCertificate(certificateName));
        keyStore.deleteEntry(certificateName);
        assertNull(keyStore.getCertificate(certificateName));
        Thread.sleep(2000);
        assertNotNull(keyStore.getCertificate(certificateName));
    }

}
