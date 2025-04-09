// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.credentials;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * Unit tests for {@link KeyCredential}.
 */
public class KeyCredentialTests {
    private static final String DUMMY_VALUE = "DummyValue";

    @ParameterizedTest
    @MethodSource("keyCredentialsInvalidArgumentTestSupplier")
    public void keyCredentialsInvalidArgumentTest(String key, Class<Exception> exceptionType) {
        Assertions.assertThrows(exceptionType, () -> new KeyCredential(key));
    }

    private static Stream<Arguments> keyCredentialsInvalidArgumentTestSupplier() {
        return Stream.of(Arguments.of(null, NullPointerException.class),
            Arguments.of("", IllegalArgumentException.class));
    }

    @Test
    public void keyCredentialValueTest() {
        KeyCredential keyCredential = new KeyCredential(DUMMY_VALUE);

        Assertions.assertEquals(DUMMY_VALUE, keyCredential.getKey());
    }

    @Test
    public void keyCredentialUpdateTest() {
        KeyCredential keyCredential = new KeyCredential(DUMMY_VALUE);

        String expectedValue = "NewValue";
        keyCredential.update(expectedValue);

        Assertions.assertEquals(expectedValue, keyCredential.getKey());
    }
}
