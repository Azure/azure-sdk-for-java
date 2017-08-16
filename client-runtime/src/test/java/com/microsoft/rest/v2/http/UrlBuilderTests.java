package com.microsoft.rest.v2.http;

import org.junit.Test;

import static org.junit.Assert.*;

public class UrlBuilderTests {
    @Test
    public void withScheme() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http");
        assertEquals(null, builder.toString());
    }

    @Test
    public void withSchemeAndHost() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http")
                .setHost("www.example.com");
        assertEquals("http://www.example.com", builder.toString());
    }

    @Test
    public void withHost() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com");
        assertEquals("//www.example.com", builder.toString());
    }

    @Test
    public void withSchemeAndHostAndOneQueryParameter() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http")
                .setHost("www.example.com")
                .addQueryParameter("A", "B");
        assertEquals("http://www.example.com?A=B", builder.toString());
    }

    @Test
    public void withSchemeAndHostAndTwoQueryParameters() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http")
                .setHost("www.example.com")
                .addQueryParameter("A", "B")
                .addQueryParameter("C", "D");
        assertEquals("http://www.example.com?A=B&C=D", builder.toString());
    }

    @Test
    public void withSchemeAndHostAndPathAndTwoQueryParameters() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http")
                .setHost("www.example.com")
                .addQueryParameter("A", "B")
                .addQueryParameter("C", "D")
                .setPath("index.html");
        assertEquals("http://www.example.com/index.html?A=B&C=D", builder.toString());
    }
}
