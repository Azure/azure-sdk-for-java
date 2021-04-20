// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * Class to provide functions for keyVault certificates
 */
public class KeyVaultCertificateFunctions {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyVaultCertificateFunctions.class.getName());

    private static Timer timer;

    /**
     * auto refresh certificate by user set Interval time
     *
     * @param refreshInterval refresh Interval time
     */
    public static void startRefresh(long refreshInterval) {
        if (refreshInterval > 0) {
            synchronized (KeyVaultKeyStore.class) {
                if (timer != null) {
                    try {
                        timer.cancel();
                        timer.purge();
                    } catch (RuntimeException runtimeException) {
                        LOGGER.log(WARNING, "Error of terminating Timer", runtimeException);
                    }
                }
                timer = new Timer(true);
                final TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        KeyVaultKeyStore.refreshCertificate();
                        KeyVaultTrustManager.refreshTrustManagerByKeyStore();
                    }
                };
                timer.scheduleAtFixedRate(task, refreshInterval, refreshInterval);
            }
        }
    }

    /**
     * provide keyStore for user to define own SSLContext
     * @return keyStore can access certificates from portal
     * @throws KeyStoreException if KeyStore not support specified type
     * @throws CertificateException if any of the certificates in the
     *          keystore could not be loaded
     * @throws NoSuchAlgorithmException if the algorithm used to check
     *          the integrity of the keystore cannot be found
     * @throws IOException if the IOException from KeyStore.load()
     */
    public static KeyStore getKeyStore() throws KeyStoreException,
        CertificateException, NoSuchAlgorithmException, IOException {

        KeyStore trustStore = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
        trustStore.load(parameter);
        return trustStore;
    }

}
