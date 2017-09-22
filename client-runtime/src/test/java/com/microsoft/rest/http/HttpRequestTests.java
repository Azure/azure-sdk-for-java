package com.microsoft.rest.http;

import org.junit.Test;

import static org.junit.Assert.*;

public class HttpRequestTests {
    @Test
    public void constructor() {
        final HttpRequest request = new HttpRequest("request caller method", "request http method", "request url");
        assertEquals("request caller method", request.callerMethod());
        assertEquals("request http method", request.httpMethod());
        assertEquals("request url", request.url());
    }
}
