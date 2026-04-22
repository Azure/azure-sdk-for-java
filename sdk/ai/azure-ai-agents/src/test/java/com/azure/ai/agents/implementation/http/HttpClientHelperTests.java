// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class HttpClientHelperTests {

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

    // ========================================================================
    // Streaming detection tests — verifies fix for issue #48726
    // ========================================================================

    @Test
    void nonStreamingRequestSetsEagerlyReadToTrue() {
        ContextCapturingHttpClient capturingClient = new ContextCapturingHttpClient();
        com.openai.core.http.HttpClient openAiClient
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(capturingClient).build());

        // Non-streaming JSON body (no "stream":true)
        String body = "{\"model\":\"gpt-4o\",\"input\":\"Hello\"}";
        com.openai.core.http.HttpRequest openAiRequest = com.openai.core.http.HttpRequest.builder()
            .method(com.openai.core.http.HttpMethod.POST)
            .baseUrl("https://example.com")
            .addPathSegment("responses")
            .body(new TestHttpRequestBody(body, "application/json"))
            .build();

        try (com.openai.core.http.HttpResponse response = openAiClient.execute(openAiRequest)) {
            assertEquals(200, response.statusCode());
        }

        Context capturedContext = capturingClient.getLastContext();
        assertNotNull(capturedContext, "Context should have been captured");
        Object eagerlyRead = capturedContext.getData("azure-eagerly-read-response").orElse(null);
        assertTrue((Boolean) eagerlyRead, "Non-streaming requests should have azure-eagerly-read-response=true");
    }

    @Test
    void streamingRequestSetsEagerlyReadToFalse() {
        ContextCapturingHttpClient capturingClient = new ContextCapturingHttpClient();
        com.openai.core.http.HttpClient openAiClient
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(capturingClient).build());

        // Streaming JSON body (contains "stream":true)
        String body = "{\"model\":\"gpt-4o\",\"input\":\"Hello\",\"stream\":true}";
        com.openai.core.http.HttpRequest openAiRequest = com.openai.core.http.HttpRequest.builder()
            .method(com.openai.core.http.HttpMethod.POST)
            .baseUrl("https://example.com")
            .addPathSegment("responses")
            .body(new TestHttpRequestBody(body, "application/json"))
            .build();

        try (com.openai.core.http.HttpResponse response = openAiClient.execute(openAiRequest)) {
            assertEquals(200, response.statusCode());
        }

        Context capturedContext = capturingClient.getLastContext();
        assertNotNull(capturedContext, "Context should have been captured");
        Object eagerlyRead = capturedContext.getData("azure-eagerly-read-response").orElse(null);
        assertFalse((Boolean) eagerlyRead, "Streaming requests should have azure-eagerly-read-response=false");
    }

    @Test
    void streamingRequestWithWhitespaceSetsEagerlyReadToFalse() {
        ContextCapturingHttpClient capturingClient = new ContextCapturingHttpClient();
        com.openai.core.http.HttpClient openAiClient
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(capturingClient).build());

        // Streaming JSON body with spaces around the colon
        String body = "{\"model\":\"gpt-4o\", \"stream\" : true, \"input\":\"Hello\"}";
        com.openai.core.http.HttpRequest openAiRequest = com.openai.core.http.HttpRequest.builder()
            .method(com.openai.core.http.HttpMethod.POST)
            .baseUrl("https://example.com")
            .addPathSegment("responses")
            .body(new TestHttpRequestBody(body, "application/json"))
            .build();

        try (com.openai.core.http.HttpResponse response = openAiClient.execute(openAiRequest)) {
            assertEquals(200, response.statusCode());
        }

        Context capturedContext = capturingClient.getLastContext();
        assertNotNull(capturedContext, "Context should have been captured");
        Object eagerlyRead = capturedContext.getData("azure-eagerly-read-response").orElse(null);
        assertFalse((Boolean) eagerlyRead,
            "Streaming requests with whitespace around colon should have azure-eagerly-read-response=false");
    }

    @Test
    void streamingAsyncRequestSetsEagerlyReadToFalse() {
        ContextCapturingHttpClient capturingClient = new ContextCapturingHttpClient();
        com.openai.core.http.HttpClient openAiClient
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(capturingClient).build());

        // Streaming JSON body
        String body = "{\"model\":\"gpt-4o\",\"input\":\"Hello\",\"stream\":true}";
        com.openai.core.http.HttpRequest openAiRequest = com.openai.core.http.HttpRequest.builder()
            .method(com.openai.core.http.HttpMethod.POST)
            .baseUrl("https://example.com")
            .addPathSegment("responses")
            .body(new TestHttpRequestBody(body, "application/json"))
            .build();

        CompletableFuture<com.openai.core.http.HttpResponse> future = openAiClient.executeAsync(openAiRequest);
        try (com.openai.core.http.HttpResponse response = future.join()) {
            assertEquals(200, response.statusCode());
        }

        Context capturedContext = capturingClient.getLastContext();
        assertNotNull(capturedContext, "Context should have been captured");
        Object eagerlyRead = capturedContext.getData("azure-eagerly-read-response").orElse(null);
        assertFalse((Boolean) eagerlyRead, "Async streaming requests should have azure-eagerly-read-response=false");
    }

    @Test
    void nonJsonBodySetsEagerlyReadToTrue() {
        ContextCapturingHttpClient capturingClient = new ContextCapturingHttpClient();
        com.openai.core.http.HttpClient openAiClient
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(capturingClient).build());

        // Non-JSON body that happens to contain "stream":true text
        com.openai.core.http.HttpRequest openAiRequest = com.openai.core.http.HttpRequest.builder()
            .method(com.openai.core.http.HttpMethod.POST)
            .baseUrl("https://example.com")
            .body(new TestHttpRequestBody("stream\":true", "text/plain"))
            .build();

        try (com.openai.core.http.HttpResponse response = openAiClient.execute(openAiRequest)) {
            assertEquals(200, response.statusCode());
        }

        Context capturedContext = capturingClient.getLastContext();
        assertNotNull(capturedContext, "Context should have been captured");
        Object eagerlyRead = capturedContext.getData("azure-eagerly-read-response").orElse(null);
        assertTrue((Boolean) eagerlyRead, "Non-JSON bodies should always have azure-eagerly-read-response=true");
    }

    @Test
    void streamFalseInBodySetsEagerlyReadToTrue() {
        ContextCapturingHttpClient capturingClient = new ContextCapturingHttpClient();
        com.openai.core.http.HttpClient openAiClient
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(capturingClient).build());

        // JSON body with "stream":false
        String body = "{\"model\":\"gpt-4o\",\"input\":\"Hello\",\"stream\":false}";
        com.openai.core.http.HttpRequest openAiRequest = com.openai.core.http.HttpRequest.builder()
            .method(com.openai.core.http.HttpMethod.POST)
            .baseUrl("https://example.com")
            .addPathSegment("responses")
            .body(new TestHttpRequestBody(body, "application/json"))
            .build();

        try (com.openai.core.http.HttpResponse response = openAiClient.execute(openAiRequest)) {
            assertEquals(200, response.statusCode());
        }

        Context capturedContext = capturingClient.getLastContext();
        assertNotNull(capturedContext, "Context should have been captured");
        Object eagerlyRead = capturedContext.getData("azure-eagerly-read-response").orElse(null);
        assertTrue((Boolean) eagerlyRead, "Requests with stream=false should have azure-eagerly-read-response=true");
    }

    @Test
    void noBodySetsEagerlyReadToTrue() {
        ContextCapturingHttpClient capturingClient = new ContextCapturingHttpClient();
        com.openai.core.http.HttpClient openAiClient
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(capturingClient).build());

        // GET request with no body
        com.openai.core.http.HttpRequest openAiRequest = com.openai.core.http.HttpRequest.builder()
            .method(com.openai.core.http.HttpMethod.GET)
            .baseUrl("https://example.com")
            .addPathSegment("test")
            .build();

        try (com.openai.core.http.HttpResponse response = openAiClient.execute(openAiRequest)) {
            assertEquals(200, response.statusCode());
        }

        Context capturedContext = capturingClient.getLastContext();
        assertNotNull(capturedContext, "Context should have been captured");
        Object eagerlyRead = capturedContext.getData("azure-eagerly-read-response").orElse(null);
        assertTrue((Boolean) eagerlyRead, "Requests without a body should have azure-eagerly-read-response=true");
    }

    // ========================================================================
    // Test helpers
    // ========================================================================

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

    /**
     * HTTP client that captures the Context passed to send(), allowing tests to verify
     * context flags like azure-eagerly-read-response.
     */
    private static final class ContextCapturingHttpClient implements HttpClient {
        private Context lastContext;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return send(request, Context.NONE);
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request, Context context) {
            this.lastContext = context;
            return Mono.just(createMockResponse(request, 200, new HttpHeaders(), "{}"));
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            this.lastContext = context;
            return createMockResponse(request, 200, new HttpHeaders(), "{}");
        }

        Context getLastContext() {
            return lastContext;
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
