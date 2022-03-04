// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.END_SYMBOL_ERROR;
import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.ILLEGAL_SYMBOL_ERROR;
import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.LENGTH_ERROR;
import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.START_SYMBOL_ERROR;
import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.validateNamespace;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class PropertiesValidatorTest {

    @Test
    void testLengthFallShortOf(CapturedOutput output) {
        validateNamespace("a");
        assertThat(output).contains(LENGTH_ERROR);
    }

    @Test
    void testLengthExceed(CapturedOutput output) {
        validateNamespace(new String(new char[51]).replace("\0", "a"));
        assertThat(output).contains(LENGTH_ERROR);
    }

    @Test
    void testContainIllegalSymbols(CapturedOutput output) {
        validateNamespace("test+test");
        assertThat(output).contains(ILLEGAL_SYMBOL_ERROR);
    }

    @Test
    void testStartWithIllegalSymbol(CapturedOutput output) {
        validateNamespace("1testtest");
        assertThat(output).contains(START_SYMBOL_ERROR);
    }

    @Test
    void testEndWithIllegalSymbol(CapturedOutput output) {
        validateNamespace("testtest-");
        assertThat(output).contains(END_SYMBOL_ERROR);
    }

}
