// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.unit.models;

import com.azure.ai.voicelive.models.AudioEchoCancellation;
import com.azure.ai.voicelive.models.EchoCancellationReferenceSource;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link AudioEchoCancellation} including the
 * {@link EchoCancellationReferenceSource} enum and the {@code channels} property.
 */
class AudioEchoCancellationTest {

    @Test
    void testEchoCancellationReferenceSourceValues() {
        assertEquals("server", EchoCancellationReferenceSource.SERVER.toString());
        assertEquals("client", EchoCancellationReferenceSource.CLIENT.toString());
    }

    @Test
    void testAudioEchoCancellationReferenceSourceAndChannelsRoundTrip() {
        AudioEchoCancellation ec
            = new AudioEchoCancellation().setReferenceSource(EchoCancellationReferenceSource.CLIENT).setChannels(2);

        AudioEchoCancellation deserialized = BinaryData.fromObject(ec).toObject(AudioEchoCancellation.class);

        assertEquals(EchoCancellationReferenceSource.CLIENT, deserialized.getReferenceSource());
        assertEquals(2, deserialized.getChannels());

        // Defaults round-trip back as null when not set.
        AudioEchoCancellation empty = new AudioEchoCancellation();
        AudioEchoCancellation emptyDeserialized = BinaryData.fromObject(empty).toObject(AudioEchoCancellation.class);
        assertNull(emptyDeserialized.getReferenceSource());
        assertNull(emptyDeserialized.getChannels());
    }
}
