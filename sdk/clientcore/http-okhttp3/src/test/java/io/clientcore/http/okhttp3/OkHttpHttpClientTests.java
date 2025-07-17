// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.shared.LocalTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static io.clientcore.http.okhttp3.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
        server = new LocalTestServer(HttpProtocolVersion.HTTP_1_1, false, (req, resp, requestBody) -> {
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
    public void testFlowableResponseShortBodyAsByteArrayAsync() {
        checkBodyReceived(SHORT_BODY, "/short");
    }

    @Test
    public void testFlowableResponseLongBodyAsByteArrayAsync() {
        checkBodyReceived(LONG_BODY, "/long");
    }

    @Test
    public void testServerShutsDownSocketShouldPushErrorToContentFlowable() {
        HttpClient client = new OkHttpHttpClientProvider().getSharedInstance();
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri(server, "/connectionClose"));

        CoreException exception = assertThrows(CoreException.class, () -> client.send(request).getValue().toBytes());
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    public void testConcurrentRequests() throws InterruptedException {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new OkHttpHttpClientProvider().getSharedInstance();

        ForkJoinPool pool = new ForkJoinPool((int) Math.ceil(Runtime.getRuntime().availableProcessors() / 2.0));
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
    public void validateHeadersReturnAsIs() {
        HttpClient client = new OkHttpHttpClientProvider().getSharedInstance();
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

            assertEquals(singleValueHeaderName.getCaseSensitiveName(), singleValueHeader.getName().toString());
            assertEquals(singleValueHeaderValue, singleValueHeader.getValue());

            HttpHeader multiValueHeader = responseHeaders.get(multiValueHeaderName);

            assertEquals(multiValueHeaderName.getCaseSensitiveName(), multiValueHeader.getName().toString());
            assertLinesMatch(multiValueHeaderValue, multiValueHeader.getValues());
        }
    }

    static URI uri(LocalTestServer server, String path) {
        try {
            return new URI(server.getUri() + path);
        } catch (URISyntaxException e) {
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
        try (Response<BinaryData> response = doRequest(client, path)) {
            byte[] bytes = response.getValue().toBytes();

            assertArraysEqual(expectedBody, bytes);
        }
    }

    private static Response<BinaryData> doRequest(HttpClient client, String path) {
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri(server, path));

        return client.send(request);
    }
}
