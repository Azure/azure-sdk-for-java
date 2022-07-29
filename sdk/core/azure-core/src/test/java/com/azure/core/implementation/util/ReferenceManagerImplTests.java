// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ReferenceManagerImpl}.
 */
public class ReferenceManagerImplTests {
    /*
     * This is an integration test instead of a unit test because integration tests use the generated JAR, with
     * multi-release support, instead of the class files in Maven's output directory. Given that, the integration tests
     * will hook into the different Java version implementations.
     */
    @Test
    public void validateImplementationVersion() {
        String javaSpecificationVersion = System.getProperty("java.specification.version");
        if ("1.8".equals(javaSpecificationVersion)) {
            assertFalse(ReferenceManagerImpl.isCleanerUsed(), "Java 8 cannot use Cleaner-based reference management.");
        } else {
            assertTrue(ReferenceManagerImpl.isCleanerUsed(), "Java 9+ must use Cleaner-based reference management.");
        }
    }
}
