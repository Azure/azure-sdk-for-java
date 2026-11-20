// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.validation.http;

import com.azure.core.credential.ClientCertificateBinding;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * Self-signed TLS client certificates for testing mutual TLS (mTLS) proof-of-possession.
 * <p>
 * Each keystore holds one RSA certificate with the {@code clientAuth} extended key usage, under the alias
 * {@code client}.
 */
public final class MtlsTestCertificates {
    /**
     * Keystore resource for the first test client certificate.
     */
    public static final String CLIENT_A = "/mtls-client-a.jks";

    /**
     * Keystore resource for the second test client certificate.
     */
    public static final String CLIENT_B = "/mtls-client-b.jks";

    private static final char[] PASSWORD = "password".toCharArray();
    private static final String ALIAS = "client";

    private MtlsTestCertificates() {
    }

    /**
     * Creates a binding to the certificate in a test keystore, using a key manager that presents it.
     *
     * @param keyStoreResource The keystore resource, such as {@link #CLIENT_A}.
     * @return The binding.
     */
    public static ClientCertificateBinding loadBinding(String keyStoreResource) {
        return new ClientCertificateBinding(loadCertificate(keyStoreResource), loadKeyManager(keyStoreResource));
    }

    /**
     * Loads the certificate in a test keystore.
     *
     * @param keyStoreResource The keystore resource, such as {@link #CLIENT_A}.
     * @return The certificate.
     */
    public static X509Certificate loadCertificate(String keyStoreResource) {
        try {
            return (X509Certificate) loadKeyStore(keyStoreResource).getCertificate(ALIAS);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Loads a key manager that presents the certificate in a test keystore.
     *
     * @param keyStoreResource The keystore resource, such as {@link #CLIENT_A}.
     * @return The key manager.
     */
    public static X509ExtendedKeyManager loadKeyManager(String keyStoreResource) {
        try {
            KeyManagerFactory keyManagerFactory
                = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(loadKeyStore(keyStoreResource), PASSWORD);
            return (X509ExtendedKeyManager) keyManagerFactory.getKeyManagers()[0];
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static KeyStore loadKeyStore(String keyStoreResource) throws GeneralSecurityException {
        try (InputStream stream = MtlsTestCertificates.class.getResourceAsStream(keyStoreResource)) {
            if (stream == null) {
                throw new IllegalArgumentException("Keystore resource '" + keyStoreResource + "' wasn't found.");
            }

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(stream, PASSWORD);
            return keyStore;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
