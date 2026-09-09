// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.core.test.TestMode;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CrossResourceCopyTestConfigurationTest {
    @Test
    public void recordAndLiveModesRequireEveryCrossResourceValue() {
        for (TestMode mode : new TestMode[] { TestMode.RECORD, TestMode.LIVE }) {
            for (String variable : requiredCrossResourceVariables()) {
                Map<String, String> missing = createEnvironment();
                missing.remove(variable);
                TestAbortedException missingException = assertThrows(TestAbortedException.class,
                    () -> CrossResourceCopyTestConfiguration.load(mode, missing::get));
                assertTrue(missingException.getMessage().contains(variable));

                Map<String, String> blank = createEnvironment();
                blank.put(variable, " \t");
                TestAbortedException blankException = assertThrows(TestAbortedException.class,
                    () -> CrossResourceCopyTestConfiguration.load(mode, blank::get));
                assertTrue(blankException.getMessage().contains(variable));
            }
        }
    }

    @Test
    public void liveEnvironmentAssumptionRequiresEveryValue() {
        for (String variable : requiredLiveVariables()) {
            Map<String, String> environment = createEnvironment();
            environment.put("CONTENTUNDERSTANDING_ENDPOINT", "https://source.services.ai.azure.com/");
            environment.put(variable, " ");

            TestAbortedException exception = assertThrows(TestAbortedException.class,
                () -> CrossResourceCopyTestConfiguration.assumeLiveEnvironmentIsConfigured(environment::get));

            assertTrue(exception.getMessage().contains(variable));
        }
    }

    @Test
    public void playbackUsesSanitizedConfiguration() {
        Map<String, String> environment = createEnvironment();

        CrossResourceCopyTestConfiguration recordingConfiguration
            = CrossResourceCopyTestConfiguration.load(TestMode.RECORD, environment::get);
        CrossResourceCopyTestConfiguration playbackConfiguration
            = CrossResourceCopyTestConfiguration.load(TestMode.PLAYBACK, name -> {
                throw new AssertionError("PLAYBACK must not read environment variable: " + name);
            });

        assertEquals(environment.get("CONTENTUNDERSTANDING_SOURCE_RESOURCE_ID"),
            recordingConfiguration.getSourceResourceId());
        assertEquals("eastus", recordingConfiguration.getSourceRegion());
        assertEquals("https://target.services.ai.azure.com", recordingConfiguration.getTargetEndpoint());
        assertEquals("target-key", recordingConfiguration.getTargetKey());
        assertEquals("target-resource-id", recordingConfiguration.getTargetResourceId());
        assertEquals("westus", recordingConfiguration.getTargetRegion());
        assertTrue(playbackConfiguration.getSourceResourceId().contains("REDACTED"));
        assertEquals("REDACTED", playbackConfiguration.getSourceRegion());
        assertEquals("https://REDACTED.services.ai.azure.com", playbackConfiguration.getTargetEndpoint());
        assertNull(playbackConfiguration.getTargetKey());
        assertTrue(playbackConfiguration.getTargetResourceId().contains("REDACTED"));
        assertEquals("REDACTED", playbackConfiguration.getTargetRegion());
    }

    @Test
    public void targetKeyIsOptional() {
        Map<String, String> environment = createEnvironment();
        environment.remove("CONTENTUNDERSTANDING_TARGET_KEY");

        CrossResourceCopyTestConfiguration configuration
            = CrossResourceCopyTestConfiguration.load(TestMode.RECORD, environment::get);

        assertNull(configuration.getTargetKey());
    }

    @Test
    public void liveAndRecordModesLoadEquivalentConfiguration() {
        Map<String, String> environment = createEnvironment();

        CrossResourceCopyTestConfiguration live
            = CrossResourceCopyTestConfiguration.load(TestMode.LIVE, environment::get);
        CrossResourceCopyTestConfiguration record
            = CrossResourceCopyTestConfiguration.load(TestMode.RECORD, environment::get);

        assertEquals(record.getSourceResourceId(), live.getSourceResourceId());
        assertEquals(record.getSourceRegion(), live.getSourceRegion());
        assertEquals(record.getTargetEndpoint(), live.getTargetEndpoint());
        assertEquals(record.getTargetKey(), live.getTargetKey());
        assertEquals(record.getTargetResourceId(), live.getTargetResourceId());
        assertEquals(record.getTargetRegion(), live.getTargetRegion());
    }

    @Test
    public void endpointNormalizationRemovesAllTrailingSlashes() {
        Map<String, String> environment = createEnvironment();
        environment.put("CONTENTUNDERSTANDING_TARGET_ENDPOINT", "https://target.services.ai.azure.com////");

        CrossResourceCopyTestConfiguration configuration
            = CrossResourceCopyTestConfiguration.load(TestMode.RECORD, environment::get);

        assertEquals("https://target.services.ai.azure.com", configuration.getTargetEndpoint());
    }

    private static Map<String, String> createEnvironment() {
        Map<String, String> environment = new HashMap<>();
        environment.put("CONTENTUNDERSTANDING_SOURCE_RESOURCE_ID", "source-resource-id");
        environment.put("CONTENTUNDERSTANDING_SOURCE_REGION", "eastus");
        environment.put("CONTENTUNDERSTANDING_TARGET_ENDPOINT", "https://target.services.ai.azure.com/");
        environment.put("CONTENTUNDERSTANDING_TARGET_KEY", "target-key");
        environment.put("CONTENTUNDERSTANDING_TARGET_RESOURCE_ID", "target-resource-id");
        environment.put("CONTENTUNDERSTANDING_TARGET_REGION", "westus");
        return environment;
    }

    private static String[] requiredCrossResourceVariables() {
        return new String[] {
            "CONTENTUNDERSTANDING_SOURCE_RESOURCE_ID",
            "CONTENTUNDERSTANDING_SOURCE_REGION",
            "CONTENTUNDERSTANDING_TARGET_ENDPOINT",
            "CONTENTUNDERSTANDING_TARGET_RESOURCE_ID",
            "CONTENTUNDERSTANDING_TARGET_REGION" };
    }

    private static String[] requiredLiveVariables() {
        return new String[] {
            "CONTENTUNDERSTANDING_ENDPOINT",
            "CONTENTUNDERSTANDING_SOURCE_RESOURCE_ID",
            "CONTENTUNDERSTANDING_SOURCE_REGION",
            "CONTENTUNDERSTANDING_TARGET_ENDPOINT",
            "CONTENTUNDERSTANDING_TARGET_RESOURCE_ID",
            "CONTENTUNDERSTANDING_TARGET_REGION" };
    }
}
