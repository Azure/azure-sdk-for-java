// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.Header;
import com.generic.core.models.HeaderName;
import com.generic.core.models.Headers;
import com.generic.core.shared.LocalTestServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import javax.servlet.ServletException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class OkHttpHttpClientTests {
    static final String RETURN_HEADERS_AS_IS_PATH = "/returnHeadersAsIs";

    private static final byte[] SHORT_BODY = "hi there".getBytes(StandardCharsets.UTF_8);
    private static final byte[] LONG_BODY = createLongBody();

    private static LocalTestServer server;

    @BeforeAll
    public static void startTestServer() {
        server = new LocalTestServer((req, resp, requestBody) -> {
            String path = req.getServletPath();
            boolean get = "GET".equalsIgnoreCase(req.getMethod());
            boolean post = "POST".equalsIgnoreCase(req.getMethod());

            if (get && "/short".equals(path)) {
                resp.setContentType("application/octet-stream");
                resp.setContentLength(SHORT_BODY.length);
                resp.getOutputStream().write(SHORT_BODY);
            } else if (get && "/long".equals(path)) {
                resp.setContentType("application/octet-stream");
                resp.setContentLength(LONG_BODY.length);
                resp.getOutputStream().write(LONG_BODY);
            } else if (get && "/error".equals(path)) {
                resp.setStatus(500);
                resp.setContentLength(5);
                resp.getOutputStream().write("error".getBytes(StandardCharsets.UTF_8));
            } else if (post && "/shortPost".equals(path)) {
                resp.setContentType("application/octet-stream");
                resp.setContentLength(SHORT_BODY.length);
                resp.getOutputStream().write(SHORT_BODY);
            } else if (get && RETURN_HEADERS_AS_IS_PATH.equals(path)) {
                List<String> headerNames = Collections.list(req.getHeaderNames());

                headerNames.forEach(headerName -> {
                    List<String> headerValues = Collections.list(req.getHeaders(headerName));
                    headerValues.forEach(headerValue -> resp.addHeader(headerName, headerValue));
                });
            } else if (get && "/connectionClose".equals(path)) {
                resp.getHttpChannel().getConnection().close();
            } else {
                throw new ServletException("Unexpected request: " + req.getMethod() + " " + path);
            }
        }, 20);

        server.start();
    }

    @AfterAll
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testFlowableResponseShortBodyAsByteArrayAsync() {
        checkBodyReceived(SHORT_BODY, "/short");
    }

    @Test
    public void testFlowableResponseLongBodyAsByteArrayAsync() {
        checkBodyReceived(LONG_BODY, "/long");
    }

    @Test
    public void testServerShutsDownSocketShouldPushErrorToContentFlowable() {
        HttpClient client = new OkHttpHttpClientProvider().createInstance();
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, "/connectionClose"));

        assertThrows(UncheckedIOException.class, () -> client.send(request).getBody().toBytes());
    }

    @Test
    public void testConcurrentRequests() throws InterruptedException {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new OkHttpHttpClientProvider().createInstance();

        ForkJoinPool pool = new ForkJoinPool();
        List<Callable<Void>> requests = new ArrayList<>(numRequests);

        for (int i = 0; i < numRequests; i++) {
            requests.add(() -> {
                try (HttpResponse response = doRequest(client, "/long")) {
                    byte[] body = response.getBody().toBytes();
                    TestUtils.assertArraysEqual(LONG_BODY, body);

                    return null;
                }
            });
        }

        pool.invokeAll(requests);
        pool.shutdown();

        assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS));
    }

    @Test
    public void validateHeadersReturnAsIs() {
        HttpClient client = new OkHttpHttpClientProvider().createInstance();
        HeaderName singleValueHeaderName = HeaderName.fromString("singleValue");
        final String singleValueHeaderValue = "value";
        HeaderName multiValueHeaderName = HeaderName.fromString("Multi-value");
        final List<String> multiValueHeaderValue = Arrays.asList("value1", "value2");

        Headers headers = new Headers().set(singleValueHeaderName, singleValueHeaderValue)
            .set(multiValueHeaderName, multiValueHeaderValue);

        HttpResponse response = client.send(
            new HttpRequest(HttpMethod.GET, url(server, RETURN_HEADERS_AS_IS_PATH)).setHeaders(headers));

        assertEquals(200, response.getStatusCode());

        Headers responseHeaders = response.getHeaders();
        Header singleValueHeader = responseHeaders.get(singleValueHeaderName);

        assertEquals(singleValueHeaderName.getCaseSensitiveName(), singleValueHeader.getName());
        assertEquals(singleValueHeaderValue, singleValueHeader.getValue());

        Header multiValueHeader = responseHeaders.get(multiValueHeaderName);

        assertEquals(multiValueHeaderName.getCaseSensitiveName(), multiValueHeader.getName());
        assertLinesMatch(multiValueHeaderValue, multiValueHeader.getValuesList());
    }

    static URL url(LocalTestServer server, String path) {
        try {
            return new URI(server.getHttpUri() + path).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] createLongBody() {
        byte[] duplicateBytes = "abcdefghijk".getBytes(StandardCharsets.UTF_8);
        byte[] longBody = new byte[duplicateBytes.length * 100000];

        for (int i = 0; i < 100000; i++) {
            System.arraycopy(duplicateBytes, 0, longBody, i * duplicateBytes.length, duplicateBytes.length);
        }

        return longBody;
    }

    private static void checkBodyReceived(byte[] expectedBody, String path) {
        HttpClient client = new OkHttpHttpClientBuilder().build();
        byte[] bytes = doRequest(client, path).getBody().toBytes();

        assertArrayEquals(expectedBody, bytes);
    }

    private static HttpResponse doRequest(HttpClient client, String path) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));

        return client.send(request);
    }
}
