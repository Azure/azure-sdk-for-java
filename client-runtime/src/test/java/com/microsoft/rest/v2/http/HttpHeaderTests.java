package com.microsoft.rest.v2.http;

import org.junit.Test;

import static org.junit.Assert.*;

public class HttpHeaderTests {
    @Test
    public void addValue()
    {
        final HttpHeader header = new HttpHeader("a", "b");
        header.addValue("c");
        assertEquals("a:b,c", header.toString());
    }
}
