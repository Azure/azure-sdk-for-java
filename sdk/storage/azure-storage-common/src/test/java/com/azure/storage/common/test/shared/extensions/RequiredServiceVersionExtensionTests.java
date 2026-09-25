// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.extensions;

import com.azure.core.util.ServiceVersion;
import org.junit.jupiter.api.Test;

import static com.azure.storage.common.test.shared.extensions.RequiredServiceVersionExtension.shouldSkip;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequiredServiceVersionExtensionTests {
    private static final String UNKNOWN_SERVICE_VERSION = "0001-01-01";

    @Test
    public void comparesRequiredWireValueToConfiguredEnumNameInServiceVersionOrder() {
        assertTrue(shouldSkip(TestServiceVersion.class, TestServiceVersion.V2026_08_06.getVersion(),
            TestServiceVersion.V2026_06_06.name()));
        assertFalse(shouldSkip(TestServiceVersion.class, TestServiceVersion.V2026_08_06.getVersion(),
            TestServiceVersion.V2026_08_06.name()));
        assertFalse(shouldSkip(TestServiceVersion.class, TestServiceVersion.V2026_08_06.getVersion(),
            TestServiceVersion.V2026_10_06.name()));
    }

    @Test
    public void resolvesEnumNames() {
        assertFalse(shouldSkip(TestServiceVersion.class, TestServiceVersion.V2026_08_06.name(),
            TestServiceVersion.V2026_10_06.name()));
    }

    @Test
    public void defaultsToLatestWireValue() {
        assertFalse(shouldSkip(TestServiceVersion.class, TestServiceVersion.V2026_10_06.getVersion(), null));
    }

    @Test
    public void rejectsUnknownConfiguredVersion() {
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> shouldSkip(TestServiceVersion.class,
                TestServiceVersion.V2026_08_06.getVersion(), UNKNOWN_SERVICE_VERSION));

        assertTrue(exception.getMessage().contains("configured"));
    }

    @Test
    public void rejectsUnknownMinimumVersion() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> shouldSkip(TestServiceVersion.class, UNKNOWN_SERVICE_VERSION, TestServiceVersion.V2026_10_06.name()));

        assertTrue(exception.getMessage().contains("minimum"));
    }

    enum TestServiceVersion implements ServiceVersion {
        V2026_06_06("2026-06-06"), V2026_08_06("2026-08-06"), V2026_10_06("2026-10-06");

        private final String version;

        TestServiceVersion(String version) {
            this.version = version;
        }

        @Override
        public String getVersion() {
            return version;
        }

        public static TestServiceVersion getLatest() {
            return V2026_10_06;
        }
    }
}
