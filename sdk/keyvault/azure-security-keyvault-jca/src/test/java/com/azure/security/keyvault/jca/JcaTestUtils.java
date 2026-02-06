// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509TrustManager;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * Utility methods for testing KeyVault JCA.
 */
public final class JcaTestUtils {
    /**
     * Loads {@link TrustManager TrustManagers}.
     *
     * @param keyStore The {@link KeyStore}.
     * @param trustStrategy An optional predicate that is used to skip calling
     * {@link X509TrustManager#checkServerTrusted(X509Certificate[], String)}.
     * @return The {@link TrustManager TrustManagers}.
     * @throws NoSuchAlgorithmException If the algorithm used when calling
     * {@link TrustManagerFactory#getInstance(String)} doesn't exist.
     * @throws KeyStoreException If {@link TrustManagerFactory#init(KeyStore)} fails.
     */
    public static TrustManager[] loadTrustMaterial(KeyStore keyStore,
        BiPredicate<X509Certificate[], String> trustStrategy) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmFactory.init(keyStore);
        TrustManager[] trustManagers = tmFactory.getTrustManagers();

        if (trustManagers != null && trustStrategy != null) {
            for (int i = 0; i < trustManagers.length; i++) {
                TrustManager trustManager = trustManagers[i];
                if (trustManager instanceof X509TrustManager) {
                    trustManagers[i] = new TrustManagerDelegate((X509TrustManager) trustManager, trustStrategy);
                }
            }
        }

        return trustManagers;
    }

    /**
     * Loads {@link KeyManager KeyManagers}.
     *
     * @param keyStore The {@link KeyStore}.
     * @param aliasStrategy An optional function to handle aliasing.
     * @return The {@link KeyManager KeyManagers}.
     * @throws NoSuchAlgorithmException If the algorithm used when calling {@link KeyManagerFactory#getInstance(String)}
     * doesn't exist.
     * @throws KeyStoreException If {@link KeyManagerFactory#init(KeyStore, char[])} fails.
     * @throws UnrecoverableKeyException If the {@link KeyManager} can't be recovered when calling
     * {@link KeyManagerFactory#init(KeyStore, char[])}, such as the {@code password is wrong}.
     */
    public static KeyManager[] loadKeyMaterial(KeyStore keyStore, char[] password,
        BiFunction<String[], Principal[], String> aliasStrategy)
        throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        KeyManagerFactory kmFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmFactory.init(keyStore, password);
        KeyManager[] keyManagers = kmFactory.getKeyManagers();

        if (keyManagers != null && aliasStrategy != null) {
            for (int i = 0; i < keyManagers.length; i++) {
                KeyManager keyManager = keyManagers[i];
                if (keyManager instanceof X509ExtendedKeyManager) {
                    keyManagers[i] = new KeyManagerDelegate((X509ExtendedKeyManager) keyManager, aliasStrategy);
                }
            }
        }

        return keyManagers;
    }

    private static final class TrustManagerDelegate implements X509TrustManager {
        private final X509TrustManager delegate;
        private final BiPredicate<X509Certificate[], String> trustStrategy;

        private TrustManagerDelegate(X509TrustManager delegate, BiPredicate<X509Certificate[], String> trustStrategy) {
            this.delegate = delegate;
            this.trustStrategy = trustStrategy;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            delegate.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if (!trustStrategy.test(chain, authType)) {
                delegate.checkServerTrusted(chain, authType);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return delegate.getAcceptedIssuers();
        }
    }

    private static final class KeyManagerDelegate extends X509ExtendedKeyManager {
        private final X509ExtendedKeyManager delegate;
        private final BiFunction<String[], Principal[], String> aliasStrategy;

        private KeyManagerDelegate(X509ExtendedKeyManager delegate,
            BiFunction<String[], Principal[], String> aliasStrategy) {
            this.delegate = delegate;
            this.aliasStrategy = aliasStrategy;
        }

        @Override
        public String[] getClientAliases(String keyType, Principal[] issuers) {
            return delegate.getClientAliases(keyType, issuers);
        }

        @Override
        public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
            return aliasStrategy.apply(keyType, issuers);
        }

        @Override
        public String[] getServerAliases(String keyType, Principal[] issuers) {
            return delegate.getServerAliases(keyType, issuers);
        }

        @Override
        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
            return aliasStrategy.apply(new String[] { keyType }, issuers);
        }

        @Override
        public X509Certificate[] getCertificateChain(String alias) {
            return delegate.getCertificateChain(alias);
        }

        @Override
        public PrivateKey getPrivateKey(String alias) {
            return delegate.getPrivateKey(alias);
        }

        @Override
        public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
            return aliasStrategy.apply(keyType, issuers);
        }

        @Override
        public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
            return aliasStrategy.apply(new String[] { keyType }, issuers);
        }
    }

    private JcaTestUtils() {
    }
}
