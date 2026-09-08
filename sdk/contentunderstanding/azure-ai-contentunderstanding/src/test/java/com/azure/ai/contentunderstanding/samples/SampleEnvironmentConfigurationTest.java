// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SampleEnvironmentConfigurationTest {
    @Test
    public void trimsConfiguredValue() {
        assertEquals("https://example.com",
            SampleEnvironmentConfiguration.requireEnvironmentValue("ENDPOINT", "  https://example.com  "));
    }

    @Test
    public void rejectsMissingValue() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> SampleEnvironmentConfiguration.requireEnvironmentValue("ENDPOINT", null));

        assertTrue(exception.getMessage().contains("ENDPOINT"));
        assertTrue(exception.getMessage().contains("Required environment variable"));
    }

    @Test
    public void rejectsBlankValue() {
        assertThrows(IllegalStateException.class,
            () -> SampleEnvironmentConfiguration.requireEnvironmentValue("ENDPOINT", "   "));
    }
}
