// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.serializer.DefaultJsonSerializer;
import io.clientcore.core.implementation.util.UriBuilder;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.serializer.ObjectSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
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
    public static final class VoidResponse extends HttpResponse<Void> {
        VoidResponse(HttpRequest request, int statusCode, HttpHeaders headers, Void value) {
            super(request, statusCode, headers, value);
        }
    }

    // 2. HttpResponse<Foo> Type         (Ctr_args: 4)
    public static final class FooSimpleResponse extends HttpResponse<Foo> {
        FooSimpleResponse(HttpRequest request, int statusCode, HttpHeaders headers, Foo value) {
            super(request, statusCode, headers, value);
        }
    }

    // Dummy service client
    public interface FooService {
        VoidResponse getVoidResponse();

        FooSimpleResponse getFooSimpleResponse();
    }

    private static final ObjectSerializer SERIALIZER = new DefaultJsonSerializer();
    private static final HttpRequest HTTP_REQUEST = new HttpRequest(HttpMethod.GET, createUri());
    private static final HttpHeaderName HELLO = HttpHeaderName.fromString("hello");
    private static final HttpHeaders RESPONSE_HEADERS = new HttpHeaders().set(HELLO, "world");
    private static final int RESPONSE_STATUS_CODE = 200;
    private static final Foo FOO = new Foo().setName("foo1");
    private static final byte[] FOO_BYTE_ARRAY = asJsonByteArray(FOO);
    // MOCK RESPONSES
    private static final Response<?> VOID_RESPONSE = new MockHttpResponse(HTTP_REQUEST, RESPONSE_STATUS_CODE,
        RESPONSE_HEADERS, null);
    private static final Response<?> FOO_RESPONSE = new MockHttpResponse(HTTP_REQUEST, RESPONSE_STATUS_CODE,
        RESPONSE_HEADERS, BinaryData.fromBytes(FOO_BYTE_ARRAY));

    // ARRAY HOLDING TEST DATA
    private final Input[] inputs;

    ResponseConstructorsCacheBenchMarkTestData() {
        this.inputs = new Input[2];
        this.inputs[0] = new Input(FooService.class, "getVoidResponse", VOID_RESPONSE, null);
        this.inputs[1] = new Input(FooService.class, "getFooSimpleResponse", FOO_RESPONSE, FOO);
    }

    Input[] inputs() {
        return this.inputs;
    }

    private static URI createUri() {
        try {
            return UriBuilder.parse("http://localhost").toUri();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] asJsonByteArray(Object object) {
        try {
            return SERIALIZER.serializeToBytes(object);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    static class Input {
        private final Type returnType;
        private final Response<?> response;
        private final Object bodyAsObject;

        Input(Class<?> serviceClass, String methodName, Response<?> response, Object bodyAsObject) {
            this.returnType = findMethod(serviceClass, methodName).getGenericReturnType();
            this.response = response;
            this.bodyAsObject = bodyAsObject;
        }

        Type getReturnType() {
            return this.returnType;
        }

        Response<?> getResponse() {
            return this.response;
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
}
