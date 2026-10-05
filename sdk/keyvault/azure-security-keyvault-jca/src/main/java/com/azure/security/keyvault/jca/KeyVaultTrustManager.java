// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

import static java.util.logging.Level.WARNING;

/**
 * The Azure Key Vault variant of the X509TrustManager.
 *
 * @see X509ExtendedTrustManager
 */
public final class KeyVaultTrustManager extends X509ExtendedTrustManager {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyVaultTrustManager.class.getName());

    /**
     * Stores the default trust manager.
     */
    private final X509ExtendedTrustManager defaultTrustManager;

    /**
     * Stores the keystore.
     */
    private KeyStore keyStore;

    /**
     * Constructor.
     *
     * @throws IllegalStateException if an extended PKIX trust manager cannot be initialized.
     */
    public KeyVaultTrustManager() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param keyStore the keystore.
     * @throws IllegalStateException if an extended PKIX trust manager cannot be initialized.
     */
    public KeyVaultTrustManager(KeyStore keyStore) {
        this.keyStore = keyStore;
        if (this.keyStore == null) {
            try {
                this.keyStore = KeyStore.getInstance(KeyVaultKeyStore.KEY_STORE_TYPE);
                this.keyStore.load(null, null);
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
                LOGGER.log(WARNING, "Unable to get AzureKeyVault keystore.", ex);
            }
        }
        try {
            defaultTrustManager = createTrustManager(keyStore);
        } catch (CertificateException ex) {
            throw new IllegalStateException("Unable to initialize the trust manager.", ex);
        }
    }

    private static X509ExtendedTrustManager createTrustManager(KeyStore trustStore) throws CertificateException {
        CertificateException failure = new CertificateException("Unable to initialize an extended PKIX trust manager.");
        for (String provider : new String[] { "SunJSSE", "IbmJSSE" }) {
            try {
                TrustManagerFactory factory = TrustManagerFactory.getInstance("PKIX", provider);
                factory.init(trustStore);
                for (TrustManager manager : factory.getTrustManagers()) {
                    if (manager instanceof X509ExtendedTrustManager) {
                        return (X509ExtendedTrustManager) manager;
                    }
                }
                failure
                    .addSuppressed(new CertificateException(provider + " does not supply an extended trust manager."));
            } catch (NoSuchAlgorithmException | NoSuchProviderException | KeyStoreException ex) {
                failure.addSuppressed(ex);
            }
        }
        throw failure;
    }

    private void checkTrusted(X509Certificate[] chain, TrustCheck check) throws CertificateException {
        CertificateException originalFailure;
        try {
            check.check(defaultTrustManager);
            return;
        } catch (CertificateException ex) {
            originalFailure = ex;
        }
        if (keyStore == null) {
            throw originalFailure;
        }
        X509ExtendedTrustManager fallback;
        try {
            if (keyStore.getCertificateAlias(chain[0]) == null) {
                throw originalFailure;
            }
            // A matching Key Vault leaf is a trust anchor, not an exemption from connection-specific checks.
            KeyStore trustedCertificate = KeyStore.getInstance("JKS");
            trustedCertificate.load(null, null);
            trustedCertificate.setCertificateEntry("trusted", chain[0]);
            fallback = createTrustManager(trustedCertificate);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException ex) {
            CertificateException failure = new CertificateException("Unable to verify in keystore.", ex);
            failure.addSuppressed(originalFailure);
            throw failure;
        }
        try {
            check.check(fallback);
        } catch (CertificateException failure) {
            failure.addSuppressed(originalFailure);
            throw failure;
        }
    }

    @FunctionalInterface
    private interface TrustCheck {
        void check(X509ExtendedTrustManager manager) throws CertificateException;
    }

    /**
     * Check if the client is trusted.
     *
     * @param chain the chain
     * @param authType the authType
     * @throws CertificateException if any of the certificates in the
     *          keystore could not be loaded.
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        boolean pass = true;

        /*
         * Step 1 - see if the default trust manager passes.
         */
        try {
            defaultTrustManager.checkClientTrusted(chain, authType);
        } catch (CertificateException ce) {
            pass = false;
        }

        /*
         * Step 2 - see if the certificate exists in the keystore.
         */
        if (!pass) {
            String alias = null;
            try {
                alias = keyStore.getCertificateAlias(chain[0]);
            } catch (KeyStoreException kse) {
                LOGGER.log(WARNING, "Unable to get the certificate in AzureKeyVault keystore.", kse);
            }
            if (alias == null) {
                throw new CertificateException("Unable to verify in keystore");
            }
        }
    }

    /**
     * Check if the server is trusted.
     *
     * @param chain the chain
     * @param authType the authType
     * @throws CertificateException if any of the certificates in the
     *          keystore could not be loaded.
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        boolean pass = true;

        /*
         * Step 1 - see if the default trust manager passes.
         */
        try {
            defaultTrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException ce) {
            pass = false;
        }

        /*
         * Step 2 - see if the certificate exists in the keystore.
         */
        if (!pass) {
            String alias = null;
            try {
                alias = keyStore.getCertificateAlias(chain[0]);
            } catch (KeyStoreException kse) {
                LOGGER.log(WARNING, "Unable to get the certificate in AzureKeyVault keystore.", kse);
            }
            if (alias == null) {
                throw new CertificateException("Unable to verify in keystore");
            }
        }
    }

    /**
     * Get accepted issuers.
     *
     * @return X509Certificate the X509Certificate
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    /**
     * Check if the client is trusted.
     *
     * @param chain the chain
     * @param authType the authType
     * @param socket the socket
     * @throws CertificateException if any of the certificates in the
     *          keystore could not be loaded.
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
        throws CertificateException {
        checkTrusted(chain, manager -> manager.checkClientTrusted(chain, authType, socket));
    }

    /**
     * Check if the server is trusted.
     *
     * @param chain the chain
     * @param authType the authType
     * @param socket the socket
     * @throws CertificateException if any of the certificates in the
     *          keystore could not be loaded.
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
        throws CertificateException {
        checkTrusted(chain, manager -> manager.checkServerTrusted(chain, authType, socket));
    }

    /**
     * Check if the client is trusted.
     *
     * @param chain the chain
     * @param authType the authType
     * @param engine the engine
     * @throws CertificateException if any of the certificates in the
     *          keystore could not be loaded.
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
        throws CertificateException {
        checkTrusted(chain, manager -> manager.checkClientTrusted(chain, authType, engine));
    }

    /**
     * Check if the server is trusted.
     *
     * @param chain the chain
     * @param authType the authType
     * @param engine the engine
     * @throws CertificateException if any of the certificates in the
     *          keystore could not be loaded.
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
        throws CertificateException {
        checkTrusted(chain, manager -> manager.checkServerTrusted(chain, authType, engine));
    }
}
