// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.SyncAsyncExtension;
import com.azure.core.SyncAsyncTest;
import com.azure.core.v2.credential.AccessToken;
import com.azure.core.v2.credential.TokenCredential;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.implementation.http.HttpPipelineCallState;
import io.clientcore.core.util.Context;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;

import static io.clientcore.core.http.models.HttpHeaderName.CONTENT_LENGTH;
import static io.clientcore.core.http.models.HttpHeaderName.WWW_AUTHENTICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PolicyConsumesResponseBodyTest {

    private static final TokenCredential NOOP_CREDENTIAL = request -> new AccessToken("token", OffsetDateTime.MAX);

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

        Response<?> response = pipeline.send(new HttpRequest(HttpMethod.GET, "https://fake"));
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
                    TestHttpResponse r401 = createWithSyncBody(r, 401, 424242, new IOException("Fake exception"));
                    r401.headers.set(WWW_AUTHENTICATE, "Bearer");
                    responseException.set(r401);
                    return r401;
                }
                return createWithSyncBody(r, 200, 42);
            }))
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://fake");
        Response<?> response
            = SyncAsyncExtension.execute(() -> pipeline.send(request), () -> pipeline.send(request).block());

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
        Response<?> response
            = SyncAsyncExtension.execute(() -> pipeline.send(request), () -> pipeline.send(request).block());

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
        Response<?> response
            = SyncAsyncExtension.execute(() -> pipeline.send(request), () -> pipeline.send(request).block());

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
        Response<?> response
            = SyncAsyncExtension.execute(() -> pipeline.send(request), () -> pipeline.send(request).block());

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
        Response<?> response = SyncAsyncExtension.execute(() -> pipeline.send(request), () -> pipeline.send(request));

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

    private TestHttpResponse createWithSyncBodyAndException(HttpRequest request, int statusCode, int contentLength,
        Exception exception) {
        HttpHeaders headers = new HttpHeaders().set(CONTENT_LENGTH, Integer.toString(contentLength));
        return new TestHttpResponse(request, statusCode, headers, new Flux.create(s -> {
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
        public boolean authorizeRequestOnChallengeSync(HttpPipelineCallState context, Response<?> response) {
            return authorize;
        }

        @Override
        public Boolean> authorizeRequestOnChallenge(HttpPipelineCallContext context, Response<?> response) {
            return authorize);
        }
    }

    private static class TestHttpClient implements HttpClient {
        private final Function<HttpRequest, Response<?>> responseProvider;

        TestHttpClient(Function<HttpRequest, Response<?>> responseProvider) {
            this.responseProvider = responseProvider;
        }

        @Override
        public Response<?> send(HttpRequest request) {
            return responseProvider.apply(request);
        }
    }

    public class TestHttpResponse extends HttpResponse {

        private final HttpHeaders headers;

        private final ByteArrayInputStream bodyStream;
        private boolean closed = false;
        private boolean consumed = false;

        public TestHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, ByteArrayInputStream body) {
            super(request, statusCode, headers, body);
            this.headers = headers;
            this.bodyStream = body;
        }

        public boolean isConsumedOrClosed() {
            return closed || (bodyStream != null ? bodyStream.available() == 0 : consumed);
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
