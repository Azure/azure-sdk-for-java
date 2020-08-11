// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.serializer.HttpResponseDecodeData;
import com.azure.core.implementation.serializer.HttpResponseDecoder;
import com.azure.core.util.IterableStream;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.JacksonAdapter;
import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class ResponseConstructorsCacheBenchMarkTestData {
    // Model type for Http content
    static final class Foo {
        @JsonProperty("name")
        private String name;

        public Foo setName(String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return this.name;
        }
    }

    // Model type for custom Http headers
    static final class FooHeader {
        @JsonProperty("customHdr")
        private String customHdr;

        public String getCustomHdr() {
            return this.customHdr;
        }
    }

    // 1. final VoidResponse               (Ctr_args: 3)
    static final class VoidResponse extends SimpleResponse<Void> {
        VoidResponse(HttpRequest request, int statusCode, HttpHeaders headers, Void value) {
            super(request, statusCode, headers, value);
        }
    }

    // 2. SimpleResponse<Foo> Type         (Ctr_args: 4)
    static final class FooSimpleResponse extends SimpleResponse<Foo> {
        FooSimpleResponse(HttpRequest request,
                                 int statusCode,
                                 HttpHeaders headers,
                                 Foo value) {
            super(request, statusCode, headers, value);
        }
    }

    // 3. final StreamResponse             (Ctr_args: 4) // Final class cannot be extended
    //    StreamResponse(HttpRequest request, int statusCode, HttpHeaders headers, Flux<ByteBuffer> value)

    // 4. ResponseBase<FooHeader, Foo>     (Ctr_args: 5)
    static final class FooResponseBase extends ResponseBase<FooHeader, Foo> {
        FooResponseBase(HttpRequest request,
                               int statusCode,
                               HttpHeaders headers,
                               Foo value,
                               FooHeader decodedHeaders) {
            super(request, statusCode, headers, value, decodedHeaders);
        }
    }

    // 5. PagedResponseBase<FooHeader, Foo> (Ctr_args: 5)
    static final class FooPagedResponseBase extends PagedResponseBase<FooHeader, Foo> {
        FooPagedResponseBase(HttpRequest request,
                                    int statusCode,
                                    HttpHeaders headers,
                                    Page<Foo> page,
                                    FooHeader decodedHeaders) {
            super(request, statusCode, headers, page, decodedHeaders);
        }
    }

    // Dummy service client
    public interface FooService {
        VoidResponse getVoidResponse();
        FooSimpleResponse getFooSimpleResponse();
        StreamResponse getStreamResponse();
        FooResponseBase getResponseBaseFoo();
        FooPagedResponseBase getPagedResponseBaseFoo();
    }

    // Mock Http Response
    static final class MockResponse extends HttpResponse {
        private final int statusCode;
        private final HttpHeaders headers;
        private final Mono<byte[]> bodyBytes;
        private final Flux<ByteBuffer> bodyBb;
        private final Mono<String> bodyString;

        MockResponse(HttpRequest request, int statusCode, HttpHeaders headers, byte[] body) {
            super(request);
            this.statusCode = statusCode;
            this.headers = headers;
            this.bodyBytes = body == null ? Mono.empty() : Mono.just(body);
            this.bodyBb = body == null ? Flux.empty() : Flux.just(ByteBuffer.wrap(body));
            this.bodyString = body == null
                    ? Mono.empty()
                    : Mono.just(new String(body, Charset.defaultCharset()));
        }

        @Override
        public int getStatusCode() {
            return this.statusCode;
        }

        @Override
        public String getHeaderValue(String name) {
            return this.headers.getValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return this.headers;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return bodyBb;
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return this.bodyBytes;
        }

        @Override
        public Mono<String> getBodyAsString() {
            return this.bodyString;
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return this.bodyString;
        }
    }

    private static final SerializerAdapter SERIALIZER_ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();
    private static final HttpResponseDecoder RESPONSE_DECODER = new HttpResponseDecoder(SERIALIZER_ADAPTER);
    //
    private static final HttpRequest HTTP_REQUEST = new HttpRequest(HttpMethod.GET, createUrl());
    private static final HttpHeaders RESPONSE_HEADERS = new HttpHeaders().put("hello", "world");
    private static final HttpHeaders RESPONSE_CUSTOM_HEADERS = new HttpHeaders()
            .put("hello", "world")          // General header
            .put("customHdr", "customVal"); // Custom header
    private static final int RESPONSE_STATUS_CODE = 200;
    private static final Foo FOO = new Foo().setName("foo1");
    private static final byte[] FOO_BYTE_ARRAY = asJsonByteArray(FOO);
    private static final byte[] STREAM_BYTE_ARRAY = new byte[1];
    private static final Page<Foo> PAGE_FOO = new Page<Foo>() {
        @Override
        public IterableStream<Foo> getElements() {
            List<Foo> items = new ArrayList<>();
            items.add(FOO);
            return new IterableStream<Foo>(items);
        }

        @Override
        public String getContinuationToken() {
            return null;
        }
    };
    private static final byte[] PAGE_FOO_BYTE_ARRAY = asJsonByteArray(PAGE_FOO);
    // MOCK RESPONSES
    private static final Mono<HttpResponse> VOID_RESPONSE = Mono.just(new MockResponse(HTTP_REQUEST,
            RESPONSE_STATUS_CODE,
            RESPONSE_HEADERS,
            null));
    private static final Mono<HttpResponse> FOO_RESPONSE = Mono.just(new MockResponse(HTTP_REQUEST,
            RESPONSE_STATUS_CODE,
            RESPONSE_HEADERS,
            FOO_BYTE_ARRAY));
    private static final Mono<HttpResponse> STREAM_RESPONSE = Mono.just(new MockResponse(HTTP_REQUEST,
            RESPONSE_STATUS_CODE,
            RESPONSE_HEADERS,
            STREAM_BYTE_ARRAY));
    private static final Mono<HttpResponse> FOO_PAGE_RESPONSE = Mono.just(new MockResponse(HTTP_REQUEST,
            RESPONSE_STATUS_CODE,
            RESPONSE_HEADERS,
            PAGE_FOO_BYTE_ARRAY));
    private static final Mono<HttpResponse> FOO_CUSTOM_HEADER_RESPONSE = Mono.just(new MockResponse(HTTP_REQUEST,
            RESPONSE_STATUS_CODE,
            RESPONSE_CUSTOM_HEADERS,
            FOO_BYTE_ARRAY));
    // ARRAY HOLDING TEST DATA
    private final Input[] inputs;

    ResponseConstructorsCacheBenchMarkTestData() {
        this.inputs = new Input[5];
        this.inputs[0] = new Input(RESPONSE_DECODER,
                FooService.class,
                "getVoidResponse",
                VOID_RESPONSE,
                null);
        this.inputs[1] = new Input(RESPONSE_DECODER,
                FooService.class,
                "getFooSimpleResponse",
                FOO_RESPONSE,
                FOO);
        this.inputs[2] = new Input(RESPONSE_DECODER,
                FooService.class,
                "getStreamResponse",
                STREAM_RESPONSE,
                null);
        this.inputs[3] = new Input(RESPONSE_DECODER,
                FooService.class,
                "getResponseBaseFoo",
                FOO_CUSTOM_HEADER_RESPONSE,
                FOO);
        this.inputs[4] = new Input(RESPONSE_DECODER,
                FooService.class,
                "getPagedResponseBaseFoo",
                FOO_PAGE_RESPONSE,
                PAGE_FOO);
    }

    Input[] inputs() {
        return this.inputs;
    }

    private static URL createUrl() {
        try {
            return new URL("http://localhost");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] asJsonByteArray(Object object) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            SERIALIZER_ADAPTER.serialize(object, SerializerEncoding.JSON, stream);

            return stream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class Input {
        private final Type returnType;
        private final HttpResponseDecoder.HttpDecodedResponse decodedResponse;
        private final Object bodyAsObject;

        Input(HttpResponseDecoder decoder,
              Class<?> serviceClass,
              String methodName,
              Mono<HttpResponse> httpResponse,
              Object bodyAsObject) {
            this.returnType = findMethod(serviceClass, methodName).getGenericReturnType();
            this.decodedResponse = decoder.decode(httpResponse, new HttpResponseDecodeData() {
                @Override
                public Type getReturnType() {
                    return returnType;
                }

                @Override
                public boolean isExpectedResponseStatusCode(int statusCode) {
                    return false;
                }
            }).block();
            this.bodyAsObject = bodyAsObject;
        }

        Type returnType() {
            return this.returnType;
        }

        HttpResponseDecoder.HttpDecodedResponse decodedResponse() {
            return this.decodedResponse;
        }

        Object bodyAsObject() {
            return this.decodedResponse;
        }

        private Method findMethod(Class<?> cls, String methodName) {
            Optional<Method> optMethod = Arrays.stream(cls.getDeclaredMethods())
                    .filter(m -> m.getName().equalsIgnoreCase(methodName))
                    .findFirst();
            if (optMethod.isPresent()) {
                return optMethod.get();
            } else {
                throw new RuntimeException("Method with name '" + methodName + "' not found.");
            }
        }
    }
}
