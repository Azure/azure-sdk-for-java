// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.implementation.http;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class UrlBuilderTests {
    @Test
    public void withScheme() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http");
        assertEquals("http://", builder.toString());
    }

    @Test
    public void withSchemeWhenSchemeIsNull() {
        final UrlBuilder builder = new UrlBuilder()
            .withScheme("http");
        builder.withScheme(null);
        assertNull(builder.scheme());
    }

    @Test
    public void withSchemeWhenSchemeIsEmpty() {
        final UrlBuilder builder = new UrlBuilder()
            .withScheme("http");
        builder.withScheme("");
        assertNull(builder.scheme());
    }

    @Test
    public void withSchemeWhenSchemeIsNotEmpty() {
        final UrlBuilder builder = new UrlBuilder()
            .withScheme("http");
        builder.withScheme("https");
        assertEquals("https", builder.scheme());
    }

    @Test
    public void withSchemeWhenSchemeContainsTerminator() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http://");
        assertEquals("http", builder.scheme());
        assertNull(builder.host());
        assertEquals("http://", builder.toString());
    }

    @Test
    public void withSchemeWhenSchemeContainsHost() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http://www.example.com");
        assertEquals("http", builder.scheme());
        assertEquals("www.example.com", builder.host());
        assertEquals("http://www.example.com", builder.toString());
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
    public void withHostWhenHostIsNull() {
        final UrlBuilder builder = new UrlBuilder()
            .withHost("www.example.com");
        builder.withHost(null);
        assertNull(builder.host());
    }

    @Test
    public void withHostWhenHostIsEmpty() {
        final UrlBuilder builder = new UrlBuilder()
            .withHost("www.example.com");
        builder.withHost("");
        assertNull(builder.host());
    }

    @Test
    public void withHostWhenHostIsNotEmpty() {
        final UrlBuilder builder = new UrlBuilder()
            .withHost("www.example.com");
        builder.withHost("www.bing.com");
        assertEquals("www.bing.com", builder.host());
    }

    @Test
    public void withHostWhenHostContainsSchemeTerminator() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("://www.example.com");
        assertNull(builder.scheme());
        assertEquals("www.example.com", builder.host());
        assertEquals("www.example.com", builder.toString());
    }

    @Test
    public void withHostWhenHostContainsScheme() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("https://www.example.com");
        assertEquals("https", builder.scheme());
        assertEquals("www.example.com", builder.host());
        assertEquals("https://www.example.com", builder.toString());
    }

    @Test
    public void withHostWhenHostContainsColonButNoPort() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("www.example.com:");
        assertEquals("www.example.com", builder.host());
        assertNull(builder.port());
        assertEquals("www.example.com", builder.toString());
    }

    @Test
    public void withHostWhenHostContainsPort() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("www.example.com:1234");
        assertEquals("www.example.com", builder.host());
        assertEquals(1234, builder.port().intValue());
        assertEquals("www.example.com:1234", builder.toString());
    }

    @Test
    public void withHostWhenHostContainsForwardSlashButNoPath() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("www.example.com/");
        assertEquals("www.example.com", builder.host());
        assertEquals("/", builder.path());
        assertEquals("www.example.com/", builder.toString());
    }

    @Test
    public void withHostWhenHostContainsPath() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("www.example.com/index.html");
        assertEquals("www.example.com", builder.host());
        assertEquals("/index.html", builder.path());
        assertEquals("www.example.com/index.html", builder.toString());
    }

    @Test
    public void withHostWhenHostContainsQuestionMarkButNoQuery() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("www.example.com?");
        assertEquals("www.example.com", builder.host());
        assertEquals(0, builder.query().size());
        assertEquals("www.example.com", builder.toString());
    }

    @Test
    public void withHostWhenHostContainsQuery() {
        final UrlBuilder builder = new UrlBuilder()
                .withHost("www.example.com?a=b");
        assertEquals("www.example.com", builder.host());
        assertThat(builder.toString(), CoreMatchers.containsString("a=b"));
        assertEquals("www.example.com?a=b", builder.toString());
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
    public void withPortInt() {
        final UrlBuilder builder = new UrlBuilder()
                .withPort(50);
        assertEquals(50, builder.port().intValue());
        assertEquals(":50", builder.toString());
    }

    @Test
    public void withPortStringWithNull() {
        final UrlBuilder builder = new UrlBuilder()
                .withPort(null);
        assertNull(builder.port());
        assertEquals("", builder.toString());
    }

    @Test
    public void withPortStringWithEmpty() {
        final UrlBuilder builder = new UrlBuilder()
                .withPort("");
        assertNull(builder.port());
        assertEquals("", builder.toString());
    }

    @Test
    public void withPortString() {
        final UrlBuilder builder = new UrlBuilder()
                .withPort("50");
        assertEquals(50, builder.port().intValue());
        assertEquals(":50", builder.toString());
    }

    @Test
    public void withPortStringWithForwardSlashButNoPath() {
        final UrlBuilder builder = new UrlBuilder()
                .withPort("50/");
        assertEquals(50, builder.port().intValue());
        assertEquals("/", builder.path());
        assertEquals(":50/", builder.toString());
    }

    @Test
    public void withPortStringWithPath() {
        final UrlBuilder builder = new UrlBuilder()
                .withPort("50/index.html");
        assertEquals(50, builder.port().intValue());
        assertEquals("/index.html", builder.path());
        assertEquals(":50/index.html", builder.toString());
    }

    @Test
    public void withPortStringWithQuestionMarkButNoQuery() {
        final UrlBuilder builder = new UrlBuilder()
                .withPort("50?");
        assertEquals(50, builder.port().intValue());
        assertEquals(0, builder.query().size());
        assertEquals(":50", builder.toString());
    }

    @Test
    public void withPortStringWithQuery() {
        final UrlBuilder builder = new UrlBuilder()
                .withPort("50?a=b&c=d");
        assertEquals(50, builder.port().intValue());
        assertThat(builder.toString(), CoreMatchers.containsString("?a=b&c=d"));
        assertEquals(":50?a=b&c=d", builder.toString());
    }

    @Test
    public void withPortStringWhenPortIsNull() {
        final UrlBuilder builder = new UrlBuilder()
            .withPort(8080);
        builder.withPort(null);
        assertNull(builder.port());
    }

    @Test
    public void withPortStringWhenPortIsEmpty() {
        final UrlBuilder builder = new UrlBuilder()
            .withPort(8080);
        builder.withPort("");
        assertNull(builder.port());
    }

    @Test
    public void withPortStringWhenPortIsNotEmpty() {
        final UrlBuilder builder = new UrlBuilder()
            .withPort(8080);
        builder.withPort("123");
        assertEquals(123, builder.port().intValue());
    }

    @Test
    public void withSchemeAndHostAndOneQueryParameter() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .setQueryParameter("A", "B");
        assertEquals("http://www.example.com?A=B", builder.toString());
    }

    @Test
    public void withSchemeAndHostAndOneQueryParameterWhenQueryParameterNameHasWhitespace() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .setQueryParameter("App les", "B");
        assertEquals("http://www.example.com?App les=B", builder.toString());
    }

    @Test
    public void withSchemeAndHostAndOneQueryParameterWhenQueryParameterNameHasPercent20() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .setQueryParameter("App%20les", "B");
        assertEquals("http://www.example.com?App%20les=B", builder.toString());
    }

    @Test
    public void withSchemeAndHostAndOneQueryParameterWhenQueryParameterValueHasWhitespace() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .setQueryParameter("Apples", "Go od");
        assertEquals("http://www.example.com?Apples=Go od", builder.toString());
    }

    @Test
    public void withSchemeAndHostAndOneQueryParameterWhenQueryParameterValueHasPercent20() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .setQueryParameter("Apples", "Go%20od");
        assertEquals("http://www.example.com?Apples=Go%20od", builder.toString());
    }

    @Test
    public void withSchemeAndHostAndTwoQueryParameters() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .setQueryParameter("A", "B")
                .setQueryParameter("C", "D");
        assertEquals("http://www.example.com?A=B&C=D", builder.toString());
    }

    @Test
    public void withSchemeAndHostAndPathAndTwoQueryParameters() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .setQueryParameter("A", "B")
                .setQueryParameter("C", "D")
                .withPath("index.html");
        assertEquals("http://www.example.com/index.html?A=B&C=D", builder.toString());
    }

    @Test
    public void withPathWhenBuilderPathIsNullAndPathIsNull() {
        final UrlBuilder builder = new UrlBuilder();
        builder.withPath(null);
        assertNull(builder.path());
    }

    @Test
    public void withPathWhenBuilderPathIsNullAndPathIsEmptyString() {
        final UrlBuilder builder = new UrlBuilder();
        builder.withPath("");
        assertNull(builder.path());
    }

    @Test
    public void withPathWhenBuilderPathIsNullAndPathIsForwardSlash() {
        final UrlBuilder builder = new UrlBuilder();
        builder.withPath("/");
        assertEquals("/", builder.path());
    }

    @Test
    public void withPathWhenBuilderPathIsNullAndPath() {
        final UrlBuilder builder = new UrlBuilder();
        builder.withPath("test/path.html");
        assertEquals("test/path.html", builder.path());
    }

    @Test
    public void withPathWhenBuilderPathIsForwardSlashAndPathIsNull() {
        final UrlBuilder builder = new UrlBuilder()
            .withPath("/");
        builder.withPath(null);
        assertNull(builder.path());
    }

    @Test
    public void withPathWhenBuilderPathIsForwardSlashAndPathIsEmptyString() {
        final UrlBuilder builder = new UrlBuilder()
            .withPath("/");
        builder.withPath("");
        assertNull(builder.path());
    }

    @Test
    public void withPathWhenBuilderPathIsForwardSlashAndPathIsForwardSlash() {
        final UrlBuilder builder = new UrlBuilder()
            .withPath("/");
        builder.withPath("/");
        assertEquals("/", builder.path());
    }

    @Test
    public void withPathWhenBuilderPathIsForwardSlashAndPath() {
        final UrlBuilder builder = new UrlBuilder()
            .withPath("/");
        builder.withPath("test/path.html");
        assertEquals("test/path.html", builder.path());
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
                .setQueryParameter("otherthing", "otherstuff");
        assertEquals("http://www.example.com/mypath?thing=stuff&otherthing=otherstuff", builder.toString());
    }

    @Test
    public void withAbsolutePathAndQuery() {
        final UrlBuilder builder = new UrlBuilder()
                .withScheme("http")
                .withHost("www.example.com")
                .withPath("http://www.othersite.com/mypath?thing=stuff")
                .setQueryParameter("otherthing", "otherstuff");
        assertEquals("http://www.othersite.com/mypath?thing=stuff&otherthing=otherstuff", builder.toString());
    }

    @Test
    public void withQueryWithNull() {
        final UrlBuilder builder = new UrlBuilder()
                .withQuery(null);
        assertEquals(0, builder.query().size());
        assertEquals("", builder.toString());
    }

    @Test
    public void withQueryWithEmpty() {
        final UrlBuilder builder = new UrlBuilder()
                .withQuery("");
        assertEquals(0, builder.query().size());
        assertEquals("", builder.toString());
    }

    @Test
    public void withQueryWithQuestionMark() {
        final UrlBuilder builder = new UrlBuilder()
                .withQuery("?");
        assertEquals(0, builder.query().size());
        assertEquals("", builder.toString());
    }

    @Test
    public void parseWithNullString() {
        final UrlBuilder builder = UrlBuilder.parse((String) null);
        assertEquals("", builder.toString());
    }

    @Test
    public void parseWithEmpty() {
        final UrlBuilder builder = UrlBuilder.parse("");
        assertEquals("", builder.toString());
    }

    @Test
    public void parseWithHost() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com");
        assertEquals("www.bing.com", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHost() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com");
        assertEquals("https://www.bing.com", builder.toString());
    }

    @Test
    public void parseWithHostAndPort() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com:8080");
        assertEquals("www.bing.com:8080", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPort() {
        final UrlBuilder builder = UrlBuilder.parse("ftp://www.bing.com:8080");
        assertEquals("ftp://www.bing.com:8080", builder.toString());
    }

    @Test
    public void parseWithHostAndPath() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com/my/path");
        assertEquals("www.bing.com/my/path", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPath() {
        final UrlBuilder builder = UrlBuilder.parse("ftp://www.bing.com/my/path");
        assertEquals("ftp://www.bing.com/my/path", builder.toString());
    }

    @Test
    public void parseWithHostAndPortAndPath() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com:1234/my/path");
        assertEquals("www.bing.com:1234/my/path", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPortAndPath() {
        final UrlBuilder builder = UrlBuilder.parse("ftp://www.bing.com:2345/my/path");
        assertEquals("ftp://www.bing.com:2345/my/path", builder.toString());
    }

    @Test
    public void parseWithHostAndOneQueryParameter() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com?a=1");
        assertEquals("www.bing.com?a=1", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndOneQueryParameter() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com?a=1");
        assertEquals("https://www.bing.com?a=1", builder.toString());
    }

    @Test
    public void parseWithHostAndPortAndOneQueryParameter() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com:123?a=1");
        assertEquals("www.bing.com:123?a=1", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPortAndOneQueryParameter() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com:987?a=1");
        assertEquals("https://www.bing.com:987?a=1", builder.toString());
    }

    @Test
    public void parseWithHostAndPathAndOneQueryParameter() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com/folder/index.html?a=1");
        assertEquals("www.bing.com/folder/index.html?a=1", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPathAndOneQueryParameter() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com/image.gif?a=1");
        assertEquals("https://www.bing.com/image.gif?a=1", builder.toString());
    }

    @Test
    public void parseWithHostAndPortAndPathAndOneQueryParameter() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com:123/index.html?a=1");
        assertEquals("www.bing.com:123/index.html?a=1", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPortAndPathAndOneQueryParameter() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com:987/my/path/again?a=1");
        assertEquals("https://www.bing.com:987/my/path/again?a=1", builder.toString());
    }

    @Test
    public void parseWithHostAndTwoQueryParameters() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com?a=1&b=2");
        assertEquals("www.bing.com?a=1&b=2", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndTwoQueryParameters() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com?a=1&b=2");
        assertEquals("https://www.bing.com?a=1&b=2", builder.toString());
    }

    @Test
    public void parseWithHostAndPortAndTwoQueryParameters() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com:123?a=1&b=2");
        assertEquals("www.bing.com:123?a=1&b=2", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPortAndTwoQueryParameters() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com:987?a=1&b=2");
        assertEquals("https://www.bing.com:987?a=1&b=2", builder.toString());
    }

    @Test
    public void parseWithHostAndPathAndTwoQueryParameters() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com/folder/index.html?a=1&b=2");
        assertEquals("www.bing.com/folder/index.html?a=1&b=2", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPathAndTwoQueryParameters() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com/image.gif?a=1&b=2");
        assertEquals("https://www.bing.com/image.gif?a=1&b=2", builder.toString());
    }

    @Test
    public void parseWithHostAndPortAndPathAndTwoQueryParameters() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com:123/index.html?a=1&b=2");
        assertEquals("www.bing.com:123/index.html?a=1&b=2", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPortAndPathAndTwoQueryParameters() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com:987/my/path/again?a=1&b=2");
        assertEquals("https://www.bing.com:987/my/path/again?a=1&b=2", builder.toString());
    }

    @Test
    public void parseWithColonInPath() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com/my:/path");
        assertEquals("https://www.bing.com/my:/path", builder.toString());
    }

    @Test
    public void parseURLWithNull() {
        final UrlBuilder builder = UrlBuilder.parse((URL) null);
        assertEquals("", builder.toString());
    }

    @Test
    public void parseURLWithSchemeAndHost() throws MalformedURLException {
        final UrlBuilder builder = UrlBuilder.parse(new URL("http://www.bing.com"));
        assertEquals("http://www.bing.com", builder.toString());
    }
}
