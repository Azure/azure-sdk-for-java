// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlParserPathTest {

    @Test
    public void testGetPathFromUrl() {
        assertThat(UrlParser.getPathFromUrl("https://localhost")).isEqualTo("");
        assertThat(UrlParser.getPathFromUrl("https://localhost/")).isEqualTo("/");
        assertThat(UrlParser.getPathFromUrl("https://localhost/path")).isEqualTo("/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost/path/")).isEqualTo("/path/");
        assertThat(UrlParser.getPathFromUrl("https://localhost/more/path")).isEqualTo("/more/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost/more/path/")).isEqualTo("/more/path/");

        assertThat(UrlParser.getPathFromUrl("https://localhost?")).isEqualTo("");
        assertThat(UrlParser.getPathFromUrl("https://localhost/?")).isEqualTo("/");
        assertThat(UrlParser.getPathFromUrl("https://localhost/path?")).isEqualTo("/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost/path/?")).isEqualTo("/path/");
        assertThat(UrlParser.getPathFromUrl("https://localhost/more/path?")).isEqualTo("/more/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost/more/path/?")).isEqualTo("/more/path/");

        assertThat(UrlParser.getPathFromUrl("https://localhost?query")).isEqualTo("");
        assertThat(UrlParser.getPathFromUrl("https://localhost/?query")).isEqualTo("/");
        assertThat(UrlParser.getPathFromUrl("https://localhost/path?query")).isEqualTo("/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost/path/?query")).isEqualTo("/path/");
        assertThat(UrlParser.getPathFromUrl("https://localhost/more/path?query"))
            .isEqualTo("/more/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost/more/path/?query"))
            .isEqualTo("/more/path/");

        assertThat(UrlParser.getPathFromUrl("https://localhost#")).isEqualTo("");
        assertThat(UrlParser.getPathFromUrl("https://localhost/#")).isEqualTo("/");
        assertThat(UrlParser.getPathFromUrl("https://localhost/path#")).isEqualTo("/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost/path/#")).isEqualTo("/path/");
        assertThat(UrlParser.getPathFromUrl("https://localhost/more/path#")).isEqualTo("/more/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost/more/path/#")).isEqualTo("/more/path/");

        assertThat(UrlParser.getPathFromUrl("https://localhost#fragment")).isEqualTo("");
        assertThat(UrlParser.getPathFromUrl("https://localhost/#fragment")).isEqualTo("/");
        assertThat(UrlParser.getPathFromUrl("https://localhost/path#fragment")).isEqualTo("/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost/path/#fragment")).isEqualTo("/path/");
        assertThat(UrlParser.getPathFromUrl("https://localhost/more/path#fragment"))
            .isEqualTo("/more/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost/more/path/#fragment"))
            .isEqualTo("/more/path/");
    }

    @Test
    public void testGetPathFromUrlWithPort() {
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080")).isEqualTo("");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/")).isEqualTo("/");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/path")).isEqualTo("/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/path/")).isEqualTo("/path/");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/more/path"))
            .isEqualTo("/more/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/more/path/"))
            .isEqualTo("/more/path/");

        assertThat(UrlParser.getPathFromUrl("https://localhost:8080?")).isEqualTo("");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/?")).isEqualTo("/");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/path?")).isEqualTo("/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/path/?")).isEqualTo("/path/");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/more/path?"))
            .isEqualTo("/more/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/more/path/?"))
            .isEqualTo("/more/path/");

        assertThat(UrlParser.getPathFromUrl("https://localhost:8080?query")).isEqualTo("");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/?query")).isEqualTo("/");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/path?query")).isEqualTo("/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/path/?query")).isEqualTo("/path/");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/more/path?query"))
            .isEqualTo("/more/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/more/path/?query"))
            .isEqualTo("/more/path/");

        assertThat(UrlParser.getPathFromUrl("https://localhost:8080#")).isEqualTo("");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/#")).isEqualTo("/");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/path#")).isEqualTo("/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/path/#")).isEqualTo("/path/");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/more/path#"))
            .isEqualTo("/more/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/more/path/#"))
            .isEqualTo("/more/path/");

        assertThat(UrlParser.getPathFromUrl("https://localhost:8080#fragment")).isEqualTo("");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/#fragment")).isEqualTo("/");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/path#fragment")).isEqualTo("/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/path/#fragment"))
            .isEqualTo("/path/");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/more/path#fragment"))
            .isEqualTo("/more/path");
        assertThat(UrlParser.getPathFromUrl("https://localhost:8080/more/path/#fragment"))
            .isEqualTo("/more/path/");
    }

    @Test
    public void testGetPathFromUrlWithNoAuthority() {
        assertThat(UrlParser.getPathFromUrl("https:")).isEqualTo("");
        assertThat(UrlParser.getPathFromUrl("https:/")).isEqualTo("/");
        assertThat(UrlParser.getPathFromUrl("https:/path")).isEqualTo("/path");
        assertThat(UrlParser.getPathFromUrl("https:/path/")).isEqualTo("/path/");
        assertThat(UrlParser.getPathFromUrl("https:/more/path")).isEqualTo("/more/path");
        assertThat(UrlParser.getPathFromUrl("https:/more/path/")).isEqualTo("/more/path/");

        assertThat(UrlParser.getPathFromUrl("https:?")).isEqualTo("");
        assertThat(UrlParser.getPathFromUrl("https:/?")).isEqualTo("/");
        assertThat(UrlParser.getPathFromUrl("https:/path?")).isEqualTo("/path");
        assertThat(UrlParser.getPathFromUrl("https:/path/?")).isEqualTo("/path/");
        assertThat(UrlParser.getPathFromUrl("https:/more/path?")).isEqualTo("/more/path");
        assertThat(UrlParser.getPathFromUrl("https:/more/path/?")).isEqualTo("/more/path/");

        assertThat(UrlParser.getPathFromUrl("https:?query")).isEqualTo("");
        assertThat(UrlParser.getPathFromUrl("https:/?query")).isEqualTo("/");
        assertThat(UrlParser.getPathFromUrl("https:/path?query")).isEqualTo("/path");
        assertThat(UrlParser.getPathFromUrl("https:/path/?query")).isEqualTo("/path/");
        assertThat(UrlParser.getPathFromUrl("https:/more/path?query")).isEqualTo("/more/path");
        assertThat(UrlParser.getPathFromUrl("https:/more/path/?query")).isEqualTo("/more/path/");

        assertThat(UrlParser.getPathFromUrl("https:#")).isEqualTo("");
        assertThat(UrlParser.getPathFromUrl("https:/#")).isEqualTo("/");
        assertThat(UrlParser.getPathFromUrl("https:/path#")).isEqualTo("/path");
        assertThat(UrlParser.getPathFromUrl("https:/path/#")).isEqualTo("/path/");
        assertThat(UrlParser.getPathFromUrl("https:/more/path#")).isEqualTo("/more/path");
        assertThat(UrlParser.getPathFromUrl("https:/more/path/#")).isEqualTo("/more/path/");

        assertThat(UrlParser.getPathFromUrl("https:#fragment")).isEqualTo("");
        assertThat(UrlParser.getPathFromUrl("https:/#fragment")).isEqualTo("/");
        assertThat(UrlParser.getPathFromUrl("https:/path#fragment")).isEqualTo("/path");
        assertThat(UrlParser.getPathFromUrl("https:/path/#fragment")).isEqualTo("/path/");
        assertThat(UrlParser.getPathFromUrl("https:/more/path#fragment")).isEqualTo("/more/path");
        assertThat(UrlParser.getPathFromUrl("https:/more/path/#fragment")).isEqualTo("/more/path/");
    }
}
