// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.http.client;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.client.JdkHttpClientBuilder;
import io.clientcore.core.shared.HttpClientTests;
import io.clientcore.core.shared.HttpClientTestsServer;
import io.clientcore.core.shared.InsecureTrustManager;
import io.clientcore.core.shared.LocalTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * Reactor Netty {@link HttpClientTests}.
 */
@DisabledForJreRange(max = JRE.JAVA_11)
@Execution(ExecutionMode.SAME_THREAD)
public class JdkHttp2ClientTestIT extends HttpClientTests {
    private static LocalTestServer server;

    private static final HttpClient HTTP_CLIENT_INSTANCE;

    static {
        try {
            SSLContext trustAll = SSLContext.getInstance("TLS");
            trustAll.init(null, new TrustManager[] { new InsecureTrustManager() }, new SecureRandom());

            HTTP_CLIENT_INSTANCE = new JdkHttpClientBuilder().sslContext(trustAll)
                .setMaximumHttpVersion(HttpProtocolVersion.HTTP_2)
                .build();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    public static void startTestServer() {
        server = HttpClientTestsServer.getHttpClientTestsServer(HttpProtocolVersion.HTTP_2, true);
        server.start();
    }

    @AfterAll
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Override
    protected boolean isSecure() {
        return true;
    }

    @Override
    protected boolean isHttp2() {
        return true;
    }

    @Override
    @Deprecated
    protected int getPort() {
        return server.getPort();
    }

    @Override
    protected String getServerUri(boolean secure) {
        return server.getHttpsUri();
    }

    @Override
    protected HttpClient getHttpClient() {
        return HTTP_CLIENT_INSTANCE;
    }
}
