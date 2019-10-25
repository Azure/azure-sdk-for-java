// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import org.junit.Assert;
import org.junit.Test;

public class SearchServiceUrlParserTests {

    @Test(expected = IllegalArgumentException.class)
    public void throwsOnNull() {
        SearchServiceUrlParser.parseServiceUrlParts(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsOnEmpty() {
        SearchServiceUrlParser.parseServiceUrlParts("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsOnBadUrl() {
        SearchServiceUrlParser.parseServiceUrlParts("1234!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsOnInvalidHost() {
        SearchServiceUrlParser.parseServiceUrlParts("https://localhost");
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
