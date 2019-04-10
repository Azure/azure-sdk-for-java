package com.azure.common.http;

import org.junit.Test;

import static org.junit.Assert.*;

public class HttpHeadersTests {
    @Test
    public void testSet()
    {
        final HttpHeaders headers = new HttpHeaders();

        headers.set("a", "b");
        assertEquals("b", headers.value("a"));

        headers.set("a", "c");
        assertEquals("c", headers.value("a"));

        headers.set("a", null);
        assertNull(headers.value("a"));

        headers.set("A", "");
        assertEquals("", headers.value("a"));

        headers.set("A", "b");
        assertEquals("b", headers.value("A"));

        headers.set("a", null);
        assertNull(headers.value("a"));
    }
}
