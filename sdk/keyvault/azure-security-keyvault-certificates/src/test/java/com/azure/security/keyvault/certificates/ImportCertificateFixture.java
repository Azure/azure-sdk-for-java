// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

final class ImportCertificateFixture {
    private static final byte[] PKCS12;
    private static final byte[] PEM;
    private static final byte[] THUMBPRINT;

    static {
        try {
            char[] password = "fakePasswordPlaceholder".toCharArray();
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(
                new ByteArrayInputStream(Base64.getDecoder().decode(FakeCredentialsForTests.FAKE_CERTIFICATE)),
                password);
            String alias = keyStore.aliases().nextElement();
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password);
            X509CertificateHolder original = new X509CertificateHolder(keyStore.getCertificate(alias).getEncoded());

            // Renew the existing fake test key with fixed validity dates to avoid expired fixtures and playback drift.
            X509CertificateHolder renewed
                = new X509v3CertificateBuilder(original.getIssuer(), original.getSerialNumber(),
                    Date.from(Instant.parse("2020-01-01T00:00:00Z")), Date.from(Instant.parse("2050-01-01T00:00:00Z")),
                    original.getSubject(), original.getSubjectPublicKeyInfo())
                        .build(new JcaContentSignerBuilder("SHA256withRSA").build(privateKey));
            X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(renewed);
            keyStore.setKeyEntry(alias, privateKey, password, new Certificate[] { certificate });
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            keyStore.store(output, password);
            PKCS12 = output.toByteArray();
            Base64.Encoder encoder = Base64.getMimeEncoder(64, new byte[] { '\n' });
            PEM = ("-----BEGIN CERTIFICATE-----\n" + encoder.encodeToString(certificate.getEncoded())
                + "\n-----END CERTIFICATE-----\n-----BEGIN PRIVATE KEY-----\n"
                + encoder.encodeToString(privateKey.getEncoded()) + "\n-----END PRIVATE KEY-----\n")
                    .getBytes(StandardCharsets.UTF_8);
            THUMBPRINT = MessageDigest.getInstance("SHA-1").digest(certificate.getEncoded());
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private ImportCertificateFixture() {
    }

    static byte[] getPkcs12() {
        return PKCS12.clone();
    }

    static byte[] getPem() {
        return PEM.clone();
    }

    static byte[] getThumbprint() {
        return THUMBPRINT.clone();
    }
}
