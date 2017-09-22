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
    }
}
