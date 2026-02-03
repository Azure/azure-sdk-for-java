// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.shared;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.cert.X509Certificate;

/**
 * A trust manager that trusts all certificates.
 * <p>
 * Used for tests against {@link LocalTestServer}'s HTTPS endpoints as the server uses a self-signed certificate.
 */
public final class InsecureTrustManager extends X509ExtendedTrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
