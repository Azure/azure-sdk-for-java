// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.unit.models;

import com.azure.ai.voicelive.models.AzureAvatarVoiceSyncVoice;
import com.azure.ai.voicelive.models.AzureVoice;
import com.azure.ai.voicelive.models.AzureVoiceType;
import com.azure.ai.voicelive.models.PersonalVoiceModels;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AzureAvatarVoiceSyncVoice} (the new {@code avatar-voice-sync} voice type
 * introduced for video avatar voice synchronization).
 */
class AzureAvatarVoiceSyncVoiceTest {

    @Test
    void testAvatarVoiceSyncTypeRegistered() {
        assertNotNull(AzureVoiceType.AVATAR_VOICE_SYNC);
        assertEquals("avatar-voice-sync", AzureVoiceType.AVATAR_VOICE_SYNC.toString());
        assertEquals(AzureVoiceType.AVATAR_VOICE_SYNC, AzureVoiceType.fromString("avatar-voice-sync"));
    }

    @Test
    void testConstructionAndDefaults() {
        AzureAvatarVoiceSyncVoice voice = new AzureAvatarVoiceSyncVoice(PersonalVoiceModels.PHOENIX_LATEST_NEURAL);

        assertEquals(AzureVoiceType.AVATAR_VOICE_SYNC, voice.getType());
        assertEquals(PersonalVoiceModels.PHOENIX_LATEST_NEURAL, voice.getModel());
        assertNull(voice.getTemperature());
        assertNull(voice.getStyle());
        assertNull(voice.getLocale());
        assertNull(voice.getPitch());
        assertNull(voice.getRate());
        assertNull(voice.getVolume());
        assertNull(voice.getCustomLexiconUrl());
        assertNull(voice.getCustomTextNormalizationUrl());
        assertNull(voice.getPreferLocales());
    }

    @Test
    void testFluentSettersAndGetters() {
        List<String> preferLocales = Arrays.asList("en-GB", "es-ES");
        AzureAvatarVoiceSyncVoice voice
            = new AzureAvatarVoiceSyncVoice(PersonalVoiceModels.MAI_VOICE_1).setTemperature(0.7)
                .setStyle("cheerful")
                .setLocale("en-US")
                .setPitch("+10%")
                .setRate("medium")
                .setVolume("loud")
                .setCustomLexiconUrl("https://example.com/lexicon.xml")
                .setCustomTextNormalizationUrl("https://example.com/normalize")
                .setPreferLocales(preferLocales);

        assertEquals(PersonalVoiceModels.MAI_VOICE_1, voice.getModel());
        assertEquals(0.7, voice.getTemperature());
        assertEquals("cheerful", voice.getStyle());
        assertEquals("en-US", voice.getLocale());
        assertEquals("+10%", voice.getPitch());
        assertEquals("medium", voice.getRate());
        assertEquals("loud", voice.getVolume());
        assertEquals("https://example.com/lexicon.xml", voice.getCustomLexiconUrl());
        assertEquals("https://example.com/normalize", voice.getCustomTextNormalizationUrl());
        assertEquals(preferLocales, voice.getPreferLocales());
    }

    @Test
    void testSerializesDiscriminatorType() {
        AzureAvatarVoiceSyncVoice voice
            = new AzureAvatarVoiceSyncVoice(PersonalVoiceModels.DRAGON_HDOMNI_LATEST_NEURAL).setStyle("neutral");

        String json = BinaryData.fromObject(voice).toString();

        assertTrue(json.contains("\"type\":\"avatar-voice-sync\""), "expected discriminator: " + json);
        assertTrue(json.contains("\"model\":\"DragonHDOmniLatestNeural\""), "expected model: " + json);
        assertTrue(json.contains("\"style\":\"neutral\""), "expected style: " + json);
    }

    @Test
    void testJsonRoundTrip() {
        AzureAvatarVoiceSyncVoice original
            = new AzureAvatarVoiceSyncVoice(PersonalVoiceModels.MAI_VOICE_1).setTemperature(0.5)
                .setStyle("sad")
                .setLocale("ja-JP")
                .setRate("0.9")
                .setPreferLocales(Arrays.asList("ja-JP"));

        BinaryData serialized = BinaryData.fromObject(original);
        AzureAvatarVoiceSyncVoice deserialized = serialized.toObject(AzureAvatarVoiceSyncVoice.class);

        assertEquals(original.getType(), deserialized.getType());
        assertEquals(original.getModel(), deserialized.getModel());
        assertEquals(original.getTemperature(), deserialized.getTemperature());
        assertEquals(original.getStyle(), deserialized.getStyle());
        assertEquals(original.getLocale(), deserialized.getLocale());
        assertEquals(original.getRate(), deserialized.getRate());
        assertEquals(original.getPreferLocales(), deserialized.getPreferLocales());
    }

    @Test
    void testPolymorphicDeserializationViaAzureVoice() {
        AzureAvatarVoiceSyncVoice original
            = new AzureAvatarVoiceSyncVoice(PersonalVoiceModels.PHOENIX_LATEST_NEURAL).setStyle("cheerful");
        BinaryData serialized = BinaryData.fromObject(original);

        AzureVoice deserialized = serialized.toObject(AzureVoice.class);

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof AzureAvatarVoiceSyncVoice,
            "Expected AzureAvatarVoiceSyncVoice, got " + deserialized.getClass());
        AzureAvatarVoiceSyncVoice typed = (AzureAvatarVoiceSyncVoice) deserialized;
        assertEquals(AzureVoiceType.AVATAR_VOICE_SYNC, typed.getType());
        assertEquals(PersonalVoiceModels.PHOENIX_LATEST_NEURAL, typed.getModel());
        assertEquals("cheerful", typed.getStyle());
    }

    @Test
    void testNewPersonalVoiceModelConstants() {
        // Newly added GA personal voice models should be registered.
        assertNotNull(PersonalVoiceModels.DRAGON_HDOMNI_LATEST_NEURAL);
        assertNotNull(PersonalVoiceModels.MAI_VOICE_1);
        assertEquals("DragonHDOmniLatestNeural", PersonalVoiceModels.DRAGON_HDOMNI_LATEST_NEURAL.toString());
        assertEquals("MAI-Voice-1", PersonalVoiceModels.MAI_VOICE_1.toString());
    }
}
