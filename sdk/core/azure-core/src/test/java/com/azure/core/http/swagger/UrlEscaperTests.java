// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.swagger;

import org.junit.Assert;
import org.junit.Test;

public class UrlEscaperTests {
    private static String simple = "abcABC-123";
    private static String genDelim = "abc[456#78";
    private static String safeForPath = "abc:456@78";
    private static String safeForQuery = "abc/456?78";

    @Test
    public void canEscapePathSimple() {
        PercentEscaper escaper = UrlEscapers.PATH_ESCAPER;
        String actual = escaper.escape(simple);
        Assert.assertEquals(simple, actual);
    }

    @Test
    public void canEscapeQuerySimple() {
        PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;
        String actual = escaper.escape(simple);
        Assert.assertEquals(simple, actual);
    }

    @Test
    public void canEscapePathWithGenDelim() {
        PercentEscaper escaper = UrlEscapers.PATH_ESCAPER;
        String actual = escaper.escape(genDelim);
        Assert.assertEquals("abc%5b456%2378", actual);
    }

    @Test
    public void canEscapeQueryWithGenDelim() {
        PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;
        String actual = escaper.escape(genDelim);
        Assert.assertEquals("abc%5b456%2378", actual);
    }

    @Test
    public void canEscapePathWithSafeForPath() {
        PercentEscaper escaper = UrlEscapers.PATH_ESCAPER;
        String actual = escaper.escape(safeForPath);
        Assert.assertEquals(safeForPath, actual);
    }

    @Test
    public void canEscapeQueryWithSafeForPath() {
        PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;
        String actual = escaper.escape(safeForPath);
        Assert.assertEquals("abc%3a456%4078", actual);
    }

    @Test
    public void canEscapePathWithSafeForQuery() {
        PercentEscaper escaper = UrlEscapers.PATH_ESCAPER;
        String actual = escaper.escape(safeForQuery);
        Assert.assertEquals("abc%2f456%3f78", actual);
    }

    @Test
    public void canEscapeQueryWithSafeForQuery() {
        PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;
        String actual = escaper.escape(safeForQuery);
        Assert.assertEquals(safeForQuery, actual);
    }
}
