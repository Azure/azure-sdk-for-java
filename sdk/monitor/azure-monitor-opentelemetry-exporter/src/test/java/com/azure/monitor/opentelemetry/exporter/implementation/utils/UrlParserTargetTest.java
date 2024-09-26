// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlParserTargetTest {

    // there are more test cases here than needed, but they are mirroring tests in UrlParserPathTest

    @Test
    public void testGetTargetFromUrl() {
        assertThat(UrlParser.getTarget("https://localhost")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/path")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/path/")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/more/path")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/more/path/")).isEqualTo("localhost");

        assertThat(UrlParser.getTarget("https://localhost?")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/?")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/path?")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/path/?")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/more/path?")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/more/path/?")).isEqualTo("localhost");

        assertThat(UrlParser.getTarget("https://localhost?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/path?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/path/?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/more/path?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/more/path/?query")).isEqualTo("localhost");

        assertThat(UrlParser.getTarget("https://localhost#")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/#")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/path#")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/path/#")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/more/path#")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/more/path/#")).isEqualTo("localhost");

        assertThat(UrlParser.getTarget("https://localhost#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/path#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/path/#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/more/path#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost/more/path/#fragment")).isEqualTo("localhost");
    }

    @Test
    public void testGetTargetFromHttpsUrlWithDefaultPort() {
        assertThat(UrlParser.getTarget("https://localhost:443")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/path")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/path/")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/more/path")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/more/path/")).isEqualTo("localhost");

        assertThat(UrlParser.getTarget("https://localhost:443?")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/?")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/path?")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/path/?")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/more/path?")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/more/path/?")).isEqualTo("localhost");

        assertThat(UrlParser.getTarget("https://localhost:443?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/path?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/path/?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/more/path?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/more/path/?query")).isEqualTo("localhost");

        assertThat(UrlParser.getTarget("https://localhost:443#")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/#")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/path#")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/path/#")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/more/path#")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/more/path/#")).isEqualTo("localhost");

        assertThat(UrlParser.getTarget("https://localhost:443#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/path#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/path/#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/more/path#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("https://localhost:443/more/path/#fragment")).isEqualTo("localhost");
    }

    @Test
    public void testGetTargetFromHttpUrlWithDefaultPort() {
        assertThat(UrlParser.getTarget("http://localhost:80")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/path")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/path/")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/more/path")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/more/path/")).isEqualTo("localhost");

        assertThat(UrlParser.getTarget("http://localhost:80?")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/?")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/path?")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/path/?")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/more/path?")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/more/path/?")).isEqualTo("localhost");

        assertThat(UrlParser.getTarget("http://localhost:80?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/path?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/path/?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/more/path?query")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/more/path/?query")).isEqualTo("localhost");

        assertThat(UrlParser.getTarget("http://localhost:80#")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/#")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/path#")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/path/#")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/more/path#")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/more/path/#")).isEqualTo("localhost");

        assertThat(UrlParser.getTarget("http://localhost:80#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/path#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/path/#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/more/path#fragment")).isEqualTo("localhost");
        assertThat(UrlParser.getTarget("http://localhost:80/more/path/#fragment")).isEqualTo("localhost");
    }

    @Test
    public void testGetTargetFromUrlWithPort() {
        assertThat(UrlParser.getTarget("https://localhost:8080")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/path")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/path/")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/more/path")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/more/path/")).isEqualTo("localhost:8080");

        assertThat(UrlParser.getTarget("https://localhost:8080?")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/?")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/path?")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/path/?")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/more/path?")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/more/path/?")).isEqualTo("localhost:8080");

        assertThat(UrlParser.getTarget("https://localhost:8080?query")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/?query")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/path?query")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/path/?query")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/more/path?query")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/more/path/?query")).isEqualTo("localhost:8080");

        assertThat(UrlParser.getTarget("https://localhost:8080#")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/#")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/path#")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/path/#")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/more/path#")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/more/path/#")).isEqualTo("localhost:8080");

        assertThat(UrlParser.getTarget("https://localhost:8080#fragment")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/#fragment")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/path#fragment")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/path/#fragment")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/more/path#fragment")).isEqualTo("localhost:8080");
        assertThat(UrlParser.getTarget("https://localhost:8080/more/path/#fragment")).isEqualTo("localhost:8080");
    }

    @Test
    public void testGetTargetFromUrlWithNoAuthority() {
        assertThat(UrlParser.getTarget("https:")).isNull();
        assertThat(UrlParser.getTarget("https:/")).isNull();
        assertThat(UrlParser.getTarget("https:/path")).isNull();
        assertThat(UrlParser.getTarget("https:/path/")).isNull();
        assertThat(UrlParser.getTarget("https:/more/path")).isNull();
        assertThat(UrlParser.getTarget("https:/more/path/")).isNull();

        assertThat(UrlParser.getTarget("https:?")).isNull();
        assertThat(UrlParser.getTarget("https:/?")).isNull();
        assertThat(UrlParser.getTarget("https:/path?")).isNull();
        assertThat(UrlParser.getTarget("https:/path/?")).isNull();
        assertThat(UrlParser.getTarget("https:/more/path?")).isNull();
        assertThat(UrlParser.getTarget("https:/more/path/?")).isNull();

        assertThat(UrlParser.getTarget("https:?query")).isNull();
        assertThat(UrlParser.getTarget("https:/?query")).isNull();
        assertThat(UrlParser.getTarget("https:/path?query")).isNull();
        assertThat(UrlParser.getTarget("https:/path/?query")).isNull();
        assertThat(UrlParser.getTarget("https:/more/path?query")).isNull();
        assertThat(UrlParser.getTarget("https:/more/path/?query")).isNull();

        assertThat(UrlParser.getTarget("https:#")).isNull();
        assertThat(UrlParser.getTarget("https:/#")).isNull();
        assertThat(UrlParser.getTarget("https:/path#")).isNull();
        assertThat(UrlParser.getTarget("https:/path/#")).isNull();
        assertThat(UrlParser.getTarget("https:/more/path#")).isNull();
        assertThat(UrlParser.getTarget("https:/more/path/#")).isNull();

        assertThat(UrlParser.getTarget("https:#fragment")).isNull();
        assertThat(UrlParser.getTarget("https:/#fragment")).isNull();
        assertThat(UrlParser.getTarget("https:/path#fragment")).isNull();
        assertThat(UrlParser.getTarget("https:/path/#fragment")).isNull();
        assertThat(UrlParser.getTarget("https:/more/path#fragment")).isNull();
        assertThat(UrlParser.getTarget("https:/more/path/#fragment")).isNull();
    }

    @Test
    public void testGetTargetFromMalformedUrl() {
        assertThat(UrlParser.getTarget("")).isNull();
        assertThat(UrlParser.getTarget("http:")).isNull();
        assertThat(UrlParser.getTarget("http:/")).isNull();
        assertThat(UrlParser.getTarget("http//")).isNull();
        assertThat(UrlParser.getTarget("http:localhost/path")).isNull();
        assertThat(UrlParser.getTarget("http:/localhost/path")).isNull();
        assertThat(UrlParser.getTarget("http//localhost/path")).isNull();
    }
}
