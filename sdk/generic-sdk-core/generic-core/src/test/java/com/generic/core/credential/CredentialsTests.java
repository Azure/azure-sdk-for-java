// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.credential;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

public class CredentialsTests {

    private static final String DUMMY_NAME = "Dummy-Name";
    private static final String DUMMY_VALUE = "DummyValue";


    static class InvalidInputsArgumentProvider implements ArgumentsProvider {

        InvalidInputsArgumentProvider() { }

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(null, NullPointerException.class),
                Arguments.of(null, NullPointerException.class),
                Arguments.of("", IllegalArgumentException.class),
                Arguments.of("", IllegalArgumentException.class),
                Arguments.of("", IllegalArgumentException.class)
            );
        }
    }


    @ParameterizedTest
    @ArgumentsSource(InvalidInputsArgumentProvider.class)
    public void keyCredentialsInvalidArgumentTest(String key, Class<Exception> excepionType) {
        Assertions.assertThrows(excepionType, () -> {
            new KeyCredential(key);
        });
    }

    @Test
    public void keyCredentialValueTest() {
        KeyCredential KeyCredential =
            new KeyCredential(DUMMY_VALUE);

        Assertions.assertEquals(DUMMY_VALUE, KeyCredential.getKey());
    }

    @Test
    public void keyCredentialUpdateTest() {
        KeyCredential KeyCredential =
            new KeyCredential(DUMMY_NAME);

        String expectedValue = "NewValue";

        KeyCredential.update(expectedValue);

        Assertions.assertEquals(expectedValue, KeyCredential.getKey());
    }
}
