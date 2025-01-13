// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Specifies the audio format used for encoding, including sample rate and channel type.
 */
public final class AudioFormat extends ExpandableStringEnum<AudioFormat> {
    /**
     * Pcm16KMono.
     */
    public static final AudioFormat PCM_16K_MONO = fromString("Pcm16KMono");

    /**
     * Pcm24KMono.
     */
    public static final AudioFormat PCM_24K_MONO = fromString("Pcm24KMono");

    /**
     * Creates a new instance of AudioFormat value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public AudioFormat() {
    }

    /**
     * Creates or finds a AudioFormat from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding AudioFormat.
     */
    public static AudioFormat fromString(String name) {
        return fromString(name, AudioFormat.class);
    }

    /**
     * Gets known AudioFormat values.
     * 
     * @return known AudioFormat values.
     */
    public static Collection<AudioFormat> values() {
        return values(AudioFormat.class);
    }
}
