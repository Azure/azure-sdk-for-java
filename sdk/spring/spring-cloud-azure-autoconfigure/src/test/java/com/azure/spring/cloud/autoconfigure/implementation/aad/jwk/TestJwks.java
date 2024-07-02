// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.jwk;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public final class TestJwks {

    private static final ClientLogger LOGGER = new ClientLogger(TestJwks.class);

    // @formatter:off
    public static final RSAKey DEFAULT_RSA_JWK =
            jwk(
                    TestKeys.DEFAULT_PUBLIC_KEY,
                    TestKeys.DEFAULT_PRIVATE_KEY,
                    TestKeys.DEFAULT_CERTIFICATE
            ).build();
    // @formatter:on

    private TestJwks() {
    }

    @SuppressWarnings("deprecation")
    public static RSAKey.Builder jwk(RSAPublicKey publicKey, RSAPrivateKey privateKey, X509Certificate cert) {
        // @formatter:off
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(cert.getEncoded());
            byte[] bytes = digest.digest();
            return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .x509CertThumbprint(Base64URL.encode(bytes))
                .keyID("rsa-jwk-kid");
        } catch (CertificateEncodingException | NoSuchAlgorithmException e) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Failed to generate thumbprint for certificate.", e);
        }
        return null;
        // @formatter:on
    }
}
