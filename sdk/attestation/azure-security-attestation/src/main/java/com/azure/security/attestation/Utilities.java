// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.attestation.implementation.models.JsonWebKey;
import com.azure.security.attestation.implementation.models.JsonWebKeySet;
import com.azure.security.attestation.models.AttestationResponse;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.AttestationToken;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * Utility class with helper functions.
 */
class Utilities {

    /**
     * Generates a new public response type from an internal model type.
     * @param response Response from the generated API
     * @param value Value to be included in the new response
     * @param <T> Type of `value`.
     * @param <R> Ignored.
     * @return Returns a newly created Response type.
     */
    static <T, R> ResponseBase<Void, T> generateResponseFromModelType(Response<R> response, T value) {
        return new ResponseBase<>(response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            value,
            null);
    }

    /**
     * Generates a new public response type from an internal model type.
     * @param response Response from the generated API
     * @param value Value to be included in the new response
     * @param <T> Type of `value`.
     * @param <R> Ignored.
     * @return Returns a newly created Response type.
     */
    static <T, R> AttestationResponse<T> generateAttestationResponseFromModelType(
        Response<R> response, AttestationToken token, T value) {
        return new AttestationResponse<>(response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            value,
            token);
    }


    /**
     * Convert a base64 encoded string into a byte stream.
     * @param base64 - Base64 encoded string to be decoded
     * @return stream of bytes encoded in the base64 encoded string.
     */
    static InputStream base64ToStream(String base64) {
        byte[] decoded = Base64.getDecoder().decode(base64);
        return new ByteArrayInputStream(decoded);
    }

    /**
     * Private method to create an AttestationSigner from a JWKS.
     * @param jwks JWKS to create.
     * @return Array of {@link AttestationSigner}s created from the JWK.
     */
    static AttestationSigner[] attestationSignersFromJwks(JsonWebKeySet jwks) {
        return jwks
            .getKeys()
            .stream()
            .map(Utilities::attestationSignerFromJwk)
            .toArray(AttestationSigner[]::new);
    }


    /**
     * Private method to create an AttestationSigner from a JWK.
     * @param jwk JWK to create.
     * @return {@link AttestationSigner} created from the JWK.
     */
    static AttestationSigner attestationSignerFromJwk(JsonWebKey jwk) {
        X509Certificate[] certificates = jwk.getX5C()
            .stream()
            .map(Utilities::certificateFromBase64)
            .toArray(X509Certificate[]::new);

        return new AttestationSigner()
            .certificates(certificates)
            .keyId(jwk.getKid());
    }

    /**
     * Private method to create an x509 certificate from a Base64 encoded string.
     * @param base64certificate - Base64 encoded certificate
     * @return Certificate read from that string.
     */
    static X509Certificate certificateFromBase64(String base64certificate) {
        ClientLogger logger = new ClientLogger(Utilities.class);

        CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
        Certificate cert;
        try {
            cert = cf.generateCertificate(Utilities.base64ToStream(base64certificate));
        } catch (CertificateException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }

        return (X509Certificate) cert;
    }
}
