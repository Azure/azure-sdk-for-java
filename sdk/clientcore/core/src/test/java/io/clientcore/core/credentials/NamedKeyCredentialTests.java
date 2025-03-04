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
 * Unit tests for {@link NamedKeyCredential}.
 */
public class NamedKeyCredentialTests {
    private static final String DUMMY_NAME = "Dummy-Name";
    private static final String DUMMY_VALUE = "DummyValue";

    @ParameterizedTest
    @MethodSource("namedKeyCredentialsInvalidArgumentTestSupplier")
    public void namedKeyCredentialsInvalidArgumentTest(String name, String key, Class<Exception> exceptionType) {
        Assertions.assertThrows(exceptionType, () -> new NamedKeyCredential(name, key));
    }

    @ParameterizedTest
    @MethodSource("namedKeyCredentialsInvalidArgumentTestSupplier")
    public void namedKeyCredentialInvalidUpdateTest(String name, String key, Class<Exception> exceptionType) {
        Assertions.assertThrows(exceptionType, () -> new NamedKeyCredential(DUMMY_NAME, DUMMY_VALUE).update(name, key));
    }

    private static Stream<Arguments> namedKeyCredentialsInvalidArgumentTestSupplier() {
        return Stream.of(Arguments.of(null, "key", NullPointerException.class),
            Arguments.of("name", null, NullPointerException.class),
            Arguments.of("", "key", IllegalArgumentException.class),
            Arguments.of("name", "", IllegalArgumentException.class));
    }

    @Test
    public void keyCredentialValueTest() {
        NamedKeyCredential keyCredential = new NamedKeyCredential(DUMMY_NAME, DUMMY_VALUE);

        Assertions.assertEquals(DUMMY_NAME, keyCredential.getNamedKey().getName());
        Assertions.assertEquals(DUMMY_VALUE, keyCredential.getNamedKey().getKey());
    }

    @Test
    public void keyCredentialUpdateTest() {
        NamedKeyCredential keyCredential = new NamedKeyCredential(DUMMY_NAME, DUMMY_VALUE);

        String expectedName = "New-Name";
        String expectedValue = "NewValue";
        keyCredential.update(expectedName, expectedValue);

        Assertions.assertEquals(expectedName, keyCredential.getNamedKey().getName());
        Assertions.assertEquals(expectedValue, keyCredential.getNamedKey().getKey());
    }
}
