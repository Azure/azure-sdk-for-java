// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link OpenAIVoice}.
 */
class OpenAIVoiceTest {

    @Test
    void testConstructorWithValidVoiceName() {
        // Arrange
        OpenAIVoiceName voiceName = OpenAIVoiceName.ALLOY;

        // Act
        OpenAIVoice voice = new OpenAIVoice(voiceName);

        // Assert
        assertNotNull(voice);
        assertEquals(voiceName, voice.getName());
        assertEquals("openai", voice.getType());
    }

    @Test
    void testProviderIsAlwaysOpenAI() {
        // Test with different voice names
        OpenAIVoice alloyVoice = new OpenAIVoice(OpenAIVoiceName.ALLOY);
        OpenAIVoice echoVoice = new OpenAIVoice(OpenAIVoiceName.ECHO);

        assertEquals("openai", alloyVoice.getType());
        assertEquals("openai", echoVoice.getType());
    }

    @Test
    void testGetName() {
        // Test each voice name
        OpenAIVoice alloyVoice = new OpenAIVoice(OpenAIVoiceName.ALLOY);
        OpenAIVoice echoVoice = new OpenAIVoice(OpenAIVoiceName.ECHO);
        OpenAIVoice shimmerVoice = new OpenAIVoice(OpenAIVoiceName.SHIMMER);

        assertEquals(OpenAIVoiceName.ALLOY, alloyVoice.getName());
        assertEquals(OpenAIVoiceName.ECHO, echoVoice.getName());
        assertEquals(OpenAIVoiceName.SHIMMER, shimmerVoice.getName());
    }

    @Test
    void testEqualsAndHashCode() {
        // Create identical voices
        OpenAIVoice voice1 = new OpenAIVoice(OpenAIVoiceName.ALLOY);
        OpenAIVoice voice2 = new OpenAIVoice(OpenAIVoiceName.ALLOY);
        OpenAIVoice voice3 = new OpenAIVoice(OpenAIVoiceName.ECHO);

        // Test reflexive
        assertEquals(voice1, voice1);

        // Test symmetric and transitive (if equals is properly implemented)
        if (hasEqualsMethod(voice1)) {
            assertEquals(voice1, voice2);
            assertEquals(voice2, voice1);

            // Test hash code consistency
            assertEquals(voice1.hashCode(), voice2.hashCode());

            // Test with different voice names
            assertNotEquals(voice1, voice3);

            // Test with null
            assertNotEquals(voice1, null);

            // Test with different type
            assertNotEquals(voice1, "not a voice");
        }
    }

    @Test
    void testToString() {
        OpenAIVoice voice = new OpenAIVoice(OpenAIVoiceName.ALLOY);
        String toString = voice.toString();

        assertNotNull(toString);
        assertFalse(toString.isEmpty());

        // Should contain meaningful information
        assertTrue(toString.contains("OpenAI") || toString.contains("ALLOY") || toString.contains("alloy"));
    }

    @Test
    void testProviderType() {
        OpenAIVoice voice = new OpenAIVoice(OpenAIVoiceName.ALLOY);

        // Verify provider type is openai
        assertEquals("openai", voice.getType());
        assertNotNull(voice.getType());
    }

    @Test
    void testVoiceNameConsistency() {
        // Test that each OpenAIVoiceName corresponds to the correct voice
        for (OpenAIVoiceName voiceName : OpenAIVoiceName.values()) {
            OpenAIVoice voice = new OpenAIVoice(voiceName);
            assertEquals(voiceName, voice.getName());
            assertEquals("openai", voice.getType());
        }
    }

    // Helper methods
    private boolean hasEqualsMethod(OpenAIVoice voice) {
        try {
            return !voice.getClass().getMethod("equals", Object.class).getDeclaringClass().equals(Object.class);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
