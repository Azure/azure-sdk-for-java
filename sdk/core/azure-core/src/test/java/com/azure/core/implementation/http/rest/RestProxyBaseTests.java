// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.clients.NoOpHttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RestProxyBaseTests {
    @Host("https://example.com/root")
    @ServiceInterface(name = "requestConstruction")
    interface TestService {
        @Post("{path}")
        @ExpectedResponses({ 200 })
        Response<Void> post(@PathParam(value = "path", encoded = true) String path,
            @BodyParam("application/json") String body);
    }

    @ParameterizedTest
    @MethodSource("urlCompositionSupplier")
    public void requestUrlCompositionIsPreserved(String path, String expected, boolean async)
        throws ReflectiveOperationException, IOException {
        SerializerAdapter serializer = JacksonAdapter.createDefaultSerializerAdapter();
        RestProxyBase proxy = createProxy(serializer, async);
        SwaggerMethodParser parser
            = new SwaggerMethodParser(TestService.class.getDeclaredMethod("post", String.class, String.class));

        HttpRequest request = proxy.createHttpRequest(parser, serializer, async, new Object[] { path, null });

        assertEquals(expected, request.getUrl().toString());
    }

    private static Stream<Arguments> urlCompositionSupplier() {
        return Stream
            .of(Arguments.of(null, "https://example.com/root"), Arguments.of("", "https://example.com/root"),
                Arguments.of("/", "https://example.com/root"), Arguments.of("items", "https://example.com/root/items"),
                Arguments.of("/items", "https://example.com/root/items"),
                Arguments.of("items?token=%2F%3A", "https://example.com/root/items?token=%2F%3A"),
                Arguments.of("/items:name", "https://example.com/root/items:name"),
                Arguments.of("host:8080/items", "https://example.com/root/host:8080/items"),
                Arguments.of("https://other.example.com:8443/items?token=%2F%3A",
                    "https://other.example.com:8443/items?token=%2F%3A"))
            .flatMap(args -> Stream.of(Arguments.of(args.get()[0], args.get()[1], false),
                Arguments.of(args.get()[0], args.get()[1], true)));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void colonContainingPathRetainsPortValidation(boolean async) throws ReflectiveOperationException {
        SerializerAdapter serializer = JacksonAdapter.createDefaultSerializerAdapter();
        RestProxyBase proxy = createProxy(serializer, async);
        SwaggerMethodParser parser
            = new SwaggerMethodParser(TestService.class.getDeclaredMethod("post", String.class, String.class));

        assertThrows(NumberFormatException.class,
            () -> proxy.createHttpRequest(parser, serializer, async, new Object[] { "host:invalid/items", null }));
    }

    @ParameterizedTest
    @MethodSource("contentTypeClassificationSupplier")
    public void contentTypeClassificationIsPreserved(String contentType, boolean expectedJson, boolean async)
        throws ReflectiveOperationException, IOException {
        SerializerAdapter serializer = JacksonAdapter.createDefaultSerializerAdapter();
        RestProxyBase proxy = createProxy(serializer, async);
        SwaggerMethodParser parser
            = new SwaggerMethodParser(TestService.class.getDeclaredMethod("post", String.class, String.class)) {
                @Override
                public String getBodyContentType() {
                    return contentType;
                }
            };

        HttpRequest request = proxy.createHttpRequest(parser, serializer, async, new Object[] { "items", "value" });

        assertEquals(expectedJson ? "\"value\"" : "value", request.getBodyAsBinaryData().toString());
        assertEquals(contentType, request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
    }

    private static Stream<Arguments> contentTypeClassificationSupplier() {
        return Stream
            .of(Arguments.of("application/json", true), Arguments.of(" APPLICATION/JSON ", true),
                Arguments.of("application/json; charset=utf-8", true), Arguments.of("application/json;", true),
                Arguments.of("text/plain; application/json", true), Arguments.of(";application/json", true),
                Arguments.of("application/problem+json", false), Arguments.of("text/plain", false))
            .flatMap(args -> Stream.of(Arguments.of(args.get()[0], args.get()[1], false),
                Arguments.of(args.get()[0], args.get()[1], true)));
    }

    private static RestProxyBase createProxy(SerializerAdapter serializer, boolean async) {
        SwaggerInterfaceParser parser = SwaggerInterfaceParser.getInstance(TestService.class);
        return async
            ? new AsyncRestProxy(new HttpPipelineBuilder().httpClient(new NoOpHttpClient()).build(), serializer, parser)
            : new SyncRestProxy(new HttpPipelineBuilder().httpClient(new NoOpHttpClient()).build(), serializer, parser);
    }
}
