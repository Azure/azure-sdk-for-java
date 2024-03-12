// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UrlTokenizerTests {
    @Test
    public void constructor() {
        final UrlTokenizer tokenizer = new UrlTokenizer("http://www.bing.com");
        assertNull(tokenizer.current());
    }

    @Test
    public void nextWithNullText() {
        final UrlTokenizer tokenizer = new UrlTokenizer(null);
        assertFalse(tokenizer.next());
        assertNull(tokenizer.current());
    }

    @Test
    public void nextWithEmptyText() {
        final UrlTokenizer tokenizer = new UrlTokenizer("");
        assertFalse(tokenizer.next());
        assertNull(tokenizer.current());
    }

    @Test
    public void nextWithSchemeButNoSeparator() {
        nextTest("http", UrlToken.host("http"));
    }

    @Test
    public void nextWithSchemeAndColon() {
        nextTest("http:", UrlToken.host("http"), UrlToken.port(""));
    }

    @Test
    public void nextWithSchemeAndColonAndForwardSlash() {
        nextTest("http:/", UrlToken.host("http"), UrlToken.port(""), UrlToken.path("/"));
    }

    @Test
    public void nextWithSchemeAndColonAndTwoForwardSlashes() {
        nextTest("http://", UrlToken.scheme("http"), UrlToken.host(""));
    }

    @Test
    public void nextWithSchemeAndHost() {
        nextTest("https://www.example.com", UrlToken.scheme("https"), UrlToken.host("www.example.com"));
    }

    @Test
    public void nextWithSchemeAndHostAndColon() {
        nextTest("https://www.example.com:", UrlToken.scheme("https"), UrlToken.host("www.example.com"),
            UrlToken.port(""));
    }

    @Test
    public void nextWithSchemeAndHostAndPort() {
        nextTest("https://www.example.com:8080", UrlToken.scheme("https"), UrlToken.host("www.example.com"),
            UrlToken.port("8080"));
    }

    @Test
    public void nextWithSchemeAndHostAndPortAndForwardSlash() {
        nextTest("ftp://www.bing.com:132/", UrlToken.scheme("ftp"), UrlToken.host("www.bing.com"), UrlToken.port("132"),
            UrlToken.path("/"));
    }

    @Test
    public void nextWithSchemeAndHostAndPortAndPath() {
        nextTest("ftp://www.bing.com:132/a/b/c.txt", UrlToken.scheme("ftp"), UrlToken.host("www.bing.com"),
            UrlToken.port("132"), UrlToken.path("/a/b/c.txt"));
    }

    @Test
    public void nextWithSchemeAndHostAndPortAndQuestionMark() {
        nextTest("ftp://www.bing.com:132?", UrlToken.scheme("ftp"), UrlToken.host("www.bing.com"), UrlToken.port("132"),
            UrlToken.query(""));
    }

    @Test
    public void nextWithSchemeAndHostAndPortAndQuery() {
        nextTest("ftp://www.bing.com:132?a=b&c=d", UrlToken.scheme("ftp"), UrlToken.host("www.bing.com"),
            UrlToken.port("132"), UrlToken.query("a=b&c=d"));
    }

    @Test
    public void nextWithSchemeAndHostAndForwardSlash() {
        nextTest("https://www.example.com/", UrlToken.scheme("https"), UrlToken.host("www.example.com"),
            UrlToken.path("/"));
    }

    @Test
    public void nextWithSchemeAndHostAndPath() {
        nextTest("https://www.example.com/index.html", UrlToken.scheme("https"), UrlToken.host("www.example.com"),
            UrlToken.path("/index.html"));
    }

    @Test
    public void nextWithSchemeAndHostAndPathAndQuestionMark() {
        nextTest("https://www.example.com/index.html?", UrlToken.scheme("https"), UrlToken.host("www.example.com"),
            UrlToken.path("/index.html"), UrlToken.query(""));
    }

    @Test
    public void nextWithSchemeAndHostAndPathAndQuery() {
        nextTest("https://www.example.com/index.html?alpha=beta", UrlToken.scheme("https"),
            UrlToken.host("www.example.com"), UrlToken.path("/index.html"), UrlToken.query("alpha=beta"));
    }

    @Test
    public void nextWithSchemeAndHostAndQuestionMark() {
        nextTest("https://www.example.com?", UrlToken.scheme("https"), UrlToken.host("www.example.com"),
            UrlToken.query(""));
    }

    @Test
    public void nextWithSchemeAndHostAndQuery() {
        nextTest("https://www.example.com?a=b", UrlToken.scheme("https"), UrlToken.host("www.example.com"),
            UrlToken.query("a=b"));
    }

    @Test
    public void nextWithHostAndForwardSlash() {
        nextTest("www.test.com/", UrlToken.host("www.test.com"), UrlToken.path("/"));
    }

    @Test
    public void nextWithHostAndQuestionMark() {
        nextTest("www.test.com?", UrlToken.host("www.test.com"), UrlToken.query(""));
    }

    @Test
    public void nextWithPath() {
        nextTest("folder/index.html", UrlToken.host("folder"), UrlToken.path("/index.html"));
    }

    @Test
    public void nextWithForwardSlashAndPath() {
        nextTest("/folder/index.html", UrlToken.host(""), UrlToken.path("/folder/index.html"));
    }

    private static void nextTest(String text, UrlToken... expectedTokens) {
        final UrlTokenizer tokenizer = new UrlTokenizer(text);
        final List<UrlToken> tokenList = new ArrayList<>();
        while (tokenizer.next()) {
            tokenList.add(tokenizer.current());
        }
        final UrlToken[] tokenArray = new UrlToken[tokenList.size()];
        tokenList.toArray(tokenArray);
        assertArrayEquals(expectedTokens, tokenArray);

        assertFalse(tokenizer.next());
        assertNull(tokenizer.current());
    }
}
