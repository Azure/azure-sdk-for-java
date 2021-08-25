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
public final class KeyVaultTrustManager extends X509ExtendedTrustManager {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyVaultTrustManager.class.getName());

    /**
     * Stores the default trust manager.
     */
    private static final X509TrustManager defaultTrustManager = defaultTrustManager();

    /**
     * Stores the default trust manager with key vault key store.
     */
    private X509TrustManager kvTrustManager;


    /**
     * Stores the default trust manager with other key store.
     */
    private X509TrustManager otherTrustManager;

    /**
     * Stores the key vault keystore.
     */
    private KeyStore keyStore;

    /**
     * Constructor.
     */
    public KeyVaultTrustManager() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param keyStore the keystore.
     */
    public KeyVaultTrustManager(KeyStore keyStore) {
        if (keyStore == null) {
            initKeyVaultKS();
        } else {
            if (keyStore.getType().equals(KeyVaultKeyStore.KEY_STORE_TYPE)) {
                this.keyStore = keyStore;
            } else {
                if (!keyStore.getType().equals(KeyStore.getDefaultType())) {
                    initOtherTrustManager(keyStore);
                }
                initKeyVaultKS();
            }
        }

    }

    private void initKeyVaultKS(){
        try {
            this.keyStore = KeyStore.getInstance(KeyVaultKeyStore.KEY_STORE_TYPE);
            this.keyStore.load(null, null);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            LOGGER.log(WARNING, "Unable to get AzureKeyVault keystore.", ex);
        }
    }

    /**
     * This method aims to use the jdk default key store and default trust manager. Please do not override the javax.net.ssl.trustStoreType
     */
    private static X509TrustManager defaultTrustManager() {
        X509TrustManager trustManager = null;
        try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm(), "SunJSSE");
            factory.init((KeyStore) null); // Do not override the system property javax.net.ssl.trustStoreType. Then this factory will create a key store of type specified by keystore.type in java.security file, and load it with local jdk key store.
            trustManager = (X509TrustManager) factory.getTrustManagers()[0];
        } catch (NoSuchAlgorithmException | NoSuchProviderException | KeyStoreException ex) {
            LOGGER.log(WARNING, "Unable to get the default trust manager factory.", ex);
        }

        if (trustManager == null) {
            try {
                TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm(), "IbmJSSE");
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                factory.init(ks);
                trustManager = (X509TrustManager) factory.getTrustManagers()[0];
            } catch (NoSuchAlgorithmException | NoSuchProviderException | KeyStoreException ex) {
                LOGGER.log(WARNING, "Unable to get the default trust manager factory.", ex);
            }
        }
        return trustManager;

    }

    private void initOtherTrustManager(KeyStore keyStore) {
        try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm(), "SunJSSE");
            factory.init(keyStore);
            otherTrustManager = (X509TrustManager) factory.getTrustManagers()[0];
        } catch (NoSuchAlgorithmException | NoSuchProviderException | KeyStoreException ex) {
            LOGGER.log(WARNING, "Unable to get the trust manager factory.", ex);
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
         * Step 2 - see if the trust manager with key vault passes
         */
        if (!pass) {
            initKVTrustManager();
            try {
                kvTrustManager.checkClientTrusted(chain, authType);
                pass = true;
            } catch (CertificateException ce) {
            }
        }

        /*
         * Step 3 - see if the certificate exists in key vault.
         */
        if (!pass) {
           pass = tryKeyVault(chain, authType);
        }

        /*
         * Step 4 - see if the trust manager with 3rd part key store passes.
         */
        if ((!pass) && (otherTrustManager != null)) {
            try {
                otherTrustManager.checkClientTrusted(chain, authType);
                pass = true;
            } catch (CertificateException ce) {
            }
        }

        if (!pass) {
            throw new CertificateException("Unable to verify in keystore");
        }
    }

    private boolean tryKeyVault(X509Certificate[] chain, String authType){
        String alias = null;
        try {
            alias = keyStore.getCertificateAlias(chain[0]);
        } catch (KeyStoreException kse) {
            LOGGER.log(WARNING, "Unable to get the certificate in AzureKeyVault keystore.", kse);
        }
        if (alias != null) {
            return true;
        }
        return false;
    }

    private void initKVTrustManager() {
        if (kvTrustManager == null) {
            try {
                TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm(), "SunJSSE");
                factory.init(keyStore);
                kvTrustManager = (X509TrustManager) factory.getTrustManagers()[0];
            } catch (NoSuchAlgorithmException | NoSuchProviderException | KeyStoreException ex) {
                LOGGER.log(WARNING, "Unable to get the trust manager factory.", ex);
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
         * Step 2 - see if the trust manager with key vault passes
         */
        if (!pass) {
            initKVTrustManager();
            try {
                kvTrustManager.checkServerTrusted(chain, authType);
                pass = true;
            } catch (CertificateException ce) {
            }
        }

        /*
         * Step 3 - see if the certificate exists in key vault.
         */
        if (!pass) {
           pass = tryKeyVault(chain, authType);
        }

        /*
         * Step 4 - see if the trust manager with 3rd part key store passes.
         */
        if ((!pass) && (otherTrustManager != null)) {
            try {
                otherTrustManager.checkServerTrusted(chain, authType);
                pass = true;
            } catch (CertificateException ce) {
            }
        }

        if (!pass) {
            throw new CertificateException("Unable to verify in keystore");
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
