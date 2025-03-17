// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.utils;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.utils.ProgressReporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequestContextTests {
    private static final HttpHeaderName X_MS_FOO = HttpHeaderName.fromString("x-ms-foo");

    @Test
    public void addQueryParam() {
        final HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.POST).setUri(URI.create("http://request.uri"));

        RequestContext context = new RequestContext().addQueryParam("foo", "bar").addQueryParam("$skipToken", "1");

        context.getRequestCallback().accept(request);

        assertTrue(request.getUri().toString().contains("?foo=bar&%24skipToken=1"));
    }

    @Test
    public void addHeader() {
        final HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.POST).setUri(URI.create("http://request.uri"));

        RequestContext context = new RequestContext().addRequestCallback(r -> r.getHeaders()
            .add(new HttpHeader(X_MS_FOO, "bar"))
            .add(new HttpHeader(HttpHeaderName.CONTENT_TYPE, "application/json")));
        context.getRequestCallback().accept(request);

        HttpHeaders headers = request.getHeaders();
        assertEquals("bar", headers.getValue(X_MS_FOO));
        assertEquals("application/json", headers.getValue(HttpHeaderName.CONTENT_TYPE));
    }

    @Test
    public void addRequestCallback() {
        final HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.POST).setUri(URI.create("http://request.uri"));

        RequestContext context
            = new RequestContext().addRequestCallback(r -> r.getHeaders().add(new HttpHeader(X_MS_FOO, "bar")))
                .addRequestCallback(r -> r.setMethod(HttpMethod.GET))
                .addRequestCallback(r -> r.setUri("https://request.uri"))
                .addQueryParam("$skipToken", "1")
                .addRequestCallback(r -> r.getHeaders().set(X_MS_FOO, "baz"));

        context.getRequestCallback().accept(request);

        HttpHeaders headers = request.getHeaders();
        assertEquals("baz", headers.getValue(X_MS_FOO));
        assertEquals(HttpMethod.GET, request.getHttpMethod());
        assertEquals("https://request.uri?%24skipToken=1", request.getUri().toString());
    }

    @Test
    public void simpleContext() {
        Object complexObject = ProgressReporter.withProgressListener(value -> {
        });
        RequestContext context = new RequestContext().putData("stringKey", "value")
            .putData("longKey", 10L)
            .putData("booleanKey", true)
            .putData("doubleKey", 42.0)
            .putData("complexObject", complexObject);

        assertEquals("value", context.getData("stringKey"));
        assertEquals("value", context.getData("stringKey"));
        assertEquals(10L, context.getData("longKey"));
        assertEquals(true, context.getData("booleanKey"));
        assertEquals(42.0, context.getData("doubleKey"));
        assertSame(complexObject, context.getData("complexObject"));
        assertNull(context.getData("fakeKey"));
    }

    @Test
    public void keysCannotBeNull() {
        RequestContext context = new RequestContext();
        assertThrows(NullPointerException.class, () -> context.putData(null, null));
        assertThrows(NullPointerException.class, () -> context.putData(null, "value"));
    }

    @ParameterizedTest
    @MethodSource("addDataSupplier")
    public void addContext(String key, String value, String expectedOriginalValue) {
        RequestContext context = new RequestContext().putData("key", "value").putData(key, value);

        assertEquals(value, context.getData(key));
        assertEquals(expectedOriginalValue, context.getData("key"));
    }

    private static Stream<Arguments> addDataSupplier() {
        return Stream.of(
            // Adding with same key overwrites value.
            Arguments.of("key", "newValue", "newValue"), Arguments.of("key", "", ""),

            // New values.
            Arguments.of("key2", "newValue", "value"), Arguments.of("key2", "", "value"));
    }

    @Test
    public void putValueCanBeNull() {
        RequestContext context = new RequestContext().putData("key", null);

        assertNull(context.getData("key"));
    }

    @Test
    public void getValueKeyCannotBeNull() {
        assertThrows(NullPointerException.class, () -> RequestContext.none().getData(null));
    }
}
