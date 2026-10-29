// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.ACCEPT_ENCODING_KEY;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.ACCEPT_ENCODING_VALUE;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.DEFAULT_USER_AGENT_VALUE_PREFIX;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.VERSION;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated("Mutates JVM-wide HTTPS and security configuration")
public class HttpUtilTest {
    private static final String TRUST_MANAGER_FACTORY_ALGORITHM_PROPERTY = "ssl.TrustManagerFactory.algorithm";

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
        requestHeaders.put(ACCEPT_ENCODING_KEY, "identity");

        String result = HttpUtil.get("https://example.test/value", requestHeaders, ignored -> connection);

        assertEquals("response", result);
        assertEquals("value", connection.getRequestProperty("x-test"));
        assertEquals(HttpUtil.USER_AGENT_VALUE, connection.getRequestProperty(HttpUtil.USER_AGENT_KEY));
        assertEquals("identity", connection.getRequestProperty(ACCEPT_ENCODING_KEY));
        assertEquals(HttpUtil.HTTP_TIMEOUT_IN_MILLISECONDS, connection.getConnectTimeout());
        assertEquals(HttpUtil.HTTP_TIMEOUT_IN_MILLISECONDS, connection.getReadTimeout());
        assertTrue(connection.disconnected);
    }

    @ParameterizedTest
    @ValueSource(strings = { "gzip", "x-gzip", "deflate", "deflate-raw" })
    void textGetRequestsAndDecodesCompressedResponse(String encoding) throws Exception {
        byte[] body = compress("response".getBytes(StandardCharsets.UTF_8), encoding);
        String contentEncoding = "deflate-raw".equals(encoding) ? "deflate" : encoding;
        Map<String, List<String>> responseHeaders
            = Collections.singletonMap("Content-Encoding", Collections.singletonList(contentEncoding));
        TestHttpURLConnection connection
            = new TestHttpURLConnection("https://example.test/value", 200, body, responseHeaders);

        String result = HttpUtil.get("https://example.test/value", null, ignored -> connection);

        assertEquals("response", result);
        assertEquals(ACCEPT_ENCODING_VALUE, connection.getRequestProperty(ACCEPT_ENCODING_KEY));
        assertTrue(connection.disconnected);
    }

    @ParameterizedTest
    @ValueSource(strings = { "identity", "custom" })
    void textGetLeavesIdentityAndUnknownEncodingUnchanged(String encoding) throws Exception {
        byte[] body = "response".getBytes(StandardCharsets.UTF_8);
        Map<String, List<String>> responseHeaders
            = Collections.singletonMap("Content-Encoding", Collections.singletonList(encoding));
        TestHttpURLConnection connection
            = new TestHttpURLConnection("https://example.test/value", 200, body, responseHeaders);

        String result = HttpUtil.get("https://example.test/value", null, ignored -> connection);

        assertEquals("response", result);
        assertTrue(connection.disconnected);
    }

    @Test
    void textGetDecodesChainedContentEncodingsInReverseOrder() throws Exception {
        byte[] body = deflate(gzip("response".getBytes(StandardCharsets.UTF_8)), false);
        Map<String, List<String>> responseHeaders
            = Collections.singletonMap("Content-Encoding", Collections.singletonList("gzip, deflate"));
        TestHttpURLConnection connection
            = new TestHttpURLConnection("https://example.test/value", 200, body, responseHeaders);

        String result = HttpUtil.get("https://example.test/value", null, ignored -> connection);

        assertEquals("response", result);
        assertTrue(connection.disconnected);
    }

    @Test
    void textGetPreservesWireOrderForRepeatedContentEncodingHeaders() throws Exception {
        byte[] body = deflate(gzip("response".getBytes(StandardCharsets.UTF_8)), false);
        Map<String, List<String>> responseHeaders
            = Collections.singletonMap("Content-Encoding", Arrays.asList("gzip", "deflate"));
        TestHttpURLConnection connection
            = new TestHttpURLConnection("https://example.test/value", 200, body, responseHeaders);
        connection.reverseHeaderMapValues = true;

        String result = HttpUtil.get("https://example.test/value", null, ignored -> connection);

        assertEquals("response", result);
        assertTrue(connection.disconnected);
    }

    @Test
    void textGetClosesResponseAfterMalformedGzipHeader() throws Exception {
        Map<String, List<String>> responseHeaders
            = Collections.singletonMap("Content-Encoding", Collections.singletonList("gzip"));
        TestHttpURLConnection connection = new TestHttpURLConnection("https://example.test/value", 200,
            "not-gzip".getBytes(StandardCharsets.UTF_8), responseHeaders);

        String result = HttpUtil.get("https://example.test/value", null, ignored -> connection);

        assertNull(result);
        assertTrue(connection.inputStreamClosed);
        assertTrue(connection.disconnected);
    }

    @Test
    void compressedResponseChecksDeadlineWhileReadingEncodingHeader() throws Exception {
        Map<String, List<String>> responseHeaders
            = Collections.singletonMap("Content-Encoding", Collections.singletonList("gzip"));
        TestHttpURLConnection connection
            = new TestHttpURLConnection(200, gzip("response".getBytes(StandardCharsets.UTF_8)), responseHeaders);

        IOException exception = assertThrows(IOException.class, () -> HttpUtil.getDecodedResponseBody(connection,
            HttpUtil.MAX_AIA_RESPONSE_SIZE_IN_BYTES, System.nanoTime() - 1));

        assertEquals("AIA response exceeded the maximum download time.", exception.getMessage());
        assertTrue(connection.inputStreamClosed);
    }

    @Test
    void textGetThrowsForNonSuccessfulResponse() throws Exception {
        TestHttpURLConnection connection = new TestHttpURLConnection("https://example.test/value", 429,
            "{\"error\":\"throttled\"}".getBytes(StandardCharsets.UTF_8), Collections.emptyMap());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> HttpUtil.get("https://example.test/value", null, ignored -> connection));

        assertTrue(exception.getMessage().contains("HTTP status code was 429"));
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
    void binaryResponseRequestsAndDecodesGzipResponse() throws Exception {
        byte[] expectedBody = new byte[] { 1, 2, 3 };
        Map<String, List<String>> headers
            = Collections.singletonMap("Content-Encoding", Collections.singletonList("gzip"));
        TestHttpURLConnection connection = new TestHttpURLConnection(200, gzip(expectedBody), headers);

        HttpUtil.BinaryHttpResponse result
            = HttpUtil.getAiaBytesWithMetadata("https://example.test/cert.crt", ignored -> connection);

        assertArrayEquals(expectedBody, result.getBody());
        assertEquals(ACCEPT_ENCODING_VALUE, connection.getRequestProperty(ACCEPT_ENCODING_KEY));
        assertTrue(connection.disconnected);
    }

    @Test
    void connectionOpeningFailuresReturnNull() {
        String unsupportedUrl = "unsupported://example.test";

        assertNull(HttpUtil.get(unsupportedUrl, null));
        assertNull(HttpUtil.post(unsupportedUrl, null, "request", "application/json"));
        assertNull(HttpUtil.getWithOnlyResponseHeaders(unsupportedUrl));
    }

    @Test
    void httpsConnectionDoesNotInheritPermissiveGlobalHostnameVerifier() throws Exception {
        HostnameVerifier originalHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        HostnameVerifier permissiveHostnameVerifier = (hostname, session) -> true;
        HttpsURLConnection connection = null;

        try {
            HttpsURLConnection.setDefaultHostnameVerifier(permissiveHostnameVerifier);
            connection = (HttpsURLConnection) HttpUtil.openConnection("https://example.test");

            assertNotSame(permissiveHostnameVerifier, connection.getHostnameVerifier());
            assertFalse(connection.getHostnameVerifier().verify("example.test", null));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            HttpsURLConnection.setDefaultHostnameVerifier(originalHostnameVerifier);
        }
    }

    @Test
    void httpsRequestsFailWhenTrustManagerCannotBeCreated() {
        String originalAlgorithm = Security.getProperty(TRUST_MANAGER_FACTORY_ALGORITHM_PROPERTY);
        assertNotNull(originalAlgorithm);

        try {
            Security.setProperty(TRUST_MANAGER_FACTORY_ALGORITHM_PROPERTY, "MissingTrustManagerAlgorithm");

            IOException exception
                = assertThrows(IOException.class, () -> HttpUtil.openConnection("https://example.test"));

            assertEquals("Unable to build the SSL context.", exception.getMessage());
            assertTrue(exception.getCause() instanceof NoSuchAlgorithmException);
            assertNull(HttpUtil.get("https://example.test", null));
            assertEmptyBinaryResponse(HttpUtil.getAiaBytesWithMetadata("https://example.test/cert.crt"));
        } finally {
            Security.setProperty(TRUST_MANAGER_FACTORY_ALGORITHM_PROPERTY, originalAlgorithm);
        }
    }

    @Test
    void binaryResponseHandlesConnectionOpeningFailure() {
        HttpUtil.BinaryHttpResponse result
            = HttpUtil.getAiaBytesWithMetadata("https://example.test/cert.crt", ignored -> {
                throw new IOException("Connection failed");
            });

        assertEmptyBinaryResponse(result);
    }

    @Test
    void binaryResponseHandlesConnectionSecurityFailure() {
        HttpUtil.BinaryHttpResponse result
            = HttpUtil.getAiaBytesWithMetadata("https://example.test/cert.crt", ignored -> {
                throw new SecurityException("Connection blocked");
            });

        assertEmptyBinaryResponse(result);
    }

    @Test
    void binaryResponseDisconnectsAfterRuntimeFailure() throws Exception {
        TestHttpURLConnection connection = new TestHttpURLConnection(200, new byte[0], Collections.emptyMap(),
            new SecurityException("Response blocked"));

        HttpUtil.BinaryHttpResponse result
            = HttpUtil.getAiaBytesWithMetadata("https://example.test/cert.crt", ignored -> connection);

        assertEmptyBinaryResponse(result);
        assertTrue(connection.disconnected);
    }

    @Test
    void binaryResponseDoesNotCatchJvmErrors() {
        AssertionError error = assertThrows(AssertionError.class,
            () -> HttpUtil.getAiaBytesWithMetadata("https://example.test/cert.crt", ignored -> {
                throw new AssertionError("Fatal failure");
            }));

        assertEquals("Fatal failure", error.getMessage());
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
        byte[] body = new byte[HttpUtil.MAX_AIA_RESPONSE_SIZE_IN_BYTES + 4096];
        TestHttpURLConnection connection = new TestHttpURLConnection(200, body, Collections.emptyMap());

        HttpUtil.BinaryHttpResponse result
            = HttpUtil.getAiaBytesWithMetadata("https://example.test/cert.crt", ignored -> connection);

        assertNull(result.getBody());
        assertEquals(HttpUtil.MAX_AIA_RESPONSE_SIZE_IN_BYTES + 1, connection.inputStreamBytesRead);
        assertTrue(connection.disconnected);
    }

    @Test
    void binaryResponseRejectsGzipBodyWhoseDecodedSizeExceedsMaximum() throws Exception {
        byte[] oversizedBody = new byte[HttpUtil.MAX_AIA_RESPONSE_SIZE_IN_BYTES + 1];
        Map<String, List<String>> headers
            = Collections.singletonMap("Content-Encoding", Collections.singletonList("gzip"));
        TestHttpURLConnection connection = new TestHttpURLConnection(200, gzip(oversizedBody), headers);

        HttpUtil.BinaryHttpResponse result
            = HttpUtil.getAiaBytesWithMetadata("https://example.test/cert.crt", ignored -> connection);

        assertEmptyBinaryResponse(result);
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

    private static void assertEmptyBinaryResponse(HttpUtil.BinaryHttpResponse response) {
        assertNull(response.getBody());
        assertNull(response.getCacheControl());
        assertNull(response.getDate());
        assertNull(response.getAge());
        assertNull(response.getExpires());
    }

    private static byte[] gzip(byte[] value) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipStream = new GZIPOutputStream(outputStream)) {
            gzipStream.write(value);
        }
        return outputStream.toByteArray();
    }

    private static byte[] compress(byte[] value, String encoding) throws IOException {
        if ("gzip".equals(encoding) || "x-gzip".equals(encoding)) {
            return gzip(value);
        }

        return deflate(value, "deflate-raw".equals(encoding));
    }

    private static byte[] deflate(byte[] value, boolean raw) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, raw);
        try (DeflaterOutputStream deflateStream = new DeflaterOutputStream(outputStream, deflater)) {
            deflateStream.write(value);
        } finally {
            deflater.end();
        }
        return outputStream.toByteArray();
    }

    private static final class TestHttpURLConnection extends HttpURLConnection {
        private final int status;
        private final byte[] body;
        private final Map<String, List<String>> headers;
        private final RuntimeException responseFailure;
        private final ByteArrayOutputStream requestBody = new ByteArrayOutputStream();
        private boolean disconnected;
        private boolean inputStreamClosed;
        private boolean reverseHeaderMapValues;
        private int inputStreamBytesRead;

        private TestHttpURLConnection(int status, byte[] body, Map<String, List<String>> headers) throws Exception {
            this("https://example.test/cert.crt", status, body, headers, null);
        }

        private TestHttpURLConnection(int status, byte[] body, Map<String, List<String>> headers,
            RuntimeException responseFailure) throws Exception {
            this("https://example.test/cert.crt", status, body, headers, responseFailure);
        }

        private TestHttpURLConnection(String url, int status, byte[] body, Map<String, List<String>> headers)
            throws Exception {
            this(url, status, body, headers, null);
        }

        private TestHttpURLConnection(String url, int status, byte[] body, Map<String, List<String>> headers,
            RuntimeException responseFailure) throws Exception {
            super(new URL(url));
            this.status = status;
            this.body = body;
            this.headers = headers;
            this.responseFailure = responseFailure;
        }

        @Override
        public int getResponseCode() {
            if (responseFailure != null) {
                throw responseFailure;
            }
            return status;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(body) {
                @Override
                public synchronized int read() {
                    int value = super.read();
                    if (value != -1) {
                        inputStreamBytesRead++;
                    }
                    return value;
                }

                @Override
                public synchronized int read(byte[] buffer, int offset, int length) {
                    int read = super.read(buffer, offset, length);
                    if (read > 0) {
                        inputStreamBytesRead += read;
                    }
                    return read;
                }

                @Override
                public void close() throws IOException {
                    inputStreamClosed = true;
                    super.close();
                }
            };
        }

        @Override
        public OutputStream getOutputStream() {
            return requestBody;
        }

        @Override
        public Map<String, List<String>> getHeaderFields() {
            if (reverseHeaderMapValues) {
                Map<String, List<String>> reversedHeaders = new LinkedHashMap<>();
                headers.forEach((name, values) -> {
                    List<String> reversedValues = new ArrayList<>(values);
                    Collections.reverse(reversedValues);
                    reversedHeaders.put(name, reversedValues);
                });
                return reversedHeaders;
            }
            return headers;
        }

        @Override
        public String getHeaderFieldKey(int index) {
            return getIndexedHeader(index, true);
        }

        @Override
        public String getHeaderField(int index) {
            return index == 0 ? "HTTP/1.1 " + status : getIndexedHeader(index, false);
        }

        private String getIndexedHeader(int index, boolean returnName) {
            int currentIndex = 1;
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                for (String headerValue : entry.getValue()) {
                    if (currentIndex++ == index) {
                        return returnName ? entry.getKey() : headerValue;
                    }
                }
            }
            return null;
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
