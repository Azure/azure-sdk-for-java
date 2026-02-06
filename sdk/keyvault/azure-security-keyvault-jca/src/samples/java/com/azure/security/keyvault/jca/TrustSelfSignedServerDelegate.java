// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;

/**
 * Implementation of {@link X509TrustManager} that wraps another {@link X509TrustManager} with a check where self-signed
 * server chains are trusted.
 * <p>
 * This implementation uses basic validation for checking if the chain is self-signed, where it only checks that the
 * chain has a length of one. This validation only applies when running
 * {@link X509TrustManager#checkServerTrusted(X509Certificate[], String)}, and if it passes that method call does not
 * delegate to the wrapped {@link X509TrustManager}.
 * <p>
 * {@link X509TrustManager#checkClientTrusted(X509Certificate[], String)} and
 * {@link X509TrustManager#getAcceptedIssuers()} always delegate to the wrapped {@link X509TrustManager}.
 */
public final class TrustSelfSignedServerDelegate implements X509TrustManager {
    private final X509TrustManager delegate;

    /**
     * Creates a new instance of {@link TrustSelfSignedServerDelegate}.
     *
     * @param delegate The {@link X509TrustManager} that this {@link TrustSelfSignedServerDelegate} will delegate.
     */
    public TrustSelfSignedServerDelegate(X509TrustManager delegate) {
        this.delegate = Objects.requireNonNull(delegate, "'delegate' cannot be null.");
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        delegate.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (chain.length != 1) {
            checkServerTrusted(chain, authType);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return delegate.getAcceptedIssuers();
    }
}
