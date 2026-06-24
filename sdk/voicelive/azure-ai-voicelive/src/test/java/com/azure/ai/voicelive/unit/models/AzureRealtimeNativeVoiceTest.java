// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.unit.models;

import com.azure.ai.voicelive.models.AzureRealtimeNativeVoice;
import com.azure.ai.voicelive.models.AzureRealtimeNativeVoiceName;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link AzureRealtimeNativeVoice} and {@link AzureRealtimeNativeVoiceName}.
 */
class AzureRealtimeNativeVoiceTest {

    @Test
    void testAzureRealtimeNativeVoiceRoundTrip() {
        AzureRealtimeNativeVoice voice = new AzureRealtimeNativeVoice(AzureRealtimeNativeVoiceName.AVA);

        assertEquals("azure-realtime-native", voice.getType());
        assertEquals(AzureRealtimeNativeVoiceName.AVA, voice.getName());

        AzureRealtimeNativeVoice deserialized = BinaryData.fromObject(voice).toObject(AzureRealtimeNativeVoice.class);
        assertEquals("azure-realtime-native", deserialized.getType());
        assertEquals(AzureRealtimeNativeVoiceName.AVA, deserialized.getName());
    }

    @Test
    void testAzureRealtimeNativeVoiceNameKnownValues() {
        // Spot-check a few known voice names round-trip through fromString.
        assertSame(AzureRealtimeNativeVoiceName.ANDREW, AzureRealtimeNativeVoiceName.fromString("andrew"));
        assertSame(AzureRealtimeNativeVoiceName.XIAOXIAO, AzureRealtimeNativeVoiceName.fromString("xiaoxiao"));
        assertSame(AzureRealtimeNativeVoiceName.YUNXI, AzureRealtimeNativeVoiceName.fromString("yunxi"));
    }
}
