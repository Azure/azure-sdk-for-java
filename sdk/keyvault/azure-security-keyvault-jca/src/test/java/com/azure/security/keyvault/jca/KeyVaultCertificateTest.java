// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The JUnit tests for the KeyVaultCertificate class.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
 */
public class KeyVaultCertificateTest {

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

    /**
     * Stores the X.509 certificate.
     */
    private X509Certificate x509Certificate;

    /**
     * Setup before each test.
     *
     */
    @BeforeEach
    public void setUp() {
        try {
            byte[] certificateBytes = Base64.getDecoder().decode(TEST_CERTIFICATE);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            x509Certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateBytes));
        } catch (CertificateException e) {
            throw new ProviderException(e);
        }
    }

    /**
     * Test checkValidity method.
     */
    @Test
    public void testCheckValidity() {
        try {
            KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
            certificate.checkValidity();
        } catch (CertificateExpiredException | CertificateNotYetValidException cnyve) {
            fail();
        }
    }

    /**
     * Test checkValidity method.
     */
    @Test
    public void testCheckValidity2() {
        try {
            KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
            certificate.checkValidity(new Date(100, Calendar.FEBRUARY, 1));
            fail();
        } catch (CertificateExpiredException ex) {
            fail();
        } catch (CertificateNotYetValidException exception) {
            // expecting this as the TEST_CERTIFICATE is not valid against given date.
        }
    }

    /**
     * Test checkValidity method.
     */
    @Test
    public void testCheckValidity3() {
        try {
            KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
            certificate.checkValidity(new Date(200, Calendar.FEBRUARY, 1));
        } catch (CertificateExpiredException | CertificateNotYetValidException exception) {
            fail();
        }
    }

    /**
     * Test getBasicConstraints method.
     */
    @Test
    public void testGetBasicConstraints() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        assertEquals(-1, certificate.getBasicConstraints());
    }

    /**
     * Test getCriticalExtensionOIDs method.
     */
    @Test
    public void testGetCriticalExtensionOIDs() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        Set<String> criticalExtensions = certificate.getCriticalExtensionOIDs();
        assertFalse(criticalExtensions.isEmpty());
        assertTrue(criticalExtensions.contains("2.5.29.15"));
    }

    /**
     * Test getEncoded method.
     */
    @Test
    public void testGetEncoded() {
        try {
            KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
            assertNotNull(certificate.getEncoded());
        } catch (CertificateEncodingException cee) {
            fail();
        }
    }

    /**
     * Test getExtensionValue method.
     */
    @Test
    public void testGetExtensionValue() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        assertNotNull(certificate.getExtensionValue("2.5.29.15"));
    }

    /**
     * Test getIssuerDN method.
     */
    @Test
    public void testGetIssuerDN() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        assertEquals("CN=hundred-years.example.com", certificate.getIssuerDN().getName());
    }

    /**
     * Test getIssuerUniqueID method.
     */
    @Test
    public void testGetIssuerUniqueID() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        assertNull(certificate.getIssuerUniqueID());
    }

    /**
     * Test getKeyUsage method.
     */
    @Test
    public void testGetKeyUsage() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        assertNotNull(certificate.getKeyUsage());
    }

    /**
     * Test getNonCriticalExtensionOIDs method.
     */
    @Test
    public void testGetNonCriticalExtensionOIDs() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        Set<String> nonCriticalExtensions = certificate.getNonCriticalExtensionOIDs();
        assertFalse(nonCriticalExtensions.isEmpty());
    }

    /**
     * Test getNotAfter method.
     */
    @Test
    public void testGetNotAfter() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        Date notAfter = certificate.getNotAfter();
        assertTrue(new Date().before(notAfter));
    }

    /**
     * Test getNotBefore method.
     */
    @Test
    public void testGetNotBefore() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        Date notBefore = certificate.getNotBefore();
        assertTrue(new Date().after(notBefore));
    }

    /**
     * Test getPublicKey method.
     */
    @Test
    public void testGetPublicKey() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        assertNotNull(certificate.getPublicKey());
    }

    /**
     * Test getSerialNumber method.
     */
    @Test
    public void testGetSerialNumber() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        assertNotNull(certificate.getSerialNumber());
    }

    /**
     * Test getSigAlgName method.
     */
    @Test
    public void testGetSigAlgName() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        assertEquals("SHA256withRSA", certificate.getSigAlgName());
    }

    /**
     * Test getSigAlgOID method.
     */
    @Test
    public void testGetSigAlgOID() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        assertEquals("1.2.840.113549.1.1.11", certificate.getSigAlgOID());
    }

    /**
     * Test getSigAlgParams method.
     */
    @Test
    public void testGetSigAlgParams() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        assertNull(certificate.getSigAlgParams());
    }

    /**
     * Test getSignature method.
     */
    @Test
    public void testGetSignature() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        assertNotNull(certificate.getSignature());
    }

    /**
     * Test getSubjectDN method.
     */
    @Test
    public void testGetSubjectDN() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        assertEquals("CN=hundred-years.example.com", certificate.getSubjectDN().getName());
    }

    /**
     * Test getSubjectUniqueID method.
     */
    @Test
    public void testGetSubjectUniqueID() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        assertNull(certificate.getSubjectUniqueID());
    }

    /**
     * Test getTBSCertificate method.
     */
    @Test
    public void testGetTBSCertificate() {
        try {
            KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
            assertNotNull(certificate.getTBSCertificate());
        } catch (CertificateEncodingException cee) {
            fail();
        }
    }

    /**
     * Test getVersion method.
     */
    @Test
    public void testGetVersion() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        assertEquals(3, certificate.getVersion());
    }

    /**
     * Test hasUnsupportedCriticalExtension method.
     */
    @Test
    public void testHasUnsupportedCriticalExtension() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        assertFalse(certificate.hasUnsupportedCriticalExtension());
    }

    /**
     * Test toString method.
     */
    @Test
    public void testToString() {
        KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
        assertNotNull(certificate.toString());
    }

    /**
     * Test verify method.
     */
    @Test
    public void testVerify() {
        try {
            KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
            PublicKey publicKey = certificate.getPublicKey();
            certificate.verify(publicKey);
        } catch (CertificateException | NoSuchAlgorithmException
            | InvalidKeyException | NoSuchProviderException
            | SignatureException e) {
            fail();
        }
    }

    /**
     * Test verify method.
     */
    @Test
    public void testVerify2() {
        try {
            KeyVaultCertificate certificate = new KeyVaultCertificate(x509Certificate);
            PublicKey publicKey = certificate.getPublicKey();
            certificate.verify(publicKey, "SunRsaSign");
        } catch (CertificateException | NoSuchAlgorithmException
            | InvalidKeyException | NoSuchProviderException
            | SignatureException e) {
            fail();
        }
    }
}
