// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link GenAiTracingConfiguration}.
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class GenAiTracingConfigurationTests {

    @BeforeEach
    void setUp() {
        GenAiTracingConfiguration.disableGenAiTracing();
    }

    @AfterEach
    void tearDown() {
        GenAiTracingConfiguration.disableGenAiTracing();
    }

    @Test
    void defaultsAreCorrect() {
        assertFalse(GenAiTracingConfiguration.isTracingEnabled());
        assertFalse(GenAiTracingConfiguration.isContentRecordingEnabled());
    }

    @Test
    void enableWithNullOptionsUsesDefaults() {
        GenAiTracingConfiguration.enableGenAiTracing(null);

        // Without experimental acknowledged (env var not set), tracing is not applied
        assertFalse(GenAiTracingConfiguration.isTracingEnabled());
        assertFalse(GenAiTracingConfiguration.isContentRecordingEnabled());
    }

    @Test
    void enableWithExperimentalAcknowledged() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));

        assertTrue(GenAiTracingConfiguration.isTracingEnabled());
        assertFalse(GenAiTracingConfiguration.isContentRecordingEnabled());
    }

    @Test
    void enableWithoutExperimentalDoesNotApply() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(false));

        assertFalse(GenAiTracingConfiguration.isTracingEnabled());
    }

    @Test
    void enableWithContentRecording() {
        GenAiTracingOptions options = new GenAiTracingOptions().setExperimental(true).setContentRecording(true);
        GenAiTracingConfiguration.enableGenAiTracing(options);

        assertTrue(GenAiTracingConfiguration.isTracingEnabled());
        assertTrue(GenAiTracingConfiguration.isContentRecordingEnabled());
    }

    @Test
    void disableResetsToDefaults() {
        GenAiTracingOptions options = new GenAiTracingOptions().setExperimental(true).setContentRecording(true);
        GenAiTracingConfiguration.enableGenAiTracing(options);

        assertTrue(GenAiTracingConfiguration.isTracingEnabled());
        assertTrue(GenAiTracingConfiguration.isContentRecordingEnabled());

        GenAiTracingConfiguration.disableGenAiTracing();

        assertFalse(GenAiTracingConfiguration.isTracingEnabled());
        assertFalse(GenAiTracingConfiguration.isContentRecordingEnabled());
    }

    @Test
    void programmaticContentRecordingFalseOverridesDefault() {
        GenAiTracingOptions options = new GenAiTracingOptions().setExperimental(true).setContentRecording(false);
        GenAiTracingConfiguration.enableGenAiTracing(options);

        assertTrue(GenAiTracingConfiguration.isTracingEnabled());
        assertFalse(GenAiTracingConfiguration.isContentRecordingEnabled());
    }

    @Test
    void programmaticContentRecordingTrue() {
        GenAiTracingOptions options = new GenAiTracingOptions().setExperimental(true).setContentRecording(true);
        GenAiTracingConfiguration.enableGenAiTracing(options);

        assertTrue(GenAiTracingConfiguration.isContentRecordingEnabled());
    }

    @Test
    void reEnableResetsAllOptions() {
        GenAiTracingOptions options1 = new GenAiTracingOptions().setExperimental(true).setContentRecording(true);
        GenAiTracingConfiguration.enableGenAiTracing(options1);
        assertTrue(GenAiTracingConfiguration.isContentRecordingEnabled());

        GenAiTracingOptions options2 = new GenAiTracingOptions().setExperimental(true).setContentRecording(false);
        GenAiTracingConfiguration.enableGenAiTracing(options2);
        assertFalse(GenAiTracingConfiguration.isContentRecordingEnabled());
    }

    @Test
    void enableDisableEnableCycle() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));
        assertTrue(GenAiTracingConfiguration.isTracingEnabled());
        assertTrue(GenAiTracingConfiguration.isContentRecordingEnabled());

        GenAiTracingConfiguration.disableGenAiTracing();
        assertFalse(GenAiTracingConfiguration.isTracingEnabled());
        assertFalse(GenAiTracingConfiguration.isContentRecordingEnabled());

        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        assertTrue(GenAiTracingConfiguration.isTracingEnabled());
        assertFalse(GenAiTracingConfiguration.isContentRecordingEnabled());
    }
}
