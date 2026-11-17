// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.credential.ClientCertificateBinding;
import com.azure.core.util.logging.ClientLogger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.net.http.HttpClient;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Creates the JDK HTTP clients for requests whose access token is bound to a TLS client certificate, for mutual TLS
 * (mTLS) proof-of-possession.
 */
public final class ClientCertificateBinder {
    private static final ClientLogger LOGGER = new ClientLogger(ClientCertificateBinder.class);

    // mTLS proof-of-possession currently requires TLS 1.2.
    private static final String TLS_1_2 = "TLSv1.2";

    // The cipher suite of an SSL engine's session until its first handshake completes.
    private static final String NULL_CIPHER_SUITE = "SSL_NULL_WITH_NULL_NULL";

    // Bounds the search for the client certificate error in a chain of causes.
    private static final int MAXIMUM_CAUSE_DEPTH = 8;

    private ClientCertificateBinder() {
    }

    /**
     * Creates a client for requests whose access token is bound to a TLS client certificate.
     * <p>
     * The client builds its TLS connections from the binding's key manager, with the JDK's TLS implementation and TLS
     * 1.2, and doesn't follow redirects. It takes the HTTP version, connection timeout, proxy, authenticator, cookie
     * handler and executor of {@code client}, but not its SSL context or parameters, which can't be read. Its
     * connections refuse to encrypt a request unless their TLS handshake presented the certificate.
     *
     * @param client The client to take the configuration from.
     * @param binding The client certificate the requests' access token is bound to.
     * @param trustManagerFactory The trust for the client's connections, or null to use the JVM's default trust.
     * @return The client that presents the certificate.
     * @throws IllegalStateException If the TLS context for the certificate can't be created.
     */
    public static HttpClient bindToCertificate(HttpClient client, ClientCertificateBinding binding,
        TrustManagerFactory trustManagerFactory) {
        SSLContext sslContext = createSslContext(binding, trustManagerFactory);
        SSLParameters sslParameters = sslContext.getDefaultSSLParameters();
        sslParameters.setProtocols(new String[] { TLS_1_2 });

        HttpClient.Builder builder = HttpClient.newBuilder()
            .version(client.version())
            .followRedirects(HttpClient.Redirect.NEVER)
            .sslContext(sslContext)
            .sslParameters(sslParameters);
        client.connectTimeout().ifPresent(builder::connectTimeout);
        client.proxy().ifPresent(builder::proxy);
        client.authenticator().ifPresent(builder::authenticator);
        client.cookieHandler().ifPresent(builder::cookieHandler);
        client.executor().ifPresent(builder::executor);
        return builder.build();
    }

    /**
     * Creates the error a request whose access token is bound to a TLS client certificate fails with in the JDK HTTP
     * client when it's sent without TLS, so its connection can't present the certificate.
     *
     * @param host The host the request is sent to.
     * @param binding The client certificate the request's access token is bound to.
     * @return The error.
     */
    public static SSLException createNoTlsError(String host, ClientCertificateBinding binding) {
        return new ClientCertificateMismatchException(createError(host, null, binding));
    }

    /**
     * Gets the error to fail a request with when the request failed because its connection didn't present the client
     * certificate its access token is bound to.
     *
     * @param error The error the request failed with in the JDK HTTP client.
     * @return The error to fail the request with, or null if the request failed for another reason.
     */
    public static IllegalStateException getClientCertificateError(Throwable error) {
        // The JDK HTTP client may wrap the error.
        Throwable current = error;
        for (int depth = 0; current != null && depth < MAXIMUM_CAUSE_DEPTH; depth++) {
            if (current instanceof ClientCertificateMismatchException) {
                return (IllegalStateException) current.getCause();
            }

            current = current.getCause();
        }

        return null;
    }

