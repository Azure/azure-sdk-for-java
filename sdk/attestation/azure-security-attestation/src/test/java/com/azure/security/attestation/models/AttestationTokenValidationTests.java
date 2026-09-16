// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation.models;

import com.azure.security.attestation.implementation.models.AttestationSignerImpl;
import com.azure.security.attestation.implementation.models.AttestationTokenImpl;
import com.nimbusds.jose.util.Base64;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.jupiter.api.Test;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Offline unit tests for {@link AttestationTokenImpl#validate} signature verification.
 * <p>
 * These tests are deliberately independent of the test-proxy machinery because they exercise pure, offline
 * cryptographic validation and require no recorded HTTP interactions.
 */
public class AttestationTokenValidationTests {

    @Test
    void verifyForgedTokenSignatureIsRejected() {
        // Regression test for CWE-347: a token signed with an untrusted key must be rejected rather than
        // silently accepted when none of the trusted signers can verify its signature.
        KeyPair serviceKey = assertDoesNotThrow(() -> createKeyPair("RSA"));
        X509Certificate serviceCert
            = assertDoesNotThrow(() -> createSelfSignedCertificate("Trusted Service", serviceKey));

        KeyPair attackerKey = assertDoesNotThrow(() -> createKeyPair("RSA"));
        X509Certificate attackerCert = assertDoesNotThrow(() -> createSelfSignedCertificate("Attacker", attackerKey));

        AttestationSigningKey attackerSigningKey
            = new AttestationSigningKey(attackerCert, attackerKey.getPrivate()).setWeakKeyAllowed(true);

        // A token forged by the attacker asserting sensitive claims.
        AttestationToken forgedToken
            = AttestationTokenImpl.createSecuredToken("{\"x-ms-sgx-mrenclave\": \"forged\" }", attackerSigningKey);

        // The only trusted signer is the service certificate; the token was signed by the attacker key.
        AttestationSigner trustedSigner = AttestationSignerImpl.fromCertificateChain(
            Collections.singletonList(Base64.encode(assertDoesNotThrow(serviceCert::getEncoded))));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> ((AttestationTokenImpl) forgedToken)
            .validate(Collections.singletonList(trustedSigner), new AttestationTokenValidationOptions()));
        assertTrue(ex.getMessage().contains("Could not find a certificate which was used to sign the token"),
            () -> "Expected the forged token to be rejected. Actual message: " + ex.getMessage());
    }

    @Test
    void verifyTokenSignedByTrustedSignerIsAccepted() {
        // Companion to verifyForgedTokenSignatureIsRejected: a token signed by a trusted signer must validate.
        KeyPair serviceKey = assertDoesNotThrow(() -> createKeyPair("RSA"));
        X509Certificate serviceCert
            = assertDoesNotThrow(() -> createSelfSignedCertificate("Trusted Service", serviceKey));

        AttestationSigningKey serviceSigningKey
            = new AttestationSigningKey(serviceCert, serviceKey.getPrivate()).setWeakKeyAllowed(true);

        AttestationToken token
            = AttestationTokenImpl.createSecuredToken("{\"x-ms-sgx-mrenclave\": \"genuine\" }", serviceSigningKey);

        AttestationSigner trustedSigner = AttestationSignerImpl.fromCertificateChain(
            Collections.singletonList(Base64.encode(assertDoesNotThrow(serviceCert::getEncoded))));

        assertDoesNotThrow(() -> ((AttestationTokenImpl) token).validate(Collections.singletonList(trustedSigner),
            new AttestationTokenValidationOptions()));
    }

    @Test
    void verifyEmptySignerListIsRejectedForSignedToken() {
        // Regression test for CWE-347: a signed token must not be allowed to vouch for its own signature via
        // its embedded certificate chain when the caller provides no trusted signers.
        KeyPair rsaKey = assertDoesNotThrow(() -> createKeyPair("RSA"));
        X509Certificate cert = assertDoesNotThrow(() -> createSelfSignedCertificate("Self Signed", rsaKey));
        AttestationSigningKey signingKey = new AttestationSigningKey(cert, rsaKey.getPrivate()).setWeakKeyAllowed(true);

        AttestationToken token
            = AttestationTokenImpl.createSecuredToken("{\"x-ms-sgx-mrenclave\": \"self\" }", signingKey);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> ((AttestationTokenImpl) token).validate(null, new AttestationTokenValidationOptions()));
        assertTrue(ex.getMessage().contains("Unable to find any certificates which can be used to validate the token"),
            () -> "Expected the self-vouching token to be rejected. Actual message: " + ex.getMessage());
    }

    private static KeyPair createKeyPair(String algorithm) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen;
        if ("EC".equals(algorithm)) {
            keyGen = KeyPairGenerator.getInstance(algorithm, Security.getProvider("SunEC"));
        } else {
            keyGen = KeyPairGenerator.getInstance(algorithm);
        }
        if ("RSA".equals(algorithm)) {
            keyGen.initialize(2048);
        }
        return keyGen.generateKeyPair();
    }

    @SuppressWarnings("deprecation")
    private static X509Certificate createSelfSignedCertificate(String subjectName, KeyPair certificateKey)
        throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        final X509V3CertificateGenerator generator = new X509V3CertificateGenerator();
        generator.setIssuerDN(new X500Principal("CN=" + subjectName));
        generator.setSubjectDN(new X500Principal("CN=" + subjectName));
        generator.setPublicKey(certificateKey.getPublic());
        if (certificateKey.getPublic().getAlgorithm().equals("EC")) {
            generator.setSignatureAlgorithm("SHA256WITHECDSA");
        } else {
            generator.setSignatureAlgorithm("SHA256WITHRSA");
        }
        generator.setSerialNumber(BigInteger.valueOf(Math.abs(new Random().nextInt())));
        generator.setNotBefore(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        generator.setNotAfter(
            Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().plus(1, ChronoUnit.DAYS)));
        generator.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
        return generator.generate(certificateKey.getPrivate());
    }
}
