// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.extensions;

import com.azure.core.util.ServiceVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.azure.storage.common.test.shared.extensions.RequiredServiceVersionExtension.evaluateServiceVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequiredServiceVersionExtensionTests {
    private static final String UNKNOWN_SERVICE_VERSION = "0001-01-01";

    @Test
    public void comparesRequiredWireValueToConfiguredEnumNameInServiceVersionOrder() {
        ConditionEvaluationResult olderVersion
            = evaluate(TestServiceVersion.V2026_08_06.getVersion(), TestServiceVersion.V2026_06_06.name());

        assertTrue(olderVersion.isDisabled());
        assertEquals("Test requires minimum service version '2026-08-06'.",
            olderVersion.getReason().orElseThrow(AssertionError::new));
        assertFalse(
            evaluate(TestServiceVersion.V2026_08_06.getVersion(), TestServiceVersion.V2026_08_06.name()).isDisabled());
        assertFalse(
            evaluate(TestServiceVersion.V2026_08_06.getVersion(), TestServiceVersion.V2026_10_06.name()).isDisabled());
    }

    @Test
    public void resolvesEnumNamesAndWireValues() {
        assertFalse(
            evaluate(TestServiceVersion.V2026_08_06.name(), TestServiceVersion.V2026_10_06.name()).isDisabled());
        assertFalse(evaluate(TestServiceVersion.V2026_08_06.getVersion(), TestServiceVersion.V2026_10_06.getVersion())
            .isDisabled());
    }

    @Test
    public void defaultsToLatestWireValue() {
        ConditionEvaluationResult result
            = evaluateServiceVersion(TestServiceVersion.class, TestServiceVersion.V2026_10_06.getVersion(), null);

        assertFalse(result.isDisabled());
    }

    @ParameterizedTest
    @ValueSource(strings = { "V2027_03_07", "2027-03-07", "not-a-service-version" })
    public void rejectsUnknownConfiguredVersion(String unknownServiceVersion) {
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> evaluateServiceVersion(TestServiceVersion.class,
                TestServiceVersion.V2026_08_06.getVersion(), unknownServiceVersion));

        assertEquals("AZURE_LIVE_TEST_SERVICE_VERSION '" + unknownServiceVersion + "' is not supported by "
            + TestServiceVersion.class.getSimpleName() + ".", exception.getMessage());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "V2026_10_06", "not-a-service-version" })
    public void rejectsUnknownMinimumVersionBeforeConfiguredVersion(String configuredServiceVersion) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> evaluateServiceVersion(TestServiceVersion.class, UNKNOWN_SERVICE_VERSION, configuredServiceVersion));

        assertEquals("Minimum service version '" + UNKNOWN_SERVICE_VERSION + "' is not supported.",
            exception.getMessage());
    }

    private static ConditionEvaluationResult evaluate(String minimumServiceVersion, String configuredServiceVersion) {
        return evaluateServiceVersion(TestServiceVersion.class, minimumServiceVersion, configuredServiceVersion);
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
