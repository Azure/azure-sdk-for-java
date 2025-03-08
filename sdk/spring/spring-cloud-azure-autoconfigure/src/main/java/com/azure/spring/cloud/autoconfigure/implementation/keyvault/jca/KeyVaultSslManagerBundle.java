// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca;

import org.springframework.boot.ssl.SslBundleKey;
import org.springframework.boot.ssl.SslManagerBundle;
import org.springframework.boot.ssl.SslStoreBundle;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

class KeyVaultSslManagerBundle implements SslManagerBundle {

    private final SslStoreBundle sslStoreBundle;

    private final SslBundleKey sslBundleKey;

    private final boolean forClientAuth;

    private final SslManagerBundle delegate;

    KeyVaultSslManagerBundle(SslStoreBundle sslStoreBundle, SslBundleKey sslBundleKey, boolean forClientAuth) {
        this.sslStoreBundle = sslStoreBundle;
        this.sslBundleKey = sslBundleKey;
        this.delegate = SslManagerBundle.from(sslStoreBundle, sslBundleKey);
        this.forClientAuth = forClientAuth;
    }

    @Override
    public KeyManagerFactory getKeyManagerFactory() {
        if (!this.forClientAuth) {
            return this.delegate.getKeyManagerFactory();
        } else {
            try {
                KeyStore store = this.sslStoreBundle.getKeyStore();
                this.sslBundleKey.assertContainsAlias(store);
                String alias = this.sslBundleKey.getAlias();
                String algorithm = KeyManagerFactory.getDefaultAlgorithm();
                KeyManagerFactory factory = KeyManagerFactory.getInstance(algorithm);
                factory = (alias != null) ? new KeyVaultAliasKeyManagerFactory(factory, alias, algorithm) : factory;
                String password = this.sslBundleKey.getPassword();
                password = (password != null) ? password : this.sslStoreBundle.getKeyStorePassword();
                factory.init(store, (password != null) ? password.toCharArray() : null);
                return factory;
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IllegalStateException("Could not load key manager factory: " + ex.getMessage(), ex);
            }
        }

    }

    @Override
    public TrustManagerFactory getTrustManagerFactory() {
        return this.delegate.getTrustManagerFactory();
    }

}
