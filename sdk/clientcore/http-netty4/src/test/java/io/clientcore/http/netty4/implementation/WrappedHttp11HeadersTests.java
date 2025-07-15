// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.utils.DateTimeRfc1123;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link WrappedHttp11Headers}.
 */
public class WrappedHttp11HeadersTests {
    @Test
    public void throwsOnNullClientCoreHttpHeaders() {
        assertThrows(NullPointerException.class, () -> new WrappedHttp11Headers(null));
    }

    @Test
    public void addCharSequenceIterable() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        wrappedHttp11Headers.add(HttpHeaderNames.ACCEPT_ENCODING, Arrays.asList("gzip", "deflate"));

        HttpHeader header = wrappedHttp11Headers.getCoreHeaders().get(HttpHeaderName.ACCEPT_ENCODING);
        assertNotNull(header);
        assertLinesMatch(Arrays.asList("gzip", "deflate"), header.getValues());
    }

    @Test
    public void addCharSequenceIterableThrowsOnNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttp11Headers.add(HttpHeaderNames.ACCEPT_ENCODING, (Iterable<?>) null));
    }

    @Test
    public void addCharSequenceIterableThrowsIfAnyNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttp11Headers.add(HttpHeaderNames.ACCEPT_ENCODING, Arrays.asList("gzip", null)));
    }

    @Test
    public void addCharSequenceObject() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        wrappedHttp11Headers.add(HttpHeaderNames.CONTENT_LENGTH, 42);

        HttpHeader header = wrappedHttp11Headers.getCoreHeaders().get(HttpHeaderName.CONTENT_LENGTH);
        assertNotNull(header);
        assertEquals("42", header.getValue());
    }

    @Test
    public void addCharSequenceObjectThrowsOnNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttp11Headers.add(HttpHeaderNames.CONTENT_LENGTH, (Object) null));
    }

    @Test
    public void addStringIterable() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        wrappedHttp11Headers.add("Accept-Encoding", Arrays.asList("gzip", "deflate"));

        HttpHeader header = wrappedHttp11Headers.getCoreHeaders().get(HttpHeaderName.ACCEPT_ENCODING);
        assertNotNull(header);
        assertLinesMatch(Arrays.asList("gzip", "deflate"), header.getValues());
    }

    @Test
    public void addStringIterableThrowsOnNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        assertThrows(NullPointerException.class, () -> wrappedHttp11Headers.add("Accept-Encoding", (Iterable<?>) null));
    }

    @Test
    public void addStringIterableThrowsIfAnyNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttp11Headers.add("Accept-Encoding", Arrays.asList("gzip", null)));
    }

    @Test
    public void addStringObject() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        wrappedHttp11Headers.add("Content-Length", 42);

        HttpHeader header = wrappedHttp11Headers.getCoreHeaders().get(HttpHeaderName.CONTENT_LENGTH);
        assertNotNull(header);
        assertEquals("42", header.getValue());
    }

    @Test
    public void addStringObjectThrowsOnNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        assertThrows(NullPointerException.class, () -> wrappedHttp11Headers.add("Content-Length", (Object) null));
    }

    @Test
    public void addCharSequenceInt() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        wrappedHttp11Headers.addInt("Content-Length", 42);

        HttpHeader header = wrappedHttp11Headers.getCoreHeaders().get(HttpHeaderName.CONTENT_LENGTH);
        assertNotNull(header);
        assertEquals("42", header.getValue());
    }

    @Test
    public void addCharSequenceShort() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        wrappedHttp11Headers.addShort("Content-Length", (short) 42);

        HttpHeader header = wrappedHttp11Headers.getCoreHeaders().get(HttpHeaderName.CONTENT_LENGTH);
        assertNotNull(header);
        assertEquals("42", header.getValue());
    }

    @Test
    public void addHttpHeadersHotPathsWrappedHttpHeaders() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        WrappedHttp11Headers toAdd = new WrappedHttp11Headers(
            new HttpHeaders().set(HttpHeaderName.KEEP_ALIVE, "true").set(HttpHeaderName.CONTENT_LENGTH, "42"));

        wrappedHttp11Headers.add(toAdd);

        HttpHeaders coreHeaders = wrappedHttp11Headers.getCoreHeaders();
        assertEquals(2, coreHeaders.getSize());
        assertEquals("true", coreHeaders.getValue(HttpHeaderName.KEEP_ALIVE));
        assertEquals("42", coreHeaders.getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void addHttpHeadersFallsBackToSuperImplementation() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        io.netty.handler.codec.http.HttpHeaders toAdd = new DefaultHttpHeaders().add(HttpHeaderNames.KEEP_ALIVE, "true")
            .add(HttpHeaderNames.CONTENT_LENGTH, "42");

        wrappedHttp11Headers.add(toAdd);

        HttpHeaders coreHeaders = wrappedHttp11Headers.getCoreHeaders();
        assertEquals(2, coreHeaders.getSize());
        assertEquals("true", coreHeaders.getValue(HttpHeaderName.KEEP_ALIVE));
        assertEquals("42", coreHeaders.getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void addHttpHeadersThrowsOnNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttp11Headers.add((io.netty.handler.codec.http.HttpHeaders) null));
    }

    @Test
    public void clearUsesNewClientCoreHttpHeaders() {
        HttpHeaders initial = new HttpHeaders();
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(initial);
        wrappedHttp11Headers.clear();

        assertNotSame(initial, wrappedHttp11Headers.getCoreHeaders());
    }

    @Test
    public void containsCharSequenceReturnsFalseWhenHeaderDoesNotExist() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());

        assertFalse(wrappedHttp11Headers.contains(HttpHeaderNames.CONTENT_LENGTH));
    }

    @Test
    public void containsCharSequence() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.CONNECTION, "connection"));

        assertTrue(wrappedHttp11Headers.contains(HttpHeaderNames.CONNECTION));
    }

    @Test
    public void containsStringReturnsFalseWhenHeaderDoesNotExist() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());

        assertFalse(wrappedHttp11Headers.contains("Content-Length"));
    }

    @Test
    public void containsString() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.CONNECTION, "connection"));

        assertTrue(wrappedHttp11Headers.contains("Connection"));
    }

    @Test
    public void copyUsesNewClientCoreHeaders() {
        HttpHeaders initial = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "42");
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(initial);

        WrappedHttp11Headers copy = (WrappedHttp11Headers) wrappedHttp11Headers.copy();

        assertNotSame(initial, copy.getCoreHeaders());
        assertEquals("42", wrappedHttp11Headers.get(HttpHeaderNames.CONTENT_LENGTH));
    }

    @Test
    public void getCharSequenceReturnsNullWhenHeaderDoesNotExist() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());

        assertNull(wrappedHttp11Headers.get(HttpHeaderNames.CONTENT_ENCODING));
    }

    @Test
    public void getCharSequenceReturnsFirstValue() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders()
            .set(HttpHeaderName.CONTENT_ENCODING, Arrays.asList("application/json", "application/xml")));

        assertEquals("application/json", wrappedHttp11Headers.get(HttpHeaderNames.CONTENT_ENCODING));
    }

    @Test
    public void getStringReturnsNullWhenHeaderDoesNotExist() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());

        assertNull(wrappedHttp11Headers.get("Content-Encoding"));
    }

    @Test
    public void getStringReturnsFirstValue() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders()
            .set(HttpHeaderName.CONTENT_ENCODING, Arrays.asList("application/json", "application/xml")));

        assertEquals("application/json", wrappedHttp11Headers.get("Content-Encoding"));
    }

    @Test
    public void getAllCharSequenceReturnsEmptyListWhenHeaderDoesNotExist() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());

        assertSame(Collections.emptyList(), wrappedHttp11Headers.getAll(HttpHeaderNames.CONTENT_TYPE));
    }

    @Test
    public void getAllCharSequence() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, Arrays.asList("application/json", "application/xml")));

        assertLinesMatch(Arrays.asList("application/json", "application/xml"),
            wrappedHttp11Headers.getAll(HttpHeaderNames.CONTENT_TYPE));
    }

    @Test
    public void getAllStringReturnsEmptyListWhenHeaderDoesNotExist() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());

        assertSame(Collections.emptyList(), wrappedHttp11Headers.getAll("Content-Type"));
    }

    @Test
    public void getAllString() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, Arrays.asList("application/json", "application/xml")));

        assertLinesMatch(Arrays.asList("application/json", "application/xml"),
            wrappedHttp11Headers.getAll("Content-Type"));
    }

    @Test
    public void getIntReturnsNullWhenHeaderDoesNotExist() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());

        assertNull(wrappedHttp11Headers.getInt(HttpHeaderNames.CONTENT_LENGTH));
    }

    @Test
    public void getIntReturnsFirstValue() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.COOKIE, Arrays.asList("1", "2")));

        assertEquals(1, wrappedHttp11Headers.getInt(HttpHeaderNames.COOKIE));
    }

    @Test
    public void getIntReturnsNullOnInvalidParse() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.EXPECT, "expect"));

        assertNull(wrappedHttp11Headers.getInt(HttpHeaderNames.EXPECT));
    }

    @Test
    public void getIntWithDefaultReturnsDefaultOnNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());

        assertEquals(1, wrappedHttp11Headers.getInt(HttpHeaderNames.CONTENT_LENGTH, 1));
    }

    @Test
    public void getIntWithDefaultReturnsDefaultOnInvalidParse() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.HOST, "host"));

        assertEquals(1, wrappedHttp11Headers.getInt(HttpHeaderNames.HOST, 1));
    }

    @Test
    public void getIntReturnsActualValue() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "42"));

        assertEquals(42, wrappedHttp11Headers.getInt(HttpHeaderNames.CONTENT_LENGTH, 24));
    }

    @Test
    public void getShortReturnsNullWhenHeaderDoesNotExist() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());

        assertNull(wrappedHttp11Headers.getShort(HttpHeaderNames.CONTENT_LENGTH));
    }

    @Test
    public void getShortReturnsFirstValue() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.COOKIE, Arrays.asList("1", "2")));

        assertEquals((short) 1, wrappedHttp11Headers.getShort(HttpHeaderNames.COOKIE));
    }

    @Test
    public void getShortReturnsNullOnInvalidParse() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.PROXY_AUTHORIZATION, "authorization"));

        assertNull(wrappedHttp11Headers.getShort(HttpHeaderNames.PROXY_AUTHORIZATION));
    }

    @Test
    public void getShortWithDefaultReturnsDefaultOnNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());

        assertEquals((short) 1, wrappedHttp11Headers.getShort(HttpHeaderNames.CONTENT_LENGTH, (short) 1));
    }

    @Test
    public void getShortWithDefaultReturnsDefaultOnInvalidParse() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.TE, "TE"));

        assertEquals((short) 1, wrappedHttp11Headers.getShort(HttpHeaderNames.TE, (short) 1));
    }

    @Test
    public void getShortReturnsActualValue() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "42"));

        assertEquals((short) 42, wrappedHttp11Headers.getShort(HttpHeaderNames.CONTENT_LENGTH, (short) 24));
    }

    @Test
    public void isEmptyIsBasedOnClientCoreHeaders() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        assertTrue(wrappedHttp11Headers.isEmpty());

        wrappedHttp11Headers.getCoreHeaders().set(HttpHeaderName.CONTENT_LENGTH, "42");
        assertFalse(wrappedHttp11Headers.isEmpty());
    }

    @Test
    public void removeCharSequence() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.TRAILER, "trailer"));
        wrappedHttp11Headers.remove(HttpHeaderNames.TRAILER);

        assertTrue(wrappedHttp11Headers.isEmpty());
        assertNull(wrappedHttp11Headers.getCoreHeaders().get(HttpHeaderName.TRAILER));
    }

    @Test
    public void removeString() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.TRAILER, "trailer"));
        wrappedHttp11Headers.remove("Trailer");

        assertTrue(wrappedHttp11Headers.isEmpty());
        assertNull(wrappedHttp11Headers.getCoreHeaders().get(HttpHeaderName.TRAILER));
    }

    @Test
    public void setCharSequenceIterable() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.ACCEPT_ENCODING, "*"));
        wrappedHttp11Headers.set(HttpHeaderNames.ACCEPT_ENCODING, Arrays.asList("gzip", "deflate"));

        HttpHeader header = wrappedHttp11Headers.getCoreHeaders().get(HttpHeaderName.ACCEPT_ENCODING);
        assertNotNull(header);
        assertLinesMatch(Arrays.asList("gzip", "deflate"), header.getValues());
    }

    @Test
    public void setCharSequenceIterableThrowsOnNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttp11Headers.set(HttpHeaderNames.ACCEPT_ENCODING, (Iterable<?>) null));
    }

    @Test
    public void setCharSequenceIterableThrowsIfAnyNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttp11Headers.set(HttpHeaderNames.ACCEPT_ENCODING, Arrays.asList("gzip", null)));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void setCharSequenceObject() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.KEEP_ALIVE, "false"));
        wrappedHttp11Headers.set(HttpHeaderNames.KEEP_ALIVE, "true");

        HttpHeader header = wrappedHttp11Headers.getCoreHeaders().get(HttpHeaderName.KEEP_ALIVE);
        assertNotNull(header);
        assertEquals("true", header.getValue());
    }

    @Test
    public void setCharSequenceObjectThrowsOnNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttp11Headers.set(HttpHeaderNames.CONTENT_LENGTH, (Object) null));
    }

    @Test
    public void setStringIterable() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.ACCEPT_ENCODING, "*"));
        wrappedHttp11Headers.set("Accept-Encoding", Arrays.asList("gzip", "deflate"));

        HttpHeader header = wrappedHttp11Headers.getCoreHeaders().get(HttpHeaderName.ACCEPT_ENCODING);
        assertNotNull(header);
        assertLinesMatch(Arrays.asList("gzip", "deflate"), header.getValues());
    }

    @Test
    public void setStringIterableThrowsOnNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        assertThrows(NullPointerException.class, () -> wrappedHttp11Headers.set("Accept-Encoding", (Iterable<?>) null));
    }

    @Test
    public void setStringIterableThrowsIfAnyNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttp11Headers.set("Accept-Encoding", Arrays.asList("gzip", null)));
    }

    @Test
    public void setStringObject() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "24"));
        wrappedHttp11Headers.set("Content-Length", 42);

        HttpHeader header = wrappedHttp11Headers.getCoreHeaders().get(HttpHeaderName.CONTENT_LENGTH);
        assertNotNull(header);
        assertEquals("42", header.getValue());
    }

    @Test
    public void setStringObjectThrowsOnNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        assertThrows(NullPointerException.class, () -> wrappedHttp11Headers.set("Content-Length", (Object) null));
    }

    @Test
    public void setCharSequenceInt() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "24"));
        wrappedHttp11Headers.setInt("Content-Length", 42);

        HttpHeader header = wrappedHttp11Headers.getCoreHeaders().get(HttpHeaderName.CONTENT_LENGTH);
        assertNotNull(header);
        assertEquals("42", header.getValue());
    }

    @Test
    public void setCharSequenceShort() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "24"));
        wrappedHttp11Headers.setShort("Content-Length", (short) 42);

        HttpHeader header = wrappedHttp11Headers.getCoreHeaders().get(HttpHeaderName.CONTENT_LENGTH);
        assertNotNull(header);
        assertEquals("42", header.getValue());
    }

    @Test
    public void setHttpHeadersHotPathsWrappedHttpHeaders() {
        HttpHeaders initial = new HttpHeaders(0);
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(initial);
        WrappedHttp11Headers toAdd = new WrappedHttp11Headers(
            new HttpHeaders().set(HttpHeaderName.KEEP_ALIVE, "true").set(HttpHeaderName.CONTENT_LENGTH, "42"));

        wrappedHttp11Headers.set(toAdd);

        HttpHeaders coreHeaders = wrappedHttp11Headers.getCoreHeaders();
        assertNotSame(initial, coreHeaders);
        assertNotSame(toAdd.getCoreHeaders(), coreHeaders);
        assertEquals(2, coreHeaders.getSize());
        assertEquals("true", coreHeaders.getValue(HttpHeaderName.KEEP_ALIVE));
        assertEquals("42", coreHeaders.getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void setHttpHeadersFallsBackToSuperImplementation() {
        HttpHeaders initial = new HttpHeaders();
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(initial);
        io.netty.handler.codec.http.HttpHeaders toAdd
            = new DefaultHttpHeaders().add("Keep-Alive", "true").add(HttpHeaderNames.CONTENT_LENGTH, "42");

        wrappedHttp11Headers.set(toAdd);

        HttpHeaders coreHeaders = wrappedHttp11Headers.getCoreHeaders();
        assertNotSame(initial, coreHeaders);
        assertEquals(2, coreHeaders.getSize());
        assertEquals("true", coreHeaders.getValue(HttpHeaderName.KEEP_ALIVE));
        assertEquals("42", coreHeaders.getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void setHttpHeadersThrowsOnNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttp11Headers.set((io.netty.handler.codec.http.HttpHeaders) null));
    }

    @Test
    public void setAllHttpHeadersHotPathsWrappedHttpHeaders() {
        HttpHeaders initial = new HttpHeaders(0);
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(initial);
        WrappedHttp11Headers toAdd = new WrappedHttp11Headers(
            new HttpHeaders().set(HttpHeaderName.KEEP_ALIVE, "true").set(HttpHeaderName.CONTENT_LENGTH, "42"));

        wrappedHttp11Headers.setAll(toAdd);

        HttpHeaders coreHeaders = wrappedHttp11Headers.getCoreHeaders();
        assertSame(initial, coreHeaders);
        assertEquals(2, coreHeaders.getSize());
        assertEquals("true", coreHeaders.getValue(HttpHeaderName.KEEP_ALIVE));
        assertEquals("42", coreHeaders.getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void setAllHttpHeadersFallsBackToSuperImplementation() {
        HttpHeaders initial = new HttpHeaders();
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(initial);
        io.netty.handler.codec.http.HttpHeaders toAdd
            = new DefaultHttpHeaders().add("Keep-Alive", "true").add(HttpHeaderNames.CONTENT_LENGTH, "42");

        wrappedHttp11Headers.setAll(toAdd);

        HttpHeaders coreHeaders = wrappedHttp11Headers.getCoreHeaders();
        assertSame(initial, coreHeaders);
        assertEquals(2, coreHeaders.getSize());
        assertEquals("true", coreHeaders.getValue(HttpHeaderName.KEEP_ALIVE));
        assertEquals("42", coreHeaders.getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void setAllHttpHeadersThrowsOnNull() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());
        assertThrows(NullPointerException.class, () -> wrappedHttp11Headers.setAll(null));
    }

    @Test
    public void names() {
        HttpHeaders clientCoreHeaders = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "42")
            .set(HttpHeaderName.CONTENT_TYPE, "application/json")
            .set(HttpHeaderName.ACCEPT, Arrays.asList("application/json", "text/json"));
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(clientCoreHeaders);

        Set<String> expectedNames = new HashSet<>(Arrays.asList(HttpHeaderName.CONTENT_LENGTH.getCaseSensitiveName(),
            HttpHeaderName.CONTENT_TYPE.getCaseSensitiveName(), HttpHeaderName.ACCEPT.getCaseSensitiveName()));
        Set<String> names = wrappedHttp11Headers.names();

        assertEquals(expectedNames, names);
    }

    @Test
    public void getTimeMillis() {
        OffsetDateTime now = OffsetDateTime.now();
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(
            new HttpHeaders().set(HttpHeaderName.DATE, DateTimeRfc1123.toRfc1123String(now)));

        Long timeMillis = wrappedHttp11Headers.getTimeMillis(HttpHeaderNames.DATE);
        assertNotNull(timeMillis);

        // Use OffsetDateTime.toEpochSecond() * 1000L to get the expected millis as DateTimeRfc1123 only has the ability
        // to represent to seconds. If OffsetDateTime.toInstant().toEpochMilli() is used, it will return the current
        // time in milliseconds, which cannot be represented.
        assertEquals(now.toEpochSecond() * 1000L, timeMillis);
    }

    @Test
    public void getTimeMillisReturnsNullWhenHeaderDoesNotExist() {
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(new HttpHeaders());

        assertNull(wrappedHttp11Headers.getTimeMillis(HttpHeaderNames.DATE));
    }

    @Test
    public void getTimeMillisReturnsNullWhenHeaderIsNotADate() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.DATE, "notADate"));

        assertNull(wrappedHttp11Headers.getTimeMillis(HttpHeaderNames.DATE));
    }

    @Test
    public void getTimeMillisReturnsDefaultWhenHeaderIsNotADate() {
        WrappedHttp11Headers wrappedHttp11Headers
            = new WrappedHttp11Headers(new HttpHeaders().set(HttpHeaderName.DATE, "notADate"));

        assertEquals(42L, wrappedHttp11Headers.getTimeMillis(HttpHeaderNames.DATE, 42L));
    }

    @Test
    public void getTimeMillisReturnsActualValue() {
        OffsetDateTime now = OffsetDateTime.now();
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(
            new HttpHeaders().set(HttpHeaderName.DATE, DateTimeRfc1123.toRfc1123String(now)));

        // Use OffsetDateTime.toEpochSecond() * 1000L to get the expected millis as DateTimeRfc1123 only has the ability
        // to represent to seconds. If OffsetDateTime.toInstant().toEpochMilli() is used, it will return the current
        // time in milliseconds, which cannot be represented.
        assertEquals(now.toEpochSecond() * 1000L,
            wrappedHttp11Headers.getTimeMillis(HttpHeaderNames.DATE, now.toInstant().toEpochMilli()));
    }
}
