// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlParserPathTest {

    @Test
    public void testGetPathFromUrl() {
        assertThat(UrlParser.getPath("https://localhost")).isEqualTo("");
        assertThat(UrlParser.getPath("https://localhost/")).isEqualTo("/");
        assertThat(UrlParser.getPath("https://localhost/path")).isEqualTo("/path");
        assertThat(UrlParser.getPath("https://localhost/path/")).isEqualTo("/path/");
        assertThat(UrlParser.getPath("https://localhost/more/path")).isEqualTo("/more/path");
        assertThat(UrlParser.getPath("https://localhost/more/path/")).isEqualTo("/more/path/");

        assertThat(UrlParser.getPath("https://localhost?")).isEqualTo("");
        assertThat(UrlParser.getPath("https://localhost/?")).isEqualTo("/");
        assertThat(UrlParser.getPath("https://localhost/path?")).isEqualTo("/path");
        assertThat(UrlParser.getPath("https://localhost/path/?")).isEqualTo("/path/");
        assertThat(UrlParser.getPath("https://localhost/more/path?")).isEqualTo("/more/path");
        assertThat(UrlParser.getPath("https://localhost/more/path/?")).isEqualTo("/more/path/");

        assertThat(UrlParser.getPath("https://localhost?query")).isEqualTo("");
        assertThat(UrlParser.getPath("https://localhost/?query")).isEqualTo("/");
        assertThat(UrlParser.getPath("https://localhost/path?query")).isEqualTo("/path");
        assertThat(UrlParser.getPath("https://localhost/path/?query")).isEqualTo("/path/");
        assertThat(UrlParser.getPath("https://localhost/more/path?query")).isEqualTo("/more/path");
        assertThat(UrlParser.getPath("https://localhost/more/path/?query")).isEqualTo("/more/path/");

        assertThat(UrlParser.getPath("https://localhost#")).isEqualTo("");
        assertThat(UrlParser.getPath("https://localhost/#")).isEqualTo("/");
        assertThat(UrlParser.getPath("https://localhost/path#")).isEqualTo("/path");
        assertThat(UrlParser.getPath("https://localhost/path/#")).isEqualTo("/path/");
        assertThat(UrlParser.getPath("https://localhost/more/path#")).isEqualTo("/more/path");
        assertThat(UrlParser.getPath("https://localhost/more/path/#")).isEqualTo("/more/path/");

        assertThat(UrlParser.getPath("https://localhost#fragment")).isEqualTo("");
        assertThat(UrlParser.getPath("https://localhost/#fragment")).isEqualTo("/");
        assertThat(UrlParser.getPath("https://localhost/path#fragment")).isEqualTo("/path");
        assertThat(UrlParser.getPath("https://localhost/path/#fragment")).isEqualTo("/path/");
        assertThat(UrlParser.getPath("https://localhost/more/path#fragment")).isEqualTo("/more/path");
        assertThat(UrlParser.getPath("https://localhost/more/path/#fragment")).isEqualTo("/more/path/");
    }

    @Test
    public void testGetPathFromUrlWithPort() {
        assertThat(UrlParser.getPath("https://localhost:8080")).isEqualTo("");
        assertThat(UrlParser.getPath("https://localhost:8080/")).isEqualTo("/");
        assertThat(UrlParser.getPath("https://localhost:8080/path")).isEqualTo("/path");
        assertThat(UrlParser.getPath("https://localhost:8080/path/")).isEqualTo("/path/");
        assertThat(UrlParser.getPath("https://localhost:8080/more/path")).isEqualTo("/more/path");
        assertThat(UrlParser.getPath("https://localhost:8080/more/path/")).isEqualTo("/more/path/");

        assertThat(UrlParser.getPath("https://localhost:8080?")).isEqualTo("");
        assertThat(UrlParser.getPath("https://localhost:8080/?")).isEqualTo("/");
        assertThat(UrlParser.getPath("https://localhost:8080/path?")).isEqualTo("/path");
        assertThat(UrlParser.getPath("https://localhost:8080/path/?")).isEqualTo("/path/");
        assertThat(UrlParser.getPath("https://localhost:8080/more/path?")).isEqualTo("/more/path");
        assertThat(UrlParser.getPath("https://localhost:8080/more/path/?")).isEqualTo("/more/path/");

        assertThat(UrlParser.getPath("https://localhost:8080?query")).isEqualTo("");
        assertThat(UrlParser.getPath("https://localhost:8080/?query")).isEqualTo("/");
        assertThat(UrlParser.getPath("https://localhost:8080/path?query")).isEqualTo("/path");
        assertThat(UrlParser.getPath("https://localhost:8080/path/?query")).isEqualTo("/path/");
        assertThat(UrlParser.getPath("https://localhost:8080/more/path?query")).isEqualTo("/more/path");
        assertThat(UrlParser.getPath("https://localhost:8080/more/path/?query")).isEqualTo("/more/path/");

        assertThat(UrlParser.getPath("https://localhost:8080#")).isEqualTo("");
        assertThat(UrlParser.getPath("https://localhost:8080/#")).isEqualTo("/");
        assertThat(UrlParser.getPath("https://localhost:8080/path#")).isEqualTo("/path");
        assertThat(UrlParser.getPath("https://localhost:8080/path/#")).isEqualTo("/path/");
        assertThat(UrlParser.getPath("https://localhost:8080/more/path#")).isEqualTo("/more/path");
        assertThat(UrlParser.getPath("https://localhost:8080/more/path/#")).isEqualTo("/more/path/");

        assertThat(UrlParser.getPath("https://localhost:8080#fragment")).isEqualTo("");
        assertThat(UrlParser.getPath("https://localhost:8080/#fragment")).isEqualTo("/");
        assertThat(UrlParser.getPath("https://localhost:8080/path#fragment")).isEqualTo("/path");
        assertThat(UrlParser.getPath("https://localhost:8080/path/#fragment")).isEqualTo("/path/");
        assertThat(UrlParser.getPath("https://localhost:8080/more/path#fragment")).isEqualTo("/more/path");
        assertThat(UrlParser.getPath("https://localhost:8080/more/path/#fragment")).isEqualTo("/more/path/");
    }

    @Test
    public void testGetPathFromMalformedUrl() {
        assertThat(UrlParser.getPath("")).isNull();
        assertThat(UrlParser.getPath("http:")).isNull();
        assertThat(UrlParser.getPath("http:/")).isNull();
        assertThat(UrlParser.getPath("http//")).isNull();
        assertThat(UrlParser.getPath("http:localhost/path")).isNull();
        assertThat(UrlParser.getPath("http:/localhost/path")).isNull();
        assertThat(UrlParser.getPath("http//localhost/path")).isNull();
    }
}
