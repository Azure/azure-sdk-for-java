// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

/**
 * Utility methods for samples.
 */
public final class SampleUtils {
    /**
     * Loads the {@link TrustManager TrustManagers} for the {@link KeyStore}.
     * <p>
     * This wraps {@link X509TrustManager X509TrustManagers} with {@link TrustSelfSignedServerDelegate} to support
     * self-signed certificates.
     *
     * @param keyStore The {@link KeyStore} where {@link TrustManager TrustManagers} will be loaded.
     * @return The {@link TrustManager TrustManagers} that were loaded.
     * @throws NoSuchAlgorithmException If the algorithm used when calling
     * {@link TrustManagerFactory#getInstance(String)} isn't available.
     * @throws KeyStoreException If calling {@link TrustManagerFactory#init(KeyStore)} fails.
     */
    public static TrustManager[] loadTrustMaterial(KeyStore keyStore) throws NoSuchAlgorithmException,
        KeyStoreException {
        TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmFactory.init(keyStore);
        TrustManager[] trustManagers = tmFactory.getTrustManagers();
        if (trustManagers != null) {
            for (int i = 0; i < trustManagers.length; i++) {
                TrustManager trustManager = trustManagers[i];
                if (trustManager instanceof X509TrustManager) {
                    // Wrap X509TrustManagers with an implementation that trusts self-signed certificates.
                    // This doesn't need to be done and is just an example.
                    trustManagers[i] = new TrustSelfSignedServerDelegate((X509TrustManager) trustManager);
                }
            }
        }

        return trustManagers;
    }

    /**
     * Loads the {@link KeyManager KeyManagers} for the {@link KeyStore}.
     *
     * @param keyStore The {@link KeyStore} where {@link KeyManager KeyManagers} will be loaded.
     * @param password The password for recovering {@link KeyManager KeyManagers} in the {@link KeyStore}.
     * @return The {@link KeyManager KeyManagers} that were loaded.
     * @throws NoSuchAlgorithmException If the algorithm used when calling {@link KeyManagerFactory#getInstance(String)}
     * isn't available.
     * @throws KeyStoreException If calling {@link KeyManagerFactory#init(KeyStore, char[])} fails.
     * @throws UnrecoverableKeyException If the {@link KeyManager} can't be recovered when calling
     * {@link KeyManagerFactory#init(KeyStore, char[])}, such as the {@code password is wrong}.
     */
    public static KeyManager[] loadKeyMaterial(KeyStore keyStore, char[] password)
        throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        KeyManagerFactory kmFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmFactory.init(keyStore, password);
        return kmFactory.getKeyManagers();
    }

    /**
     * Reads the {@link HttpURLConnection} response body to a string.
     *
     * @param connection The {@link HttpURLConnection} to read the response body for.
     * @return The response body as a string.
     * @throws IOException If an I/O error occurs while reading the response body.
     */
    @SuppressWarnings("StringOperationCanBeSimplified")
    public static String readResponse(HttpURLConnection connection) throws IOException {
        InputStream response = (connection.getInputStream() != null)
            ? connection.getInputStream()
            : connection.getErrorStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = response.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }

        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }

    private SampleUtils() {
    }
}
