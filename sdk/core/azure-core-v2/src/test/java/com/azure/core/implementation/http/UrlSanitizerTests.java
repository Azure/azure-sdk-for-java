// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http;

import com.azure.core.v2.implementation.http.UrlSanitizer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlSanitizerTests {

    @ParameterizedTest()
    @MethodSource("urlSanitizationArgs")
    public void testUrlSanitizer(String original, Set<String> allowedParams, String expected)
        throws URISyntaxException, MalformedURLException {
        UrlSanitizer sanitizer = new UrlSanitizer(allowedParams);
        assertEquals(expected, sanitizer.getRedactedUrl(new URI(original).toURL()));
    }

    public static Stream<Arguments> urlSanitizationArgs() {
        List<Arguments> arguments = new ArrayList<>();
        arguments.add(Arguments.of("http://example.com", null, "http://example.com"));
        arguments.add(Arguments.of("https://example.com", Collections.emptySet(), "https://example.com"));
        arguments
            .add(Arguments.of("https://example.com/?api-version=123", null, "https://example.com/?api-version=123"));
        arguments.add(Arguments.of("http://example.com?api-version=123", Collections.emptySet(),
            "http://example.com?api-version=123"));
        arguments.add(Arguments.of("https://example.com/hello?", Collections.emptySet(), "https://example.com/hello?"));

        String url = "https://example.com/hello?foo=bar&api-version=1.2.3";
        arguments
            .add(Arguments.of(url, Collections.emptySet(), "https://example.com/hello?foo=REDACTED&api-version=1.2.3"));
        arguments.add(
            Arguments.of(url, Collections.singleton("foo"), "https://example.com/hello?foo=bar&api-version=1.2.3"));

        Set<String> allowed = new HashSet<>();
        allowed.add("foo");
        allowed.add("baz");
        arguments.add(Arguments.of(url, allowed, "https://example.com/hello?foo=bar&api-version=1.2.3"));
        arguments.add(Arguments.of(url, Collections.unmodifiableSet(allowed),
            "https://example.com/hello?foo=bar&api-version=1.2.3"));

        return arguments.stream();
    }
}
