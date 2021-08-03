// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.models;
import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.util.Base64;

import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents an attestation signing certificate returned by the attestation service.
 */
@Fluent
public class AttestationSigner {

     /**
     * Sets the signing certificate.
     * @param certificates Array of X509Certificate objects.
     * @return AttestationSigner
     */
    public AttestationSigner certificates(final java.security.cert.X509Certificate[] certificates) {
        this.certificates = cloneX509CertificateChain(certificates);
        return this;
    }

    /**
     * Clone an X.509 certificate chain. Used to ensure that the `certificates` property remains immutable.
     *
     * @param certificates X.509 certificate chain to clone.
     * @return Deep cloned X.509 certificate chain.
     */
    private X509Certificate[] cloneX509CertificateChain(X509Certificate[] certificates) {
        ClientLogger logger = new ClientLogger(AttestationSigner.class);
        return Arrays.stream(certificates).map(certificate -> {
            X509Certificate newCert;
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                newCert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificate.getEncoded()));
            } catch (CertificateException e) {
                throw logger.logExceptionAsError(new RuntimeException(e));
            }
            return newCert;
        }).toArray(X509Certificate[]::new);
    }

    /**
     * Sets the KeyId.
     *
     * The KeyId is matched with the "kid" property in a JsonWebSignature object. It corresponds
     * to the kid property defined in <a href="https://datatracker.ietf.org/doc/html/rfc7517#section-4.5">JsonWebKey RFC section 4.5</a>
     *
     * @param keyId Key ID associated with this signer
     * @return AttestationSigner
     */
    public AttestationSigner keyId(String keyId) {
        this.keyId = keyId;
        return this;
    }

    /**
     * Gets the Certificates associated with this signer.
     *
     * The Certificates is an X.509 certificate chain associated with a particular attestation signer.
     *
     * It corresponds to the `x5c` property on a JSON Web Key. See <a href="https://datatracker.ietf.org/doc/html/rfc7517#section-4.7">JsonWebKey RFC Section 4.7</a>
     * for more details.
     *
     * @return Certificate chain used to sign an attestation token.
     */
    public final X509Certificate[] getCertificates() {
        return cloneX509CertificateChain(this.certificates);
    }

    /**
     * Gets the KeyId.
     *
     * The KeyId is matched with the "kid" property in a JsonWebSignature object. It corresponds
     * to the kid property defined in <a href="https://datatracker.ietf.org/doc/html/rfc7517#section-4.5">JsonWebKey RFC section 4.5</a>
     *
     * @return KeyId.
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Validate that the attestation signer is valid.
     */
    public void validate() {
        Objects.requireNonNull(certificates);
        for (X509Certificate certificate : certificates) {
            Objects.requireNonNull(certificate);
        }
    }

    static AttestationSigner fromCertificateChain(List<Base64> certificateChain) {
        X509Certificate[] certChain = certificateChain
            .stream()
            .map(AttestationSigner::certificateFromBase64)
            .toArray(X509Certificate[]::new);
        return new AttestationSigner()
            .certificates(certChain);
    }

    static AttestationSigner fromJWK(JWK jwk) {
        List<X509Certificate> certificateChain = jwk.getParsedX509CertChain();
        if (certificateChain != null) {
            X509Certificate[] certificateArray =  certificateChain.toArray(new X509Certificate[0]);
            return new AttestationSigner()
                .certificates(certificateArray)
                .keyId(jwk.getKeyID());
        }
        throw new Error("Could not resolve AttestationSigner from JWK.");
    }

    static X509Certificate certificateFromBase64(Base64 base64certificate) {
        ClientLogger logger = new ClientLogger(AttestationSigner.class);

        CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
        Certificate cert;
        try {
            cert = cf.generateCertificate(new ByteArrayInputStream(base64certificate.decode()));
        } catch (CertificateException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }

        return (X509Certificate) cert;
    }

    private java.security.cert.X509Certificate[] certificates;
    private String keyId;
}
