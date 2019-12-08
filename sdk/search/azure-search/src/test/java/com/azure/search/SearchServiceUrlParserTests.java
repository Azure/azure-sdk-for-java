// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SearchServiceUrlParserTests {

    @Test
    public void throwsOnNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            SearchServiceUrlParser.parseServiceUrlParts(null));
    }

    @Test
    public void throwsOnEmpty() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            SearchServiceUrlParser.parseServiceUrlParts(""));
    }

    @Test
    public void throwsOnBadUrl() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            SearchServiceUrlParser.parseServiceUrlParts("1234!"));
    }

    @Test
    public void throwsOnInvalidHost() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            SearchServiceUrlParser.parseServiceUrlParts("https://localhost"));
    }

    @Test
    public void parsesProdEndpoint() {
        SearchServiceUrlParser.SearchServiceUrlParts parts = SearchServiceUrlParser.parseServiceUrlParts("https://test1.search.windows.net");
        Assert.assertEquals("test1", parts.serviceName);
        Assert.assertEquals("search.windows.net", parts.dnsSuffix);
    }

    @Test
    public void parsesDfEndpoint() {
        SearchServiceUrlParser.SearchServiceUrlParts parts = SearchServiceUrlParser.parseServiceUrlParts("https://test1.search-dogfood.windows-int.net");
        Assert.assertEquals("test1", parts.serviceName);
        Assert.assertEquals("search-dogfood.windows-int.net", parts.dnsSuffix);
    }
}
