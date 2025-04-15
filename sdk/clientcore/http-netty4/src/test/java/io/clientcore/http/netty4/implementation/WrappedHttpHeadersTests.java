// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

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
 * Unit tests for {@link WrappedHttpHeaders}.
 */
public class WrappedHttpHeadersTests {
    @Test
    public void throwsOnNullClientCoreHttpHeaders() {
        assertThrows(NullPointerException.class, () -> new WrappedHttpHeaders(null));
    }

    @Test
    public void addCharSequenceIterable() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        wrappedHttpHeaders.add(HttpHeaderNames.ACCEPT_ENCODING, Arrays.asList("gzip", "deflate"));

        HttpHeader header = wrappedHttpHeaders.getCoreHeaders().get(HttpHeaderName.ACCEPT_ENCODING);
        assertNotNull(header);
        assertLinesMatch(Arrays.asList("gzip", "deflate"), header.getValues());
    }

    @Test
    public void addCharSequenceIterableThrowsOnNull() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttpHeaders.add(HttpHeaderNames.ACCEPT_ENCODING, (Iterable<?>) null));
    }

    @Test
    public void addCharSequenceIterableThrowsIfAnyNull() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttpHeaders.add(HttpHeaderNames.ACCEPT_ENCODING, Arrays.asList("gzip", null)));
    }

    @Test
    public void addCharSequenceObject() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        wrappedHttpHeaders.add(HttpHeaderNames.CONTENT_LENGTH, 42);

        HttpHeader header = wrappedHttpHeaders.getCoreHeaders().get(HttpHeaderName.CONTENT_LENGTH);
        assertNotNull(header);
        assertEquals("42", header.getValue());
    }

    @Test
    public void addCharSequenceObjectThrowsOnNull() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttpHeaders.add(HttpHeaderNames.CONTENT_LENGTH, (Object) null));
    }

    @Test
    public void addStringIterable() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        wrappedHttpHeaders.add("Accept-Encoding", Arrays.asList("gzip", "deflate"));

        HttpHeader header = wrappedHttpHeaders.getCoreHeaders().get(HttpHeaderName.ACCEPT_ENCODING);
        assertNotNull(header);
        assertLinesMatch(Arrays.asList("gzip", "deflate"), header.getValues());
    }

    @Test
    public void addStringIterableThrowsOnNull() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        assertThrows(NullPointerException.class, () -> wrappedHttpHeaders.add("Accept-Encoding", (Iterable<?>) null));
    }

    @Test
    public void addStringIterableThrowsIfAnyNull() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttpHeaders.add("Accept-Encoding", Arrays.asList("gzip", null)));
    }

    @Test
    public void addStringObject() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        wrappedHttpHeaders.add("Content-Length", 42);

        HttpHeader header = wrappedHttpHeaders.getCoreHeaders().get(HttpHeaderName.CONTENT_LENGTH);
        assertNotNull(header);
        assertEquals("42", header.getValue());
    }

    @Test
    public void addStringObjectThrowsOnNull() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        assertThrows(NullPointerException.class, () -> wrappedHttpHeaders.add("Content-Length", (Object) null));
    }

    @Test
    public void addCharSequenceInt() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        wrappedHttpHeaders.addInt("Content-Length", 42);

        HttpHeader header = wrappedHttpHeaders.getCoreHeaders().get(HttpHeaderName.CONTENT_LENGTH);
        assertNotNull(header);
        assertEquals("42", header.getValue());
    }

    @Test
    public void addCharSequenceShort() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        wrappedHttpHeaders.addShort("Content-Length", (short) 42);

        HttpHeader header = wrappedHttpHeaders.getCoreHeaders().get(HttpHeaderName.CONTENT_LENGTH);
        assertNotNull(header);
        assertEquals("42", header.getValue());
    }

    @Test
    public void addHttpHeadersHotPathsWrappedHttpHeaders() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        WrappedHttpHeaders toAdd = new WrappedHttpHeaders(
            new HttpHeaders().set(HttpHeaderName.KEEP_ALIVE, "true").set(HttpHeaderName.CONTENT_LENGTH, "42"));

        wrappedHttpHeaders.add(toAdd);

        HttpHeaders coreHeaders = wrappedHttpHeaders.getCoreHeaders();
        assertEquals(2, coreHeaders.getSize());
        assertEquals("true", coreHeaders.getValue(HttpHeaderName.KEEP_ALIVE));
        assertEquals("42", coreHeaders.getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void addHttpHeadersFallsBackToSuperImplementation() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        io.netty.handler.codec.http.HttpHeaders toAdd
            = new DefaultHttpHeaders().add("Keep-Alive", "true").add(HttpHeaderNames.CONTENT_LENGTH, "42");

        wrappedHttpHeaders.add(toAdd);

        HttpHeaders coreHeaders = wrappedHttpHeaders.getCoreHeaders();
        assertEquals(2, coreHeaders.getSize());
        assertEquals("true", coreHeaders.getValue(HttpHeaderName.KEEP_ALIVE));
        assertEquals("42", coreHeaders.getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void addHttpHeadersThrowsOnNull() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttpHeaders.add((io.netty.handler.codec.http.HttpHeaders) null));
    }

    @Test
    public void clearUsesNewClientCoreHttpHeaders() {
        HttpHeaders initial = new HttpHeaders();
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(initial);
        wrappedHttpHeaders.clear();

        assertNotSame(initial, wrappedHttpHeaders.getCoreHeaders());
    }

    @Test
    public void containsCharSequence() {
        WrappedHttpHeaders wrappedHttpHeaders
            = new WrappedHttpHeaders(new HttpHeaders().set(HttpHeaderName.CONNECTION, "connection"));

        assertTrue(wrappedHttpHeaders.contains(HttpHeaderNames.CONNECTION));
    }

    @Test
    public void containsString() {
        WrappedHttpHeaders wrappedHttpHeaders
            = new WrappedHttpHeaders(new HttpHeaders().set(HttpHeaderName.CONNECTION, "connection"));

        assertTrue(wrappedHttpHeaders.contains("Connection"));
    }

    @Test
    public void copyUsesNewClientCoreHeaders() {
        HttpHeaders initial = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "42");
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(initial);

        WrappedHttpHeaders copy = (WrappedHttpHeaders) wrappedHttpHeaders.copy();

        assertNotSame(initial, copy.getCoreHeaders());
        assertEquals("42", wrappedHttpHeaders.get(HttpHeaderNames.CONTENT_LENGTH));
    }

    @Test
    public void getCharSequenceReturnsFirstValue() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders()
            .set(HttpHeaderName.CONTENT_ENCODING, Arrays.asList("application/json", "application/xml")));

        assertEquals("application/json", wrappedHttpHeaders.get(HttpHeaderNames.CONTENT_ENCODING));
    }

    @Test
    public void getStringReturnsFirstValue() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders()
            .set(HttpHeaderName.CONTENT_ENCODING, Arrays.asList("application/json", "application/xml")));

        assertEquals("application/json", wrappedHttpHeaders.get("Content-Encoding"));
    }

    @Test
    public void getAllCharSequence() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, Arrays.asList("application/json", "application/xml")));

        assertLinesMatch(Arrays.asList("application/json", "application/xml"),
            wrappedHttpHeaders.getAll(HttpHeaderNames.CONTENT_TYPE));
    }

    @Test
    public void getAllStringReturns() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, Arrays.asList("application/json", "application/xml")));

        assertLinesMatch(Arrays.asList("application/json", "application/xml"),
            wrappedHttpHeaders.getAll("Content-Type"));
    }

    @Test
    public void getIntReturnsFirstValue() {
        WrappedHttpHeaders wrappedHttpHeaders
            = new WrappedHttpHeaders(new HttpHeaders().set(HttpHeaderName.COOKIE, Arrays.asList("1", "2")));

        assertEquals(1, wrappedHttpHeaders.getInt(HttpHeaderNames.COOKIE));
    }

    @Test
    public void getIntReturnsNullOnInvalidParse() {
        WrappedHttpHeaders wrappedHttpHeaders
            = new WrappedHttpHeaders(new HttpHeaders().set(HttpHeaderName.EXPECT, "expect"));

        assertNull(wrappedHttpHeaders.getInt(HttpHeaderNames.EXPECT));
    }

    @Test
    public void getIntWithDefaultReturnsDefaultOnNullOrInvalidParse() {
        WrappedHttpHeaders wrappedHttpHeaders
            = new WrappedHttpHeaders(new HttpHeaders().set(HttpHeaderName.HOST, "host"));

        assertEquals(1, wrappedHttpHeaders.getInt(HttpHeaderNames.HOST, 1));
    }

    @Test
    public void getShortReturnsFirstValue() {
        WrappedHttpHeaders wrappedHttpHeaders
            = new WrappedHttpHeaders(new HttpHeaders().set(HttpHeaderName.COOKIE, Arrays.asList("1", "2")));

        assertEquals((short) 1, wrappedHttpHeaders.getShort(HttpHeaderNames.COOKIE));
    }

    @Test
    public void getShortReturnsNullOnInvalidParse() {
        WrappedHttpHeaders wrappedHttpHeaders
            = new WrappedHttpHeaders(new HttpHeaders().set(HttpHeaderName.PROXY_AUTHORIZATION, "authorization"));

        assertNull(wrappedHttpHeaders.getShort(HttpHeaderNames.PROXY_AUTHORIZATION));
    }

    @Test
    public void getShortWithDefaultReturnsDefaultOnNullOrInvalidParse() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders().set(HttpHeaderName.TE, "TE"));

        assertEquals((short) 1, wrappedHttpHeaders.getShort(HttpHeaderNames.TE, (short) 1));
    }

    @Test
    public void isEmptyIsBasedOnClientCoreHeaders() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        assertTrue(wrappedHttpHeaders.isEmpty());

        wrappedHttpHeaders.getCoreHeaders().set(HttpHeaderName.CONTENT_LENGTH, "42");
        assertFalse(wrappedHttpHeaders.isEmpty());
    }

    @Test
    public void removeCharSequence() {
        WrappedHttpHeaders wrappedHttpHeaders
            = new WrappedHttpHeaders(new HttpHeaders().set(HttpHeaderName.TRAILER, "trailer"));
        wrappedHttpHeaders.remove(HttpHeaderNames.TRAILER);

        assertTrue(wrappedHttpHeaders.isEmpty());
        assertNull(wrappedHttpHeaders.getCoreHeaders().get(HttpHeaderName.TRAILER));
    }

    @Test
    public void removeString() {
        WrappedHttpHeaders wrappedHttpHeaders
            = new WrappedHttpHeaders(new HttpHeaders().set(HttpHeaderName.TRAILER, "trailer"));
        wrappedHttpHeaders.remove("Trailer");

        assertTrue(wrappedHttpHeaders.isEmpty());
        assertNull(wrappedHttpHeaders.getCoreHeaders().get(HttpHeaderName.TRAILER));
    }

    @Test
    public void setCharSequenceIterable() {
        WrappedHttpHeaders wrappedHttpHeaders
            = new WrappedHttpHeaders(new HttpHeaders().set(HttpHeaderName.ACCEPT_ENCODING, "*"));
        wrappedHttpHeaders.set(HttpHeaderNames.ACCEPT_ENCODING, Arrays.asList("gzip", "deflate"));

        HttpHeader header = wrappedHttpHeaders.getCoreHeaders().get(HttpHeaderName.ACCEPT_ENCODING);
        assertNotNull(header);
        assertLinesMatch(Arrays.asList("gzip", "deflate"), header.getValues());
    }

    @Test
    public void setCharSequenceIterableThrowsOnNull() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttpHeaders.set(HttpHeaderNames.ACCEPT_ENCODING, (Iterable<?>) null));
    }

    @Test
    public void setCharSequenceIterableThrowsIfAnyNull() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttpHeaders.set(HttpHeaderNames.ACCEPT_ENCODING, Arrays.asList("gzip", null)));
    }

    @Test
    public void setCharSequenceObject() {
        WrappedHttpHeaders wrappedHttpHeaders
            = new WrappedHttpHeaders(new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "24"));
        wrappedHttpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, 42);

        HttpHeader header = wrappedHttpHeaders.getCoreHeaders().get(HttpHeaderName.CONTENT_LENGTH);
        assertNotNull(header);
        assertEquals("42", header.getValue());
    }

    @Test
    public void setCharSequenceObjectThrowsOnNull() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, (Object) null));
    }

    @Test
    public void setStringIterable() {
        WrappedHttpHeaders wrappedHttpHeaders
            = new WrappedHttpHeaders(new HttpHeaders().set(HttpHeaderName.ACCEPT_ENCODING, "*"));
        wrappedHttpHeaders.set("Accept-Encoding", Arrays.asList("gzip", "deflate"));

        HttpHeader header = wrappedHttpHeaders.getCoreHeaders().get(HttpHeaderName.ACCEPT_ENCODING);
        assertNotNull(header);
        assertLinesMatch(Arrays.asList("gzip", "deflate"), header.getValues());
    }

    @Test
    public void setStringIterableThrowsOnNull() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        assertThrows(NullPointerException.class, () -> wrappedHttpHeaders.set("Accept-Encoding", (Iterable<?>) null));
    }

    @Test
    public void setStringIterableThrowsIfAnyNull() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttpHeaders.set("Accept-Encoding", Arrays.asList("gzip", null)));
    }

    @Test
    public void setStringObject() {
        WrappedHttpHeaders wrappedHttpHeaders
            = new WrappedHttpHeaders(new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "24"));
        wrappedHttpHeaders.set("Content-Length", 42);

        HttpHeader header = wrappedHttpHeaders.getCoreHeaders().get(HttpHeaderName.CONTENT_LENGTH);
        assertNotNull(header);
        assertEquals("42", header.getValue());
    }

    @Test
    public void setStringObjectThrowsOnNull() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        assertThrows(NullPointerException.class, () -> wrappedHttpHeaders.set("Content-Length", (Object) null));
    }

    @Test
    public void setCharSequenceInt() {
        WrappedHttpHeaders wrappedHttpHeaders
            = new WrappedHttpHeaders(new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "24"));
        wrappedHttpHeaders.setInt("Content-Length", 42);

        HttpHeader header = wrappedHttpHeaders.getCoreHeaders().get(HttpHeaderName.CONTENT_LENGTH);
        assertNotNull(header);
        assertEquals("42", header.getValue());
    }

    @Test
    public void setCharSequenceShort() {
        WrappedHttpHeaders wrappedHttpHeaders
            = new WrappedHttpHeaders(new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "24"));
        wrappedHttpHeaders.setShort("Content-Length", (short) 42);

        HttpHeader header = wrappedHttpHeaders.getCoreHeaders().get(HttpHeaderName.CONTENT_LENGTH);
        assertNotNull(header);
        assertEquals("42", header.getValue());
    }

    @Test
    public void setHttpHeadersHotPathsWrappedHttpHeaders() {
        HttpHeaders initial = new HttpHeaders(0);
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(initial);
        WrappedHttpHeaders toAdd = new WrappedHttpHeaders(
            new HttpHeaders().set(HttpHeaderName.KEEP_ALIVE, "true").set(HttpHeaderName.CONTENT_LENGTH, "42"));

        wrappedHttpHeaders.set(toAdd);

        HttpHeaders coreHeaders = wrappedHttpHeaders.getCoreHeaders();
        assertNotSame(initial, coreHeaders);
        assertNotSame(toAdd.getCoreHeaders(), coreHeaders);
        assertEquals(2, coreHeaders.getSize());
        assertEquals("true", coreHeaders.getValue(HttpHeaderName.KEEP_ALIVE));
        assertEquals("42", coreHeaders.getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void setHttpHeadersFallsBackToSuperImplementation() {
        HttpHeaders initial = new HttpHeaders();
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(initial);
        io.netty.handler.codec.http.HttpHeaders toAdd
            = new DefaultHttpHeaders().add("Keep-Alive", "true").add(HttpHeaderNames.CONTENT_LENGTH, "42");

        wrappedHttpHeaders.set(toAdd);

        HttpHeaders coreHeaders = wrappedHttpHeaders.getCoreHeaders();
        assertNotSame(initial, coreHeaders);
        assertEquals(2, coreHeaders.getSize());
        assertEquals("true", coreHeaders.getValue(HttpHeaderName.KEEP_ALIVE));
        assertEquals("42", coreHeaders.getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void setHttpHeadersThrowsOnNull() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        assertThrows(NullPointerException.class,
            () -> wrappedHttpHeaders.set((io.netty.handler.codec.http.HttpHeaders) null));
    }

    @Test
    public void setAllHttpHeadersHotPathsWrappedHttpHeaders() {
        HttpHeaders initial = new HttpHeaders(0);
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(initial);
        WrappedHttpHeaders toAdd = new WrappedHttpHeaders(
            new HttpHeaders().set(HttpHeaderName.KEEP_ALIVE, "true").set(HttpHeaderName.CONTENT_LENGTH, "42"));

        wrappedHttpHeaders.setAll(toAdd);

        HttpHeaders coreHeaders = wrappedHttpHeaders.getCoreHeaders();
        assertSame(initial, coreHeaders);
        assertEquals(2, coreHeaders.getSize());
        assertEquals("true", coreHeaders.getValue(HttpHeaderName.KEEP_ALIVE));
        assertEquals("42", coreHeaders.getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void setAllHttpHeadersFallsBackToSuperImplementation() {
        HttpHeaders initial = new HttpHeaders();
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(initial);
        io.netty.handler.codec.http.HttpHeaders toAdd
            = new DefaultHttpHeaders().add("Keep-Alive", "true").add(HttpHeaderNames.CONTENT_LENGTH, "42");

        wrappedHttpHeaders.setAll(toAdd);

        HttpHeaders coreHeaders = wrappedHttpHeaders.getCoreHeaders();
        assertSame(initial, coreHeaders);
        assertEquals(2, coreHeaders.getSize());
        assertEquals("true", coreHeaders.getValue(HttpHeaderName.KEEP_ALIVE));
        assertEquals("42", coreHeaders.getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void setAllHttpHeadersThrowsOnNull() {
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(new HttpHeaders());
        assertThrows(NullPointerException.class, () -> wrappedHttpHeaders.setAll(null));
    }
}
