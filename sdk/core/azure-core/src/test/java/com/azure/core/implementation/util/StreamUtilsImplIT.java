// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StreamUtilsImplIT {
    /*
     * This is an integration test instead of a unit test because integration tests use the generated JAR, with
     * multi-release support, instead of the class files in Maven's output directory. Given that, the integration tests
     * will hook into the different Java version implementations.
     */
    @Test
    public void validateImplementationVersion() {
        String javaSpecificationVersion = System.getProperty("java.specification.version");
        int implementationSpecificationVersion = StreamUtilsImpl.getJavaImplementationMajorVersion();
        if ("1.8".equals(javaSpecificationVersion)) {
            assertEquals(8, implementationSpecificationVersion);
        } else {
            assertEquals(9, implementationSpecificationVersion);
        }
    }
}
