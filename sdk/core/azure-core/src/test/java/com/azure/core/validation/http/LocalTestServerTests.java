// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.validation.http;

import com.azure.core.http.HttpProtocolVersion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that existing HTTP/1.1 clients work with both local server modes.
 */
public class LocalTestServerTests {
    @ParameterizedTest
    @EnumSource(HttpProtocolVersion.class)
    public void supportsHttp1Clients(HttpProtocolVersion version) throws Exception {
        LocalTestServer.RequestHandler handler = (request, response, body) -> {
            response.setHeader("x-test-http-protocol", request.getProtocol());
            response.getOutputStream().write(1);
        };
        LocalTestServer server = new LocalTestServer(handler, 32, version);
        try {
            server.start();
            assertHttp1Response((HttpURLConnection) URI.create(server.getHttpUri()).toURL().openConnection());
            HttpsURLConnection secure = (HttpsURLConnection) URI.create(server.getHttpsUri()).toURL().openConnection();
            secure.setSSLSocketFactory(HttpProtocolVersionTests.getSslContext().getSocketFactory());
            assertHttp1Response(secure);
        } finally {
            server.stop();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void preservesLegacyConstructors(boolean specifyThreadCount) throws Exception {
        LocalTestServer.RequestHandler handler = (request, response, body) -> {
            response.setHeader("x-test-http-protocol", request.getProtocol());
            response.getOutputStream().write(1);
        };
        LocalTestServer server = specifyThreadCount ? new LocalTestServer(handler, 32) : new LocalTestServer(handler);
        try {
            server.start();
            assertHttp1Response((HttpURLConnection) URI.create(server.getHttpUri()).toURL().openConnection());
        } finally {
            server.stop();
        }
    }

    private static void assertHttp1Response(HttpURLConnection connection) throws Exception {
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        try {
            assertEquals(200, connection.getResponseCode());
            assertEquals("HTTP/1.1", connection.getHeaderField("x-test-http-protocol"));
            try (InputStream body = connection.getInputStream()) {
                assertEquals(1, body.read());
                assertEquals(-1, body.read());
            }
        } finally {
            connection.disconnect();
        }
    }
}
