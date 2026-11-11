// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Head;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.implementation.util.HttpUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for {@link AsyncRestProxy}
 */
public class AsyncRestProxyTests {

    private SwaggerInterfaceParser swaggerInterfaceParser;

    @Host("https://example.com")
    @ServiceInterface(name = "async-rest-proxy-tests")
    private interface MockService {
        @Head("headBoolean")
        boolean headBoolean();

        @Get("getByteArray")
        byte[] getByteArray();

        @Get("getInputStream")
        InputStream getInputStream();

        @Get("getStreamResponse")
        Flux<ByteBuffer> getStreamResponse();

        @Get("getStreamingResponse")
        @ExpectedResponses({ 200 })
        Mono<Response<BinaryData>> getStreamingResponse(RequestOptions options, Context context);
    }

    @BeforeEach
    public void beforeEach() {
        swaggerInterfaceParser = new SwaggerInterfaceParser(MockService.class);
    }

    public static Stream<Arguments> handleBodyReturnTypeBoolean() {
        return Stream.of(Arguments.of(boolean.class, 200, true), Arguments.of(Boolean.class, 404, false));
    }

    /**
     * Validates scenario for decoding boolean return values.
     */
    @ParameterizedTest
    @MethodSource
    public void handleBodyReturnTypeBoolean(Type returnType, int statusCode, Boolean expectedValue)
        throws NoSuchMethodException {

        // Arrange
        SwaggerMethodParser methodParser
            = swaggerInterfaceParser.getMethodParser(MockService.class.getDeclaredMethod("headBoolean"));
        HttpResponse httpResponse = new MockHttpResponse(null, statusCode);

        // Act
        StepVerifier
            .create(AsyncRestProxy.handleBodyReturnType(httpResponse, ignored -> null, methodParser, returnType))
            .assertNext(value -> {
                assertTrue(value instanceof Boolean);
                assertEquals(expectedValue, value);
            })
            .expectComplete()
            .verify();
    }

    /**
     * Validates scenario for decoding boolean return values.
     */
    @Test
    public void handleBodyReturnTypeByte() throws NoSuchMethodException {
        // Arrange
        SwaggerMethodParser methodParser
            = swaggerInterfaceParser.getMethodParser(MockService.class.getDeclaredMethod("getByteArray"));

        final byte[] expectedBytes = "hello".getBytes(StandardCharsets.UTF_8);
        final Type returnType = byte[].class;

        HttpResponse httpResponse = new MockHttpResponse(null, 200, new HttpHeaders(), expectedBytes);

        // Act
        StepVerifier
            .create(AsyncRestProxy.handleBodyReturnType(httpResponse, ignored -> null, methodParser, returnType))
            .assertNext(value -> {
                assertTrue(value instanceof byte[]);
                assertArraysEqual(expectedBytes, (byte[]) value);
            })
            .expectComplete()
            .verify();
    }

    /**
     * Validates scenario for decoding input stream.
     */
    @Test
    public void handleBodyReturnTypeInputStream() throws NoSuchMethodException {
        // Arrange
        SwaggerMethodParser methodParser
            = swaggerInterfaceParser.getMethodParser(MockService.class.getDeclaredMethod("getInputStream"));

        final byte[] expectedBytes = "hello".getBytes(StandardCharsets.UTF_8);
        final Type returnType = InputStream.class;

        HttpResponse httpResponse = new MockHttpResponse(null, 200, new HttpHeaders(), expectedBytes);

        // Act
        StepVerifier
            .create(AsyncRestProxy.handleBodyReturnType(httpResponse, ignored -> null, methodParser, returnType))
            .assertNext(value -> {
                assertTrue(value instanceof InputStream);

                try {
                    InputStream inputStream = (InputStream) value;
                    final int available = inputStream.available();

                    assertEquals(expectedBytes.length, available);

                    final byte[] actualBytes = new byte[available];
                    final int read = inputStream.read(actualBytes);

                    assertEquals(available, read, "Should have read same number of bytes available.");
                    assertArraysEqual(expectedBytes, actualBytes);
                } catch (IOException e) {
                    fail("Should not have thrown an error.", e);
                }
            })
            .expectComplete()
            .verify();
    }

