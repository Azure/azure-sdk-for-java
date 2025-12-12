// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.openai.core.http.HttpRequestBody;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class HttpClientHelperTests {

    private static final HttpHeaderName REQUEST_ID_HEADER = HttpHeaderName.fromString("x-request-id");
    private static final HttpHeaderName CUSTOM_HEADER_NAME = HttpHeaderName.fromString("custom-header");
    private static final HttpHeaderName X_TEST_HEADER = HttpHeaderName.fromString("X-Test");
    private static final HttpHeaderName X_MULTI_HEADER = HttpHeaderName.fromString("X-Multi");

    @Test
    void executeMapsRequestAndResponse() {
        RecordingHttpClient recordingClient = new RecordingHttpClient(request -> createMockResponse(request, 201,
            new HttpHeaders().set(REQUEST_ID_HEADER, "req-123").set(CUSTOM_HEADER_NAME, "custom-value"), "pong"));
        com.openai.core.http.HttpClient openAiClient
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(recordingClient).build());

        com.openai.core.http.HttpRequest openAiRequest = createOpenAiRequest();

        try (com.openai.core.http.HttpResponse response = openAiClient.execute(openAiRequest)) {
            HttpRequest sentRequest = recordingClient.getLastRequest();
            assertNotNull(sentRequest, "Azure HttpClient should receive a request");
            assertEquals(HttpMethod.POST, sentRequest.getHttpMethod());
            assertEquals("https://example.com/path/segment?q=a%20b", sentRequest.getUrl().toString());
            assertEquals("alpha", sentRequest.getHeaders().getValue(X_TEST_HEADER));
            assertArrayEquals(new String[] { "first", "second" }, sentRequest.getHeaders().getValues(X_MULTI_HEADER));
            assertEquals("text/plain", sentRequest.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            assertEquals("payload", new String(sentRequest.getBodyAsBinaryData().toBytes(), StandardCharsets.UTF_8));

            assertEquals(201, response.statusCode());
            assertEquals("req-123", response.requestId().orElseThrow(() -> new AssertionError("Missing request id")));
            assertEquals("custom-value", response.headers().values("custom-header").get(0));
            assertEquals("pong", new String(readAllBytes(response.body()), StandardCharsets.UTF_8));
        } catch (Exception e) {
            fail("Exception thrown while reading response", e);
        }
    }

    @Test
    void executeAsyncCompletesSuccessfully() {
        RecordingHttpClient recordingClient
            = new RecordingHttpClient(request -> createMockResponse(request, 204, new HttpHeaders(), ""));
        com.openai.core.http.HttpClient openAiClient
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(recordingClient).build());

        com.openai.core.http.HttpRequest openAiRequest = createOpenAiRequest();

        CompletableFuture<com.openai.core.http.HttpResponse> future = openAiClient.executeAsync(openAiRequest);
        try (com.openai.core.http.HttpResponse response = future.join()) {
            assertEquals(204, response.statusCode());
        } catch (Exception e) {
            fail("Exception thrown while reading response", e);
        }
        assertEquals(1, recordingClient.getSendCount());
    }

    @Test
    void executeWithNullRequestBodySucceeds() throws Exception {
        RecordingHttpClient recordingClient = new RecordingHttpClient(request -> {
            // Verify the request has no body (or empty body)
            com.azure.core.util.BinaryData bodyData = request.getBodyAsBinaryData();
            if (bodyData != null) {
                assertEquals(0, bodyData.toBytes().length);
            }
            return createMockResponse(request, 200, new HttpHeaders(), "success");
        });
        com.openai.core.http.HttpClient openAiClient
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(recordingClient).build());

        com.openai.core.http.HttpRequest openAiRequest = com.openai.core.http.HttpRequest.builder()
            .method(com.openai.core.http.HttpMethod.GET)
            .baseUrl("https://example.com")
            .addPathSegment("test")
            .build();

        try (com.openai.core.http.HttpResponse response = openAiClient.execute(openAiRequest)) {
            assertEquals(200, response.statusCode());
            assertEquals("success", new String(readAllBytes(response.body()), StandardCharsets.UTF_8));
        }
    }

    @Disabled("Body gets eagerly evaluated. Instrumentation could be wrong.")
    @Test
    void executeThrowsUncheckedIOExceptionOnBodyBufferingFailure() {
        RecordingHttpClient recordingClient
            = new RecordingHttpClient(request -> createMockResponse(request, 200, new HttpHeaders(), ""));
        com.openai.core.http.HttpClient openAiClient
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(recordingClient).build());

        com.openai.core.http.HttpRequest openAiRequest = com.openai.core.http.HttpRequest.builder()
            .method(com.openai.core.http.HttpMethod.POST)
            .baseUrl("https://example.com")
            .body(new FailingHttpRequestBody())
            .build();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            openAiClient.execute(openAiRequest);
        });
        // Verify the error is related to body buffering failure
        boolean hasBufferMessage = exception.getMessage() != null && exception.getMessage().contains("buffer");
        boolean hasIOCause = exception.getCause() instanceof IOException;
        assertTrue(hasBufferMessage || hasIOCause, "Expected error related to buffer failure, got: " + exception);
    }

    @Test
    void executeThrowsExceptionOnMalformedUrl() {
        RecordingHttpClient recordingClient
            = new RecordingHttpClient(request -> createMockResponse(request, 200, new HttpHeaders(), ""));
        com.openai.core.http.HttpClient openAiClient
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(recordingClient).build());

        com.openai.core.http.HttpRequest openAiRequest = com.openai.core.http.HttpRequest.builder()
            .method(com.openai.core.http.HttpMethod.GET)
            .baseUrl("not-a-valid-url")
            .build();

        // Malformed URLs should throw an exception (typically IllegalArgumentException or IllegalStateException)
        assertThrows(RuntimeException.class, () -> {
            openAiClient.execute(openAiRequest);
        });
    }

    @Disabled("Body gets eagerly evaluated. Instrumentation could be wrong.")
    @Test
    void executeAsyncPropagatesRequestBuildingErrors() {
        RecordingHttpClient recordingClient
            = new RecordingHttpClient(request -> createMockResponse(request, 200, new HttpHeaders(), ""));
        com.openai.core.http.HttpClient openAiClient
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(recordingClient).build());

        com.openai.core.http.HttpRequest openAiRequest = com.openai.core.http.HttpRequest.builder()
            .method(com.openai.core.http.HttpMethod.POST)
            .baseUrl("https://example.com")
            .body(new FailingHttpRequestBody())
            .build();

        CompletableFuture<com.openai.core.http.HttpResponse> future = openAiClient.executeAsync(openAiRequest);

        Exception exception = assertThrows(Exception.class, future::join);
        Throwable cause = exception.getCause();
        assertNotNull(cause, "Expected a cause for the exception");
        // The error should be related to request building/buffering failure
        assertTrue(cause instanceof RuntimeException, "Expected RuntimeException, got: " + cause.getClass().getName());
    }

    @Test
    void executeAsyncPropagatesHttpClientFailures() {
        FailingHttpClient failingClient = new FailingHttpClient(new RuntimeException("Network error"));
        com.openai.core.http.HttpClient openAiClient
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(failingClient).build());

        com.openai.core.http.HttpRequest openAiRequest = com.openai.core.http.HttpRequest.builder()
            .method(com.openai.core.http.HttpMethod.GET)
            .baseUrl("https://example.com")
            .build();

        CompletableFuture<com.openai.core.http.HttpResponse> future = openAiClient.executeAsync(openAiRequest);

        Exception exception = assertThrows(Exception.class, future::join);
        Throwable cause = exception.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof RuntimeException);
        assertEquals("Network error", cause.getMessage());
    }

    private static com.openai.core.http.HttpRequest createOpenAiRequest() {
        return com.openai.core.http.HttpRequest.builder()
            .method(com.openai.core.http.HttpMethod.POST)
            .baseUrl("https://example.com")
            .addPathSegment("path")
            .addPathSegment("segment")
            .putHeader("X-Test", "alpha")
            .putHeaders("X-Multi", Arrays.asList("first", "second"))
            .putQueryParam("q", "a b")
            .body(new TestHttpRequestBody("payload", "text/plain"))
            .build();
    }

    private static MockHttpResponse createMockResponse(HttpRequest request, int statusCode, HttpHeaders headers,
        String body) {
        byte[] bytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        return new MockHttpResponse(request, statusCode, headers, bytes);
    }

    private static byte[] readAllBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int read;
        while ((read = stream.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }

    private static final class RecordingHttpClient implements HttpClient {
        private final Function<HttpRequest, HttpResponse> responseFactory;
        private HttpRequest lastRequest;
        private int sendCount;

        private RecordingHttpClient(Function<HttpRequest, HttpResponse> responseFactory) {
            this.responseFactory = responseFactory;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            this.lastRequest = request;
            this.sendCount++;
            return Mono.just(responseFactory.apply(request));
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request, Context context) {
            return send(request);
        }

        HttpRequest getLastRequest() {
            return lastRequest;
        }

        int getSendCount() {
            return sendCount;
        }
    }

    private static final class TestHttpRequestBody implements HttpRequestBody {
        private final byte[] content;
        private final String contentType;

        private TestHttpRequestBody(String content, String contentType) {
            this.content = content.getBytes(StandardCharsets.UTF_8);
            this.contentType = contentType;
        }

        @Override
        public void writeTo(OutputStream outputStream) {
            try {
                outputStream.write(content);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public String contentType() {
            return contentType;
        }

        @Override
        public long contentLength() {
            return content.length;
        }

        @Override
        public boolean repeatable() {
            return true;
        }

        @Override
        public void close() {
            // no-op
        }
    }

    private static final class FailingHttpRequestBody implements HttpRequestBody {
        @Override
        public void writeTo(OutputStream outputStream) {
            // Simulate an I/O failure during body write
            throw new UncheckedIOException(new IOException("Simulated I/O failure during body write"));
        }

        @Override
        public String contentType() {
            return "application/octet-stream";
        }

        @Override
        public long contentLength() {
            return -1;
        }

        @Override
        public boolean repeatable() {
            return false;
        }

        @Override
        public void close() {
            // no-op
        }
    }

    private static final class FailingHttpClient implements HttpClient {
        private final RuntimeException error;

        private FailingHttpClient(RuntimeException error) {
            this.error = error;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.error(error);
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request, Context context) {
            return send(request);
        }
    }
}
