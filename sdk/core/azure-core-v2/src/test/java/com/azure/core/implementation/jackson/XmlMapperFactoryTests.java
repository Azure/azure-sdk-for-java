// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.fasterxml.jackson.databind.cfg.PackageVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XmlMapperFactoryTests {
    @Test
    public void instantiate() {
        // Since we have a Jackson version test matrix this test will need to check the version of Jackson Databind
        // being used.
        if (PackageVersion.VERSION.getMinorVersion() >= 12) {
            // This should be true when running with Jackson Databind 2.12+.
            assertTrue(XmlMapperFactory.INSTANCE.useJackson212);
        } else {
            // This should be false when running with Jackson Databind 2.11-.
            assertFalse(XmlMapperFactory.INSTANCE.useJackson212);
        }
    }
}
