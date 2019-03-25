package com.azure.common.http;

import org.junit.Test;

import static org.junit.Assert.*;

public class HttpMethodTests {
    @Test
    public void GET()
    {
        assertEquals("GET", HttpMethod.GET.toString());
    }

    @Test
    public void PUT()
    {
        assertEquals("PUT", HttpMethod.PUT.toString());
    }

    @Test
    public void POST()
    {
        assertEquals("POST", HttpMethod.POST.toString());
    }

    @Test
    public void PATCH()
    {
        assertEquals("PATCH", HttpMethod.PATCH.toString());
    }

    @Test
    public void DELETE()
    {
        assertEquals("DELETE", HttpMethod.DELETE.toString());
    }

    @Test
    public void HEAD()
    {
        assertEquals("HEAD", HttpMethod.HEAD.toString());
    }
}
