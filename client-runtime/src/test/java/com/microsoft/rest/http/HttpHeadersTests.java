package com.microsoft.rest.http;

import org.junit.Test;

import static org.junit.Assert.*;

public class HttpHeadersTests {
    @Test
    public void add()
    {
        final HttpHeaders headers = new HttpHeaders();

        headers.add("a", "b");
        assertEquals("b", headers.value("a"));

        headers.add("a", "c");
        assertEquals("b,c", headers.value("a"));

        headers.add("a", null);
        assertEquals("b,c", headers.value("a"));

        headers.set("A", "");
        assertNull(headers.value("a"));

        headers.add("A", "b");
        assertEquals("b", headers.value("A"));

        headers.set("a", null);
        assertNull(headers.value("a"));
    }
}
