// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.shared.HttpClientTests;
import io.clientcore.core.shared.HttpClientTestsServer;
import io.clientcore.core.shared.InsecureTrustManager;
import io.clientcore.core.shared.LocalTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

@Execution(ExecutionMode.SAME_THREAD)
public class OkHttpHttp2ClientHttpClientTests extends HttpClientTests {
    private static LocalTestServer server;

    private static final HttpClient HTTP_CLIENT_INSTANCE;

    static {
        try {
            X509ExtendedTrustManager trustManager = new InsecureTrustManager();
            SSLContext trustAll = SSLContext.getInstance("TLSv1.2");
            trustAll.init(null, new TrustManager[] { trustManager }, new SecureRandom());

            HTTP_CLIENT_INSTANCE
                = new OkHttpHttpClientBuilder().sslSocketFactory(trustAll.getSocketFactory(), trustManager)
                    .hostnameVerifier((hostname, session) -> true)
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
    protected boolean isHttp2() {
        return true;
    }

    @Override
    protected boolean isSecure() {
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
