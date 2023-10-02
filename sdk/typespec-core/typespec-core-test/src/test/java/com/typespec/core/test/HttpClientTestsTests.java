// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test;

import com.typespec.core.http.HttpClient;
import com.typespec.core.test.http.HttpClientTests;
import com.typespec.core.test.http.LocalTestServer;
import com.typespec.core.test.utils.HttpURLConnectionHttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
public class HttpClientTestsTests extends HttpClientTests {
    private static LocalTestServer server;

    @BeforeAll
    public static void getServer() {
        server = HttpClientTestsServer.getHttpClientTestsServer();
        server.start();
    }

    @AfterAll
    public static void shutdownServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Override
    @Deprecated
    protected int getPort() {
        return server.getHttpPort();
    }

    @Override
    protected String getServerUri(boolean secure) {
        return secure ? server.getHttpsUri() : server.getHttpUri();
    }

    @Override
    protected HttpClient createHttpClient() {
        return new HttpURLConnectionHttpClient();
    }

    @Override
    @Disabled("HttpUrlConnection client doesn't support PATCH requests.")
    public void asyncPatchRequest() {
        super.asyncPatchRequest();
    }

    @Override
    @Disabled("HttpUrlConnection client doesn't support PATCH requests.")
    public void syncPatchRequest() {
        super.syncPatchRequest();
    }
}
