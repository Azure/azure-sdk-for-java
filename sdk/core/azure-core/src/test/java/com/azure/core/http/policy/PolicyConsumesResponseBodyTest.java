// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.SyncAsyncExtension;
import com.azure.core.SyncAsyncTest;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.azure.core.http.HttpHeaderName.CONTENT_LENGTH;
import static com.azure.core.http.HttpHeaderName.LOCATION;
import static com.azure.core.http.HttpHeaderName.WWW_AUTHENTICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PolicyConsumesResponseBodyTest {

    private static final TokenCredential NOOP_CREDENTIAL
        = request -> Mono.just(new AccessToken("token", OffsetDateTime.MAX));

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testResponseClosureOn401Sync(boolean authorizeOnChallenge) {
        AtomicInteger tryCount = new AtomicInteger(0);

        AtomicReference<TestHttpResponse> response401 = new AtomicReference<>();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new BearerPolicyImpl(authorizeOnChallenge, NOOP_CREDENTIAL, "scope"))
                .httpClient(new TestHttpClient(r -> {
                    if (tryCount.getAndIncrement() == 0) {
                        TestHttpResponse r401 = createWithSyncBody(r, 401, 424242);
                        r401.headers.set(WWW_AUTHENTICATE, "Bearer");
                        response401.set(r401);
                        return r401;
                    }
                    return createWithSyncBody(r, 200, 42);
                }))
                .build();

        HttpResponse response = pipeline.sendSync(new HttpRequest(HttpMethod.GET, "https://fake"), Context.NONE);
        assertEquals(authorizeOnChallenge ? 200 : 401, response.getStatusCode());
        assertEquals(authorizeOnChallenge ? 2 : 1, tryCount.get());

        if (authorizeOnChallenge) {
            assertTrue(response401.get().isConsumedOrClosed());
        }
        assertInstanceOf(TestHttpResponse.class, response);
        assertFalse(((TestHttpResponse) response).isConsumedOrClosed());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testResponseClosureOn401Async(boolean authorizeOnChallenge) {
        AtomicInteger tryCount = new AtomicInteger(0);

        AtomicReference<TestHttpResponse> response401 = new AtomicReference<>();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new BearerPolicyImpl(authorizeOnChallenge, NOOP_CREDENTIAL, "scope"))
                .httpClient(new TestHttpClient(r -> {
                    if (tryCount.getAndIncrement() == 0) {
                        TestHttpResponse r401 = createWithAsyncBody(r, 401, 424242);
                        r401.headers.set(WWW_AUTHENTICATE, "Bearer");
                        response401.set(r401);
                        return r401;
                    }
                    return createWithAsyncBody(r, 200, 42);
                }))
                .build();

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET, "https://fake")).block();
        assertEquals(authorizeOnChallenge ? 200 : 401, response.getStatusCode());
        assertEquals(authorizeOnChallenge ? 2 : 1, tryCount.get());

        if (authorizeOnChallenge) {
            assertTrue(response401.get().isConsumedOrClosed());
        }

        assertInstanceOf(TestHttpResponse.class, response);
        assertFalse(((TestHttpResponse) response).isConsumedOrClosed());
    }

    @SyncAsyncTest
    public void testResponseClosureOn401AndException() throws Exception {
        AtomicInteger tryCount = new AtomicInteger(0);

        AtomicReference<TestHttpResponse> responseException = new AtomicReference<>();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new BearerPolicyImpl(true, NOOP_CREDENTIAL, "scope"))
            .httpClient(new TestHttpClient(r -> {
                if (tryCount.getAndIncrement() == 0) {
                    TestHttpResponse r401
                        = createWithAsyncBodyAndException(r, 401, 424242, new IOException("Fake exception"));
                    r401.headers.set(WWW_AUTHENTICATE, "Bearer");
                    responseException.set(r401);
                    return r401;
                }
                return createWithSyncBody(r, 200, 42);
            }))
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://fake");
        HttpResponse response
            = SyncAsyncExtension.execute(() -> pipeline.sendSync(request, Context.NONE), () -> pipeline.send(request));

        assertEquals(200, response.getStatusCode());

        assertEquals(2, tryCount.get());
        assertTrue(responseException.get().isConsumedOrClosed());
        assertInstanceOf(TestHttpResponse.class, response);
        assertFalse(((TestHttpResponse) response).isConsumedOrClosed());
    }

    @SyncAsyncTest
    public void testResponseClosureOn302() throws Exception {
        AtomicInteger tryCount = new AtomicInteger(0);

        AtomicReference<TestHttpResponse> response302 = new AtomicReference<>();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RedirectPolicy()).httpClient(new TestHttpClient(r -> {
                if (tryCount.getAndIncrement() == 0) {
                    TestHttpResponse r302 = createWithAsyncBody(r, 302, 424242);
                    r302.headers.set(LOCATION, "https://fake");
                    response302.set(r302);
                    return r302;
                }
                return createWithAsyncBody(r, 200, 42);
            })).build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://fake");
        HttpResponse response
            = SyncAsyncExtension.execute(() -> pipeline.sendSync(request, Context.NONE), () -> pipeline.send(request));

        assertEquals(200, response.getStatusCode());

        assertEquals(2, tryCount.get());
        assertTrue(response302.get().isConsumedOrClosed());
        assertInstanceOf(TestHttpResponse.class, response);
        assertFalse(((TestHttpResponse) response).isConsumedOrClosed());
    }

    @SyncAsyncTest
    public void testResponseClosureOn302AndException() throws Exception {
        AtomicInteger tryCount = new AtomicInteger(0);

        AtomicReference<TestHttpResponse> responseException = new AtomicReference<>();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RedirectPolicy()).httpClient(new TestHttpClient(r -> {
                if (tryCount.getAndIncrement() == 0) {
                    TestHttpResponse rEx
                        = createWithAsyncBodyAndException(r, 302, 424242, new IOException("Fake exception"));
                    rEx.headers.set(LOCATION, "https://fake");
                    responseException.set(rEx);
                    return rEx;
                }
                return createWithSyncBody(r, 200, 42);
            })).build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://fake");
        HttpResponse response
            = SyncAsyncExtension.execute(() -> pipeline.sendSync(request, Context.NONE), () -> pipeline.send(request));

        assertEquals(200, response.getStatusCode());

        assertEquals(2, tryCount.get());
        assertTrue(responseException.get().isConsumedOrClosed());
        assertInstanceOf(TestHttpResponse.class, response);
        assertFalse(((TestHttpResponse) response).isConsumedOrClosed());
    }

    @SyncAsyncTest
    public void testResponseClosureOn503() throws Exception {
        AtomicInteger tryCount = new AtomicInteger(0);

        AtomicReference<TestHttpResponse> response503 = new AtomicReference<>();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RetryPolicy()).httpClient(new TestHttpClient(r -> {
                if (tryCount.getAndIncrement() == 0) {
                    response503.set(createWithSyncBody(r, 503, 424242));
                    return response503.get();
                }
                return createWithSyncBody(r, 200, 42);
            })).build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://fake");
        HttpResponse response
            = SyncAsyncExtension.execute(() -> pipeline.sendSync(request, Context.NONE), () -> pipeline.send(request));

        assertEquals(200, response.getStatusCode());

        assertEquals(2, tryCount.get());
        assertTrue(response503.get().isConsumedOrClosed());
        assertInstanceOf(TestHttpResponse.class, response);
        assertFalse(((TestHttpResponse) response).isConsumedOrClosed());
    }

    @SyncAsyncTest
    public void testResponseClosureOn503AndException() throws Exception {
        AtomicInteger tryCount = new AtomicInteger(0);

        AtomicReference<TestHttpResponse> responseException = new AtomicReference<>();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RetryPolicy()).httpClient(new TestHttpClient(r -> {
                if (tryCount.getAndIncrement() == 0) {
                    TestHttpResponse rEx
                        = createWithAsyncBodyAndException(r, 503, 424242, new IOException("Fake exception"));
                    responseException.set(rEx);
                    return rEx;
                }
                return createWithSyncBody(r, 200, 42);
            })).build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://fake");
        HttpResponse response
            = SyncAsyncExtension.execute(() -> pipeline.sendSync(request, Context.NONE), () -> pipeline.send(request));

        assertEquals(200, response.getStatusCode());

        assertEquals(2, tryCount.get());
        assertTrue(responseException.get().isConsumedOrClosed());
        assertInstanceOf(TestHttpResponse.class, response);
        assertFalse(((TestHttpResponse) response).isConsumedOrClosed());
    }

    private TestHttpResponse createWithSyncBody(HttpRequest request, int statusCode, int contentLength) {
        HttpHeaders headers = new HttpHeaders().set(CONTENT_LENGTH, Integer.toString(contentLength));
        return new TestHttpResponse(request, statusCode, headers, new ByteArrayInputStream(new byte[contentLength]));
    }

    private TestHttpResponse createWithAsyncBody(HttpRequest request, int statusCode, int contentLength) {
        HttpHeaders headers = new HttpHeaders().set(CONTENT_LENGTH, Integer.toString(contentLength));
        return new TestHttpResponse(request, statusCode, headers, Flux.create(s -> {
            for (int remaining = contentLength; remaining > 0; remaining -= 10) {
                byte[] bytes = new byte[Math.min(10, remaining)];
                s.next(ByteBuffer.wrap(bytes));
            }
            s.complete();
        }));
    }

    private TestHttpResponse createWithAsyncBodyAndException(HttpRequest request, int statusCode, int contentLength,
        Exception exception) {
        HttpHeaders headers = new HttpHeaders().set(CONTENT_LENGTH, Integer.toString(contentLength));
        return new TestHttpResponse(request, statusCode, headers, Flux.create(s -> {
            byte[] bytes = new byte[Math.min(10, contentLength)];
            s.next(ByteBuffer.wrap(bytes));
            s.error(exception);
        }));
    }

    private class BearerPolicyImpl extends BearerTokenAuthenticationPolicy {
        private final boolean authorize;

        BearerPolicyImpl(boolean authorize, TokenCredential credential, String... scopes) {
            super(credential, scopes);
            this.authorize = authorize;
        }

        @Override
        public boolean authorizeRequestOnChallengeSync(HttpPipelineCallContext context, HttpResponse response) {
            return authorize;
        }

        @Override
        public Mono<Boolean> authorizeRequestOnChallenge(HttpPipelineCallContext context, HttpResponse response) {
            return Mono.just(authorize);
        }
    }

    private static class TestHttpClient implements HttpClient {
        private final Function<HttpRequest, HttpResponse> responseProvider;

        TestHttpClient(Function<HttpRequest, HttpResponse> responseProvider) {
            this.responseProvider = responseProvider;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.just(responseProvider.apply(request));
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            return responseProvider.apply(request);
        }
    }

    public class TestHttpResponse extends HttpResponse {

        private final int statusCode;

        private final HttpHeaders headers;

        private final Flux<ByteBuffer> bodyFlux;
        private final ByteArrayInputStream bodyStream;
        private boolean closed = false;
        private boolean consumed = false;

        public TestHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, Flux<ByteBuffer> body) {
            super(request);
            this.statusCode = statusCode;
            this.headers = headers;
            this.bodyFlux = body.doFinally(__ -> consumed = true);
            this.bodyStream = null;
        }

        public TestHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, ByteArrayInputStream body) {
            super(request);
            this.statusCode = statusCode;
            this.headers = headers;
            this.bodyStream = body;
            this.bodyFlux = null;
        }

        public boolean isConsumedOrClosed() {
            return closed || (bodyStream != null ? bodyStream.available() == 0 : consumed);
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        @Deprecated
        public String getHeaderValue(String name) {
            return headers.getValue(name);
        }

        @Override
        public String getHeaderValue(HttpHeaderName headerName) {
            return headers.getValue(headerName);
        }

        @Override
        public HttpHeaders getHeaders() {
            return this.headers;
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            if (bodyStream != null) {
                return Mono.just(BinaryData.fromStream(bodyStream).toBytes());
            } else {
                return FluxUtil.collectBytesInByteBufferStream(bodyFlux);
            }
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            if (bodyStream != null) {
                return FluxUtil.toFluxByteBuffer(bodyStream);
            } else {
                return bodyFlux;
            }
        }

        @Override
        public Mono<String> getBodyAsString() {
            return getBodyAsString(StandardCharsets.UTF_8);
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
        }

        @Override
        public BinaryData getBodyAsBinaryData() {
            if (bodyStream != null) {
                return BinaryData.fromStream(bodyStream);
            } else {
                return BinaryData.fromFlux(bodyFlux).block();
            }
        }

        @Override
        public Mono<InputStream> getBodyAsInputStream() {
            if (bodyStream != null) {
                return Mono.just(bodyStream);
            } else {
                return getBodyAsByteArray().map(ByteArrayInputStream::new);
            }
        }

        @Override
        public HttpResponse buffer() {
            return new MockHttpResponse(getRequest(), getStatusCode(), getHeaders(), getBodyAsBinaryData().toBytes());
        }

        @Override
        public Mono<Void> writeBodyToAsync(AsynchronousByteChannel channel) {
            return FluxUtil.writeToAsynchronousByteChannel(getBody(), channel);
        }

        @Override
        public void writeBodyTo(WritableByteChannel channel) throws IOException {
            FluxUtil.writeToWritableByteChannel(getBody(), channel).block();
        }

        @Override
        public void close() {
            this.closed = true;
            if (bodyStream != null) {
                try {
                    bodyStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
