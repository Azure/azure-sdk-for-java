// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.FluxByteBufferContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link RestProxy}.
 */
public class SyncRestProxyTests {
    @Host("https://azure.com")
    @ServiceInterface(name = "myService")
    interface TestInterface {
        @Post("my/url/path")
        @ExpectedResponses({ 200 })
        Response<Void> testMethod(@BodyParam("application/octet-stream") BinaryData data,
            @HeaderParam("Content-Type") String contentType, @HeaderParam("Content-Length") Long contentLength,
            Context context);

        @Get("my/url/path")
        @ExpectedResponses({ 200 })
        StreamResponse testDownload(Context context);

        @Get("my/url/path")
        @ExpectedResponses({ 200 })
        void testVoidMethod(Context context);

        @Put("my/url/path")
        @ExpectedResponses({ 200 })
        Response<InputStream> testInputStreamResponse(Context context);

        @Get("my/url/path")
        @ExpectedResponses({ 200 })
        Response<BinaryData> testBinaryDataResponse(Context context);
    }

    @Test
    public void voidReturningApiClosesResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        testInterface.testVoidMethod(Context.NONE);

        assertTrue(client.lastResponseClosed);
    }

    @Test
    public void contextFlagDisablesSyncStack() {
        AtomicBoolean asyncMethodCalled = new AtomicBoolean(false);
        HttpClient client = new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                asyncMethodCalled.set(true);
                return Mono.just(new MockHttpResponse(request, 200));
            }

            @Override
            public HttpResponse sendSync(HttpRequest request, Context context) {
                throw new IllegalStateException("Sync send API was Invoked.");
            }
        };

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        testInterface.testVoidMethod(new Context("com.azure.core.http.restproxy.syncproxy.enable", false));
        assertTrue(asyncMethodCalled.get());
    }

    @Test
    public void contentTypeHeaderPriorityOverBodyParamAnnotationTest() {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        byte[] bytes = "hello".getBytes();
        Response<Void> response
            = testInterface.testMethod(BinaryData.fromStream(new ByteArrayInputStream(bytes), (long) bytes.length),
                "application/json", (long) bytes.length, Context.NONE);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void streamResponseShouldHaveHttpResponseReference() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        StreamResponse streamResponse = testInterface.testDownload(Context.NONE);
        streamResponse.close();
        // This indirectly tests that StreamResponse has HttpResponse reference
        assertTrue(client.lastResponseClosed);
    }

    private static final class LocalHttpClient implements HttpClient {

        private volatile boolean lastResponseClosed;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.error(new IllegalStateException("Async Send API was Invoked."));
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            boolean success = request.getUrl().getPath().equals("/my/url/path");
            if (request.getHttpMethod().equals(HttpMethod.POST)) {
                success &= "application/json".equals(request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            } else if (request.getHttpMethod().equals(HttpMethod.GET)) {
                success &= request.getHttpMethod().equals(HttpMethod.GET);
            } else {
                success &= request.getHttpMethod().equals(HttpMethod.PUT);
                return new MockHttpResponse(request, success ? 200 : 400,
                    (InputStream) new ByteArrayInputStream("hello".getBytes())) {
                    @Override
                    public void close() {
                        lastResponseClosed = true;
                        super.close();
                    }
                };
            }

            return new MockHttpResponse(request, success ? 200 : 400) {
                @Override
                public void close() {
                    lastResponseClosed = true;
                    super.close();
                }
            };
        }
    }

    @ParameterizedTest
    @MethodSource("mergeRequestOptionsContextSupplier")
    public void mergeRequestOptionsContext(Context context, RequestOptions options,
        Map<Object, Object> expectedContextValues) {
        Map<Object, Object> actualContextValues
            = RestProxyUtils.mergeRequestOptionsContext(context, options).getValues();

        assertEquals(expectedContextValues.size(), actualContextValues.size());
        for (Map.Entry<Object, Object> expectedKvp : expectedContextValues.entrySet()) {
            assertTrue(actualContextValues.containsKey(expectedKvp.getKey()),
                () -> "Missing expected key '" + expectedKvp.getKey() + "'.");
            assertEquals(expectedKvp.getValue(), actualContextValues.get(expectedKvp.getKey()));
        }
    }

    @Test
    public void testInputStream() throws IOException {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        Response<InputStream> inputStreamResponse = testInterface.testInputStreamResponse(Context.NONE);
        InputStream stream = inputStreamResponse.getValue();
        byte[] bytes = MockHttpResponse.readAllBytes(stream);
        assertEquals("hello", new String(bytes));
    }

    @Test
    public void binaryDataResponseClosesOnCompletion() {
        AtomicInteger responseCloseCount = new AtomicInteger();
        TestInterface testInterface
            = createBinaryDataService(Flux.just(ByteBuffer.wrap("hello".getBytes())), responseCloseCount);

        Response<BinaryData> response = testInterface.testBinaryDataResponse(Context.NONE);
        BinaryData responseBody = response.getValue();

        assertFalse(response instanceof java.io.Closeable);
        assertFalse(responseBody.isReplayable());
        assertEquals("hello", responseBody.toString());
        assertEquals(1, responseCloseCount.get());
        assertEquals("hello", responseBody.toString());
        assertEquals(1, responseCloseCount.get());
    }

    @Test
    public void binaryDataResponseClosesOnCancellation() {
        AtomicInteger responseCloseCount = new AtomicInteger();
        Flux<ByteBuffer> responseBody = Flux.concat(Flux.just(ByteBuffer.wrap("hello".getBytes())), Flux.never());
        TestInterface testInterface = createBinaryDataService(responseBody, responseCloseCount);

        Disposable subscription
            = testInterface.testBinaryDataResponse(Context.NONE).getValue().toFluxByteBuffer().subscribe();
        subscription.dispose();

        assertEquals(1, responseCloseCount.get());
    }

    @Test
    public void binaryDataResponseToStreamReadsWithoutWaitingForCompletion() throws IOException {
        byte[] firstChunk = new byte[8192];
        byte[] secondChunk = new byte[8192];
        firstChunk[0] = 1;
        secondChunk[0] = 2;
        AtomicInteger responseCloseCount = new AtomicInteger();
        Flux<ByteBuffer> responseBody
            = Flux.concat(Flux.just(ByteBuffer.wrap(firstChunk), ByteBuffer.wrap(secondChunk)), Flux.never());
        TestInterface testInterface = createBinaryDataService(responseBody, responseCloseCount);

        BinaryData binaryData = testInterface.testBinaryDataResponse(Context.NONE).getValue();
        assertFalse(binaryData.isReplayable());

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            try (InputStream stream = binaryData.toStream()) {
                byte[] actualFirstChunk = new byte[firstChunk.length];
                byte[] actualSecondChunk = new byte[secondChunk.length];
                assertEquals(firstChunk.length, stream.read(actualFirstChunk));
                assertEquals(secondChunk.length, stream.read(actualSecondChunk));
                assertArrayEquals(firstChunk, actualFirstChunk);
                assertArrayEquals(secondChunk, actualSecondChunk);
                assertEquals(0, responseCloseCount.get());
            }
        });

        assertEquals(1, responseCloseCount.get());
    }

    @Test
    public void binaryDataResponseToStreamClosesOnError() throws IOException {
        AtomicInteger responseCloseCount = new AtomicInteger();
        TestInterface testInterface
            = createBinaryDataService(Flux.error(new IllegalStateException("Body read failed.")), responseCloseCount);

        try (InputStream stream = testInterface.testBinaryDataResponse(Context.NONE).getValue().toStream()) {
            assertThrows(IOException.class, stream::read);
            assertEquals(1, responseCloseCount.get());
        }

        assertEquals(1, responseCloseCount.get());
    }

    @Test
    public void binaryDataResponseToStreamClosesBeforeFirstRead() throws IOException {
        AtomicInteger responseCloseCount = new AtomicInteger();
        TestInterface testInterface = createBinaryDataService(Flux.never(), responseCloseCount);

        BinaryData responseBody = testInterface.testBinaryDataResponse(Context.NONE).getValue();
        assertInstanceOf(FluxByteBufferContent.class, BinaryDataHelper.getContent(responseBody));
        responseBody.toStream().close();

        assertEquals(1, responseCloseCount.get());
    }

    @Test
    public void binaryDataResponseClosesOnError() {
        AtomicInteger responseCloseCount = new AtomicInteger();
        TestInterface testInterface
            = createBinaryDataService(Flux.error(new IllegalStateException("Body read failed.")), responseCloseCount);

        StepVerifier.create(testInterface.testBinaryDataResponse(Context.NONE).getValue().toFluxByteBuffer())
            .expectErrorMessage("Body read failed.")
            .verify();

        assertEquals(1, responseCloseCount.get());
    }

    private static TestInterface createBinaryDataService(Flux<ByteBuffer> responseBody,
        AtomicInteger responseCloseCount) {
        HttpClient client = new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.error(new IllegalStateException("Async Send API was Invoked."));
            }

            @Override
            public HttpResponse sendSync(HttpRequest request, Context context) {
                return new MockHttpResponse(request, 200) {
                    @Override
                    public BinaryData getBodyAsBinaryData() {
                        return BinaryData.fromFlux(responseBody, null, false).block();
                    }

                    @Override
                    public void close() {
                        responseCloseCount.incrementAndGet();
                        super.close();
                    }
                };
            }
        };
        return RestProxy.create(TestInterface.class, new HttpPipelineBuilder().httpClient(client).build());
    }

    private static Stream<Arguments> mergeRequestOptionsContextSupplier() {
        Map<Object, Object> twoValuesMap = new HashMap<>();
        twoValuesMap.put("key", "value");
        twoValuesMap.put("key2", "value2");

        return Stream.of(
            // Cases where the RequestOptions or it's Context doesn't exist.
            Arguments.of(Context.NONE, null, Collections.emptyMap()),
            Arguments.of(Context.NONE, new RequestOptions(), Collections.emptyMap()),
            Arguments.of(Context.NONE, new RequestOptions().setContext(Context.NONE), Collections.emptyMap()),

            // Case where the RequestOptions Context is merged into an empty Context.
            Arguments.of(Context.NONE, new RequestOptions().setContext(new Context("key", "value")),
                Collections.singletonMap("key", "value")),

            // Case where the RequestOptions Context is merged, without replacement, into an existing Context.
            Arguments.of(new Context("key", "value"), new RequestOptions().setContext(new Context("key2", "value2")),
                twoValuesMap),

            // Case where the RequestOptions Context is merged and overrides an existing Context.
            Arguments.of(new Context("key", "value"), new RequestOptions().setContext(new Context("key", "value2")),
                Collections.singletonMap("key", "value2")));
    }
}
