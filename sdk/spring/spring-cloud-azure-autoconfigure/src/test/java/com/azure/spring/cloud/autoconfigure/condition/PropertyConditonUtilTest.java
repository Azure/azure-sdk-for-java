// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.condition;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertyConditonUtilTest {

    @ParameterizedTest
    @ValueSource(strings = { "", " " })
    public void testGetValidPrefixCase1(String prefixAttr) {
        String actual = PropertyConditionUtil.getValidPrefix(prefixAttr);
        assertEquals(actual, "");
    }

    @ParameterizedTest
    @ValueSource(strings = { "test", " test", " test ", "test  " })
    public void testGetValidPrefixCase2(String prefixAttr) {
        String actual = PropertyConditionUtil.getValidPrefix(prefixAttr);
        assertEquals(actual, "test.");
    }

    @ParameterizedTest
    @ValueSource(strings = { "test.", " test." })
    public void testGetValidPrefixCase3(String prefixAttr) {
        String actual = PropertyConditionUtil.getValidPrefix(prefixAttr);
        assertEquals(actual, "test.");
    }

    @ParameterizedTest
    @ValueSource(strings = { "testa.testb", " testa.testb", " testa.testb " })
    public void testGetValidPrefixCase4(String prefixAttr) {
        String actual = PropertyConditionUtil.getValidPrefix(prefixAttr);
        assertEquals(actual, "testa.testb.");
    }
}
