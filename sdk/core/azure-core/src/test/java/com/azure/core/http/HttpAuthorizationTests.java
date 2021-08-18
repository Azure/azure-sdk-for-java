// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link HttpAuthorization}.
 */
public class HttpAuthorizationTests {
    @ParameterizedTest
    @MethodSource("invalidConstructorParametersSupplier")
    public void invalidConstructorParameters(String scheme, String parameter,
        Class<? extends Throwable> expectedException) {
        assertThrows(expectedException, () -> new HttpAuthorization(scheme, parameter));
    }

    private static Stream<Arguments> invalidConstructorParametersSupplier() {
        return Stream.of(
            // Constructor arguments cannot be null.
            Arguments.of(null, "parameter", NullPointerException.class),
            Arguments.of("scheme", null, NullPointerException.class),

            // Constructor arguments cannot be empty strings.
            Arguments.of("", "parameter", IllegalArgumentException.class),
            Arguments.of("scheme", "", IllegalArgumentException.class)
        );
    }

    @Test
    public void toStringTest() {
        String scheme = "scheme";
        String parameter = "parameter";
        HttpAuthorization httpAuthorization = new HttpAuthorization(scheme, parameter);

        Assertions.assertEquals(String.format("%s %s", scheme, parameter), httpAuthorization.toString());
    }
}
