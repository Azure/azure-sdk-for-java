// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client.httpurlconnection;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import javax.servlet.ServletException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Execution(ExecutionMode.SAME_THREAD)
public class HttpUrlConnectionClientTest {
    static final String RETURN_HEADERS_AS_IS_PATH = "/returnHeadersAsIs";
    private static final byte[] SHORT_BODY = "hi there".getBytes(StandardCharsets.UTF_8);
//    private static final byte[] LONG_BODY = createLongBody();
    private static LocalTestServer server;

//    private static final StepVerifierOptions EMPTY_INITIAL_REQUEST_OPTIONS = StepVerifierOptions.create()
//        .initialRequest(0);

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
//                resp.setContentLength(LONG_BODY.length);
//                resp.getOutputStream().write(LONG_BODY);
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

//    @Test
//    public void testFlowableResponseShortBodyAsByteArrayAsync() {
//        checkBodyReceived(SHORT_BODY, "/short");
//    }
//
//    @Test
//    public void testFlowableResponseLongBodyAsByteArrayAsync() {
//        checkBodyReceived(LONG_BODY, "/long");
//    }

//    @Test
//    public void responseBodyAsStringAsyncWithCharset() {
//        HttpClient client = new HttpUrlConnectionClientProvider().createInstance();
//        StepVerifier.create(doRequest(client, "/short").flatMap(HttpResponse::getBodyAsByteArray))
//            .assertNext(result -> assertArrayEquals(SHORT_BODY, result))
//            .verifyComplete();
//    }
//
//    @Test
//    public void testFlowableWhenServerReturnsBodyAndNoErrorsWhenHttp500Returned() {
//        HttpResponse response = getResponse("/error").block();
//        assertEquals(500, response.getStatusCode());
//        StepVerifier.create(response.getBodyAsString())
//            .expectNext("error")
//            .expectComplete()
//            .verify(Duration.ofSeconds(20));
//    }
//
//    @Test
//    public void testFlowableBackpressure() {
//        StepVerifier.create(getResponse("/long").flatMapMany(HttpResponse::getBody), EMPTY_INITIAL_REQUEST_OPTIONS)
//            .expectNextCount(0)
//            .thenRequest(1)
//            .expectNextCount(1)
//            .verifyComplete();
//    }
//
//    @Test
//    public void testRequestBodyIsErrorShouldPropagateToResponse() {
//        HttpClient client = new HttpUrlConnectionClientProvider().createInstance();
//        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
//            .setHeader(HttpHeaderName.CONTENT_LENGTH, "132")
//            .setBody(Flux.error(new RuntimeException("boo")));
//
//        StepVerifier.create(client.send(request))
//            .expectErrorMessage("boo")
//            .verify();
//    }
//
//    @Test
//    public void testRequestBodyEndsInErrorShouldPropagateToResponse() {
//        HttpClient client = new HttpUrlConnectionClientProvider().createInstance();
//        String contentChunk = "abcdefgh";
//        int repetitions = 1000;
//        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
//            .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentChunk.length() * (repetitions + 1)))
//            .setBody(Flux.just(contentChunk)
//                .repeat(repetitions)
//                .map(s -> ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)))
//                .concatWith(Flux.error(new RuntimeException("boo"))));
//
//        try {
//            StepVerifier.create(client.send(request))
//                .expectErrorMessage("boo")
//                .verify(Duration.ofSeconds(10));
//        } catch (Exception ex) {
//            assertEquals("boo", ex.getMessage());
//        }
//    }
//
//    @Test
//    public void testConcurrentRequests() {
//        int numRequests = 100; // 100 = 1GB of data read
//        HttpClient client = new HttpUrlConnectionClientProvider().createInstance();
//
//        ParallelFlux<byte[]> responses = Flux.range(1, numRequests)
//            .parallel()
//            .runOn(Schedulers.boundedElastic())
//            .flatMap(ignored -> doRequest(client, "/long")
//                .flatMap(HttpResponse::getBodyAsByteArray));
//
//        StepVerifier.create(responses)
//            .thenConsumeWhile(response -> {
//                com.azure.core.test.utils.TestUtils.assertArraysEqual(LONG_BODY, response);
//                return true;
//            })
//            .expectComplete()
//            .verify(Duration.ofSeconds(60));
//    }
//
//    @Test
//    public void testConcurrentRequestsSync() throws InterruptedException {
//        int numRequests = 100; // 100 = 1GB of data read
//        HttpClient client = new HttpUrlConnectionClientProvider().createInstance();
//
//        ForkJoinPool pool = new ForkJoinPool();
//        List<Callable<Void>> requests = new ArrayList<>(numRequests);
//        for (int i = 0; i < numRequests; i++) {
//            requests.add(() -> {
//                try (HttpResponse response = doRequestSync(client, "/long")) {
//                    byte[] body = response.getBodyAsBinaryData().toBytes();
//                    com.azure.core.test.utils.TestUtils.assertArraysEqual(LONG_BODY, body);
//                    return null;
//                }
//            });
//        }
//
//        pool.invokeAll(requests);
//        pool.shutdown();
//        assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS));
//    }
//
//    @Test
//    public void validateHeadersReturnAsIs() {
//        HttpClient client = new HttpUrlConnectionClientProvider().createInstance();
//
//        HttpHeaderName singleValueHeaderName = HttpHeaderName.fromString("singleValue");
//        final String singleValueHeaderValue = "value";
//
//        HttpHeaderName multiValueHeaderName = HttpHeaderName.fromString("Multi-value");
//        final List<String> multiValueHeaderValue = Arrays.asList("value1", "value2");
//
//        HttpHeaders headers = new HttpHeaders()
//            .set(singleValueHeaderName, singleValueHeaderValue)
//            .set(multiValueHeaderName, multiValueHeaderValue);
//
//        StepVerifier.create(client.send(new HttpRequest(HttpMethod.GET, url(server, RETURN_HEADERS_AS_IS_PATH),
//                headers, Flux.empty())))
//            .assertNext(response -> {
//                assertEquals(200, response.getStatusCode());
//
//                HttpHeaders responseHeaders = response.getHeaders();
//                HttpHeader singleValueHeader = responseHeaders.get(singleValueHeaderName);
//                assertEquals(singleValueHeaderName.getCaseSensitiveName(), singleValueHeader.getName());
//                assertEquals(singleValueHeaderValue, singleValueHeader.getValue());
//
//                HttpHeader multiValueHeader = responseHeaders.get(multiValueHeaderName);
//                assertEquals(multiValueHeaderName.getCaseSensitiveName(), multiValueHeader.getName());
//                assertLinesMatch(multiValueHeaderValue, multiValueHeader.getValuesList());
//            })
//            .expectComplete()
//            .verify(Duration.ofSeconds(10));
//    }
//
//    @Test
//    public void testBufferedResponse() {
//        Context context = new Context("azure-eagerly-read-response", true);
//        HttpClient client = new HttpUrlConnectionClientProvider().createInstance();
//
//        StepVerifier.create(getResponse(client, "/short", context).flatMapMany(HttpResponse::getBody))
//            .assertNext(buffer -> assertArrayEquals(SHORT_BODY, buffer.array()))
//            .verifyComplete();
//    }
//
//    @Test
//    public void testEmptyBufferResponse() {
//        StepVerifier.create(getResponse("/empty").flatMapMany(HttpResponse::getBody), EMPTY_INITIAL_REQUEST_OPTIONS)
//            .expectNextCount(0)
//            .thenRequest(1)
//            .verifyComplete();
//    }
//
//    @Test
//    public void testEmptyBufferedResponse() {
//        Context context = new Context("azure-eagerly-read-response", true);
//        HttpClient client = new HttpUrlConnectionClientProvider().createInstance();
//
//        StepVerifier.create(getResponse(client, "/empty", context).flatMapMany(HttpResponse::getBody),
//                EMPTY_INITIAL_REQUEST_OPTIONS)
//            .expectNextCount(0)
//            .thenRequest(1)
//            .verifyComplete();
//    }
//
//    private static Mono<HttpResponse> getResponse(String path) {
//        HttpClient client = new HttpUrlConnectionClientProvider().createInstance();
//        return getResponse(client, path, Context.NONE);
//    }
//
//    private static Mono<HttpResponse> getResponse(HttpClient client, String path, Context context) {
//        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
//        return client.send(request, context);
//    }
//
//    static URL url(LocalTestServer server, String path) {
//        try {
//            return new URI(server.getHttpUri() + path).toURL();
//        } catch (URISyntaxException | MalformedURLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static byte[] createLongBody() {
//        byte[] duplicateBytes = "abcdefghijk".getBytes(StandardCharsets.UTF_8);
//        byte[] longBody = new byte[duplicateBytes.length * 100000];
//
//        for (int i = 0; i < 100000; i++) {
//            System.arraycopy(duplicateBytes, 0, longBody, i * duplicateBytes.length, duplicateBytes.length);
//        }
//
//        return longBody;
//    }
//
//    private static void checkBodyReceived(byte[] expectedBody, String path) {
//        HttpClient client = new HttpUrlConnectionClientProvider().createInstance();
//        StepVerifier.create(doRequest(client, path).flatMap(HttpResponse::getBodyAsByteArray))
//            .assertNext(bytes -> assertArrayEquals(expectedBody, bytes))
//            .verifyComplete();
//    }
//
//    private static Mono<HttpResponse> doRequest(HttpClient client, String path) {
//        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
//        return client.send(request);
//    }
//
//    private static HttpResponse doRequestSync(HttpClient client, String path) {
//        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
//        return client.sendSync(request, Context.NONE);
//    }
}
