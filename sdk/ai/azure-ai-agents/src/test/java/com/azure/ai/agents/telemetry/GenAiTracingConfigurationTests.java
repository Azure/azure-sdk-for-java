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
        assertTrue(GenAiTracingConfiguration.isTraceContextPropagationEnabled());
    }

    @Test
    void enableWithNullOptionsUsesDefaults() {
        GenAiTracingConfiguration.enableGenAiTracing(null);

        assertTrue(GenAiTracingConfiguration.isTracingEnabled());
        assertFalse(GenAiTracingConfiguration.isContentRecordingEnabled());
        assertTrue(GenAiTracingConfiguration.isTraceContextPropagationEnabled());
    }

    @Test
    void enableWithContentRecording() {
        GenAiTracingOptions options = new GenAiTracingOptions().setContentRecording(true);
        GenAiTracingConfiguration.enableGenAiTracing(options);

        assertTrue(GenAiTracingConfiguration.isTracingEnabled());
        assertTrue(GenAiTracingConfiguration.isContentRecordingEnabled());
    }

    @Test
    void enableWithPropagationDisabled() {
        GenAiTracingOptions options = new GenAiTracingOptions().setTraceContextPropagation(false);
        GenAiTracingConfiguration.enableGenAiTracing(options);

        assertTrue(GenAiTracingConfiguration.isTracingEnabled());
        assertFalse(GenAiTracingConfiguration.isTraceContextPropagationEnabled());
    }

    @Test
    void disableResetsToDefaults() {
        GenAiTracingOptions options
            = new GenAiTracingOptions().setContentRecording(true).setTraceContextPropagation(false);
        GenAiTracingConfiguration.enableGenAiTracing(options);

        assertTrue(GenAiTracingConfiguration.isTracingEnabled());
        assertTrue(GenAiTracingConfiguration.isContentRecordingEnabled());
        assertFalse(GenAiTracingConfiguration.isTraceContextPropagationEnabled());

        GenAiTracingConfiguration.disableGenAiTracing();

        assertFalse(GenAiTracingConfiguration.isTracingEnabled());
        assertFalse(GenAiTracingConfiguration.isContentRecordingEnabled());
        assertTrue(GenAiTracingConfiguration.isTraceContextPropagationEnabled());
    }

    @Test
    void programmaticContentRecordingFalseOverridesDefault() {
        // Programmatic option explicitly sets content recording to false
        GenAiTracingOptions options = new GenAiTracingOptions().setContentRecording(false);
        GenAiTracingConfiguration.enableGenAiTracing(options);

        assertTrue(GenAiTracingConfiguration.isTracingEnabled());
        assertFalse(GenAiTracingConfiguration.isContentRecordingEnabled());
    }

    @Test
    void programmaticContentRecordingTrue() {
        GenAiTracingOptions options = new GenAiTracingOptions().setContentRecording(true);
        GenAiTracingConfiguration.enableGenAiTracing(options);

        assertTrue(GenAiTracingConfiguration.isContentRecordingEnabled());
    }

    @Test
    void reEnableResetsAllOptions() {
        // First enable with content recording ON
        GenAiTracingOptions options1 = new GenAiTracingOptions().setContentRecording(true);
        GenAiTracingConfiguration.enableGenAiTracing(options1);
        assertTrue(GenAiTracingConfiguration.isContentRecordingEnabled());

        // Re-enable with content recording explicitly OFF
        GenAiTracingOptions options2 = new GenAiTracingOptions().setContentRecording(false);
        GenAiTracingConfiguration.enableGenAiTracing(options2);
        assertFalse(GenAiTracingConfiguration.isContentRecordingEnabled());
    }

    @Test
    void enableDisableEnableCycle() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setContentRecording(true));
        assertTrue(GenAiTracingConfiguration.isTracingEnabled());
        assertTrue(GenAiTracingConfiguration.isContentRecordingEnabled());

        GenAiTracingConfiguration.disableGenAiTracing();
        assertFalse(GenAiTracingConfiguration.isTracingEnabled());
        assertFalse(GenAiTracingConfiguration.isContentRecordingEnabled());

        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions());
        assertTrue(GenAiTracingConfiguration.isTracingEnabled());
        assertFalse(GenAiTracingConfiguration.isContentRecordingEnabled());
    }
}
