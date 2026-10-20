// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.extensions;

import com.azure.core.util.ServiceVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static com.azure.storage.common.test.shared.extensions.RequiredServiceVersionExtension.evaluateServiceVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequiredServiceVersionExtensionTests {
    private static final String UNKNOWN_SERVICE_VERSION = "0001-01-01";

    @Test
    public void comparesRequiredWireValueToConfiguredEnumNameInServiceVersionOrder() {
        assertTrue(
            evaluate(TestServiceVersion.V2026_08_06.getVersion(), TestServiceVersion.V2026_06_06.name()).isDisabled());
        assertFalse(
            evaluate(TestServiceVersion.V2026_08_06.getVersion(), TestServiceVersion.V2026_08_06.name()).isDisabled());
        assertFalse(
            evaluate(TestServiceVersion.V2026_08_06.getVersion(), TestServiceVersion.V2026_10_06.name()).isDisabled());
    }

    @Test
    public void resolvesEnumNames() {
        assertFalse(
            evaluate(TestServiceVersion.V2026_08_06.name(), TestServiceVersion.V2026_10_06.name()).isDisabled());
    }

    @Test
    public void defaultsToLatestWireValue() {
        List<String> warnings = new ArrayList<>();

        ConditionEvaluationResult result = evaluateServiceVersion(TestServiceVersion.class,
            TestServiceVersion.V2026_10_06.getVersion(), null, warnings::add);

        assertFalse(result.isDisabled());
        assertTrue(warnings.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "V2027_03_07", "2027-03-07", "not-a-service-version" })
    public void skipsUnknownConfiguredVersionWithWarning(String unknownServiceVersion) {
        List<String> warnings = new ArrayList<>();

        ConditionEvaluationResult result = evaluateServiceVersion(TestServiceVersion.class,
            TestServiceVersion.V2026_08_06.getVersion(), unknownServiceVersion, warnings::add);

        assertTrue(result.isDisabled());
        assertEquals(1, warnings.size());
        assertEquals(result.getReason().orElseThrow(AssertionError::new), warnings.get(0));
        assertTrue(warnings.get(0).contains("AZURE_LIVE_TEST_SERVICE_VERSION '" + unknownServiceVersion + "'"));
        assertTrue(warnings.get(0).contains(TestServiceVersion.class.getSimpleName()));
        assertTrue(warnings.get(0).contains(TestServiceVersion.V2026_08_06.getVersion()));
    }

    @Test
    public void rejectsUnknownMinimumVersion() {
        List<String> warnings = new ArrayList<>();

        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> evaluateServiceVersion(TestServiceVersion.class,
                UNKNOWN_SERVICE_VERSION, UNKNOWN_SERVICE_VERSION, warnings::add));

        assertEquals("Minimum service version '" + UNKNOWN_SERVICE_VERSION + "' is not supported.",
            exception.getMessage());
        assertTrue(warnings.isEmpty());
    }

    @Test
    public void knownConfiguredVersionsDoNotWarn() {
        List<String> warnings = new ArrayList<>();

        ConditionEvaluationResult result = evaluateServiceVersion(TestServiceVersion.class,
            TestServiceVersion.V2026_08_06.getVersion(), TestServiceVersion.V2026_06_06.name(), warnings::add);

        assertTrue(result.isDisabled());
        assertTrue(warnings.isEmpty());
        assertFalse(result.getReason().orElseThrow(AssertionError::new).contains("AZURE_LIVE_TEST_SERVICE_VERSION"));
    }

    private static ConditionEvaluationResult evaluate(String minimumServiceVersion, String configuredServiceVersion) {
        return evaluateServiceVersion(TestServiceVersion.class, minimumServiceVersion, configuredServiceVersion,
            warning -> {
                throw new AssertionError("Known service versions must not produce warnings.");
            });
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
