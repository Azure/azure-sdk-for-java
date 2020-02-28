// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SearchServiceUrlParserTests {

    @Test
    public void throwsOnNull() {
        assertThrows(IllegalArgumentException.class, () ->
            SearchServiceUrlParser.parseServiceUrlParts(null));
    }

    @Test
    public void throwsOnEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
            SearchServiceUrlParser.parseServiceUrlParts(""));
    }

    @Test
    public void throwsOnBadUrl() {
        assertThrows(IllegalArgumentException.class, () ->
            SearchServiceUrlParser.parseServiceUrlParts("1234!"));
    }

    @Test
    public void throwsOnInvalidHost() {
        assertThrows(IllegalArgumentException.class, () ->
            SearchServiceUrlParser.parseServiceUrlParts("https://localhost"));
    }

    @Test
    public void parsesProdEndpoint() {
        SearchServiceUrlParser.SearchServiceUrlParts parts = SearchServiceUrlParser.parseServiceUrlParts("https://test1.search.windows.net");
        assertEquals("test1", parts.serviceName);
        assertEquals("search.windows.net", parts.dnsSuffix);
    }

    @Test
    public void parsesDfEndpoint() {
        SearchServiceUrlParser.SearchServiceUrlParts parts = SearchServiceUrlParser.parseServiceUrlParts("https://test1.search-dogfood.windows-int.net");
        assertEquals("test1", parts.serviceName);
        assertEquals("search-dogfood.windows-int.net", parts.dnsSuffix);
    }
}
