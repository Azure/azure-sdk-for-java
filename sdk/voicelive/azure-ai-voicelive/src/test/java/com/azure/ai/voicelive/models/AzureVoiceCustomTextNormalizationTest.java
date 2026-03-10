// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for custom text normalization URL property in Azure voice classes:
 * {@link AzureCustomVoice}, {@link AzurePersonalVoice}, {@link AzureStandardVoice}.
 */
class AzureVoiceCustomTextNormalizationTest {

    @Test
    void testAzureCustomVoiceWithCustomTextNormalizationUrl() {
        // Arrange
        String normalizationUrl = "https://example.com/normalization.xml";

        // Act
        AzureCustomVoice voice
            = new AzureCustomVoice("custom-voice-id", "en-US").setCustomTextNormalizationUrl(normalizationUrl);

        // Assert
        assertEquals(normalizationUrl, voice.getCustomTextNormalizationUrl());
    }

    @Test
    void testAzureCustomVoiceJsonSerialization() {
        // Arrange
        AzureCustomVoice voice = new AzureCustomVoice("my-custom-voice", "en-US")
            .setCustomTextNormalizationUrl("https://storage.blob.core.windows.net/normalization.xml")
            .setCustomLexiconUri("https://storage.blob.core.windows.net/lexicon.xml");

        // Act
        BinaryData serialized = BinaryData.fromObject(voice);
        AzureCustomVoice deserialized = serialized.toObject(AzureCustomVoice.class);

        // Assert
        assertEquals(voice.getCustomTextNormalizationUrl(), deserialized.getCustomTextNormalizationUrl());
        assertEquals(voice.getCustomLexiconUri(), deserialized.getCustomLexiconUri());
        assertEquals(voice.getEndpointId(), deserialized.getEndpointId());
    }

    @Test
    void testAzurePersonalVoiceWithCustomTextNormalizationUrl() {
        // Arrange
        String normalizationUrl = "https://example.com/personal-normalization.xml";

        // Act
        AzurePersonalVoice voice
            = new AzurePersonalVoice("personal-voice-id", PersonalVoiceModels.PHOENIX_LATEST_NEURAL)
                .setCustomTextNormalizationUrl(normalizationUrl);

        // Assert
        assertEquals(normalizationUrl, voice.getCustomTextNormalizationUrl());
    }

    @Test
    void testAzurePersonalVoiceJsonSerialization() {
        // Arrange
        AzurePersonalVoice voice
            = new AzurePersonalVoice("my-personal-voice", PersonalVoiceModels.PHOENIX_LATEST_NEURAL)
                .setCustomTextNormalizationUrl("https://storage.blob.core.windows.net/personal-normalization.xml")
                .setCustomLexiconUrl("https://storage.blob.core.windows.net/personal-lexicon.xml");

        // Act
        BinaryData serialized = BinaryData.fromObject(voice);
        AzurePersonalVoice deserialized = serialized.toObject(AzurePersonalVoice.class);

        // Assert
        assertEquals(voice.getCustomTextNormalizationUrl(), deserialized.getCustomTextNormalizationUrl());
        assertEquals(voice.getCustomLexiconUrl(), deserialized.getCustomLexiconUrl());
        assertEquals(voice.getName(), deserialized.getName());
    }

    @Test
    void testAzureStandardVoiceWithCustomTextNormalizationUrl() {
        // Arrange
        String normalizationUrl = "https://example.com/standard-normalization.xml";

        // Act
        AzureStandardVoice voice
            = new AzureStandardVoice("en-US-JennyNeural").setCustomTextNormalizationUrl(normalizationUrl);

        // Assert
        assertEquals(normalizationUrl, voice.getCustomTextNormalizationUrl());
    }

    @Test
    void testAzureStandardVoiceJsonSerialization() {
        // Arrange
        AzureStandardVoice voice = new AzureStandardVoice("en-US-GuyNeural")
            .setCustomTextNormalizationUrl("https://storage.blob.core.windows.net/standard-normalization.xml")
            .setCustomLexiconUrl("https://storage.blob.core.windows.net/standard-lexicon.xml");

        // Act
        BinaryData serialized = BinaryData.fromObject(voice);
        AzureStandardVoice deserialized = serialized.toObject(AzureStandardVoice.class);

        // Assert
        assertEquals(voice.getCustomTextNormalizationUrl(), deserialized.getCustomTextNormalizationUrl());
        assertEquals(voice.getCustomLexiconUrl(), deserialized.getCustomLexiconUrl());
        assertEquals(voice.getName(), deserialized.getName());
    }

    @Test
    void testAzureCustomVoiceJsonDeserializationWithNormalizationUrl() {
        // Arrange
        String json = "{\"type\":\"azure_custom\",\"endpoint_id\":\"ep123\",\"deployment_id\":\"dep456\","
            + "\"custom_lexicon_url\":\"https://lexicon.xml\","
            + "\"custom_text_normalization_url\":\"https://normalization.xml\"}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        AzureCustomVoice voice = data.toObject(AzureCustomVoice.class);

        // Assert
        assertNotNull(voice);
        assertEquals("https://normalization.xml", voice.getCustomTextNormalizationUrl());
        assertEquals("https://lexicon.xml", voice.getCustomLexiconUri());
    }

    @Test
    void testAzurePersonalVoiceJsonDeserializationWithNormalizationUrl() {
        // Arrange
        String json = "{\"type\":\"azure_personal\",\"name\":\"voice1\",\"model\":\"personal_voice_neural\","
            + "\"custom_lexicon_url\":\"https://lexicon.xml\","
            + "\"custom_text_normalization_url\":\"https://normalization.xml\"}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        AzurePersonalVoice voice = data.toObject(AzurePersonalVoice.class);

        // Assert
        assertNotNull(voice);
        assertEquals("https://normalization.xml", voice.getCustomTextNormalizationUrl());
        assertEquals("https://lexicon.xml", voice.getCustomLexiconUrl());
    }

    @Test
    void testAzureStandardVoiceJsonDeserializationWithNormalizationUrl() {
        // Arrange
        String json = "{\"type\":\"azure_standard\",\"name\":\"en-US-AriaNeural\","
            + "\"custom_lexicon_url\":\"https://lexicon.xml\","
            + "\"custom_text_normalization_url\":\"https://normalization.xml\"}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        AzureStandardVoice voice = data.toObject(AzureStandardVoice.class);

        // Assert
        assertNotNull(voice);
        assertEquals("https://normalization.xml", voice.getCustomTextNormalizationUrl());
        assertEquals("https://lexicon.xml", voice.getCustomLexiconUrl());
    }

    @Test
    void testAzureVoicesWithoutCustomTextNormalizationUrl() {
        // Arrange & Act
        AzureCustomVoice customVoice = new AzureCustomVoice("ep", "dep");
        AzurePersonalVoice personalVoice = new AzurePersonalVoice("name", PersonalVoiceModels.PHOENIX_LATEST_NEURAL);
        AzureStandardVoice standardVoice = new AzureStandardVoice("en-US-JennyNeural");

        // Assert - default should be null
        assertNull(customVoice.getCustomTextNormalizationUrl());
        assertNull(personalVoice.getCustomTextNormalizationUrl());
        assertNull(standardVoice.getCustomTextNormalizationUrl());
    }
}
