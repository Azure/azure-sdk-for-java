// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
