// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link OutputAudioFormat} - verifies the updated format values.
 */
class OutputAudioFormatTest {

    @Test
    void testOutputAudioFormatValues() {
        // Assert all known values exist
        assertNotNull(OutputAudioFormat.PCM16);
        assertNotNull(OutputAudioFormat.PCM16_8000HZ);
        assertNotNull(OutputAudioFormat.PCM16_16000HZ);
        assertNotNull(OutputAudioFormat.G711_ULAW);
        assertNotNull(OutputAudioFormat.G711_ALAW);
    }

    @Test
    void testOutputAudioFormatToString() {
        // Assert correct string values (underscore format)
        assertEquals("pcm16", OutputAudioFormat.PCM16.toString());
        assertEquals("pcm16_8000hz", OutputAudioFormat.PCM16_8000HZ.toString());
        assertEquals("pcm16_16000hz", OutputAudioFormat.PCM16_16000HZ.toString());
        assertEquals("g711_ulaw", OutputAudioFormat.G711_ULAW.toString());
        assertEquals("g711_alaw", OutputAudioFormat.G711_ALAW.toString());
    }

    @Test
    void testOutputAudioFormatFromString() {
        // Act & Assert - using underscore format
        assertEquals(OutputAudioFormat.PCM16, OutputAudioFormat.fromString("pcm16"));
        assertEquals(OutputAudioFormat.PCM16_8000HZ, OutputAudioFormat.fromString("pcm16_8000hz"));
        assertEquals(OutputAudioFormat.PCM16_16000HZ, OutputAudioFormat.fromString("pcm16_16000hz"));
        assertEquals(OutputAudioFormat.G711_ULAW, OutputAudioFormat.fromString("g711_ulaw"));
        assertEquals(OutputAudioFormat.G711_ALAW, OutputAudioFormat.fromString("g711_alaw"));
    }

    @Test
    void testOutputAudioFormatInVoiceLiveSessionOptions() {
        // Arrange
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions();

        // Act & Assert - all formats should work
        options.setOutputAudioFormat(OutputAudioFormat.PCM16);
        assertEquals(OutputAudioFormat.PCM16, options.getOutputAudioFormat());

        options.setOutputAudioFormat(OutputAudioFormat.PCM16_8000HZ);
        assertEquals(OutputAudioFormat.PCM16_8000HZ, options.getOutputAudioFormat());

        options.setOutputAudioFormat(OutputAudioFormat.PCM16_16000HZ);
        assertEquals(OutputAudioFormat.PCM16_16000HZ, options.getOutputAudioFormat());

        options.setOutputAudioFormat(OutputAudioFormat.G711_ULAW);
        assertEquals(OutputAudioFormat.G711_ULAW, options.getOutputAudioFormat());

        options.setOutputAudioFormat(OutputAudioFormat.G711_ALAW);
        assertEquals(OutputAudioFormat.G711_ALAW, options.getOutputAudioFormat());
    }

    @Test
    void testOutputAudioFormatValuesCollection() {
        // Act
        Collection<OutputAudioFormat> values = OutputAudioFormat.values();

        // Assert
        assertNotNull(values);
        // Should contain at least the 5 known values
        assertEquals(5, values.size());
    }
}
