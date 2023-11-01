// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.rest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlEscaperTests {
    private static String simple = "abcABC-123";
    private static String genDelim = "abc[456#78";
    private static String safeForPath = "abc:456@78";
    private static String safeForQuery = "abc/456?78";

    @Test
    public void canEscapePathSimple() {
        PercentEscaper escaper = UrlEscapers.PATH_ESCAPER;
        String actual = escaper.escape(simple);

        assertEquals(simple, actual);
    }

    @Test
    public void canEscapeQuerySimple() {
        PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;
        String actual = escaper.escape(simple);

        assertEquals(simple, actual);
    }

    @Test
    public void canEscapePathWithGenDelim() {
        PercentEscaper escaper = UrlEscapers.PATH_ESCAPER;
        String actual = escaper.escape(genDelim);

        assertEquals("abc%5B456%2378", actual);
    }

    @Test
    public void canEscapeQueryWithGenDelim() {
        PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;
        String actual = escaper.escape(genDelim);

        assertEquals("abc%5B456%2378", actual);
    }

    @Test
    public void canEscapePathWithSafeForPath() {
        PercentEscaper escaper = UrlEscapers.PATH_ESCAPER;
        String actual = escaper.escape(safeForPath);

        assertEquals(safeForPath, actual);
    }

    @Test
    public void canEscapeQueryWithSafeForPath() {
        PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;
        String actual = escaper.escape(safeForPath);

        assertEquals("abc%3A456%4078", actual);
    }

    @Test
    public void canEscapePathWithSafeForQuery() {
        PercentEscaper escaper = UrlEscapers.PATH_ESCAPER;
        String actual = escaper.escape(safeForQuery);

        assertEquals("abc%2F456%3F78", actual);
    }

    @Test
    public void canEscapeQueryWithSafeForQuery() {
        PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;
        String actual = escaper.escape(safeForQuery);

        assertEquals(safeForQuery, actual);
    }
}
