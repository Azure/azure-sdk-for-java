// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UriBuilderTests {
    @Test
    public void scheme() {
        final UriBuilder builder = new UriBuilder().setScheme("http");
        assertEquals("http://", builder.toString());
    }

    @Test
    public void schemeWhenSchemeIsNull() {
        final UriBuilder builder = new UriBuilder().setScheme("http");
        builder.setScheme(null);
        assertNull(builder.getScheme());
    }

    @Test
    public void schemeWhenSchemeIsEmpty() {
        final UriBuilder builder = new UriBuilder().setScheme("http");
        builder.setScheme("");
        assertNull(builder.getScheme());
    }

    @Test
    public void schemeWhenSchemeIsNotEmpty() {
        final UriBuilder builder = new UriBuilder().setScheme("http");
        builder.setScheme("https");
        assertEquals("https", builder.getScheme());
    }

    @Test
    public void schemeWhenSchemeContainsTerminator() {
        final UriBuilder builder = new UriBuilder().setScheme("http://");
        assertEquals("http", builder.getScheme());
        assertNull(builder.getHost());
        assertEquals("http://", builder.toString());
    }

    @Test
    public void schemeWhenSchemeContainsHost() {
        final UriBuilder builder = new UriBuilder().setScheme("http://www.example.com");
        assertEquals("http", builder.getScheme());
        assertEquals("www.example.com", builder.getHost());
        assertEquals("http://www.example.com", builder.toString());
    }

    @Test
    public void schemeAndHost() {
        final UriBuilder builder = new UriBuilder().setScheme("http").setHost("www.example.com");
        assertEquals("http://www.example.com", builder.toString());
    }

    @Test
    public void schemeAndHostWhenHostHasWhitespace() {
        final UriBuilder builder = new UriBuilder().setScheme("http").setHost("www.exa mple.com");
        assertEquals("http://www.exa mple.com", builder.toString());
    }

    @Test
    public void host() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com");
        assertEquals("www.example.com", builder.toString());
    }

    @Test
    public void hostWhenHostIsNull() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com");
        builder.setHost(null);
        assertNull(builder.getHost());
    }

    @Test
    public void hostWhenHostIsEmpty() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com");
        builder.setHost("");
        assertNull(builder.getHost());
    }

    @Test
    public void hostWhenHostIsNotEmpty() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com");
        builder.setHost("www.bing.com");
        assertEquals("www.bing.com", builder.getHost());
    }

    @Test
    public void hostWhenHostContainsSchemeTerminator() {
        final UriBuilder builder = new UriBuilder().setHost("://www.example.com");
        assertNull(builder.getScheme());
        assertEquals("www.example.com", builder.getHost());
        assertEquals("www.example.com", builder.toString());
    }

    @Test
    public void hostWhenHostContainsScheme() {
        final UriBuilder builder = new UriBuilder().setHost("https://www.example.com");
        assertEquals("https", builder.getScheme());
        assertEquals("www.example.com", builder.getHost());
        assertEquals("https://www.example.com", builder.toString());
    }

    @Test
    public void hostWhenHostContainsColonButNoPort() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com:");
        assertEquals("www.example.com", builder.getHost());
        assertNull(builder.getPort());
        assertEquals("www.example.com", builder.toString());
    }

    @Test
    public void hostWhenHostContainsPort() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com:1234");
        assertEquals("www.example.com", builder.getHost());
        assertEquals(1234, builder.getPort());
        assertEquals("www.example.com:1234", builder.toString());
    }

    @Test
    public void hostWhenHostContainsForwardSlashButNoPath() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com/");
        assertEquals("www.example.com", builder.getHost());
        assertEquals("/", builder.getPath());
        assertEquals("www.example.com/", builder.toString());
    }

    @Test
    public void hostWhenHostContainsPath() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com/index.html");
        assertEquals("www.example.com", builder.getHost());
        assertEquals("/index.html", builder.getPath());
        assertEquals("www.example.com/index.html", builder.toString());
    }

    @Test
    public void hostWhenHostContainsQuestionMarkButNoQuery() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com?");
        assertEquals("www.example.com", builder.getHost());
        assertEquals(0, builder.getQuery().size());
        assertEquals("www.example.com", builder.toString());
    }

    @Test
    public void hostWhenHostContainsQuery() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com?a=b");
        assertEquals("www.example.com", builder.getHost());
        assertTrue(builder.toString().contains("a=b"), "Expected 'a=b' in " + builder);
        assertEquals("www.example.com?a=b", builder.toString());
    }

    @Test
    public void hostWhenHostHasWhitespace() {
        final UriBuilder builder = new UriBuilder().setHost("www.exampl e.com");
        assertEquals("www.exampl e.com", builder.toString());
    }

    @Test
    public void hostAndPath() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com").setPath("my/path");
        assertEquals("www.example.com/my/path", builder.toString());
    }

    @Test
    public void hostAndPathWithSlashAfterHost() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com/").setPath("my/path");
        assertEquals("www.example.com/my/path", builder.toString());
    }

    @Test
    public void hostAndPathWithSlashBeforePath() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com").setPath("/my/path");
        assertEquals("www.example.com/my/path", builder.toString());
    }

    @Test
    public void hostAndPathWithSlashAfterHostAndBeforePath() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com/").setPath("/my/path");
        assertEquals("www.example.com/my/path", builder.toString());
    }

    @Test
    public void hostAndPathWithWhitespaceInPath() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com").setPath("my path");
        assertEquals("www.example.com/my path", builder.toString());
    }

    @Test
    public void hostAndPathWithPlusInPath() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com").setPath("my+path");
        assertEquals("www.example.com/my+path", builder.toString());
    }

    @Test
    public void hostAndPathWithPercent20InPath() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com").setPath("my%20path");
        assertEquals("www.example.com/my%20path", builder.toString());
    }

    @Test
    public void portInt() {
        final UriBuilder builder = new UriBuilder().setPort(50);
        assertEquals(50, builder.getPort());
        assertEquals(":50", builder.toString());
    }

    @Test
    public void portStringWithNull() {
        final UriBuilder builder = new UriBuilder().setPort(null);
        assertNull(builder.getPort());
        assertEquals("", builder.toString());
    }

    @Test
    public void portStringWithEmpty() {
        final UriBuilder builder = new UriBuilder().setPort("");
        assertNull(builder.getPort());
        assertEquals("", builder.toString());
    }

    @Test
    public void portString() {
        final UriBuilder builder = new UriBuilder().setPort("50");
        assertEquals(50, builder.getPort());
        assertEquals(":50", builder.toString());
    }

    @Test
    public void portStringWithForwardSlashButNoPath() {
        final UriBuilder builder = new UriBuilder().setPort("50/");
        assertEquals(50, builder.getPort());
        assertEquals("/", builder.getPath());
        assertEquals(":50/", builder.toString());
    }

    @Test
    public void portStringPath() {
        final UriBuilder builder = new UriBuilder().setPort("50/index.html");
        assertEquals(50, builder.getPort());
        assertEquals("/index.html", builder.getPath());
        assertEquals(":50/index.html", builder.toString());
    }

    @Test
    public void portStringWithQuestionMarkButNoQuery() {
        final UriBuilder builder = new UriBuilder().setPort("50?");
        assertEquals(50, builder.getPort());
        assertEquals(0, builder.getQuery().size());
        assertEquals(":50", builder.toString());
    }

    @Test
    public void portStringQuery() {
        final UriBuilder builder = new UriBuilder().setPort("50?a=b&c=d");
        assertEquals(50, builder.getPort());
        assertTrue(builder.toString().contains("?a=b&c=d"), "Expected '?a=b&c=d' in " + builder);
        assertEquals(":50?a=b&c=d", builder.toString());
    }

    @Test
    public void portStringWhenPortIsNull() {
        final UriBuilder builder = new UriBuilder().setPort(8080);
        builder.setPort(null);
        assertNull(builder.getPort());
    }

    @Test
    public void portStringWhenPortIsEmpty() {
        final UriBuilder builder = new UriBuilder().setPort(8080);
        builder.setPort("");
        assertNull(builder.getPort());
    }

    @Test
    public void portStringWhenPortIsNotEmpty() {
        final UriBuilder builder = new UriBuilder().setPort(8080);
        builder.setPort("132");
        assertEquals(132, builder.getPort());
    }

    @Test
    public void schemeAndHostAndOneQueryParameter() {
        final UriBuilder builder
            = new UriBuilder().setScheme("http").setHost("www.example.com").setQueryParameter("A", "B");
        assertEquals("http://www.example.com?A=B", builder.toString());
    }

    @Test
    public void schemeAndHostAndPathAndOneQueryParameterGetQuery() {
        final UriBuilder builder
            = new UriBuilder().setScheme("http").setHost("www.example.com").setQueryParameter("A", "B");
        assertEquals(builder.getQuery().get("A"), "B");
    }

    @Test
    public void schemeAndHostAndOneQueryParameterWhenQueryParameterNameHasWhitespace() {
        final UriBuilder builder
            = new UriBuilder().setScheme("http").setHost("www.example.com").setQueryParameter("App les", "B");
        assertEquals("http://www.example.com?App les=B", builder.toString());
    }

    @Test
    public void schemeAndHostAndOneQueryParameterWhenQueryParameterNameHasPercent20() {
        final UriBuilder builder
            = new UriBuilder().setScheme("http").setHost("www.example.com").setQueryParameter("App%20les", "B");
        assertEquals("http://www.example.com?App%20les=B", builder.toString());
    }

    @Test
    public void schemeAndHostAndOneQueryParameterWhenQueryParameterValueHasWhitespace() {
        final UriBuilder builder
            = new UriBuilder().setScheme("http").setHost("www.example.com").setQueryParameter("Apples", "Go od");
        assertEquals("http://www.example.com?Apples=Go od", builder.toString());
    }

    @Test
    public void schemeAndHostAndOneQueryParameterWhenQueryParameterValueHasPercent20() {
        final UriBuilder builder
            = new UriBuilder().setScheme("http").setHost("www.example.com").setQueryParameter("Apples", "Go%20od");
        assertEquals("http://www.example.com?Apples=Go%20od", builder.toString());
    }

    @Test
    public void schemeAndHostAndTwoQueryParameters() {
        final UriBuilder builder = new UriBuilder().setScheme("http")
            .setHost("www.example.com")
            .setQueryParameter("A", "B")
            .setQueryParameter("C", "D");
        assertEquals("http://www.example.com?A=B&C=D", builder.toString());
    }

    @Test
    public void schemeAndHostAndPathAndTwoQueryParameters() {
        final UriBuilder builder = new UriBuilder().setScheme("http")
            .setHost("www.example.com")
            .setQueryParameter("A", "B")
            .setQueryParameter("C", "D")
            .setPath("index.html");
        assertEquals("http://www.example.com/index.html?A=B&C=D", builder.toString());
    }

    @Test
    public void schemeAndHostAndPathAndTwoIdenticalQueryParameters() {
        final UriBuilder builder = new UriBuilder().setScheme("http")
            .setHost("www.example.com")
            .addQueryParameter("A", "B")
            .addQueryParameter("A", "D")
            .setPath("index.html");
        assertEquals("http://www.example.com/index.html?A=B&A=D", builder.toString());
    }

    @Test
    public void schemeAndHostAndPathAndTwoIdenticalQueryParametersGetQuery() {
        final UriBuilder builder = new UriBuilder().setScheme("http")
            .setHost("www.example.com")
            .addQueryParameter("A", "B")
            .addQueryParameter("A", "D")
            .setPath("index.html");
        assertEquals(builder.getQuery().get("A"), "B,D");
    }

    @Test
    public void pathWhenBuilderPathIsNullAndPathIsNull() {
        final UriBuilder builder = new UriBuilder();
        builder.setPath(null);
        assertNull(builder.getPath());
    }

    @Test
    public void pathWhenBuilderPathIsNullAndPathIsEmptyString() {
        final UriBuilder builder = new UriBuilder();
        builder.setPath("");
        assertNull(builder.getPath());
    }

    @Test
    public void pathWhenBuilderPathIsNullAndPathIsForwardSlash() {
        final UriBuilder builder = new UriBuilder();
        builder.setPath("/");
        assertEquals("/", builder.getPath());
    }

    @Test
    public void pathWhenBuilderPathIsNullAndPath() {
        final UriBuilder builder = new UriBuilder();
        builder.setPath("test/path.html");
        assertEquals("test/path.html", builder.getPath());
    }

    @Test
    public void pathWhenBuilderPathIsForwardSlashAndPathIsNull() {
        final UriBuilder builder = new UriBuilder().setPath("/");
        builder.setPath(null);
        assertNull(builder.getPath());
    }

    @Test
    public void pathWhenBuilderPathIsForwardSlashAndPathIsEmptyString() {
        final UriBuilder builder = new UriBuilder().setPath("/");
        builder.setPath("");
        assertNull(builder.getPath());
    }

    @Test
    public void pathWhenBuilderPathIsForwardSlashAndPathIsForwardSlash() {
        final UriBuilder builder = new UriBuilder().setPath("/");
        builder.setPath("/");
        assertEquals("/", builder.getPath());
    }

    @Test
    public void pathWhenBuilderPathIsForwardSlashAndPath() {
        final UriBuilder builder = new UriBuilder().setPath("/");
        builder.setPath("test/path.html");
        assertEquals("test/path.html", builder.getPath());
    }

    @Test
    public void pathWhenHostContainsPath() {
        final UriBuilder builder = new UriBuilder().setHost("www.example.com/site").setPath("index.html");
        assertEquals("www.example.com", builder.getHost());
        assertEquals("index.html", builder.getPath());
        assertEquals("www.example.com/index.html", builder.toString());
    }

    @Test
    public void pathFirstWhenHostContainsPath() {
        final UriBuilder builder = new UriBuilder().setPath("index.html").setHost("www.example.com/site");
        assertEquals("www.example.com", builder.getHost());
        assertEquals("/site", builder.getPath());
        assertEquals("www.example.com/site", builder.toString());
    }

    @Test
    public void emptyPathWhenHostContainsPath() {
        final UriBuilder builder = new UriBuilder().setPath("").setHost("www.example.com/site");
        assertEquals("www.example.com", builder.getHost());
        assertEquals("/site", builder.getPath());
        assertEquals("www.example.com/site", builder.toString());
    }

    @Test
    public void slashPathWhenHostContainsPath() {
        final UriBuilder builder = new UriBuilder().setPath("//").setHost("www.example.com/site");
        assertEquals("www.example.com", builder.getHost());
        assertEquals("/site", builder.getPath());
        assertEquals("www.example.com/site", builder.toString());
    }

    @Test
    public void withAbsolutePath() {
        final UriBuilder builder
            = new UriBuilder().setScheme("http").setHost("www.example.com").setPath("http://www.othersite.com");
        assertEquals("http://www.othersite.com", builder.toString());
    }

    @Test
    public void queryInPath() {
        final UriBuilder builder = new UriBuilder().setScheme("http")
            .setHost("www.example.com")
            .setPath("mypath?thing=stuff")
            .setQueryParameter("otherthing", "otherstuff");
        assertEquals("http://www.example.com/mypath?thing=stuff&otherthing=otherstuff", builder.toString());
    }

    @Test
    public void withAbsolutePathAndQuery() {
        final UriBuilder builder = new UriBuilder().setScheme("http")
            .setHost("www.example.com")
            .setPath("http://www.othersite.com/mypath?thing=stuff")
            .setQueryParameter("otherthing", "otherstuff");
        assertEquals("http://www.othersite.com/mypath?thing=stuff&otherthing=otherstuff", builder.toString());
    }

    @Test
    public void queryWithNull() {
        final UriBuilder builder = new UriBuilder().setQuery(null);
        assertEquals(0, builder.getQuery().size());
        assertEquals("", builder.toString());
    }

    @Test
    public void queryWithEmpty() {
        final UriBuilder builder = new UriBuilder().setQuery("");
        assertEquals(0, builder.getQuery().size());
        assertEquals("", builder.toString());
    }

    @Test
    public void queryWithQuestionMark() {
        final UriBuilder builder = new UriBuilder().setQuery("?");
        assertEquals(0, builder.getQuery().size());
        assertEquals("", builder.toString());
    }

    @Test
    public void parseWithNullString() {
        final UriBuilder builder = UriBuilder.parse((String) null);
        assertEquals("", builder.toString());
    }

    @Test
    public void parseWithEmpty() {
        final UriBuilder builder = UriBuilder.parse("");
        assertEquals("", builder.toString());
    }

    @Test
    public void parseHost() {
        final UriBuilder builder = UriBuilder.parse("www.bing.com");
        assertEquals("www.bing.com", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHost() {
        final UriBuilder builder = UriBuilder.parse("https://www.bing.com");
        assertEquals("https://www.bing.com", builder.toString());
    }

    @Test
    public void parseHostAndPort() {
        final UriBuilder builder = UriBuilder.parse("www.bing.com:8080");
        assertEquals("www.bing.com:8080", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPort() {
        final UriBuilder builder = UriBuilder.parse("ftp://www.bing.com:8080");
        assertEquals("ftp://www.bing.com:8080", builder.toString());
    }

    @Test
    public void parseHostAndPath() {
        final UriBuilder builder = UriBuilder.parse("www.bing.com/my/path");
        assertEquals("www.bing.com/my/path", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPath() {
        final UriBuilder builder = UriBuilder.parse("ftp://www.bing.com/my/path");
        assertEquals("ftp://www.bing.com/my/path", builder.toString());
    }

    @Test
    public void parseHostAndPortAndPath() {
        final UriBuilder builder = UriBuilder.parse("www.bing.com:1234/my/path");
        assertEquals("www.bing.com:1234/my/path", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPortAndPath() {
        final UriBuilder builder = UriBuilder.parse("ftp://www.bing.com:2345/my/path");
        assertEquals("ftp://www.bing.com:2345/my/path", builder.toString());
    }

    @Test
    public void parseHostAndOneQueryParameter() {
        final UriBuilder builder = UriBuilder.parse("www.bing.com?a=1");
        assertEquals("www.bing.com?a=1", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndOneQueryParameter() {
        final UriBuilder builder = UriBuilder.parse("https://www.bing.com?a=1");
        assertEquals("https://www.bing.com?a=1", builder.toString());
    }

    @Test
    public void parseHostAndPortAndOneQueryParameter() {
        final UriBuilder builder = UriBuilder.parse("www.bing.com:123?a=1");
        assertEquals("www.bing.com:123?a=1", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPortAndOneQueryParameter() {
        final UriBuilder builder = UriBuilder.parse("https://www.bing.com:987?a=1");
        assertEquals("https://www.bing.com:987?a=1", builder.toString());
    }

    @Test
    public void parseHostAndPathAndOneQueryParameter() {
        final UriBuilder builder = UriBuilder.parse("www.bing.com/folder/index.html?a=1");
        assertEquals("www.bing.com/folder/index.html?a=1", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPathAndOneQueryParameter() {
        final UriBuilder builder = UriBuilder.parse("https://www.bing.com/image.gif?a=1");
        assertEquals("https://www.bing.com/image.gif?a=1", builder.toString());
    }

    @Test
    public void parseHostAndPortAndPathAndOneQueryParameter() {
        final UriBuilder builder = UriBuilder.parse("www.bing.com:123/index.html?a=1");
        assertEquals("www.bing.com:123/index.html?a=1", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPortAndPathAndOneQueryParameter() {
        final UriBuilder builder = UriBuilder.parse("https://www.bing.com:987/my/path/again?a=1");
        assertEquals("https://www.bing.com:987/my/path/again?a=1", builder.toString());
    }

    @Test
    public void parseHostAndTwoQueryParameters() {
        final UriBuilder builder = UriBuilder.parse("www.bing.com?a=1&b=2");
        assertEquals("www.bing.com?a=1&b=2", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndTwoQueryParameters() {
        final UriBuilder builder = UriBuilder.parse("https://www.bing.com?a=1&b=2");
        assertEquals("https://www.bing.com?a=1&b=2", builder.toString());
    }

    @Test
    public void parseHostAndPortAndTwoQueryParameters() {
        final UriBuilder builder = UriBuilder.parse("www.bing.com:123?a=1&b=2");
        assertEquals("www.bing.com:123?a=1&b=2", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPortAndTwoQueryParameters() {
        final UriBuilder builder = UriBuilder.parse("https://www.bing.com:987?a=1&b=2");
        assertEquals("https://www.bing.com:987?a=1&b=2", builder.toString());
    }

    @Test
    public void parseHostAndPathAndTwoQueryParameters() {
        final UriBuilder builder = UriBuilder.parse("www.bing.com/folder/index.html?a=1&b=2");
        assertEquals("www.bing.com/folder/index.html?a=1&b=2", builder.toString());
    }

    @Test
    public void parseHostAndPathAndTwoIdenticalQueryParameters() {
        final UriBuilder builder = UriBuilder.parse("www.bing.com/folder/index.html?a=1&a=2");
        assertEquals("www.bing.com/folder/index.html?a=1&a=2", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPathAndTwoQueryParameters() {
        final UriBuilder builder = UriBuilder.parse("https://www.bing.com/image.gif?a=1&b=2");
        assertEquals("https://www.bing.com/image.gif?a=1&b=2", builder.toString());
    }

    @Test
    public void parseHostAndPortAndPathAndTwoQueryParameters() {
        final UriBuilder builder = UriBuilder.parse("www.bing.com:123/index.html?a=1&b=2");
        assertEquals("www.bing.com:123/index.html?a=1&b=2", builder.toString());
    }

    @Test
    public void parseWithProtocolAndHostAndPortAndPathAndTwoQueryParameters() {
        final UriBuilder builder = UriBuilder.parse("https://www.bing.com:987/my/path/again?a=1&b=2");
        assertEquals("https://www.bing.com:987/my/path/again?a=1&b=2", builder.toString());
    }

    @Test
    public void parseWithColonInPath() {
        final UriBuilder builder = UriBuilder.parse("https://www.bing.com/my:/path");
        assertEquals("https://www.bing.com/my:/path", builder.toString());
    }

    @Test
    public void parseURIWithNull() {
        final UriBuilder builder = UriBuilder.parse((URI) null);
        assertEquals("", builder.toString());
    }

    @Test
    public void parseURISchemeAndHost() {
        final UriBuilder builder = UriBuilder.parse(URI.create("http://www.bing.com"));
        assertEquals("http://www.bing.com", builder.toString());
    }

    @Test
    public void parseUriEverything() {
        final UriBuilder builder = UriBuilder.parse(URI.create("https://www.bing.com:123/index.html?a=1&b=2"));
        assertEquals("https://www.bing.com:123/index.html?a=1&b=2", builder.toString());
    }

    @Test
    public void parallelParsing() throws InterruptedException {
        AtomicInteger callCount = new AtomicInteger();
        List<Callable<UriBuilder>> tasks = IntStream.range(0, 20000).mapToObj(i -> (Callable<UriBuilder>) () -> {
            callCount.incrementAndGet();
            return UriBuilder.parse("https://example" + i + ".com");
        }).collect(Collectors.toCollection(() -> new ArrayList<>(20000)));

        List<Future<UriBuilder>> futures = SharedExecutorService.getInstance().invokeAll(tasks, 10, TimeUnit.SECONDS);
        for (Future<UriBuilder> future : futures) {
            assertTrue(future.isDone());
            assertDoesNotThrow(() -> future.get());
        }
        assertEquals(20000, callCount.get());
    }

    @Test
    public void parseUniqueURIs() {
        IntStream.range(0, 20000).parallel().forEach(i -> {
            UriBuilder uriBuilder = io.clientcore.core.utils.UriBuilder.parse("www.bing.com:123/index.html?a=" + i);
            assertNotNull(uriBuilder);
            assertEquals("www.bing.com:123/index.html?a=" + i, uriBuilder.toString());
        });

        // validate the size of the cache is not greater than 10000
        assertTrue(UriBuilder.getParsedUris().size() <= 10000);
    }

    @Test
    public void clearQuery() {
        final UriBuilder builder = new UriBuilder().setScheme("http")
            .setHost("www.example.com")
            .setQueryParameter("A", "B")
            .setQueryParameter("C", "D");
        assertEquals("http://www.example.com?A=B&C=D", builder.toString());
        builder.clearQuery();
        assertEquals("http://www.example.com", builder.toString());
    }
}
