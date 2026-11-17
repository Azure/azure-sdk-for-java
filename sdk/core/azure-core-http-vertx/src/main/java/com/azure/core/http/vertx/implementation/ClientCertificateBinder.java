// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.credential.ClientCertificateBinding;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.net.JdkSSLEngineOptions;
import io.vertx.core.net.KeyCertOptions;

import javax.net.ssl.SSLSession;
import java.security.cert.Certificate;
import java.util.Collections;

/**
 * Configures the Vert.x HTTP clients for requests whose access token is bound to a TLS client certificate, for mutual
 * TLS (mTLS) proof-of-possession.
 */
public final class ClientCertificateBinder {
    // mTLS proof-of-possession currently requires TLS 1.2.
    private static final String TLS_1_2 = "TLSv1.2";

    private ClientCertificateBinder() {
    }

    /**
     * Creates the options of a client for requests whose access token is bound to a TLS client certificate.
     * <p>
     * The options are a copy of {@code options}, so they keep its trust settings, with the binding's key manager, the
     * JDK's SSL engine and TLS 1.2.
     *
     * @param options The options to copy.
     * @param binding The client certificate the requests' access token is bound to.
     * @return The options of the client that presents the certificate.
     */
    public static HttpClientOptions bindToCertificate(HttpClientOptions options, ClientCertificateBinding binding) {
        HttpClientOptions boundOptions = new HttpClientOptions(options);
        boundOptions.setKeyCertOptions(KeyCertOptions.wrap(binding.getKeyManager()));
        boundOptions.setSslEngineOptions(new JdkSSLEngineOptions());
        boundOptions.setEnabledSecureTransportProtocols(Collections.singleton(TLS_1_2));
        return boundOptions;
    }

    /**
     * Checks that the connection of a request presented the client certificate the request's access token is bound to
     * during its TLS handshake.
     *
     * @param request The request, which hasn't been written yet.
     * @param binding The client certificate the request's access token is bound to.
     * @param host The host the request is sent to.
     * @return The error to fail the request with, or null if the request can be sent.
     */
    public static IllegalStateException checkClientCertificate(HttpClientRequest request,
        ClientCertificateBinding binding, String host) {
        HttpConnection connection = request.connection();
        SSLSession session = connection == null ? null : connection.sslSession();
        Certificate[] certificates = session == null ? null : session.getLocalCertificates();
        Certificate presented = certificates == null || certificates.length == 0 ? null : certificates[0];
        if (binding.getCertificate().equals(presented)) {
            return null;
        }

        String problem
            = presented == null ? "didn't present a client certificate" : "presented a different client certificate";
        return new IllegalStateException("The connection to '" + host + "' " + problem
            + ", but the request's access token is bound to the client certificate with key ID '" + binding.getKeyId()
            + "', so the request wasn't sent.");
    }
}
