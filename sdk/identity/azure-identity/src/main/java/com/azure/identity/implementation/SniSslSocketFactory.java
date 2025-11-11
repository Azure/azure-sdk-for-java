// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.CoreUtils;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.net.InetAddress;

public final class SniSslSocketFactory extends SSLSocketFactory {
    private final SSLSocketFactory sslSocketFactory;
    private final String sniName;

    public SniSslSocketFactory(SSLSocketFactory sslSocketFactory, String sniName) {
        this.sslSocketFactory = sslSocketFactory;
        this.sniName = sniName;
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        Socket sslSocket = (SSLSocket) sslSocketFactory.createSocket(s, host, port, autoClose);
        configureSni(sslSocket);
        return sslSocket;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Socket sslSocket = sslSocketFactory.createSocket(host, port);
        configureSni(sslSocket);
        return sslSocket;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException {
        Socket sslSocket = sslSocketFactory.createSocket(host, port, localAddress, localPort);
        configureSni(sslSocket);
        return sslSocket;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        Socket sslSocket = sslSocketFactory.createSocket(host, port);
        configureSni(sslSocket);
        return sslSocket;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
        throws IOException {
        Socket sslSocket = sslSocketFactory.createSocket(address, port, localAddress, localPort);
        configureSni(sslSocket);
        return sslSocket;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return sslSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return sslSocketFactory.getSupportedCipherSuites();
    }

    private void configureSni(Socket socket) {
        if (socket instanceof SSLSocket && !CoreUtils.isNullOrEmpty(sniName)) {
            SSLSocket sslSocket = (SSLSocket) socket;
            SSLParameters sslParameters = sslSocket.getSSLParameters();
            sslParameters.setServerNames(Collections.singletonList(new RawSniServerName(sniName)));
            sslSocket.setSSLParameters(sslParameters);
        }
    }

    public static final class RawSniServerName extends SNIServerName {
        public RawSniServerName(String sniHost) {
            super(0, sniHost.getBytes(StandardCharsets.UTF_8));
        }
    }
}
