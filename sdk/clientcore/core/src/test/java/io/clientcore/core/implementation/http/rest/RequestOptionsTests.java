// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.utils;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
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

public class RequestOptionsTests {
    private static final HttpHeaderName X_MS_FOO = HttpHeaderName.fromString("x-ms-foo");

    @Test
    public void addQueryParam() {
        final HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.POST).setUri(URI.create("http://request.uri"));

        RequestOptions options = new RequestOptions().addQueryParam("foo", "bar").addQueryParam("$skipToken", "1");

        options.getRequestCallback().accept(request);

        assertTrue(request.getUri().toString().contains("?foo=bar&%24skipToken=1"));
    }

    @Test
    public void addHeader() {
        final HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.POST).setUri(URI.create("http://request.uri"));

        RequestOptions options = new RequestOptions().addRequestCallback(r -> r.getHeaders()
            .add(new HttpHeader(X_MS_FOO, "bar"))
            .add(new HttpHeader(HttpHeaderName.CONTENT_TYPE, "application/json")));
        options.getRequestCallback().accept(request);

        HttpHeaders headers = request.getHeaders();
        assertEquals("bar", headers.getValue(X_MS_FOO));
        assertEquals("application/json", headers.getValue(HttpHeaderName.CONTENT_TYPE));
    }

    @Test
    public void addRequestCallback() {
        final HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.POST).setUri(URI.create("http://request.uri"));

        RequestOptions options
            = new RequestOptions().addRequestCallback(r -> r.getHeaders().add(new HttpHeader(X_MS_FOO, "bar")))
                .addRequestCallback(r -> r.setMethod(HttpMethod.GET))
                .addRequestCallback(r -> r.setUri("https://request.uri"))
                .addQueryParam("$skipToken", "1")
                .addRequestCallback(r -> r.getHeaders().set(X_MS_FOO, "baz"));

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
        RequestOptions options = new RequestOptions().putMetadata("stringKey", "value")
            .putMetadata("longKey", 10L)
            .putMetadata("booleanKey", true)
            .putMetadata("doubleKey", 42.0)
            .putMetadata("complexObject", complexObject);

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
        RequestOptions options = new RequestOptions();
        assertThrows(NullPointerException.class, () -> options.putMetadata(null, null));
        assertThrows(NullPointerException.class, () -> options.putMetadata(null, "value"));
    }

    @ParameterizedTest
    @MethodSource("addDataSupplier")
    public void addContext(String key, String value, String expectedOriginalValue) {
        RequestOptions options = new RequestOptions().putMetadata("key", "value").putMetadata(key, value);

        assertEquals(value, options.getMetadata(key));
        assertEquals(expectedOriginalValue, options.getMetadata("key"));
    }

    @Test
    public void noneIsLocked() {
        assertThrows(IllegalStateException.class, () -> RequestOptions.none().putMetadata("key", "value"));
        assertThrows(IllegalStateException.class, () -> RequestOptions.none().addRequestCallback(request -> {
        }));
        assertThrows(IllegalStateException.class, () -> RequestOptions.none().addQueryParam("key", "value"));
        assertThrows(IllegalStateException.class, () -> RequestOptions.none().addQueryParam("key", "value", true));
        assertThrows(IllegalStateException.class, () -> RequestOptions.none().setLogger(new ClientLogger("test")));
        assertThrows(IllegalStateException.class, () -> RequestOptions.none().setInstrumentationContext(null));
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
        RequestOptions options = new RequestOptions().putMetadata("key", null);

        assertNull(options.getMetadata("key"));
    }

    @Test
    public void getValueKeyCannotBeNull() {
        assertThrows(NullPointerException.class, () -> RequestOptions.none().getMetadata(null));
    }
}
