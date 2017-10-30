package com.microsoft.rest.v2.http;

import org.junit.Test;

import static org.junit.Assert.*;

public class UrlBuilderTests {
    @Test
    public void withScheme() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http");
        assertEquals("http://", builder.toString());
    }

    @Test
    public void withSchemeAndHost() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com");
        assertEquals("http://www.example.com", builder.toString());
    }

    @Test
    public void withSchemeAndHostWhenHostHasWhitespace() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.exa mple.com");
        assertEquals("http://www.exa mple.com", builder.toString());
    }

    @Test
    public void withHost() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("www.example.com");
        assertEquals("www.example.com", builder.toString());
    }

    @Test
    public void withHostWhenHostHasWhitespace() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("www.exampl e.com");
        assertEquals("www.exampl e.com", builder.toString());
    }

    @Test
    public void withHostAndPath() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("www.example.com")
                .withPath("my/path");
        assertEquals("www.example.com/my/path", builder.toString());
    }

    @Test
    public void withHostAndPathWithSlashAfterHost() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("www.example.com/")
                .withPath("my/path");
        assertEquals("www.example.com/my/path", builder.toString());
    }

    @Test
    public void withHostAndPathWithSlashBeforePath() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("www.example.com")
                .withPath("/my/path");
        assertEquals("www.example.com/my/path", builder.toString());
    }

    @Test
    public void withHostAndPathWithSlashAfterHostAndBeforePath() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("www.example.com/")
                .withPath("/my/path");
        assertEquals("www.example.com/my/path", builder.toString());
    }

    @Test
    public void withHostAndPathWithWhitespaceInPath() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("www.example.com")
                .withPath("my path");
        assertEquals("www.example.com/my path", builder.toString());
    }

    @Test
    public void withHostAndPathWithPlusInPath() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("www.example.com")
                .withPath("my+path");
        assertEquals("www.example.com/my+path", builder.toString());
    }

    @Test
    public void withHostAndPathWithPercent20InPath() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("www.example.com")
                .withPath("my%20path");
        assertEquals("www.example.com/my%20path", builder.toString());
    }

    @Test
    public void withSchemeAndHostAndOneQueryParameter() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .withQueryParameter("A", "B");
        assertEquals("http://www.example.com?A=B", builder.toString());
    }

    @Test
    public void withSchemeAndHostAndOneQueryParameterWhenQueryParameterNameHasWhitespace() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .withQueryParameter("App les", "B");
        assertEquals("http://www.example.com?App les=B", builder.toString());
    }

    @Test
    public void withSchemeAndHostAndOneQueryParameterWhenQueryParameterNameHasPercent20() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .withQueryParameter("App%20les", "B");
        assertEquals("http://www.example.com?App%20les=B", builder.toString());
    }

    @Test
    public void withSchemeAndHostAndOneQueryParameterWhenQueryParameterValueHasWhitespace() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .withQueryParameter("Apples", "Go od");
        assertEquals("http://www.example.com?Apples=Go od", builder.toString());
    }

    @Test
    public void withSchemeAndHostAndOneQueryParameterWhenQueryParameterValueHasPercent20() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .withQueryParameter("Apples", "Go%20od");
        assertEquals("http://www.example.com?Apples=Go%20od", builder.toString());
    }

    @Test
    public void withSchemeAndHostAndTwoQueryParameters() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .withQueryParameter("A", "B")
                .withQueryParameter("C", "D");
        assertEquals("http://www.example.com?A=B&C=D", builder.toString());
    }

    @Test
    public void withSchemeAndHostAndPathAndTwoQueryParameters() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .withQueryParameter("A", "B")
                .withQueryParameter("C", "D")
                .withPath("index.html");
        assertEquals("http://www.example.com/index.html?A=B&C=D", builder.toString());
    }

    @Test
    public void withAbsolutePath() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .withPath("http://www.othersite.com");
        assertEquals("http://www.othersite.com", builder.toString());
    }

    @Test
    public void withQueryInPath() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .withPath("mypath?thing=stuff")
                .withQueryParameter("otherthing", "otherstuff");
        assertEquals("http://www.example.com/mypath?thing=stuff&otherthing=otherstuff", builder.toString());
    }

    @Test
    public void withAbsolutePathAndQuery() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .withPath("http://www.othersite.com/mypath?thing=stuff")
                .withQueryParameter("otherthing", "otherstuff");
        assertEquals("http://www.othersite.com/mypath?thing=stuff&otherthing=otherstuff", builder.toString());
    }
}
