package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpClient;
import com.azure.core.test.HttpClientTestsServer;
import com.azure.core.test.http.HttpClientTests;
import com.azure.core.test.http.LocalTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class HttpUrlConnectionAsyncClientHttpClientTests extends HttpClientTests {

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
    protected HttpClient createHttpClient() {
        return new HttpUrlConnectionAsyncClientProvider().createInstance();
    }

    @Override
    protected int getPort() {
        return server.getHttpPort();
    }

    @Override
    protected String getServerUri(boolean secure) {
        return secure ? server.getHttpsUri() : server.getHttpUri();
    }
}
