/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
