// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Taken from:
 * $/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/implementation/handler/StrictTlsContextSpi.java
 */
class StrictTlsContextSpi extends SSLContextSpi {
    private static final String SSL_V2_HELLO = "SSLv2Hello";

    private final Logger logger = LoggerFactory.getLogger(StrictTlsContextSpi.class);
    private final SSLContext sslContext;

    /**
     * Creates an instance with the given SSL context.
     *
     * @param sslContext SSL context to use.
     *
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
     * Creates an SSLEngine from the context without SSLv2Hello protocol enabled.
     *
     * @return An {@code SSLEngine} object.
     */
    @Override
    protected SSLEngine engineCreateSSLEngine() {
        final SSLEngine sslEngine = sslContext.createSSLEngine();
        final String[] protocols = getAllowedProtocols(sslEngine.getEnabledProtocols());

        sslEngine.setEnabledProtocols(protocols);
        return sslEngine;
    }

    /**
     * Creates an SSLEngine from the context without SSLv2Hello protocol enabled.
     *
     * @param host the non-authoritative name of the host
     * @param port the non-authoritative port
     *
     * @return An {@code SSLEngine} object.
     */
    @Override
    protected SSLEngine engineCreateSSLEngine(String host, int port) {
        final SSLEngine sslEngine = sslContext.createSSLEngine(host, port);
        final String[] protocols = getAllowedProtocols(sslEngine.getEnabledProtocols());

        sslEngine.setEnabledProtocols(protocols);
        return sslEngine;
    }

    @Override
    protected SSLSessionContext engineGetServerSessionContext() {
        return sslContext.getServerSessionContext();
    }

    @Override
    protected SSLSessionContext engineGetClientSessionContext() {
        return sslContext.getClientSessionContext();
    }

    /**
     * Removes {@link #SSL_V2_HELLO} protocol if it is available.
     *
     * @return Enabled protocols.
     */
    private String[] getAllowedProtocols(String[] protocols) {
        return Stream.of(protocols)
            .filter(protocol -> {
                final boolean isSSLv2Hello = protocol.equalsIgnoreCase(SSL_V2_HELLO);
                if (isSSLv2Hello) {
                    logger.info("{} was an enabled protocol. Filtering out.", SSL_V2_HELLO);
                }

                return !isSSLv2Hello;
            }).toArray(String[]::new);
    }
}
