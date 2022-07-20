// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AzureStringUtilsTest {

    private static final String PROPERTY_SUFFIX = ".";

    @ParameterizedTest
    @ValueSource(strings = { "", " " })
    public void testEnsureEndsWithSuffixCase1(String prefixAttr) {
        String actual = AzureStringUtils.ensureEndsWithSuffix(prefixAttr.trim(), PROPERTY_SUFFIX);
        assertEquals(actual, "");
    }

    @ParameterizedTest
    @ValueSource(strings = { "test", " test", " test ", "test  " })
    public void testEnsureEndsWithSuffixCase2(String prefixAttr) {
        String actual = AzureStringUtils.ensureEndsWithSuffix(prefixAttr.trim(), PROPERTY_SUFFIX);
        assertEquals(actual, "test.");
    }

    @ParameterizedTest
    @ValueSource(strings = { "test.", " test." })
    public void testEnsureEndsWithSuffixCase3(String prefixAttr) {
        String actual = AzureStringUtils.ensureEndsWithSuffix(prefixAttr.trim(), PROPERTY_SUFFIX);
        assertEquals(actual, "test.");
    }

    @ParameterizedTest
    @ValueSource(strings = { "testa.testb", " testa.testb", " testa.testb " })
    public void testEnsureEndsWithSuffixCase4(String prefixAttr) {
        String actual = AzureStringUtils.ensureEndsWithSuffix(prefixAttr.trim(), PROPERTY_SUFFIX);
        assertEquals(actual, "testa.testb.");
    }
}
