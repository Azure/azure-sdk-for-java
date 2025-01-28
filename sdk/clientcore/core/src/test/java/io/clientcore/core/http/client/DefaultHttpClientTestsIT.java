// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.client;

import io.clientcore.core.shared.HttpClientTests;
import io.clientcore.core.shared.HttpClientTestsServer;
import io.clientcore.core.shared.LocalTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

/**
 * Tests for the {@link DefaultHttpClient} class.
 * <p>
 * Now that the default HttpClient, and related code, are using multi-release JARs this must be an integration test as
 * the full JAR must be available to use the multi-release code.
 */
@DisabledForJreRange(max = JRE.JAVA_11)
public class DefaultHttpClientTestsIT extends HttpClientTests {
    private static LocalTestServer server;

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
    protected HttpClient getHttpClient() {
        return new DefaultHttpClientProvider().getSharedInstance();
    }

    @Override
    protected String getServerUri(boolean secure) {
        return secure ? server.getHttpsUri() : server.getHttpUri();
    }

    @Override
    protected int getPort() {
        return server.getHttpPort();
    }
}
