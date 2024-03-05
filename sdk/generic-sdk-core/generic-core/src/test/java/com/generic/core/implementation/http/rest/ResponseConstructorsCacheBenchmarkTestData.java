// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.rest;

import com.generic.core.http.MockHttpResponse;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.implementation.http.serializer.DefaultJsonSerializer;
import com.generic.core.implementation.http.serializer.HttpResponseDecodeData;
import com.generic.core.implementation.util.UrlBuilder;
import com.generic.core.models.HeaderName;
import com.generic.core.models.Headers;
import com.generic.core.util.serializer.ObjectSerializer;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

import static com.generic.core.implementation.http.serializer.HttpResponseBodyDecoder.decodeByteArray;

class ResponseConstructorsCacheBenchmarkTestData {
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
    public static final class VoidResponse extends HttpResponse<Void> {
        VoidResponse(HttpRequest request, int statusCode, Headers headers, Void value) {
            super(request, statusCode, headers, value);
        }
    }

    // 2. HttpResponse<Foo> Type         (Ctr_args: 4)
    public static final class FooSimpleResponse extends HttpResponse<Foo> {
        FooSimpleResponse(HttpRequest request, int statusCode, Headers headers, Foo value) {
            super(request, statusCode, headers, value);
        }
    }

    // Dummy service client
    public interface FooService {
        VoidResponse getVoidResponse();

        FooSimpleResponse getFooSimpleResponse();
    }

    private static final ObjectSerializer SERIALIZER = new DefaultJsonSerializer();
    private static final HttpRequest HTTP_REQUEST = new HttpRequest(HttpMethod.GET, createUrl());
    private static final HeaderName HELLO = HeaderName.fromString("hello");
    private static final Headers RESPONSE_HEADERS = new Headers().set(HELLO, "world");
    private static final int RESPONSE_STATUS_CODE = 200;
    private static final Foo FOO = new Foo().setName("foo1");
    private static final byte[] FOO_BYTE_ARRAY = asJsonByteArray(FOO);
    private static final Type voidType = findMethod(FooService.class, "getVoidResponse").getGenericReturnType();
    private static final Type fooType = findMethod(FooService.class, "getFooSimpleResponse").getGenericReturnType();
    // MOCK RESPONSES
    private static final HttpResponse<?> VOID_RESPONSE =
        prepareMockResponse(HTTP_REQUEST.copy(), voidType, null);
    private static final HttpResponse<?> FOO_RESPONSE =
        prepareMockResponse(HTTP_REQUEST.copy(), fooType, FOO_BYTE_ARRAY);

    // ARRAY HOLDING TEST DATA
    private final Input[] inputs;

    ResponseConstructorsCacheBenchmarkTestData() {
        this.inputs = new Input[2];
        this.inputs[0] = new Input(VOID_RESPONSE, voidType, null);
        this.inputs[1] = new Input(FOO_RESPONSE, fooType, FOO);
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

        SERIALIZER.serializeToStream(stream, object);

        return stream.toByteArray();
    }

    static class Input {
        private final Type returnType;
        private final HttpResponse<?> httpResponse;
        private final Object bodyAsObject;

        Input(HttpResponse<?> httpResponse, Type returnType, Object bodyAsObject) {
            this.returnType = returnType;
            this.httpResponse = httpResponse;
            this.bodyAsObject = bodyAsObject;
        }

        Type getReturnType() {
            return this.returnType;
        }

        HttpResponse<?> getResponse() {
            return this.httpResponse;
        }

        Object getBodyAsObject() {
            return this.bodyAsObject;
        }
    }

    private static Method findMethod(Class<?> clazz, String methodName) {
        Optional<Method> optMethod = Arrays.stream(clazz.getDeclaredMethods())
            .filter(m -> m.getName().equalsIgnoreCase(methodName))
            .findFirst();

        if (optMethod.isPresent()) {
            return optMethod.get();
        } else {
            throw new RuntimeException("Method with name '" + methodName + "' not found.");
        }
    }

    private static MockHttpResponse prepareMockResponse(HttpRequest request, Type returnType, byte[] bodyBytes) {
        request.setResponsBodyDeserializationCallback((response) -> {
            HttpResponse<?> httpResponse = (HttpResponse<?>) response;

            return decodeByteArray(httpResponse.getBody().toBytes(), httpResponse, SERIALIZER,
                new HttpResponseDecodeData() {
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
        });

        return new MockHttpResponse(request, RESPONSE_STATUS_CODE, RESPONSE_HEADERS, bodyBytes);
    }
}
