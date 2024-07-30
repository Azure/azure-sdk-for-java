// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlEscaperTests {
    private static final String SIMPLE = "abcABC-123";
    private static final String GEN_DELIM = "abc[456#78";
    private static final String SAFE_FOR_PATH = "abc:456@78";
    private static final String SAFE_FOR_QUERY = "abc/456?78";

    @Test
    public void canEscapePathSimple() {
        PercentEscaper escaper = UrlEscapers.PATH_ESCAPER;
        String actual = escaper.escape(SIMPLE);

        assertEquals(SIMPLE, actual);
    }

    @Test
    public void canEscapeQuerySimple() {
        PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;
        String actual = escaper.escape(SIMPLE);

        assertEquals(SIMPLE, actual);
    }

    @Test
    public void canEscapePathWithGenDelim() {
        PercentEscaper escaper = UrlEscapers.PATH_ESCAPER;
        String actual = escaper.escape(GEN_DELIM);

        assertEquals("abc%5B456%2378", actual);
    }

    @Test
    public void canEscapeQueryWithGenDelim() {
        PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;
        String actual = escaper.escape(GEN_DELIM);

        assertEquals("abc%5B456%2378", actual);
    }

    @Test
    public void canEscapePathWithSafeForPath() {
        PercentEscaper escaper = UrlEscapers.PATH_ESCAPER;
        String actual = escaper.escape(SAFE_FOR_PATH);

        assertEquals(SAFE_FOR_PATH, actual);
    }

    @Test
    public void canEscapeQueryWithSafeForPath() {
        PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;
        String actual = escaper.escape(SAFE_FOR_PATH);

        assertEquals("abc%3A456%4078", actual);
    }

    @Test
    public void canEscapePathWithSafeForQuery() {
        PercentEscaper escaper = UrlEscapers.PATH_ESCAPER;
        String actual = escaper.escape(SAFE_FOR_QUERY);

        assertEquals("abc%2F456%3F78", actual);
    }

    @Test
    public void canEscapeQueryWithSafeForQuery() {
        PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;
        String actual = escaper.escape(SAFE_FOR_QUERY);

        assertEquals(SAFE_FOR_QUERY, actual);
    }
}
