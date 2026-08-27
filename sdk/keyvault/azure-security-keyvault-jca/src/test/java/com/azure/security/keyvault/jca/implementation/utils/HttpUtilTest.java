// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.DEFAULT_USER_AGENT_VALUE_PREFIX;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.VERSION;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpUtilTest {

    @Test
    public void getUserAgentPrefixTest() {
        assertEquals(DEFAULT_USER_AGENT_VALUE_PREFIX, HttpUtil.getUserAgentPrefix());
        assertEquals(DEFAULT_USER_AGENT_VALUE_PREFIX + VERSION, HttpUtil.USER_AGENT_VALUE);
    }

    @Test
    @Disabled("Disable this because it will cause pipeline failure: https://dev.azure.com/azure-sdk/internal/_build/results?buildId=1196171&view=logs&j=4a83f3be-c53d-53dd-7954-86872056fb11&t=54174aae-5a55-579d-08e2-94fb446f7b77&l=29")
    public void testHttpUtilGet() {
        String url = "https://azure.com/";
        String result = HttpUtil.get(url, null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Disabled("This is only used to test in localhost manually")
    public void testHttpUtilGet1() {
        String url = "http://localhost:8000/";
        String result = HttpUtil.get(url, null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void textGetReturnsSuccessfulResponseBody() throws Exception {
        byte[] body = "response".getBytes(StandardCharsets.UTF_8);
        TestHttpURLConnection connection
            = new TestHttpURLConnection("https://example.test/value", 200, body, Collections.emptyMap());
        Map<String, String> requestHeaders = new LinkedHashMap<>();
        requestHeaders.put("x-test", "value");
        requestHeaders.put(HttpUtil.USER_AGENT_KEY, "caller-value");

        String result = HttpUtil.get("https://example.test/value", requestHeaders, ignored -> connection);

        assertEquals("response", result);
        assertEquals("value", connection.getRequestProperty("x-test"));
        assertEquals(HttpUtil.USER_AGENT_VALUE, connection.getRequestProperty(HttpUtil.USER_AGENT_KEY));
        assertEquals(HttpUtil.HTTP_TIMEOUT_IN_MILLISECONDS, connection.getConnectTimeout());
        assertEquals(HttpUtil.HTTP_TIMEOUT_IN_MILLISECONDS, connection.getReadTimeout());
        assertTrue(connection.disconnected);
    }

    @Test
    void textGetThrowsForNonSuccessfulResponse() throws Exception {
        TestHttpURLConnection connection = new TestHttpURLConnection("https://example.test/value", 429,
            "{\"error\":\"throttled\"}".getBytes(StandardCharsets.UTF_8), Collections.emptyMap());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> HttpUtil.get("https://example.test/value", null, ignored -> connection));

        assertTrue(exception.getMessage().contains("429"));
        assertTrue(connection.disconnected);
    }

    @Test
    void postThrowsForNonSuccessfulResponse() throws Exception {
        TestHttpURLConnection connection = new TestHttpURLConnection("https://example.test/value", 403,
            "{\"error\":\"forbidden\"}".getBytes(StandardCharsets.UTF_8), Collections.emptyMap());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> HttpUtil
            .post("https://example.test/value", null, "request", "application/json", ignored -> connection));

        assertTrue(exception.getMessage().contains("403"));
        assertEquals("request", new String(connection.requestBody.toByteArray(), StandardCharsets.UTF_8));
        assertEquals(HttpUtil.HTTP_TIMEOUT_IN_MILLISECONDS, connection.getConnectTimeout());
        assertEquals(HttpUtil.HTTP_TIMEOUT_IN_MILLISECONDS, connection.getReadTimeout());
        assertTrue(connection.disconnected);
    }

    @Test
    void authenticationChallengeReturnsCaseInsensitiveHeadersOnlyFor401() throws Exception {
        Map<String, List<String>> headers
            = Collections.singletonMap("www-authenticate", Collections.singletonList("Bearer authorization=test"));
        TestHttpURLConnection unauthorized
            = new TestHttpURLConnection("https://example.test/challenge", 401, new byte[0], headers);

        Map<String, List<String>> result
            = HttpUtil.getWithOnlyResponseHeaders("https://example.test/challenge", ignored -> unauthorized);

        assertEquals("Bearer authorization=test", result.get("WWW-Authenticate").get(0));
        assertEquals(HttpUtil.HTTP_TIMEOUT_IN_MILLISECONDS, unauthorized.getConnectTimeout());
        assertEquals(HttpUtil.HTTP_TIMEOUT_IN_MILLISECONDS, unauthorized.getReadTimeout());
        assertTrue(unauthorized.disconnected);

        TestHttpURLConnection successful
            = new TestHttpURLConnection("https://example.test/challenge", 200, new byte[0], headers);
        assertNull(HttpUtil.getWithOnlyResponseHeaders("https://example.test/challenge", ignored -> successful));
        assertTrue(successful.disconnected);
    }

    @Test
    void binaryResponsePreservesBodyAndFreshnessHeaders() throws Exception {
        byte[] body = new byte[] { 1, 2, 3 };
        Map<String, List<String>> headers = new LinkedHashMap<>();
        headers.put("Cache-Control", Collections.singletonList("public, max-age=300"));
        headers.put("Date", Collections.singletonList("Wed, 05 Aug 2026 10:00:00 GMT"));
        headers.put("Age", Collections.singletonList("30"));
        headers.put("Expires", Collections.singletonList("Wed, 05 Aug 2026 10:05:00 GMT"));
        TestHttpURLConnection connection = new TestHttpURLConnection(200, body, headers);

        HttpUtil.BinaryHttpResponse result
            = HttpUtil.getAiaBytesWithMetadata("https://example.test/cert.crt", ignored -> connection);

        assertArrayEquals(body, result.getBody());
        assertEquals("public, max-age=300", result.getCacheControl());
        assertEquals("Wed, 05 Aug 2026 10:00:00 GMT", result.getDate());
        assertEquals("30", result.getAge());
        assertEquals("Wed, 05 Aug 2026 10:05:00 GMT", result.getExpires());
        assertEquals(10_000, connection.getConnectTimeout());
        assertEquals(10_000, connection.getReadTimeout());
        assertEquals(HttpUtil.USER_AGENT_VALUE, connection.getRequestProperty(HttpUtil.USER_AGENT_KEY));
        assertTrue(connection.disconnected);
    }

    @Test
    void binaryResponseForFailureHasNoBodyAndPreservesFreshnessMetadata() throws Exception {
        Map<String, List<String>> headers
            = Collections.singletonMap("Cache-Control", Collections.singletonList("max-age=3600"));
        TestHttpURLConnection connection = new TestHttpURLConnection(503, new byte[] { 1 }, headers);

        HttpUtil.BinaryHttpResponse result
            = HttpUtil.getAiaBytesWithMetadata("https://example.test/cert.crt", ignored -> connection);

        assertNull(result.getBody());
        assertEquals("max-age=3600", result.getCacheControl());
        assertNull(result.getDate());
        assertNull(result.getAge());
        assertNull(result.getExpires());
    }

    @Test
    void binaryResponseCombinesMultipleCacheControlHeaders() throws Exception {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        headers.put("cache-control", Arrays.asList("public, max-age=300", "no-store"));
        TestHttpURLConnection connection = new TestHttpURLConnection(200, new byte[] { 1 }, headers);

        HttpUtil.BinaryHttpResponse result
            = HttpUtil.getAiaBytesWithMetadata("https://example.test/cert.crt", ignored -> connection);

        assertEquals("public, max-age=300, no-store", result.getCacheControl());
    }

    @Test
    void binaryResponseRejectsOversizedContentLength() throws Exception {
        Map<String, List<String>> headers = Collections.singletonMap("Content-Length",
            Collections.singletonList(String.valueOf(HttpUtil.MAX_AIA_RESPONSE_SIZE_IN_BYTES + 1)));
        TestHttpURLConnection connection = new TestHttpURLConnection(200, new byte[] { 1 }, headers);

        HttpUtil.BinaryHttpResponse result
            = HttpUtil.getAiaBytesWithMetadata("https://example.test/cert.crt", ignored -> connection);

        assertNull(result.getBody());
        assertTrue(connection.disconnected);
    }

    @Test
    void binaryResponseRejectsStreamThatExceedsMaximumSize() throws Exception {
        byte[] body = new byte[HttpUtil.MAX_AIA_RESPONSE_SIZE_IN_BYTES + 1];
        TestHttpURLConnection connection = new TestHttpURLConnection(200, body, Collections.emptyMap());

        HttpUtil.BinaryHttpResponse result
            = HttpUtil.getAiaBytesWithMetadata("https://example.test/cert.crt", ignored -> connection);

        assertNull(result.getBody());
        assertTrue(connection.disconnected);
    }

    @Test
    void binaryResponseFollowsHttpToHttpsRedirect() throws Exception {
        String sourceUrl = "http://example.test/cert.crt";
        String targetUrl = "https://example.test/cert.crt";
        Map<String, List<String>> redirectHeaders
            = Collections.singletonMap("Location", Collections.singletonList(targetUrl));
        TestHttpURLConnection redirect = new TestHttpURLConnection(sourceUrl, 302, new byte[0], redirectHeaders);
        byte[] body = new byte[] { 1, 2, 3 };
        TestHttpURLConnection response = new TestHttpURLConnection(targetUrl, 200, body, Collections.emptyMap());

        HttpUtil.BinaryHttpResponse result = HttpUtil.getAiaBytesWithMetadata(sourceUrl, url -> {
            if (sourceUrl.equals(url)) {
                return redirect;
            }
            if (targetUrl.equals(url)) {
                return response;
            }
            throw new AssertionError("Unexpected redirect URL: " + url);
        });

        assertArrayEquals(body, result.getBody());
        assertFalse(redirect.getInstanceFollowRedirects());
        assertTrue(redirect.disconnected);
        assertTrue(response.disconnected);
    }

    @Test
    void binaryResponsePreservesPathForQueryOnlyRedirect() throws Exception {
        String sourceUrl = "https://example.test/certificates/issuer.crt?v=1";
        String targetUrl = "https://example.test/certificates/issuer.crt?v=2";
        Map<String, List<String>> redirectHeaders
            = Collections.singletonMap("Location", Collections.singletonList("?v=2"));
        TestHttpURLConnection redirect = new TestHttpURLConnection(sourceUrl, 302, new byte[0], redirectHeaders);
        byte[] body = new byte[] { 1, 2, 3 };
        TestHttpURLConnection response = new TestHttpURLConnection(targetUrl, 200, body, Collections.emptyMap());

        HttpUtil.BinaryHttpResponse result = HttpUtil.getAiaBytesWithMetadata(sourceUrl, url -> {
            if (sourceUrl.equals(url)) {
                return redirect;
            }
            if (targetUrl.equals(url)) {
                return response;
            }
            throw new AssertionError("Unexpected redirect URL: " + url);
        });

        assertArrayEquals(body, result.getBody());
        assertTrue(redirect.disconnected);
        assertTrue(response.disconnected);
    }

    private static final class TestHttpURLConnection extends HttpURLConnection {
        private final int status;
        private final byte[] body;
        private final Map<String, List<String>> headers;
        private final ByteArrayOutputStream requestBody = new ByteArrayOutputStream();
        private boolean disconnected;

        private TestHttpURLConnection(int status, byte[] body, Map<String, List<String>> headers) throws Exception {
            this("https://example.test/cert.crt", status, body, headers);
        }

        private TestHttpURLConnection(String url, int status, byte[] body, Map<String, List<String>> headers)
            throws Exception {
            super(new URL(url));
            this.status = status;
            this.body = body;
            this.headers = headers;
        }

        @Override
        public int getResponseCode() {
            return status;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(body);
        }

        @Override
        public OutputStream getOutputStream() {
            return requestBody;
        }

        @Override
        public Map<String, List<String>> getHeaderFields() {
            return headers;
        }

        @Override
        public String getHeaderField(String name) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name) && !entry.getValue().isEmpty()) {
                    return entry.getValue().get(0);
                }
            }
            return null;
        }

        @Override
        public void disconnect() {
            disconnected = true;
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public void connect() {
        }
    }
}
