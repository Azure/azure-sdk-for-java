// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ConfigurationServiceVersion}.
 */
public class ConfigurationServiceVersionTest {
    @Test
    void getLatestReturnsNewestVersion() {
        assertEquals(ConfigurationServiceVersion.V2026_05_01_PREVIEW, ConfigurationServiceVersion.getLatest());
    }

    @Test
    void previewVersionMapsToApiVersionString() {
        assertEquals("2026-05-01-preview", ConfigurationServiceVersion.V2026_05_01_PREVIEW.getVersion());
    }
}
