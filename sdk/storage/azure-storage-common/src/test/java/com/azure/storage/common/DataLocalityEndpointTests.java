// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DataLocalityEndpointTests {
    @ParameterizedTest
    @MethodSource("validEndpoints")
    public void parsesValidEndpoint(String value, String expectedHost, Integer expectedPort, String expectedAuthority) {
        DataLocalityEndpoint endpoint = DataLocalityEndpoint.fromString(value);

        assertEquals(expectedHost, endpoint.getHost());
        assertEquals(expectedPort, endpoint.getPort());
        assertEquals(expectedAuthority, endpoint.toString());
    }

    @ParameterizedTest
    @MethodSource("equivalentEndpoints")
    public void equivalentEndpointsAreEqual(String first, String second) {
        assertEquals(DataLocalityEndpoint.fromString(first), DataLocalityEndpoint.fromString(second));
        assertEquals(DataLocalityEndpoint.fromString(first).hashCode(),
            DataLocalityEndpoint.fromString(second).hashCode());
    }

    @ParameterizedTest
    @MethodSource("invalidEndpoints")
    public void rejectsInvalidEndpoint(String value) {
        assertThrows(IllegalArgumentException.class, () -> DataLocalityEndpoint.fromString(value));
    }

    private static Stream<Arguments> validEndpoints() {
        return Stream.of(Arguments.of("layout.example.net", "layout.example.net", null, "layout.example.net"),
            Arguments.of("layout.example.net:8443", "layout.example.net", 8443, "layout.example.net:8443"),
            Arguments.of("https://layout.example.net:443", "layout.example.net", 443, "layout.example.net:443"),
            Arguments.of("http://LAYOUT.EXAMPLE.NET/", "layout.example.net", null, "layout.example.net"),
            Arguments.of("[2001:db8::1]:443", "[2001:db8::1]", 443, "[2001:db8::1]:443"));
    }

    private static Stream<Arguments> equivalentEndpoints() {
        return Stream.of(Arguments.of("layout.example.net:443", "https://LAYOUT.EXAMPLE.NET:443"),
            Arguments.of("layout.example.net", "http://layout.example.net/"));
    }

    private static Stream<Arguments> invalidEndpoints() {
        return Stream.of(Arguments.of((Object) null), Arguments.of(""), Arguments.of("   "),
            Arguments.of(" layout.example.net"), Arguments.of("layout.example.net "), Arguments.of("http://"),
            Arguments.of("ftp://layout.example.net"), Arguments.of("https://user@layout.example.net"),
            Arguments.of("https://layout.example.net/path"), Arguments.of("https://layout.example.net?query=value"),
            Arguments.of("https://layout.example.net#fragment"), Arguments.of("layout.example.net:0"),
            Arguments.of("layout.example.net:"), Arguments.of("layout.example.net:-1"),
            Arguments.of("layout.example.net:65536"), Arguments.of("layout.example.net:not-a-port"));
    }
}
