// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.client;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.JdkHttpClientBuilder;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.ContentType;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.shared.InsecureTrustManager;
import io.clientcore.core.shared.LocalTestServer;
import io.clientcore.core.utils.TestUtils;
import org.conscrypt.Conscrypt;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static io.clientcore.core.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link JdkHttpClient}.
 * <p>
 * Now that the default HttpClient, and related code, are using multi-release JARs this must be an integration test as
 * the full JAR must be available to use the multi-release code.
 */
@DisabledForJreRange(max = JRE.JAVA_11)
@Execution(ExecutionMode.SAME_THREAD)
public class JdkHttpClientIT {
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
            } else if (get && SSE_RESPONSE.equals(path)) {
                if (req.getHeader("Last-Event-Id") != null) {
                    sendSSELastEventIdResponse(resp);
                } else {
                    sendSSEResponseWithRetry(resp);
                }
            } else if (post && SSE_RESPONSE.equals(path)) {
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

    private static void sendSSEResponseWithDataOnly(org.eclipse.jetty.server.Response resp) throws IOException {
        resp.addHeader("Content-Type", ContentType.TEXT_EVENT_STREAM);
        resp.getOutputStream().write(("data: YHOO\ndata: +2\ndata: 10\n\n").getBytes());
        resp.flushBuffer();
    }

    private static String addServerSentEventWithRetry() {
        return ": test stream\ndata: first event\nid: 1\nretry: 100\n\n"
            + "data: This is the second message, it\ndata: has two lines.\nid: 2\n\ndata:  third event";
    }

    private static void sendSSEResponseWithRetry(org.eclipse.jetty.server.Response resp) throws IOException {
        resp.addHeader("Content-Type", ContentType.TEXT_EVENT_STREAM);
        resp.getOutputStream().write(addServerSentEventWithRetry().getBytes());
        resp.flushBuffer();
    }

    private static String addServerSentEventLast() {
        return "data: This is the second message, it\ndata: has two lines.\nid: 2\n\ndata:  third event";
    }

    private static void sendSSELastEventIdResponse(org.eclipse.jetty.server.Response resp) throws IOException {
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
    public void testFlowableWhenServerReturnsBodyAndNoErrorsWhenHttp500Returned() throws IOException {
        HttpClient client = new JdkHttpClientBuilder().build();

        try (Response<BinaryData> response = doRequest(client, "/error")) {
            assertEquals(500, response.getStatusCode());

            String responseBodyAsString = response.getValue().toString();

            assertTrue(responseBodyAsString.contains("error"));
        }

    }

    @Test
    public void testConcurrentRequests() throws InterruptedException {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new JdkHttpClientBuilder().build();

        ForkJoinPool pool = new ForkJoinPool();
        List<Callable<Void>> requests = new ArrayList<>(numRequests);
        for (int i = 0; i < numRequests; i++) {
            requests.add(() -> {
                try (Response<BinaryData> response = doRequest(client, "/long")) {
                    byte[] body = response.getValue().toBytes();
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
    public void validateHeadersReturnAsIs() throws IOException {
        HttpClient client = new JdkHttpClientBuilder().build();

        HttpHeaderName singleValueHeaderName = HttpHeaderName.fromString("singleValue");
        final String singleValueHeaderValue = "value";

        HttpHeaderName multiValueHeaderName = HttpHeaderName.fromString("Multi-value");
        final List<String> multiValueHeaderValue = Arrays.asList("value1", "value2");

        HttpHeaders headers = new HttpHeaders().set(singleValueHeaderName, singleValueHeaderValue)
            .set(multiValueHeaderName, multiValueHeaderValue);

        try (Response<?> response = client.send(new HttpRequest().setMethod(HttpMethod.GET)
            .setUri(uri(server, RETURN_HEADERS_AS_IS_PATH))
            .setHeaders(headers))) {
            assertEquals(200, response.getStatusCode());

            HttpHeaders responseHeaders = response.getHeaders();
            HttpHeader singleValueHeader = responseHeaders.get(singleValueHeaderName);

            assertEquals(singleValueHeaderName.getCaseInsensitiveName(), singleValueHeader.getName().toString());
            assertEquals(singleValueHeaderValue, singleValueHeader.getValue());

            HttpHeader multiValueHeader = responseHeaders.get(multiValueHeaderName);

            assertEquals(multiValueHeaderName.getCaseInsensitiveName(), multiValueHeader.getName().toString());
            assertEquals(multiValueHeaderValue.size(), multiValueHeader.getValues().size());
            assertTrue(multiValueHeaderValue.containsAll(multiValueHeader.getValues()));
        }
    }

    @Test
    public void testBufferedResponse() throws IOException {
        HttpClient client = new JdkHttpClientBuilder().build();

        try (Response<BinaryData> response = getResponse(client, "/short", RequestContext.none())) {
            assertArraysEqual(SHORT_BODY, response.getValue().toBytes());
        }
    }

    @Test
    public void testEmptyBufferResponse() throws IOException {
        HttpClient client = new JdkHttpClientBuilder().build();

        try (Response<BinaryData> response = getResponse(client, "/empty", RequestContext.none())) {
            assertEquals(0L, response.getValue().toBytes().length);
        }
    }

    @Test
    public void testRequestBodyPost() throws IOException {
        HttpClient client = new JdkHttpClientBuilder().build();
        String contentChunk = "abcdefgh";
        int repetitions = 1000;
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST).setUri(uri(server, "/shortPost"));
        request.getHeaders()
            .set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentChunk.length() * (repetitions + 1)));
        request.setBody(BinaryData.fromString(contentChunk));

        try (Response<BinaryData> response = client.send(request)) {
            assertArraysEqual(SHORT_BODY, response.getValue().toBytes());
        }
    }

    @Test
    public void testCustomSslSocketFactory() throws IOException, GeneralSecurityException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2", Conscrypt.newProvider());

        // Initialize the SSL context with a trust manager that trusts all certificates.
        sslContext.init(null, new TrustManager[] { new InsecureTrustManager() }, null);

        HttpClient httpClient = new JdkHttpClientBuilder().sslContext(sslContext).build();

        try (Response<BinaryData> response
            = httpClient.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(httpsUri(server, "/short")))) {
            TestUtils.assertArraysEqual(SHORT_BODY, response.getValue().toBytes());
        }
    }

    private static Response<BinaryData> getResponse(HttpClient client, String path, RequestContext context)
        throws IOException {
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri(server, path)).setContext(context);

        return client.send(request);
    }

    static URI uri(LocalTestServer server, String path) {
        return URI.create(server.getHttpUri() + path);
    }

    static URI httpsUri(LocalTestServer server, String path) {
        return URI.create(server.getHttpsUri() + path);
    }

    private static byte[] createLongBody() {
        byte[] duplicateBytes = "abcdefghijk".getBytes(StandardCharsets.UTF_8);
        byte[] longBody = new byte[duplicateBytes.length * 100000];

        for (int i = 0; i < 100000; i++) {
            System.arraycopy(duplicateBytes, 0, longBody, i * duplicateBytes.length, duplicateBytes.length);
        }

        return longBody;
    }

    private static Response<BinaryData> doRequest(HttpClient client, String path) throws IOException {
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri(server, path));

        return client.send(request);
    }
}
