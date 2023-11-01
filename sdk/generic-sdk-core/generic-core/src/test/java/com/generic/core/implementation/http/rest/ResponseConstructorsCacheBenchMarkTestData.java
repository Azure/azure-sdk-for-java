// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.rest;

import com.generic.core.http.SimpleResponse;
import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.implementation.http.serializer.DefaultJsonSerializer;
import com.generic.core.implementation.http.serializer.HttpResponseDecodeData;
import com.generic.core.implementation.http.serializer.HttpResponseDecoder;
import com.generic.core.implementation.util.UrlBuilder;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Headers;
import com.generic.core.util.serializer.ObjectSerializer;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

class ResponseConstructorsCacheBenchMarkTestData {
    // Model type for Http content
    static final class Foo {
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

        private String customHdr;

        public String getCustomHdr() {
            return this.customHdr;
        }
    }

    // 1. final VoidResponse               (Ctr_args: 3)
    static final class VoidResponse extends SimpleResponse<Void> {
        VoidResponse(HttpRequest request, int statusCode, Headers headers, Void value) {
            super(request, statusCode, headers, value);
        }
    }

    // 2. SimpleResponse<Foo> Type         (Ctr_args: 4)
    static final class FooSimpleResponse extends SimpleResponse<Foo> {
        FooSimpleResponse(HttpRequest request, int statusCode, Headers headers, Foo value) {
            super(request, statusCode, headers, value);
        }
    }

    // Dummy service client
    public interface FooService {
        VoidResponse getVoidResponse();

        FooSimpleResponse getFooSimpleResponse();
    }

    // Mock Http Response
    static final class MockResponse extends HttpResponse {
        private final int statusCode;
        private final Headers headers;
        private final byte[] bodyBytes;

        MockResponse(HttpRequest request, int statusCode, Headers headers, byte[] body) {
            super(request, body);

            this.statusCode = statusCode;
            this.headers = headers;
            this.bodyBytes = body;
        }

        @Override
        public int getStatusCode() {
            return this.statusCode;
        }

        @Override
        public Headers getHeaders() {
            return this.headers;
        }

        @Override
        public BinaryData getBody() {
            return BinaryData.fromBytes(bodyBytes);
        }
    }

    private static final ObjectSerializer SERIALIZER = new DefaultJsonSerializer();
    private static final HttpResponseDecoder RESPONSE_DECODER = new HttpResponseDecoder(SERIALIZER);
    private static final HttpRequest HTTP_REQUEST = new HttpRequest(HttpMethod.GET, createUrl());
    private static final HttpHeaderName HELLO = HttpHeaderName.fromString("hello");
    private static final HttpHeaderName CUSTOM_HDR = HttpHeaderName.fromString("customHdr");
    private static final Headers RESPONSE_HEADERS = new Headers().set(HELLO, "world");
    private static final Headers RESPONSE_CUSTOM_HEADERS = new Headers()
        .set(HELLO, "world")           // General header
        .set(CUSTOM_HDR, "customVal"); // Custom header
    private static final int RESPONSE_STATUS_CODE = 200;
    private static final Foo FOO = new Foo().setName("foo1");
    private static final byte[] FOO_BYTE_ARRAY = asJsonByteArray(FOO);
    private static final byte[] STREAM_BYTE_ARRAY = new byte[1];
    // MOCK RESPONSES
    private static final HttpResponse VOID_RESPONSE = new MockResponse(HTTP_REQUEST, RESPONSE_STATUS_CODE,
        RESPONSE_HEADERS, null);
    private static final HttpResponse FOO_RESPONSE = new MockResponse(HTTP_REQUEST, RESPONSE_STATUS_CODE,
        RESPONSE_HEADERS, FOO_BYTE_ARRAY);
    private static final HttpResponse STREAM_RESPONSE = new MockResponse(HTTP_REQUEST, RESPONSE_STATUS_CODE,
        RESPONSE_HEADERS, STREAM_BYTE_ARRAY);
    private static final HttpResponse FOO_CUSTOM_HEADER_RESPONSE = new MockResponse(HTTP_REQUEST, RESPONSE_STATUS_CODE,
        RESPONSE_CUSTOM_HEADERS, FOO_BYTE_ARRAY);

    // ARRAY HOLDING TEST DATA
    private final Input[] inputs;

    ResponseConstructorsCacheBenchMarkTestData() {
        this.inputs = new Input[4];
        this.inputs[0] = new Input(RESPONSE_DECODER, FooService.class, "getVoidResponse", VOID_RESPONSE, null);
        this.inputs[1] = new Input(RESPONSE_DECODER, FooService.class, "getFooSimpleResponse", FOO_RESPONSE, FOO);
        this.inputs[2] = new Input(RESPONSE_DECODER, FooService.class, "getStreamResponse", STREAM_RESPONSE, null);
        this.inputs[3] =
            new Input(RESPONSE_DECODER, FooService.class, "getResponseBaseFoo", FOO_CUSTOM_HEADER_RESPONSE, FOO);
    }

    Input[] inputs() {
        return this.inputs;
    }

    private static URL createUrl() {
        try {
            return UrlBuilder.parse("http://localhost").toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] asJsonByteArray(Object object) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        SERIALIZER.serialize(stream, object);

        return stream.toByteArray();
    }

    static class Input {
        private final Type returnType;
        private final HttpResponseDecoder.HttpDecodedResponse decodedResponse;
        private final Object bodyAsObject;

        Input(HttpResponseDecoder decoder, Class<?> serviceClass, String methodName, HttpResponse httpResponse,
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

                @Override
                public boolean isHeadersEagerlyConverted() {
                    return false;
                }
            });
            this.bodyAsObject = bodyAsObject;
        }

        Type returnType() {
            return this.returnType;
        }

        HttpResponseDecoder.HttpDecodedResponse decodedResponse() {
            return this.decodedResponse;
        }

        Object bodyAsObject() {
            return this.bodyAsObject;
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
