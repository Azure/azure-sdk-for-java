// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Netty4HttpProxyHandlerTests {
    @ParameterizedTest
    @MethodSource("nullAndEmpty")
    public void validateProxyAuthenticationInfoIsNoOpWhenInfoHeaderIsNullOrEmpty(String infoHeader) {
        assertDoesNotThrow(
            () -> Netty4HttpProxyHandler.validateProxyAuthenticationInfo(infoHeader, "authorizationHeader"));
    }

    @ParameterizedTest
    @MethodSource("nullAndEmpty")
    public void validateProxyAuthenticationInfoIsNoOpWhenAuthorizationHeaderIsNullOrEmpty(String infoHeader) {
        assertDoesNotThrow(() -> Netty4HttpProxyHandler.validateProxyAuthenticationInfo("infoHeader", infoHeader));
    }

    private static Stream<String> nullAndEmpty() {
        return Stream.of("", null);
    }

    @ParameterizedTest
    @MethodSource("mismatchData")
    public void validateProxyAuthenticationInfoThrowsOnMismatch(String infoHeader, String authorizationHeader,
        String expectedMessage) {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Netty4HttpProxyHandler.validateProxyAuthenticationInfo(infoHeader, authorizationHeader));

        assertEquals(expectedMessage, exception.getMessage());
    }

    private static Stream<Arguments> mismatchData() {
        return Stream.of(
            Arguments.of("cnonce=1", "cnonce=2",
                "Property received in the 'Proxy-Authentication-Info' header doesn't match the value sent in the "
                    + "'Proxy-Authorization' header; {\"propertyName\":\"cnonce\",\"received\":\"1\",\"sent\":\"2\"}"),
            Arguments.of("nc=1", "nc=2",
                "Property received in the 'Proxy-Authentication-Info' header doesn't match the value sent in the "
                    + "'Proxy-Authorization' header; {\"propertyName\":\"nc\",\"received\":\"1\",\"sent\":\"2\"}"));
    }

    @Test
    public void validationIsNoOpIfOneOfInfoHeaderDoesNotContainIt() {
        assertDoesNotThrow(() -> Netty4HttpProxyHandler.validateProxyAuthenticationInfo("nc=2", "cnonce=1,nc=2"));
    }
}
