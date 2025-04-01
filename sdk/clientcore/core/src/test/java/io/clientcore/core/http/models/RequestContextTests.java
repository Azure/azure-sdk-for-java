// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

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

        RequestContext options
            = RequestContext.builder().addQueryParam("foo", "bar").addQueryParam("$skipToken", "1").build();

        options.getRequestCallback().accept(request);

        assertTrue(request.getUri().toString().contains("?foo=bar&%24skipToken=1"));
    }

    @Test
    public void addHeader() {
        final HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.POST).setUri(URI.create("http://request.uri"));

        RequestContext options = RequestContext.builder()
            .addRequestCallback(r -> r.getHeaders()
                .add(new HttpHeader(X_MS_FOO, "bar"))
                .add(new HttpHeader(HttpHeaderName.CONTENT_TYPE, "application/json")))
            .build();
        options.getRequestCallback().accept(request);

        HttpHeaders headers = request.getHeaders();
        assertEquals("bar", headers.getValue(X_MS_FOO));
        assertEquals("application/json", headers.getValue(HttpHeaderName.CONTENT_TYPE));
    }

    @Test
    public void addRequestCallback() {
        final HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.POST).setUri(URI.create("http://request.uri"));

        RequestContext options = RequestContext.builder()
            .addRequestCallback(r -> r.getHeaders().add(new HttpHeader(X_MS_FOO, "bar")))
            .addRequestCallback(r -> r.setMethod(HttpMethod.GET))
            .addRequestCallback(r -> r.setUri("https://request.uri"))
            .addQueryParam("$skipToken", "1")
            .addRequestCallback(r -> r.getHeaders().set(X_MS_FOO, "baz"))
            .build();

        options.getRequestCallback().accept(request);

        HttpHeaders headers = request.getHeaders();
        assertEquals("baz", headers.getValue(X_MS_FOO));
        assertEquals(HttpMethod.GET, request.getHttpMethod());
        assertEquals("https://request.uri?%24skipToken=1", request.getUri().toString());
    }

    @Test
    public void simpleContext() {
        Object complexObject = ProgressReporter.withProgressListener(value -> {
        });
        RequestContext options = RequestContext.builder()
            .putMetadata("stringKey", "value")
            .putMetadata("longKey", 10L)
            .putMetadata("booleanKey", true)
            .putMetadata("doubleKey", 42.0)
            .putMetadata("complexObject", complexObject)
            .build();

        assertEquals("value", options.getMetadata("stringKey"));
        assertEquals("value", options.getMetadata("stringKey"));
        assertEquals(10L, options.getMetadata("longKey"));
        assertEquals(true, options.getMetadata("booleanKey"));
        assertEquals(42.0, options.getMetadata("doubleKey"));
        assertSame(complexObject, options.getMetadata("complexObject"));
        assertNull(options.getMetadata("fakeKey"));
    }

    @Test
    public void keysCannotBeNull() {
        assertThrows(NullPointerException.class, () -> RequestContext.builder().putMetadata(null, null));
        assertThrows(NullPointerException.class, () -> RequestContext.builder().putMetadata(null, "value"));
    }

    @ParameterizedTest
    @MethodSource("addDataSupplier")
    public void addContext(String key, String value, String expectedOriginalValue) {
        RequestContext options
            = RequestContext.builder().putMetadata("key", "value").putMetadata(key, value).build();

        assertEquals(value, options.getMetadata(key));
        assertEquals(expectedOriginalValue, options.getMetadata("key"));
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
        RequestContext options = RequestContext.builder().putMetadata("key", null).build();

        assertNull(options.getMetadata("key"));
    }

    @Test
    public void getValueKeyCannotBeNull() {
        assertThrows(NullPointerException.class, () -> RequestContext.none().getMetadata(null));
    }
}
