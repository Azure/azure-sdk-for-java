// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client.httpurlconnection;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.Context;
import com.generic.core.models.Header;
import com.generic.core.models.Headers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import javax.servlet.ServletException;
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

import static com.generic.core.CoreTestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class HttpUrlConnectionClientTest {
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
            } else if (get && "/empty".equals(path)) {
                resp.setContentType("application/octet-stream");
                resp.setContentLength(0);
            } else if (get && "/connectionClose".equals(path)) {
                resp.getHttpChannel().getConnection().close();
            } else {
                throw new ServletException("Unexpected request " + req.getMethod() + " " + path);
            }
        });

        server.start();
    }

    @AfterAll
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testFlowableWhenServerReturnsBodyAndNoErrorsWhenHttp500Returned() {
        HttpClient client = new HttpUrlConnectionClientBuilder().build();

        try (HttpResponse response = doRequest(client, "/error")) {
            assertEquals(500, response.getStatusCode());
            String responseBodyAsString = response.getBody().toString();
            assertTrue(responseBodyAsString.contains("error"));
        }

    }

    @Test
    public void testConcurrentRequests() throws InterruptedException {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new HttpUrlConnectionClientBuilder().build();

        ForkJoinPool pool = new ForkJoinPool();
        List<Callable<Void>> requests = new ArrayList<>(numRequests);
        for (int i = 0; i < numRequests; i++) {
            requests.add(() -> {
                try (HttpResponse response = doRequest(client, "/error")) {
                    byte[] body = response.getBody().toBytes();
                    assertArraysEqual(LONG_BODY, body);
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
        HttpClient client = new HttpUrlConnectionClientBuilder().build();

        HttpHeaderName singleValueHeaderName = HttpHeaderName.fromString("singleValue");
        final String singleValueHeaderValue = "value";

        HttpHeaderName multiValueHeaderName = HttpHeaderName.fromString("Multi-value");
        final List<String> multiValueHeaderValue = Arrays.asList("value1", "value2");

        Headers headers = new Headers()
            .set(singleValueHeaderName, singleValueHeaderValue)
            .set(multiValueHeaderName, multiValueHeaderValue);

        try (HttpResponse response = client.send(new HttpRequest(HttpMethod.GET, url(server, RETURN_HEADERS_AS_IS_PATH),
            headers, null), Context.NONE)) {
            assertEquals(200, response.getStatusCode());
            Headers responseHeaders = response.getHeaders();
            Header singleValueHeader = responseHeaders.get(singleValueHeaderName);
            assertEquals(singleValueHeaderName.getCaseSensitiveName(), singleValueHeader.getName());
            assertEquals(singleValueHeaderValue, singleValueHeader.getValue());

            Header multiValueHeader = responseHeaders.get(multiValueHeaderName);
            assertEquals(multiValueHeaderName.getCaseSensitiveName(), multiValueHeader.getName());
            assertLinesMatch(multiValueHeaderValue, multiValueHeader.getValuesList());
        }
    }

    @Test
    public void testBufferedResponse() {
        HttpClient client = new HttpUrlConnectionClientBuilder().build();

        try (HttpResponse response = getResponse(client, "/short", Context.NONE)) {
            assertArrayEquals(SHORT_BODY, response.getBody().toBytes());
        }
    }

    @Test
    public void testEmptyBufferResponse() {
        HttpClient client = new HttpUrlConnectionClientBuilder().build();

        try (HttpResponse response = getResponse(client,"/empty", Context.NONE)) {
            assertEquals(0L, response.getBody().getLength());
        }
    }

    @Test
    public void testRequestBodyPost() {
        HttpClient client = new HttpUrlConnectionClientBuilder().build();
        String contentChunk = "abcdefgh";
        int repetitions = 1000;
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
            .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentChunk.length() * (repetitions + 1)))
            .setBody(contentChunk);

        try (HttpResponse response = client.send(request, Context.NONE)) {
            assertArrayEquals(SHORT_BODY, response.getBody().toBytes());
        }
    }

    private static HttpResponse getResponse(HttpClient client, String path, Context context) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        return client.send(request, context);
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
        HttpClient client = new HttpUrlConnectionClientBuilder().build();
        byte[] response = doRequest(client, path).getBody().toBytes();
        assertArrayEquals(expectedBody, response);
    }


    private static HttpResponse doRequest(HttpClient client, String path) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        return client.send(request, Context.NONE);
    }
}