    private static SSLContext createSslContext(ClientCertificateBinding binding,
        TrustManagerFactory trustManagerFactory) {
        try {
            SSLContext delegate = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = trustManagerFactory == null ? null : trustManagerFactory.getTrustManagers();
            delegate.init(new KeyManager[] { binding.getKeyManager() }, trustManagers, null);
            return new ClientCertificateCheckingContext(delegate, binding);
        } catch (GeneralSecurityException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "Failed to create the TLS context for requests bound to the client certificate with key ID '"
                    + binding.getKeyId() + "'.",
                e));
        }
    }

    private static IllegalStateException createError(String host, Certificate presented,
        ClientCertificateBinding binding) {
        String problem
            = presented == null ? "didn't present a client certificate" : "presented a different client certificate";
        return new IllegalStateException("The connection to '" + host + "' " + problem
            + ", but the request's access token is bound to the client certificate with key ID '" + binding.getKeyId()
            + "', so the request wasn't sent.");
    }

    /*
     * Carries the error through the JDK HTTP client as an SSL error, which fails the request and closes its connection.
     */
    private static final class ClientCertificateMismatchException extends SSLException {
        private static final long serialVersionUID = 1L;

        private ClientCertificateMismatchException(IllegalStateException error) {
            super(error.getMessage(), error);
        }
    }

    /*
     * An SSL context whose SSL engines refuse to encrypt application data unless their TLS handshake presented the
     * client certificate. The JDK HTTP client creates an SSL engine for each TLS connection.
     */
    private static final class ClientCertificateCheckingContext extends SSLContext {
        private ClientCertificateCheckingContext(SSLContext delegate, ClientCertificateBinding binding) {
            super(new ClientCertificateCheckingContextSpi(delegate, binding), delegate.getProvider(),
                delegate.getProtocol());
        }
    }

    private static final class ClientCertificateCheckingContextSpi extends SSLContextSpi {
        private final SSLContext delegate;
        private final ClientCertificateBinding binding;

        private ClientCertificateCheckingContextSpi(SSLContext delegate, ClientCertificateBinding binding) {
            this.delegate = delegate;
            this.binding = binding;
        }

        @Override
        protected void engineInit(KeyManager[] keyManagers, TrustManager[] trustManagers, SecureRandom random) {
            throw new UnsupportedOperationException("The SSL context is already initialized.");
        }

        // Sockets wouldn't check the client certificate, and the JDK HTTP client only uses SSL engines.
        @Override
        protected SSLSocketFactory engineGetSocketFactory() {
            throw new UnsupportedOperationException("The SSL context only supports SSL engines.");
        }

        @Override
        protected SSLServerSocketFactory engineGetServerSocketFactory() {
            throw new UnsupportedOperationException("The SSL context only supports SSL engines.");
        }

        @Override
        protected SSLEngine engineCreateSSLEngine() {
            return new ClientCertificateCheckingEngine(delegate.createSSLEngine(), binding);
        }

        @Override
        protected SSLEngine engineCreateSSLEngine(String host, int port) {
            return new ClientCertificateCheckingEngine(delegate.createSSLEngine(host, port), binding);
        }

        @Override
        protected SSLSessionContext engineGetServerSessionContext() {
            return delegate.getServerSessionContext();
        }

        @Override
        protected SSLSessionContext engineGetClientSessionContext() {
            return delegate.getClientSessionContext();
        }

        @Override
        protected SSLParameters engineGetDefaultSSLParameters() {
            return delegate.getDefaultSSLParameters();
        }

        @Override
        protected SSLParameters engineGetSupportedSSLParameters() {
            return delegate.getSupportedSSLParameters();
        }
    }

    /*
     * An SSL engine that refuses to encrypt application data unless its session presented the client certificate.
     * Until its first handshake completes, an SSL engine only produces handshake messages, so the check applies from
     * the first request on the connection, and to every request after it.
     */
    private static final class ClientCertificateCheckingEngine extends SSLEngine {
        private final SSLEngine delegate;
        private final ClientCertificateBinding binding;

        // The last session that presented the certificate, so that it's only checked once.
        private volatile SSLSession verifiedSession;

        private ClientCertificateCheckingEngine(SSLEngine delegate, ClientCertificateBinding binding) {
            super(delegate.getPeerHost(), delegate.getPeerPort());
            this.delegate = delegate;
            this.binding = binding;
        }

        @Override
        public SSLEngineResult wrap(ByteBuffer[] sources, int offset, int length, ByteBuffer destination)
            throws SSLException {
            if (hasRemaining(sources, offset, length)) {
                checkClientCertificate(false);
            }

            SSLEngineResult result = delegate.wrap(sources, offset, length, destination);
            if (result.bytesConsumed() > 0) {
                // Application data was encrypted. If its session didn't present the certificate, failing here keeps the
                // caller from sending it.
                checkClientCertificate(true);
            }

            return result;
        }

        private void checkClientCertificate(boolean encrypted) throws SSLException {
            SSLSession session = delegate.getSession();
            if (session == verifiedSession || (!encrypted && NULL_CIPHER_SUITE.equals(session.getCipherSuite()))) {
                return;
            }

            Certificate[] certificates = session.getLocalCertificates();
            Certificate presented = certificates == null || certificates.length == 0 ? null : certificates[0];
            if (!binding.getCertificate().equals(presented)) {
                throw new ClientCertificateMismatchException(createError(getPeerHost(), presented, binding));
            }

            verifiedSession = session;
        }

        private static boolean hasRemaining(ByteBuffer[] buffers, int offset, int length) {
            for (int i = offset; i < offset + length; i++) {
                if (buffers[i].hasRemaining()) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public SSLEngineResult unwrap(ByteBuffer source, ByteBuffer[] destinations, int offset, int length)
            throws SSLException {
            return delegate.unwrap(source, destinations, offset, length);
        }

        @Override
        public Runnable getDelegatedTask() {
            return delegate.getDelegatedTask();
        }

        @Override
        public void closeInbound() throws SSLException {
            delegate.closeInbound();
        }

        @Override
        public boolean isInboundDone() {
            return delegate.isInboundDone();
        }

        @Override
        public void closeOutbound() {
            delegate.closeOutbound();
        }

        @Override
        public boolean isOutboundDone() {
            return delegate.isOutboundDone();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public String[] getEnabledCipherSuites() {
            return delegate.getEnabledCipherSuites();
        }

        @Override
        public void setEnabledCipherSuites(String[] suites) {
            delegate.setEnabledCipherSuites(suites);
        }

        @Override
        public String[] getSupportedProtocols() {
            return delegate.getSupportedProtocols();
        }

        @Override
        public String[] getEnabledProtocols() {
            return delegate.getEnabledProtocols();
        }

        @Override
        public void setEnabledProtocols(String[] protocols) {
            delegate.setEnabledProtocols(protocols);
        }

        @Override
        public SSLSession getSession() {
            return delegate.getSession();
        }

        @Override
        public SSLSession getHandshakeSession() {
            return delegate.getHandshakeSession();
        }

        @Override
        public void beginHandshake() throws SSLException {
            delegate.beginHandshake();
        }

        @Override
        public SSLEngineResult.HandshakeStatus getHandshakeStatus() {
            return delegate.getHandshakeStatus();
        }

        @Override
        public void setUseClientMode(boolean mode) {
            delegate.setUseClientMode(mode);
        }

        @Override
        public boolean getUseClientMode() {
            return delegate.getUseClientMode();
        }

        @Override
        public void setNeedClientAuth(boolean need) {
            delegate.setNeedClientAuth(need);
        }

        @Override
        public boolean getNeedClientAuth() {
            return delegate.getNeedClientAuth();
        }

        @Override
        public void setWantClientAuth(boolean want) {
            delegate.setWantClientAuth(want);
        }

        @Override
        public boolean getWantClientAuth() {
            return delegate.getWantClientAuth();
        }

        @Override
        public void setEnableSessionCreation(boolean flag) {
            delegate.setEnableSessionCreation(flag);
        }

        @Override
        public boolean getEnableSessionCreation() {
            return delegate.getEnableSessionCreation();
        }

        @Override
        public SSLParameters getSSLParameters() {
            return delegate.getSSLParameters();
        }

        @Override
        public void setSSLParameters(SSLParameters parameters) {
            delegate.setSSLParameters(parameters);
        }

        @Override
        public String getApplicationProtocol() {
            return delegate.getApplicationProtocol();
        }

        @Override
        public String getHandshakeApplicationProtocol() {
            return delegate.getHandshakeApplicationProtocol();
        }

        @Override
        public void setHandshakeApplicationProtocolSelector(BiFunction<SSLEngine, List<String>, String> selector) {
            delegate.setHandshakeApplicationProtocolSelector(selector);
        }

        @Override
        public BiFunction<SSLEngine, List<String>, String> getHandshakeApplicationProtocolSelector() {
            return delegate.getHandshakeApplicationProtocolSelector();
        }
    }
}
