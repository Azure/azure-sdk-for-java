// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.implementation.models;
import com.azure.core.annotation.Fluent;
import com.azure.core.util.Base64Util;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.attestation.models.AttestationSigner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.stream.Collectors;

/**
 * Represents an attestation signing certificate returned by the attestation service.
 */
@Fluent
public class AttestationSignerImpl implements AttestationSigner {

     /**
     * Sets the signing certificate.
     * @param certificates Array of X509Certificate objects.
     * @return AttestationSigner
     */
    AttestationSignerImpl setCertificates(final X509Certificate[] certificates) {
        this.certificates = cloneX509CertificateChain(certificates);
        return this;
    }

    /**
     * Clone an X.509 certificate chain. Used to ensure that the `certificates` property remains immutable.
     *
     * @param certificates X.509 certificate chain to clone.
     * @return Deep cloned X.509 certificate chain.
     */
    private List<X509Certificate> cloneX509CertificateChain(X509Certificate[] certificates) {
        ClientLogger logger = new ClientLogger(AttestationSignerImpl.class);
        return Arrays.stream(certificates).map(certificate -> {
            X509Certificate newCert;
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                newCert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificate.getEncoded()));
            } catch (CertificateException e) {
                throw logger.logExceptionAsError(new RuntimeException(e));
            }
            return newCert;
        }).collect(Collectors.toList());
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
    AttestationSignerImpl setKeyId(String keyId) {
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
    @Override public final List<X509Certificate> getCertificates() {
        return cloneX509CertificateChain(this.certificates.toArray(new X509Certificate[0]));
    }

    /**
     * Gets the KeyId.
     *
     * The KeyId is matched with the "kid" property in a JsonWebSignature object. It corresponds
     * to the kid property defined in <a href="https://datatracker.ietf.org/doc/html/rfc7517#section-4.5">JsonWebKey RFC section 4.5</a>
     *
     * @return KeyId.
     */
    @Override public String getKeyId() {
        return keyId;
    }

    /**
     * Validate that the attestation signer is valid.
     */
    @Override public void validate() {
        Objects.requireNonNull(certificates);
        for (X509Certificate certificate : certificates) {
            Objects.requireNonNull(certificate);
        }
    }

    /**
     * Create this signer from a base64 encoded certificate chain.
     * @param certificateChain Certificate chain holding the certificates to return.
     * @return An attestation signer associated with the specified certificate chain.
     */
    public static AttestationSigner fromCertificateChain(List<Base64> certificateChain) {
        X509Certificate[] certChain = certificateChain
            .stream()
            .map(AttestationSignerImpl::certificateFromBase64)
            .toArray(X509Certificate[]::new);
        return new AttestationSignerImpl()
            .setCertificates(certChain);
    }



    /**
     * Create this signer from a Json Web Key.
     * @param jwk JSON Web Key for the signature.
     * @return {@link AttestationSigner} generated from the JWK.
     * @throws Error - when the attestation signer could not be created from the JWK.
     */
    public static AttestationSigner fromJWK(JWK jwk) throws Error {
        ClientLogger logger = new ClientLogger(AttestationSignerImpl.class);
        String serializedKey = jwk.toJSONString();

        JsonWebKey jsonWebKey;
        try {
            ObjectMapper om = new ObjectMapper();
            jsonWebKey = om.readValue(serializedKey, JsonWebKey.class);
        } catch (JsonProcessingException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
        return AttestationSignerImpl.fromJsonWebKey(jsonWebKey);

    }

    public static AttestationSigner fromJsonWebKey(JsonWebKey jsonWebKey) {
        List<String> certificateChain = jsonWebKey.getX5C();
        if (certificateChain != null) {
            X509Certificate[] certificateArray =  certificateChain
                .stream()
                .map(AttestationSignerImpl::certificateFromBase64String)
                .toArray(X509Certificate[]::new);
            return new AttestationSignerImpl()
                .setCertificates(certificateArray)
                .setKeyId(jsonWebKey.getKid());
        }
        throw new Error("Could not resolve AttestationSigner from JWK.");
    }

    /**
     * Private method to create an AttestationSigner from a JWKS.
     * @param jwks JWKS to create.
     * @return Array of {@link AttestationSigner}s created from the JWK.
     */
    public static List<AttestationSigner> attestationSignersFromJwks(JsonWebKeySet jwks) {
        return jwks
            .getKeys()
            .stream()
            .map(AttestationSignerImpl::fromJsonWebKey)
            .collect(Collectors.toList());
    }


    /**
     * Create an X.509 certificate from a Base64 encoded certificate.
     * @param base64certificate Base64 encoded certificate.
     * @return X.509 certificate.
     */
    static X509Certificate certificateFromBase64(Base64 base64certificate) {
        return certificateFromBase64String(base64certificate.toString());
    }

    static X509Certificate certificateFromBase64String(String base64certificate) {
        ClientLogger logger = new ClientLogger(AttestationSignerImpl.class);

        byte[] decodedCertificate = Base64Util.decodeString(base64certificate);

        CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
        Certificate cert;
        try {
            cert = cf.generateCertificate(new ByteArrayInputStream(decodedCertificate));
        } catch (CertificateException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }

        return (X509Certificate) cert;

    }

    private List<X509Certificate> certificates;
    private String keyId;
}
