// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyDecoratingHttpClientTests {

    private static final HttpHeaderName PER_CALL_HEADER = HttpHeaderName.fromString("X-Per-Call");
    private static final HttpHeaderName PER_RETRY_HEADER = HttpHeaderName.fromString("X-Per-Retry");

    @Test
    void policiesMutateRequestBeforeDelegation() throws Exception {
        RecordingHttpClient recordingClient = new RecordingHttpClient();
        HttpPipelinePolicy perCallPolicy
            = new HeaderAppendingPolicy(PER_CALL_HEADER, "one", HttpPipelinePosition.PER_CALL);
        HttpPipelinePolicy perRetryPolicy
            = new HeaderAppendingPolicy(PER_RETRY_HEADER, "two", HttpPipelinePosition.PER_RETRY);

        PolicyDecoratingHttpClient client
            = new PolicyDecoratingHttpClient(recordingClient, Arrays.asList(perCallPolicy, perRetryPolicy));

        HttpRequest request = new HttpRequest(HttpMethod.GET, URI.create("https://example.com").toURL());

        client.sendSync(request, Context.NONE);

        HttpRequest sentRequest = recordingClient.getLastRequest();
        assertNotNull(sentRequest);
        HttpHeaders headers = sentRequest.getHeaders();
        assertEquals("one", headers.getValue(PER_CALL_HEADER));
        assertEquals("two", headers.getValue(PER_RETRY_HEADER));
        assertEquals(1, recordingClient.getSendCount());
    }

    @Test
    void nullArgumentsAreRejected() {
        RecordingHttpClient recordingClient = new RecordingHttpClient();
        HttpPipelinePolicy noopPolicy
            = new HeaderAppendingPolicy(HttpHeaderName.fromString("X-Test"), "value", HttpPipelinePosition.PER_CALL);

        assertThrows(NullPointerException.class,
            () -> new PolicyDecoratingHttpClient(null, Collections.singletonList(noopPolicy)));
        assertThrows(NullPointerException.class, () -> new PolicyDecoratingHttpClient(recordingClient, null));
    }

    @Test
    void sendDelegatesToUnderlyingClient() throws Exception {
        RecordingHttpClient recordingClient = new RecordingHttpClient();
        PolicyDecoratingHttpClient client = new PolicyDecoratingHttpClient(recordingClient, Collections.emptyList());

        HttpRequest request = new HttpRequest(HttpMethod.POST, URI.create("https://example.com").toURL());
        HttpResponse response = client.sendSync(request, Context.NONE);

        assertNotNull(response);
        assertEquals(1, recordingClient.getSendCount());
        assertEquals(request, recordingClient.getLastRequest());
    }

    @Test
    void asyncSendAppliesPolicies() throws Exception {
        RecordingHttpClient recordingClient = new RecordingHttpClient();
        HttpPipelinePolicy perCallPolicy
            = new HeaderAppendingPolicy(PER_CALL_HEADER, "async-one", HttpPipelinePosition.PER_CALL);
        HttpPipelinePolicy perRetryPolicy
            = new HeaderAppendingPolicy(PER_RETRY_HEADER, "async-two", HttpPipelinePosition.PER_RETRY);

        PolicyDecoratingHttpClient client
            = new PolicyDecoratingHttpClient(recordingClient, Arrays.asList(perCallPolicy, perRetryPolicy));

        HttpRequest request = new HttpRequest(HttpMethod.GET, URI.create("https://example.com").toURL());

        HttpResponse response = client.send(request).block();

        assertNotNull(response);
        HttpRequest sentRequest = recordingClient.getLastRequest();
        assertNotNull(sentRequest);
        HttpHeaders headers = sentRequest.getHeaders();
        assertEquals("async-one", headers.getValue(PER_CALL_HEADER));
        assertEquals("async-two", headers.getValue(PER_RETRY_HEADER));
        assertEquals(1, recordingClient.getSendCount());
    }

    @Test
    void asyncSendWithContextAppliesPolicies() throws Exception {
        RecordingHttpClient recordingClient = new RecordingHttpClient();
        HttpPipelinePolicy perCallPolicy
            = new HeaderAppendingPolicy(PER_CALL_HEADER, "context-one", HttpPipelinePosition.PER_CALL);
        HttpPipelinePolicy perRetryPolicy
            = new HeaderAppendingPolicy(PER_RETRY_HEADER, "context-two", HttpPipelinePosition.PER_RETRY);

        PolicyDecoratingHttpClient client
            = new PolicyDecoratingHttpClient(recordingClient, Arrays.asList(perCallPolicy, perRetryPolicy));

        HttpRequest request = new HttpRequest(HttpMethod.GET, URI.create("https://example.com").toURL());

        HttpResponse response = client.send(request, Context.NONE).block();

        assertNotNull(response);
        HttpRequest sentRequest = recordingClient.getLastRequest();
        assertNotNull(sentRequest);
        HttpHeaders headers = sentRequest.getHeaders();
        assertEquals("context-one", headers.getValue(PER_CALL_HEADER));
        assertEquals("context-two", headers.getValue(PER_RETRY_HEADER));
        assertEquals(1, recordingClient.getSendCount());
    }

    @Test
    void policyErrorPropagatesInAsyncSend() throws Exception {
        RecordingHttpClient recordingClient = new RecordingHttpClient();
        RuntimeException policyException = new RuntimeException("Policy error");
        HttpPipelinePolicy failingPolicy = new FailingPolicy(policyException);

        PolicyDecoratingHttpClient client
            = new PolicyDecoratingHttpClient(recordingClient, Collections.singletonList(failingPolicy));

        HttpRequest request = new HttpRequest(HttpMethod.GET, URI.create("https://example.com").toURL());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> client.send(request).block());
        assertEquals("Policy error", thrown.getMessage());
        assertEquals(0, recordingClient.getSendCount());
    }

    @Test
    void underlyingClientErrorPropagatesInAsyncSend() throws Exception {
        RuntimeException clientException = new RuntimeException("Client error");
        FailingHttpClient failingClient = new FailingHttpClient(clientException);

        PolicyDecoratingHttpClient client = new PolicyDecoratingHttpClient(failingClient, Collections.emptyList());

        HttpRequest request = new HttpRequest(HttpMethod.GET, URI.create("https://example.com").toURL());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> client.send(request).block());
        assertEquals("Client error", thrown.getMessage());
        assertTrue(failingClient.wasCalled());
    }

    @Test
    void policyErrorPropagatesInSyncSend() throws Exception {
        RecordingHttpClient recordingClient = new RecordingHttpClient();
        RuntimeException policyException = new RuntimeException("Sync policy error");
        HttpPipelinePolicy failingPolicy = new FailingPolicy(policyException);

        PolicyDecoratingHttpClient client
            = new PolicyDecoratingHttpClient(recordingClient, Collections.singletonList(failingPolicy));

        HttpRequest request = new HttpRequest(HttpMethod.GET, URI.create("https://example.com").toURL());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> client.sendSync(request, Context.NONE));
        assertEquals("Sync policy error", thrown.getMessage());
        assertEquals(0, recordingClient.getSendCount());
    }

    @Test
    void underlyingClientErrorPropagatesInSyncSend() throws Exception {
        RuntimeException clientException = new RuntimeException("Sync client error");
        FailingHttpClient failingClient = new FailingHttpClient(clientException);

        PolicyDecoratingHttpClient client = new PolicyDecoratingHttpClient(failingClient, Collections.emptyList());

        HttpRequest request = new HttpRequest(HttpMethod.GET, URI.create("https://example.com").toURL());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> client.sendSync(request, Context.NONE));
        assertEquals("Sync client error", thrown.getMessage());
        assertTrue(failingClient.wasCalled());
    }

    private static final class RecordingHttpClient implements HttpClient {
        private HttpRequest lastRequest;
        private final AtomicInteger sendCount = new AtomicInteger();

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            this.lastRequest = request;
            this.sendCount.incrementAndGet();
            return Mono.just(new MockHttpResponse(request, 200, "ok".getBytes(StandardCharsets.UTF_8)));
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request, Context context) {
            return send(request);
        }

        HttpRequest getLastRequest() {
            return lastRequest;
        }

        int getSendCount() {
            return sendCount.get();
        }
    }

    private static final class HeaderAppendingPolicy implements HttpPipelinePolicy {
        private final HttpHeaderName headerName;
        private final String headerValue;
        private final HttpPipelinePosition position;

        private HeaderAppendingPolicy(HttpHeaderName headerName, String headerValue, HttpPipelinePosition position) {
            this.headerName = headerName;
            this.headerValue = headerValue;
            this.position = position;
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return position;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            context.getHttpRequest().getHeaders().set(headerName, headerValue);
            return next.process();
        }
    }

    private static final class FailingPolicy implements HttpPipelinePolicy {
        private final RuntimeException exception;

        private FailingPolicy(RuntimeException exception) {
            this.exception = exception;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return Mono.error(exception);
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }
    }

    private static final class FailingHttpClient implements HttpClient {
        private final RuntimeException exception;
        private boolean called = false;

        private FailingHttpClient(RuntimeException exception) {
            this.exception = exception;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            this.called = true;
            return Mono.error(exception);
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request, Context context) {
            return send(request);
        }

        boolean wasCalled() {
            return called;
        }
    }
}
