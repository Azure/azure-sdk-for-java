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

public class UrlParserTargetTest {

    // there are more test cases here than needed, but they are mirroring tests in UrlParserPathTest

    @Test
    public void testGetTargetFromUrl() {
        assertThat(UrlParser.getTargetFromUrl("https://localhost")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/path")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/path/")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/more/path")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/more/path/")).isEqualTo("localhost");

        assertThat(UrlParser.getTargetFromUrl("https://localhost?")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/?")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/path?")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/path/?")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/more/path?")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/more/path/?")).isEqualTo("localhost");

        assertThat(UrlParser.getTargetFromUrl("https://localhost?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/path?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/path/?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/more/path?query"))
            .isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/more/path/?query"))
            .isEqualTo("localhost");

        assertThat(UrlParser.getTargetFromUrl("https://localhost#")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/#")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/path#")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/path/#")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/more/path#")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/more/path/#")).isEqualTo("localhost");

        assertThat(UrlParser.getTargetFromUrl("https://localhost#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/path#fragment"))
            .isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/path/#fragment"))
            .isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/more/path#fragment"))
            .isEqualTo("localhost");
        assertThat(UrlParser.getTargetFromUrl("https://localhost/more/path/#fragment"))
            .isEqualTo("localhost");
    }

    @Test
    public void testGetTargetFromUrlWithPort() {
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/path"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/path/"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/more/path"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/more/path/"))
            .isEqualTo("localhost:8080");

        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080?")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/?")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/path?"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/path/?"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/more/path?"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/more/path/?"))
            .isEqualTo("localhost:8080");

        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080?query"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/?query"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/path?query"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/path/?query"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/more/path?query"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/more/path/?query"))
            .isEqualTo("localhost:8080");

        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080#")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/#")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/path#"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/path/#"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/more/path#"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/more/path/#"))
            .isEqualTo("localhost:8080");

        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080#fragment"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/#fragment"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/path#fragment"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/path/#fragment"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/more/path#fragment"))
            .isEqualTo("localhost:8080");
        assertThat(UrlParser.getTargetFromUrl("https://localhost:8080/more/path/#fragment"))
            .isEqualTo("localhost:8080");
    }

    @Test
    public void testGetTargetFromUrlWithNoAuthority() {
        assertThat(UrlParser.getTargetFromUrl("https:")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/path")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/path/")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/more/path")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/more/path/")).isNull();

        assertThat(UrlParser.getTargetFromUrl("https:?")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/?")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/path?")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/path/?")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/more/path?")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/more/path/?")).isNull();

        assertThat(UrlParser.getTargetFromUrl("https:?query")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/?query")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/path?query")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/path/?query")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/more/path?query")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/more/path/?query")).isNull();

        assertThat(UrlParser.getTargetFromUrl("https:#")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/#")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/path#")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/path/#")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/more/path#")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/more/path/#")).isNull();

        assertThat(UrlParser.getTargetFromUrl("https:#fragment")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/#fragment")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/path#fragment")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/path/#fragment")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/more/path#fragment")).isNull();
        assertThat(UrlParser.getTargetFromUrl("https:/more/path/#fragment")).isNull();
    }
}
