// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.models.ServerSentEvent;
import com.generic.core.http.models.ServerSentEventListener;
import com.generic.core.implementation.http.ContentType;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Context;
import com.generic.core.models.Header;
import com.generic.core.models.HeaderName;
import com.generic.core.models.Headers;
import com.generic.core.shared.LocalTestServer;

import org.eclipse.jetty.server.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static com.generic.core.util.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Execution(ExecutionMode.SAME_THREAD)
public class DefaultHttpClientTest {
    static final String RETURN_HEADERS_AS_IS_PATH = "/returnHeadersAsIs";
    private static final byte[] SHORT_BODY = "hi there".getBytes(StandardCharsets.UTF_8);
    private static final byte[] LONG_BODY = createLongBody();
    private static LocalTestServer server;
    private static final String SSE_RESPONSE = "/serversentevent";

    @BeforeAll
    public static void startTestServer() {
        server = new LocalTestServer((req, resp, requestBody) -> {
            String path = req.getServletPath();
            boolean get = "GET".equalsIgnoreCase(req.getMethod());
            boolean post = "POST".equalsIgnoreCase(req.getMethod());
            boolean put = "PUT".equalsIgnoreCase(req.getMethod());

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
            }  else if (get && SSE_RESPONSE.equals(path)) {
                if (req.getHeader("Last-Event-Id") != null) {
                    sendSSELastEventIdResponse(resp);
                } else {
                    sendSSEResponseWithRetry(resp);
                }
            }  else if (post && SSE_RESPONSE.equals(path)) {
                sendSSEResponseWithDataOnly(resp);
            } else if (put && SSE_RESPONSE.equals(path)) {
                resp.addHeader("Content-Type", ContentType.TEXT_EVENT_STREAM);
                resp.setStatus(200);
                resp.getOutputStream().write(("msg hello world \n\n").getBytes());
                resp.flushBuffer();
            } else {
                throw new ServletException("Unexpected request " + req.getMethod() + " " + path);
            }
        });

