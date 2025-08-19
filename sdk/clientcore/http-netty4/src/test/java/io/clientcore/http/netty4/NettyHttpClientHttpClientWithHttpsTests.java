// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.shared.HttpClientTests;
import io.clientcore.core.shared.HttpClientTestsServer;
import io.clientcore.core.shared.InsecureTrustManager;
import io.clientcore.core.shared.LocalTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * Netty {@link HttpClientTests} with https.
 * Some request logic branches out if it's https like file uploads.
 */
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class NettyHttpClientHttpClientWithHttpsTests extends HttpClientTests {
    private static LocalTestServer server;

    private static final HttpClient HTTP_CLIENT_INSTANCE;

    static {
        HTTP_CLIENT_INSTANCE = new NettyHttpClientBuilder() //.maximumHttpVersion(HttpProtocolVersion.HTTP_1_1)
            .connectionPoolSize(0)
            .sslContextModifier(ssl -> ssl.trustManager(new InsecureTrustManager()).secureRandom(new SecureRandom()))
            .build();
    }

    @BeforeAll
    public static void startTestServer() {
        server = HttpClientTestsServer.getHttpClientTestsServer(HttpProtocolVersion.HTTP_1_1, true);
        server.start();
    }

    @AfterAll
    public static void stopTestServer() {
        if (HTTP_CLIENT_INSTANCE instanceof NettyHttpClient) {
            ((NettyHttpClient) HTTP_CLIENT_INSTANCE).close();
        }
        if (server != null) {
            server.stop();
        }
    }

    @Override
    @Deprecated
    protected int getPort() {
        return server.getPort();
    }

    @Override
    protected String getServerUri(boolean secure) {
        return secure ? server.getHttpsUri() : server.getUri();
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
