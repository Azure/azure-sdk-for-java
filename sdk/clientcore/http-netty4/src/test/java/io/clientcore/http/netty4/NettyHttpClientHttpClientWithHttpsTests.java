// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.shared.HttpClientTests;
import io.clientcore.core.shared.HttpClientTestsServer;
import io.clientcore.core.shared.LocalTestServer;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;

import javax.net.ssl.SSLException;
import java.util.concurrent.TimeUnit;

/**
 * Reactor Netty {@link HttpClientTests} with https.
 * Some request logic branches out if it's https like file uploads.
 */
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class NettyHttpClientHttpClientWithHttpsTests extends HttpClientTests {
    private static LocalTestServer server;

    private static final HttpClient HTTP_CLIENT_INSTANCE;

    static {
        try {
            SslContext sslContext
                = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();

            HTTP_CLIENT_INSTANCE = new NettyHttpClientBuilder().sslContext(sslContext).build();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    public static void startTestServer() {
        server = HttpClientTestsServer.getHttpClientTestsServer();
        server.start();
    }

    @AfterAll
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Override
    @Deprecated
    protected int getPort() {
        return server.getHttpsPort();
    }

    @Override
    protected String getServerUri(boolean secure) {
        return secure ? server.getHttpsUri() : server.getHttpUri();
    }

    @Override
    protected boolean isSecure() {
        return true;
    }

    @Override
    protected HttpClient getHttpClient() {
        return HTTP_CLIENT_INSTANCE;
    }
}
