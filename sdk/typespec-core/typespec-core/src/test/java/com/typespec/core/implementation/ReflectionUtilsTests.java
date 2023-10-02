// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ReflectionUtils}.
 */
public class ReflectionUtilsTests {
    /*
     * This is an integration test instead of a unit test because integration tests use the generated JAR, with
     * multi-release support, instead of the class files in Maven's output directory. Given that, the integration tests
     * will hook into the different Java version implementations.
     */
    @Test
    public void validateImplementationVersion() {
        String javaSpecificationVersion = System.getProperty("java.specification.version");
        if ("1.8".equals(javaSpecificationVersion)) {
            assertFalse(ReflectionUtils.isModuleBased(), "Java 8 can't use module-based privateLookupIn.");
        } else {
            assertTrue(ReflectionUtils.isModuleBased(), "Java 9+ must use module-based privateLookupIn.");
        }
    }
}
