// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.core;

import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.END_SYMBOL_ERROR;
import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.ILLEGAL_SYMBOL_ERROR;
import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.LENGTH_ERROR;
import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.START_SYMBOL_ERROR;
import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.validateNamespace;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertiesValidatorTest {

    @Test
    void testLengthFallShortOf() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> validateNamespace("a"));
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(LENGTH_ERROR));
    }

    @Test
    void testLengthExceed() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> validateNamespace(new String(new char[51]).replace("\0", "a")));
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(LENGTH_ERROR));
    }

    @Test
    void testContainIllegalSymbols() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> validateNamespace("test+test"));
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(ILLEGAL_SYMBOL_ERROR));
    }

    @Test
    void testStartWithIllegalSymbol() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> validateNamespace("1testtest"));
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(START_SYMBOL_ERROR));
    }

    @Test
    void testEndWithIllegalSymbol() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> validateNamespace("testtest-"));
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(END_SYMBOL_ERROR));
    }

    @Test
    void testLegal() {
        assertDoesNotThrow(() -> validateNamespace("test-A"));
    }

}
