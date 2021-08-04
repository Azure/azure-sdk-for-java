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
    public AttestationSigningKey setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
        return this;
    }

    /**
     *
     * @return Returns the X.509 certificate associated with this Signing Key.
     */
    public X509Certificate getCertificate() {
        return this.certificate;
    }

    /**
     * Sets the private key for the signing key.
     * @param privateKey Private key to sign the certificate.
     * @return AttestationSigningKey.
     */
    public AttestationSigningKey setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    /**
     *
     * @return Returns the private key associated with this signing key.
     */
    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    /**
     * Sets whether the privateKey is allowed to be a weak key (less than 1024 bits).
     * @param allowWeakKey - boolean indicating if weak keys should be allowed (default False).
     * @return Returns the AttestationSigningKey.
     */
    public AttestationSigningKey setAllowWeakKey(boolean allowWeakKey) {
        this.allowWeakKey = allowWeakKey;
        return this;
    }

    /**
     *
     * @return Returns if a weak key is allowed on this signing key.
     */
    public boolean getAllowWeakKey() {
        return this.allowWeakKey;
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

    private boolean allowWeakKey;
    private X509Certificate certificate;
    private PrivateKey privateKey;
}
