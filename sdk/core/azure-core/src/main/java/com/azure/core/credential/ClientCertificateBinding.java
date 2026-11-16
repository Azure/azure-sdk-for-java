// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.util.logging.ClientLogger;

import javax.net.ssl.X509ExtendedKeyManager;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Objects;

/**
 * <p>
 * The TLS client certificate that an {@link AccessToken} is bound to, for mutual TLS (mTLS) proof-of-possession.
 * </p>
 *
 * <p>
 * A token bound to a certificate carries the SHA-256 thumbprint of that certificate in its {@code cnf} claim
 * ({@code x5t#S256}). The service only accepts the token over a TLS connection that presented the same certificate,
 * so the token can't be used without the certificate's private key. Unlike a proof-of-possession token signed over a
 * request, it isn't tied to a request URL, method or nonce.
 * </p>
 *
 * <p>
 * A credential that returns a token bound to a certificate attaches this binding to the {@link AccessToken}. HTTP
 * clients that support certificate binding, as reported by
 * {@link com.azure.core.http.HttpClient#supportsClientCertificateBinding()}, build their TLS context from
 * {@link #getKeyManager()}, so that the connection carrying the token presents the certificate. The private key may
 * not be exportable, for example when hardware protects it, so the key manager is the only way to use it.
 * </p>
 *
 * <p>
 * This class is immutable.
 * </p>
 *
 * @see AccessToken#getClientCertificateBinding()
 * @see ProofOfPossessionOptions
 */
public final class ClientCertificateBinding {
    private static final ClientLogger LOGGER = new ClientLogger(ClientCertificateBinding.class);

    private final X509Certificate certificate;
    private final X509ExtendedKeyManager keyManager;
    private final String keyId;

    /**
     * Creates a binding to a TLS client certificate.
     *
     * @param certificate The certificate the access token is bound to.
     * @param keyManager A key manager that presents {@code certificate}, and only that certificate, during TLS client
     * authentication.
     * @throws NullPointerException If {@code certificate} or {@code keyManager} is null.
     * @throws IllegalArgumentException If {@code certificate} can't be encoded.
     */
    public ClientCertificateBinding(X509Certificate certificate, X509ExtendedKeyManager keyManager) {
        this.certificate = Objects.requireNonNull(certificate, "'certificate' cannot be null.");
        this.keyManager = Objects.requireNonNull(keyManager, "'keyManager' cannot be null.");
        this.keyId = computeKeyId(certificate);
    }

    /**
     * Gets the certificate the access token is bound to.
     *
     * @return The certificate the access token is bound to.
     */
    public X509Certificate getCertificate() {
        return certificate;
    }

    /**
     * Gets the key ID of the certificate: the base64url encoding, without padding, of the SHA-256 digest of the
     * certificate's DER encoding.
     * <p>
     * It's the same value as the {@code x5t#S256} confirmation claim of the access token, and identifies the
     * certificate, for example to keep connections that present different certificates apart.
     *
     * @return The key ID of the certificate.
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Gets the key manager that presents the certificate during TLS client authentication.
     * <p>
     * HTTP clients build the TLS context for requests that carry the access token from this key manager. Its private
     * key may only be usable through the Java Cryptography Architecture, so use it with a TLS implementation that
     * signs through it, such as the JDK's.
     *
     * @return The key manager that presents the certificate.
     */
    public X509ExtendedKeyManager getKeyManager() {
        return keyManager;
    }

    private static String computeKeyId(X509Certificate certificate) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(certificate.getEncoded());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (CertificateEncodingException e) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'certificate' can't be encoded.", e));
        } catch (NoSuchAlgorithmException e) {
            // Every Java platform is required to support SHA-256.
            throw LOGGER.logExceptionAsError(new IllegalStateException(e));
        }
    }
}
