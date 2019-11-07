// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UrlEscaperTests {
    private static String simple = "abcABC-123";
    private static String genDelim = "abc[456#78";
    private static String safeForPath = "abc:456@78";
    private static String safeForQuery = "abc/456?78";

    @Test
    public void canEscapePathSimple() {
        PercentEscaper escaper = UrlEscapers.PATH_ESCAPER;
        String actual = escaper.escape(simple);
        Assertions.assertEquals(simple, actual);
    }

    @Test
    public void canEscapeQuerySimple() {
        PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;
        String actual = escaper.escape(simple);
        Assertions.assertEquals(simple, actual);
    }

    @Test
    public void canEscapePathWithGenDelim() {
        PercentEscaper escaper = UrlEscapers.PATH_ESCAPER;
        String actual = escaper.escape(genDelim);
        Assertions.assertEquals("abc%5b456%2378", actual);
    }

    @Test
    public void canEscapeQueryWithGenDelim() {
        PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;
        String actual = escaper.escape(genDelim);
        Assertions.assertEquals("abc%5b456%2378", actual);
    }

    @Test
    public void canEscapePathWithSafeForPath() {
        PercentEscaper escaper = UrlEscapers.PATH_ESCAPER;
        String actual = escaper.escape(safeForPath);
        Assertions.assertEquals(safeForPath, actual);
    }

    @Test
    public void canEscapeQueryWithSafeForPath() {
        PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;
        String actual = escaper.escape(safeForPath);
        Assertions.assertEquals("abc%3a456%4078", actual);
    }

    @Test
    public void canEscapePathWithSafeForQuery() {
        PercentEscaper escaper = UrlEscapers.PATH_ESCAPER;
        String actual = escaper.escape(safeForQuery);
        Assertions.assertEquals("abc%2f456%3f78", actual);
    }

    @Test
    public void canEscapeQueryWithSafeForQuery() {
        PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;
        String actual = escaper.escape(safeForQuery);
        Assertions.assertEquals(safeForQuery, actual);
    }
}
