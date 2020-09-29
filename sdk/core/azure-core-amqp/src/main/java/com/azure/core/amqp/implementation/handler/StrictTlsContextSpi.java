// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.util.logging.ClientLogger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * SSL context service provider that takes a standard SSLContext and disables the SSLv2Hello protocol.
 */
class StrictTlsContextSpi extends SSLContextSpi {
    private static final String SSL_V2_HELLO = "SSLv2Hello";

    private final ClientLogger logger = new ClientLogger(StrictTlsContextSpi.class);
    private final SSLContext sslContext;

    /**
     * Creates an instance with the given SSL context.
     *
     * @param sslContext SSL context to use.
     * @throws NullPointerException if {@code sslContext} is null.
     */
    StrictTlsContextSpi(SSLContext sslContext) {
        this.sslContext = Objects.requireNonNull(sslContext, "'sslContext' cannot be null.");
    }

    @Override
    protected void engineInit(KeyManager[] keyManagers, TrustManager[] trustManagers, SecureRandom secureRandom)
        throws KeyManagementException {

        sslContext.init(keyManagers, trustManagers, secureRandom);
    }

    @Override
    protected SSLSocketFactory engineGetSocketFactory() {
        return sslContext.getSocketFactory();
    }

    @Override
    protected SSLServerSocketFactory engineGetServerSocketFactory() {
        return sslContext.getServerSocketFactory();
    }

    /**
     * Gets an SSL engine without SSLv2Hello protocol enabled.
     *
     * @return SSL engine without SSLv2Hello protocol enabled.
     */
    @Override
    protected SSLEngine engineCreateSSLEngine() {
        final SSLEngine engine = sslContext.createSSLEngine();
        final String[] protocols = Stream.of(engine.getEnabledProtocols())
            .filter(protocol -> {
                final boolean isSSLv2Hello = protocol.equalsIgnoreCase(SSL_V2_HELLO);
                if (isSSLv2Hello) {
                    logger.info("{} was an enabled protocol. Filtering out.", SSL_V2_HELLO);
                }

                return !isSSLv2Hello;
            }).toArray(String[]::new);

        engine.setEnabledProtocols(protocols);
        return engine;
    }

    @Override
    protected SSLEngine engineCreateSSLEngine(String host, int port) {
        return sslContext.createSSLEngine(host, port);
    }

    @Override
    protected SSLSessionContext engineGetServerSessionContext() {
        return sslContext.getServerSessionContext();
    }

    @Override
    protected SSLSessionContext engineGetClientSessionContext() {
        return sslContext.getClientSessionContext();
    }
}
