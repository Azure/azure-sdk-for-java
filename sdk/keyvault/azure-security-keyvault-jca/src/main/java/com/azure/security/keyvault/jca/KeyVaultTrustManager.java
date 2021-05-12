// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
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
 */
public class KeyVaultTrustManager extends X509ExtendedTrustManager {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyVaultTrustManager.class.getName());

    /**
     * Stores the default trust manager.
     */
    private X509TrustManager defaultTrustManager;

    /**
     * Stores the keystore.
     */
    private KeyStore keyStore;

    /**
     * Constructor.
     *
     * @param keyStore the keystore.
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
            TrustManagerFactory factory = TrustManagerFactory.getInstance("PKIX", "SunJSSE");
            factory.init(keyStore);
            defaultTrustManager = (X509TrustManager) factory.getTrustManagers()[0];
        } catch (NoSuchAlgorithmException | NoSuchProviderException | KeyStoreException ex) {
            LOGGER.log(WARNING, "Unable to get the trust manager factory.", ex);
        }
        if (defaultTrustManager == null) {
            try {
                TrustManagerFactory factory = TrustManagerFactory.getInstance("PKIX", "IbmJSSE");
                factory.init(keyStore);
                defaultTrustManager = (X509TrustManager) factory.getTrustManagers()[0];
            } catch (NoSuchAlgorithmException | NoSuchProviderException | KeyStoreException ex) {
                LOGGER.log(WARNING, "Unable to get the trust manager factory.", ex);
            }
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {

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

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {

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

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        checkServerTrusted(chain, authType);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        checkServerTrusted(chain, authType);
    }
}