    /**
     * Validates scenario for a response with different content type headers that maps to a
     * replayable BinaryData if the content type is not a stream.
     */
    @ParameterizedTest
    @MethodSource("getResponseHeaderAndReplayability")
    public void handleBodyReturnTypeStream(HttpHeaders headers, boolean isReplayable) throws NoSuchMethodException {
        // Arrange
        SwaggerMethodParser methodParser
            = swaggerInterfaceParser.getMethodParser(MockService.class.getDeclaredMethod("getStreamResponse"));

        final byte[] expectedBytes = "hello".getBytes(StandardCharsets.UTF_8);
        final Type returnType = BinaryData.class;

        HttpResponse httpResponse = new MockHttpResponse(null, 200, headers, expectedBytes);

        // Act
        StepVerifier
            .create(AsyncRestProxy.handleBodyReturnType(httpResponse, ignored -> null, methodParser, returnType))
            .assertNext(value -> {
                assertTrue(value instanceof BinaryData);
                BinaryData binaryData = (BinaryData) value;
                assertEquals(isReplayable, binaryData.isReplayable());
                byte[] actualBytes = binaryData.toBytes();
                assertEquals(expectedBytes.length, actualBytes.length);
                assertArraysEqual(expectedBytes, actualBytes);
            })
            .expectComplete()
            .verify();
    }

