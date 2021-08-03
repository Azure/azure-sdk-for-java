// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;

import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.Objects;

/**
 * Signing Key used to sign requests to the attestation service.
 */
@Fluent
public class AttestationSigningKey {

    /**
     * Creates a new instance of an AttestationSigningKey.
     */
    public AttestationSigningKey() {
        this.certificate = null;
        this.privateKey = null;
        this.allowWeakKey = false;
    }

    /**
     * Sets the certificate in the signing key.
     * @param certificate Certificate to sign.
     * @return AttestationSigningKey
     */
    public AttestationSigningKey certificate(X509Certificate certificate) {
        this.certificate = certificate;
        return this;
    }

    /**
     * Sets the private key for the signing key.
     * @param privateKey Private key to sign the certificate.
     * @return AttestationSigningKey.
     */
    public AttestationSigningKey privateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    /**
     * Sets whether the privateKey is allowed to be a weak key (less than 1024 bits).
     * @param allowWeakKey - boolean indicating if weak keys should be allowed (default False).
     * @return Returns the AttestationSigningKey.
     */
    public AttestationSigningKey allowWeakKey(boolean allowWeakKey) {
        this.allowWeakKey = allowWeakKey;
        return this;
    }

    /**
     * Verifies that the provided privateKey can sign a buffer which is verified by certificate.
     *
     * @throws InvalidKeyException - Thrown if the PrivateKey provided is from an API family different from the certificate.
     * @throws SignatureException - Thrown if the digital signature cannot be verified or created.
     * @throws NoSuchAlgorithmException - Thrown if the signature algorithm is not supported.
     * @throws NoSuchProviderException - Thrown if the specified provider is incorrect.
     * @throws InvalidParameterException - Thrown if the certificate could not validate a buffer signed with the signing key.
     */
    public void verify() throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, NoSuchProviderException {
        Objects.requireNonNull(certificate);
        Objects.requireNonNull(privateKey);

        Signature signer = null;
        Signature verifier = null;
        if (privateKey instanceof RSAPrivateKey) {
            signer = Signature.getInstance("SHA256WITHRSA");
            verifier = Signature.getInstance("SHA256WITHRSA");
        } else if (privateKey instanceof ECPrivateKey) {
            signer = Signature.getInstance("SHA256WITHECDSA");
            verifier = Signature.getInstance("SHA256WITHECDSA");
        } else {
            ClientLogger logger = new ClientLogger(AttestationSigningKey.class);
            throw logger.logExceptionAsError(new InvalidParameterException("AttestationSigningKey privateKey must be an RSA or DSA private key"));
        }

        /* Buffer to be signed - this can basically be anything, it doesn't really matter. */
        final byte[] bufferToSign = { 1, 2, 3, 4, 5};

        // Sign the buffer.
        signer.initSign(privateKey);
        signer.update(bufferToSign);
        byte[] signedBuffer = signer.sign();

        // Verify the signed buffer.
        verifier.initVerify(certificate);
        verifier.update(bufferToSign);
        if (!verifier.verify(signedBuffer)) {
            ClientLogger logger = new ClientLogger(AttestationSigningKey.class);
            throw logger.logExceptionAsError(new IllegalArgumentException("AttestationSigningKey certificate cannot verify buffer signed with AttestationSigningKey key"));
        }
    }

    boolean allowWeakKey;
    X509Certificate certificate;
    PrivateKey privateKey;
}
