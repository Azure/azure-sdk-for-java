// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises semantic-version parsing with the packaged range-based integer parser.
 */
@EnabledForJreRange(min = JRE.JAVA_11)
public class SemanticVersionTestsIT extends SemanticVersionTests {
    @Test
    public void selectsPackagePrivateIntegerParser() {
        assertFalse(Modifier.isPublic(IntegerParser.class.getModifiers()));
        assertTrue(IntegerParser.class.getResource("IntegerParser.class").toString().contains("META-INF/versions/11/"));
    }
}