    public static Stream<Arguments> getResponseHeaderAndReplayability() {
        return Stream.of(
            Arguments.of(new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM),
                true),
            Arguments.of(new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/event-stream"), false),
            Arguments.of(new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/event-stream; charset=utf-8"), false),
            Arguments.of(new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_JSON), true),
            Arguments.of(new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml"), true));
    }

    @ParameterizedTest
    @MethodSource("streamingResponseOwnershipSupplier")
    public void streamingResponseUsesResponseContentType(String accept, String contentType, boolean replayable,
        int expectedCloseCount) {
        byte[] expectedBytes = "hello".getBytes(StandardCharsets.UTF_8);
        AtomicInteger responseCloseCount = new AtomicInteger();
        HttpClient client = request -> Mono.just(new MockHttpResponse(request, 200,
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType), expectedBytes) {
            @Override
            public void close() {
                responseCloseCount.incrementAndGet();
                super.close();
            }
        });
        MockService service = RestProxy.create(MockService.class, new HttpPipelineBuilder().httpClient(client).build());
        RequestOptions options = new RequestOptions();
        if (accept != null) {
            options.setHeader(HttpHeaderName.ACCEPT, accept);
        }

        Response<BinaryData> response = service.getStreamingResponse(options, Context.NONE).block();

        assertEquals(replayable, response.getValue().isReplayable());
        assertArraysEqual(expectedBytes, response.getValue().toBytes());
        assertEquals(expectedCloseCount, responseCloseCount.get());
    }

    @Test
    public void cancellingStreamingBinaryDataClosesResponse() {
        byte[] expectedBytes = "hello".getBytes(StandardCharsets.UTF_8);
        AtomicInteger responseCloseCount = new AtomicInteger();
        HttpClient client = request -> Mono.just(new MockHttpResponse(request, 200,
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/event-stream"), expectedBytes) {
            @Override
            public Flux<ByteBuffer> getBody() {
                return Flux.concat(Flux.just(ByteBuffer.wrap(expectedBytes)), Flux.never());
            }

            @Override
            public void close() {
                responseCloseCount.incrementAndGet();
                super.close();
            }
        });
        MockService service = RestProxy.create(MockService.class, new HttpPipelineBuilder().httpClient(client).build());

        Flux<ByteBuffer> body = service.getStreamingResponse(new RequestOptions(), Context.NONE)
            .flatMapMany(response -> response.getValue().toFluxByteBuffer());

        StepVerifier.create(body)
            .assertNext(buffer -> assertArraysEqual(expectedBytes, buffer.array()))
            .thenCancel()
            .verify();
        assertEquals(1, responseCloseCount.get());
    }

    private static Stream<Arguments> streamingResponseOwnershipSupplier() {
        return Stream.of(Arguments.of("text/event-stream", ContentType.APPLICATION_JSON, true, 0),
            Arguments.of(null, "text/event-stream; charset=utf-8", false, 1));
    }

    private static final Context STREAM_RESPONSE_BODY
        = Context.NONE.addData(HttpUtils.AZURE_STREAM_RESPONSE_BODY, true);

    @ParameterizedTest
    @ValueSource(strings = { ContentType.APPLICATION_OCTET_STREAM, "text/plain", "image/png" })
    public void streamResponseBodyStreamsBinaryData(String contentType) {
        byte[] expectedBytes = "hello".getBytes(StandardCharsets.UTF_8);
        AtomicBoolean bodySubscribed = new AtomicBoolean();
        AtomicInteger responseCloseCount = new AtomicInteger();
        MockService service = createService(
            request -> new StreamingHttpResponse(request, contentType, expectedBytes.length, Flux.defer(() -> {
                bodySubscribed.set(true);
                return Flux.just(ByteBuffer.wrap(expectedBytes));
            }), responseCloseCount));

        Response<BinaryData> response
            = service.getStreamingResponse(new RequestOptions(), STREAM_RESPONSE_BODY).block();

        assertFalse(bodySubscribed.get(), "The body must not be read before the response is emitted.");
        assertFalse(response.getValue().isReplayable());
        assertEquals(expectedBytes.length, response.getValue().getLength());
        assertArraysEqual(expectedBytes, response.getValue().toBytes());
        assertEquals(1, responseCloseCount.get());
    }

    @Test
    public void streamedResponseBodyErrorSurfacesWhenBodyIsRead() {
        byte[] firstChunk = "hello".getBytes(StandardCharsets.UTF_8);
        AtomicInteger responseCloseCount = new AtomicInteger();
        MockService service
            = createService(request -> new StreamingHttpResponse(request, ContentType.APPLICATION_OCTET_STREAM, 10,
                Flux.concat(Flux.just(ByteBuffer.wrap(firstChunk)), Flux.error(new IOException("Connection reset"))),
                responseCloseCount));

        Response<BinaryData> response
            = service.getStreamingResponse(new RequestOptions(), STREAM_RESPONSE_BODY).block();

        StepVerifier.create(response.getValue().toFluxByteBuffer())
            .assertNext(buffer -> assertArraysEqual(firstChunk, FluxUtil.byteBufferToArray(buffer)))
            .verifyError(IOException.class);
        assertEquals(1, responseCloseCount.get());
    }

    @ParameterizedTest
    @MethodSource("bufferedByDefaultSupplier")
    public void binaryDataIsBufferedByDefault(Context context) {
        byte[] expectedBytes = "hello".getBytes(StandardCharsets.UTF_8);
        AtomicBoolean bodySubscribed = new AtomicBoolean();
        MockService service = createService(request -> new StreamingHttpResponse(request,
            ContentType.APPLICATION_OCTET_STREAM, expectedBytes.length, Flux.defer(() -> {
                bodySubscribed.set(true);
                return Flux.just(ByteBuffer.wrap(expectedBytes));
            }), new AtomicInteger()));

        Response<BinaryData> response = service.getStreamingResponse(new RequestOptions(), context).block();

        assertTrue(bodySubscribed.get());
        assertTrue(response.getValue().isReplayable());
        assertArraysEqual(expectedBytes, response.getValue().toBytes());
    }

    private static Stream<Arguments> bufferedByDefaultSupplier() {
        return Stream.of(Arguments.of(Context.NONE),
            Arguments.of(Context.NONE.addData(HttpUtils.AZURE_STREAM_RESPONSE_BODY, false)));
    }

    private static MockService createService(Function<HttpRequest, HttpResponse> responseFactory) {
        HttpClient client = request -> Mono.just(responseFactory.apply(request));
        return RestProxy.create(MockService.class, new HttpPipelineBuilder().httpClient(client).build());
    }

    /**
     * A response whose body is a live, single-use stream, using the default {@link HttpResponse#getBodyAsBinaryData()}.
     */
    private static final class StreamingHttpResponse extends HttpResponse {
        private final HttpHeaders headers;
        private final Flux<ByteBuffer> body;
        private final AtomicInteger closeCount;

        StreamingHttpResponse(HttpRequest request, String contentType, long contentLength, Flux<ByteBuffer> body,
            AtomicInteger closeCount) {
            super(request);
            this.headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType)
                .set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength));
            this.body = body;
            this.closeCount = closeCount;
        }

        @Override
        public int getStatusCode() {
            return 200;
        }

        @Override
        @Deprecated
        public String getHeaderValue(String name) {
            return headers.getValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return body;
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return FluxUtil.collectBytesInByteBufferStream(body);
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
        public void close() {
            closeCount.incrementAndGet();
        }
    }
}
