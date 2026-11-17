// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.credential.ClientCertificateBinding;
import com.azure.core.util.logging.ClientLogger;
import okhttp3.Connection;
import okhttp3.Handshake;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.util.List;

/**
 * Creates the OkHttp clients for requests whose access token is bound to a TLS client certificate, for mutual TLS
 * (mTLS) proof-of-possession.
 */
public final class ClientCertificateBinder {
    private static final ClientLogger LOGGER = new ClientLogger(ClientCertificateBinder.class);

    // mTLS proof-of-possession currently requires TLS 1.2.
    private static final String TLS_1_2 = "TLSv1.2";

    // Bounds the search for the client certificate error in a chain of causes.
    private static final int MAXIMUM_CAUSE_DEPTH = 8;

    private ClientCertificateBinder() {
    }

    /**
     * Creates a client, derived from {@code client}, for requests whose access token is bound to a TLS client
     * certificate.
     * <p>
     * The client builds its TLS connections from the binding's key manager, with the JDK's TLS implementation and TLS
     * 1.2, and keeps the trust manager, hostname verifier and certificate pinner of {@code client}. It shares the
     * connection pool and dispatcher of {@code client}, but OkHttp only reuses a pooled connection for a request whose
     * address, including its SSL socket factory, is the same, so the client's connections aren't used by any other
     * client. It doesn't follow redirects, and fails a request without sending it unless its connection presented the
     * certificate.
     *
     * @param client The client to derive the client from.
     * @param binding The client certificate the requests' access token is bound to.
     * @return The client that presents the certificate.
     * @throws IllegalStateException If the TLS context for the certificate can't be created.
     */
    public static OkHttpClient bindToCertificate(OkHttpClient client, ClientCertificateBinding binding) {
        OkHttpClient.Builder builder = client.newBuilder().followRedirects(false).followSslRedirects(false);

        // A client only has a trust manager when it can make TLS connections. Without one, the request fails like any
        // HTTPS request the client sends.
        X509TrustManager trustManager = client.x509TrustManager();
        if (trustManager != null) {
            builder.sslSocketFactory(createSocketFactory(binding, trustManager), trustManager);
        }

        // First, so that the connection is checked before any other network interceptor sees the request.
        builder.networkInterceptors().add(0, new ClientCertificateCheck(binding));
        return builder.build();
    }

    /**
     * Gets the error to fail a request with when the request failed because its connection didn't present the client
     * certificate its access token is bound to.
     *
     * @param error The error the request failed with in OkHttp.
     * @return The error to fail the request with, or null if the request failed for another reason.
     */
    public static IllegalStateException getClientCertificateError(Throwable error) {
        // OkHttp may wrap the error, for example when the call timed out at the same time.
        Throwable current = error;
        for (int depth = 0; current != null && depth < MAXIMUM_CAUSE_DEPTH; depth++) {
            if (current instanceof ClientCertificateMismatchException) {
                return (IllegalStateException) current.getCause();
            }

            current = current.getCause();
        }

        return null;
    }

    private static SSLSocketFactory createSocketFactory(ClientCertificateBinding binding,
        X509TrustManager trustManager) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(new KeyManager[] { binding.getKeyManager() }, new TrustManager[] { trustManager }, null);
            return new Tls12SocketFactory(sslContext.getSocketFactory());
        } catch (GeneralSecurityException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "Failed to create the TLS context for requests bound to the client certificate with key ID '"
                    + binding.getKeyId() + "'.",
                e));
        }
    }

    /*
     * Fails the request without sending it unless its connection presented the client certificate the request's access
     * token is bound to. OkHttp runs network interceptors once the connection is established, before the request is
     * written.
     */
    private static final class ClientCertificateCheck implements Interceptor {
        private final ClientCertificateBinding binding;

        private ClientCertificateCheck(ClientCertificateBinding binding) {
            this.binding = binding;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Connection connection = chain.connection();
            Handshake handshake = connection == null ? null : connection.handshake();
            List<Certificate> certificates = handshake == null ? null : handshake.localCertificates();
            Certificate presented = certificates == null || certificates.isEmpty() ? null : certificates.get(0);
            if (!binding.getCertificate().equals(presented)) {
                String problem = presented == null
                    ? "didn't present a client certificate"
                    : "presented a different client certificate";
                throw new ClientCertificateMismatchException(
                    new IllegalStateException("The connection to '" + chain.request().url().host() + "' " + problem
                        + ", but the request's access token is bound to the client certificate with key ID '"
                        + binding.getKeyId() + "', so the request wasn't sent."));
            }

            return chain.proceed(chain.request());
        }
    }

    /*
     * Carries the error through OkHttp, which reports exceptions other than IOException thrown on its threads as
     * uncaught.
     */
    private static final class ClientCertificateMismatchException extends IOException {
        private static final long serialVersionUID = 1L;

        private ClientCertificateMismatchException(IllegalStateException error) {
            super(error.getMessage(), error);
        }
    }

    // Creates sockets that only enable TLS 1.2.
    private static final class Tls12SocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory delegate;

        private Tls12SocketFactory(SSLSocketFactory delegate) {
            this.delegate = delegate;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
            return enableTls12(delegate.createSocket(socket, host, port, autoClose));
        }

        @Override
        public Socket createSocket() throws IOException {
            return enableTls12(delegate.createSocket());
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return enableTls12(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            return enableTls12(delegate.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return enableTls12(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
            throws IOException {
            return enableTls12(delegate.createSocket(address, port, localAddress, localPort));
        }

        private static Socket enableTls12(Socket socket) {
            if (socket instanceof SSLSocket) {
                ((SSLSocket) socket).setEnabledProtocols(new String[] { TLS_1_2 });
            }

            return socket;
        }
    }
}