        server.start();
    }

    private static void sendSSEResponseWithDataOnly(Response resp) throws IOException {
            resp.addHeader("Content-Type", ContentType.TEXT_EVENT_STREAM);
            resp.getOutputStream().write(("data: YHOO\n" +
                            "data: +2\n" +
                            "data: 10").getBytes());
            resp.flushBuffer();
        }

    private static String addServerSentEventWithRetry() {
        return ": test stream\n" +
            "data: first event\n" +
            "id: 1\n" +
            "retry: 100\n\n" +
            "data: This is the second message, it\n" +
            "data: has two lines.\n" +
            "id: 2\n\n" +
            "data:  third event";
    }

    private static void sendSSEResponseWithRetry(Response resp)
        throws IOException {
        resp.addHeader("Content-Type", ContentType.TEXT_EVENT_STREAM);
        resp.getOutputStream().write(addServerSentEventWithRetry().getBytes());
        resp.flushBuffer();
    }

    private static String addServerSentEventLast() {
        return "data: This is the second message, it\n" +
            "data: has two lines.\n" +
            "id: 2\n\n" +
            "data:  third event";
    }

    private static void sendSSELastEventIdResponse(Response resp)
        throws IOException {
        resp.addHeader("Content-Type", ContentType.TEXT_EVENT_STREAM);
        resp.getOutputStream().write(addServerSentEventLast().getBytes());
        resp.flushBuffer();
    }

    @AfterAll
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testFlowableWhenServerReturnsBodyAndNoErrorsWhenHttp500Returned() {
        HttpClient client = new DefaultHttpClientBuilder().build();

        try (HttpResponse response = doRequest(client, "/error")) {
            assertEquals(500, response.getStatusCode());
            String responseBodyAsString = response.getBody().toString();
            assertTrue(responseBodyAsString.contains("error"));
        }

    }

    @Test
    public void testConcurrentRequests() throws InterruptedException {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new DefaultHttpClientBuilder().build();

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
        HttpClient client = new DefaultHttpClientBuilder().build();

        HeaderName singleValueHeaderName = HeaderName.fromString("singleValue");
        final String singleValueHeaderValue = "value";

        HeaderName multiValueHeaderName = HeaderName.fromString("Multi-value");
        final List<String> multiValueHeaderValue = Arrays.asList("value1", "value2");

        Headers headers = new Headers()
            .set(singleValueHeaderName, singleValueHeaderValue)
            .set(multiValueHeaderName, multiValueHeaderValue);

        try (HttpResponse response =
                 client.send(new HttpRequest(HttpMethod.GET, url(server, RETURN_HEADERS_AS_IS_PATH))
                     .setHeaders(headers))) {
            assertEquals(200, response.getStatusCode());

            Headers responseHeaders = response.getHeaders();
            Header singleValueHeader = responseHeaders.get(singleValueHeaderName);

            assertEquals(singleValueHeaderName.getCaseSensitiveName(), singleValueHeader.getName());
            assertEquals(singleValueHeaderValue, singleValueHeader.getValue());

            Header multiValueHeader = responseHeaders.get(multiValueHeaderName);

            assertEquals(multiValueHeaderName.getCaseSensitiveName(), multiValueHeader.getName());
            assertEquals(multiValueHeaderValue.size(), multiValueHeader.getValuesList().size());
            assertTrue(multiValueHeaderValue.containsAll(multiValueHeader.getValuesList()));
        }
    }

    @Test
    public void testBufferedResponse() {
        HttpClient client = new DefaultHttpClientBuilder().build();

        try (HttpResponse response = getResponse(client, "/short", Context.NONE)) {
            assertArrayEquals(SHORT_BODY, response.getBody().toBytes());
        }
    }

    @Test
    public void testEmptyBufferResponse() {
        HttpClient client = new DefaultHttpClientBuilder().build();

        try (HttpResponse response = getResponse(client, "/empty", Context.NONE)) {
            assertEquals(0L, response.getBody().getLength());
        }
    }

    @Test
    public void testRequestBodyPost() {
        HttpClient client = new DefaultHttpClientBuilder().build();
        String contentChunk = "abcdefgh";
        int repetitions = 1000;
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
            .setHeader(HeaderName.CONTENT_LENGTH, String.valueOf(contentChunk.length() * (repetitions + 1)))
            .setBody(contentChunk);

        try (HttpResponse response = client.send(request)) {
            assertArrayEquals(SHORT_BODY, response.getBody().toBytes());
        }
    }

    @Test
    public void canReceiveServerSentEvents() {
        final int[] i = {0};
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, SSE_RESPONSE))
            .setServerSentEventListener(sse -> {
                String expected;
                Long id;
                if (i[0] == 0) {
                    expected = "first event";
                    id = 1L;
                    Assertions.assertEquals("test stream", sse.getComment());
                } else {
                    expected = "This is the second message, it\n" +
                        "has two lines.";
                    id = 2L;
                }
                Assertions.assertEquals(expected, sse.getData());
                Assertions.assertEquals(id, sse.getId());
                if (++i[0] > 2) {
                    assertFalse(true, "Should not have received more than two messages.");
                }
            });

        createHttpClient().send(request);
        assertEquals(2, i[0]);
    }

    /**
     * Tests that eagerly converting implementation HTTP headers to azure-core Headers is done.
     */
    @Test
    public void canRecognizeServerSentEvent() {
        BinaryData requestBody = BinaryData.fromString("test body");
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, SSE_RESPONSE)).setBody(requestBody);
        request.getMetadata().setEagerlyConvertHeaders(false);

        // Rec
        createHttpClient().send(request.setServerSentEventListener(sse -> assertEquals("YHOO\\n+2\\n10", sse.getData())));
    }

    @Test
    public void onErrorServerSentEvents() {
        final int[] i = {0};
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, SSE_RESPONSE))
            .setServerSentEventListener(new ServerSentEventListener() {
                @Override
                public void onEvent(ServerSentEvent sse) throws IOException {
                    throw new IOException("test exception");
                }

                @Override
                public void onError(Throwable throwable) {
                    assertEquals("test exception", throwable.getMessage());
                    i[0]++;
                }
            });

        createHttpClient().send(request);
        assertEquals(1, i[0]);
    }

    @Test
    public void onRetryWithLastEventIdReceiveServerSentEvents() {
        final int[] i = {0};
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, SSE_RESPONSE))
            .setServerSentEventListener(new ServerSentEventListener() {
                @Override
                public void onEvent(ServerSentEvent sse) throws IOException {
                    String expected;
                    if (++i[0] == 1) {
                        expected = "first event";
                        assertEquals("test stream", sse.getComment());
                        assertEquals(Duration.ofMillis(100L), sse.getRetryAfter());
                        assertEquals(expected, sse.getData());
                        assertEquals(1, sse.getId());
                        throw new IOException("test exception");
                    } else {
                        expected = "This is the second message, it\n" +
                            "has two lines.";
                        assertTimeout(Duration.ofMillis(100L), () -> assertEquals(2, sse.getId()));
                        assertEquals(expected, sse.getData());
                    }
                    if (++i[0] > 3) {
                        fail("Should not have received more than two messages.");
                    }
                }

                @Override
                public boolean shouldRetry(Throwable throwable, Duration retryAfter, long lastEventId) {
                    return true;
                }
            });

        createHttpClient().send(request);
        assertEquals(3, i[0]);
    }

    /**
     * Test throws IllegalArgumentException for invalid data stream.
     */
    @Test
    public void throwsIllegalArgumentExceptionForInvalidSSEData() {
        BinaryData requestBody = BinaryData.fromString("test body");
        HttpRequest request = new HttpRequest(HttpMethod.PUT, url(server, SSE_RESPONSE)).setBody(requestBody);

        assertThrows(IllegalArgumentException.class, () -> createHttpClient().send(request.setServerSentEventListener(sse -> {})));
    }

    private static HttpResponse getResponse(HttpClient client, String path, Context context) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        request.getMetadata().setContext(context);

        return client.send(request);
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
        HttpClient client = new DefaultHttpClientBuilder().build();
        byte[] response = doRequest(client, path).getBody().toBytes();
        assertArrayEquals(expectedBody, response);
    }


    private static HttpResponse doRequest(HttpClient client, String path) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));

        return client.send(request);
    }

    private HttpClient createHttpClient() {
        return new DefaultHttpClientBuilder().build();
    }
}
