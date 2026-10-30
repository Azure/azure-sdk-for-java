// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportCertificateFixtureTest {
    private static final char[] PASSWORD = "fakePasswordPlaceholder".toCharArray();

    @Test
    void pkcs12CertificateIsValidWithMatchingPrivateKey() throws Exception {
        KeyStore keyStore = loadPkcs12();
        String alias = keyStore.aliases().nextElement();
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, PASSWORD);

        certificate.checkValidity();
        certificate.checkValidity(Date.from(Instant.parse("2026-10-25T00:00:00Z")));
        certificate.checkValidity(Date.from(Instant.parse("2049-12-31T23:59:59Z")));
        assertEquals(Date.from(Instant.parse("2020-01-01T00:00:00Z")), certificate.getNotBefore());
        assertEquals(Date.from(Instant.parse("2050-01-01T00:00:00Z")), certificate.getNotAfter());
        assertEquals(certificate.getSubjectX500Principal(), certificate.getIssuerX500Principal());
        certificate.verify(certificate.getPublicKey());

        byte[] content = "Key Vault import fixture key consistency".getBytes(StandardCharsets.UTF_8);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(content);
        byte[] signed = signature.sign();
        signature.initVerify(certificate.getPublicKey());
        signature.update(content);
        assertTrue(signature.verify(signed));
        assertArrayEquals(MessageDigest.getInstance("SHA-1").digest(certificate.getEncoded()),
            ImportCertificateFixture.getThumbprint());
    }

    @Test
    void pemContainsTheSameCertificateAndPrivateKey() throws Exception {
        KeyStore keyStore = loadPkcs12();
        String alias = keyStore.aliases().nextElement();
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, PASSWORD);

        try (PEMParser parser
            = new PEMParser(new StringReader(new String(ImportCertificateFixture.getPem(), StandardCharsets.UTF_8)))) {
            X509CertificateHolder pemCertificate = (X509CertificateHolder) parser.readObject();
            PrivateKeyInfo pemPrivateKey = (PrivateKeyInfo) parser.readObject();
            assertArrayEquals(certificate.getEncoded(), pemCertificate.getEncoded());
            assertArrayEquals(privateKey.getEncoded(), pemPrivateKey.getEncoded());
            assertNull(parser.readObject());
        }
    }

    private static KeyStore loadPkcs12() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new ByteArrayInputStream(ImportCertificateFixture.getPkcs12()), PASSWORD);
        return keyStore;
    }
}
