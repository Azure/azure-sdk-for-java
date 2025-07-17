// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.util.Collection;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Specifies the text format of transcription.
 */
public final class AudioChannelType extends ExpandableStringEnum<AudioChannelType> {
    /**
     * Display.
     * Audio channel type.
     */
    public static final AudioChannelType MONO = fromString("mono");

    /**
     * Display.
     * Unknown Audio channel type.
     */
    public static final AudioChannelType UNKNOWN = fromString("unknown");

    /**
     * Creates a new instance of Channels value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public AudioChannelType() {
    }

    /**
     * Creates or finds a Channels from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding Channels.
     */
    public static AudioChannelType fromString(String name) {
        return fromString(name, AudioChannelType.class);
    }

    /**
     * Gets known Channels values.
     * 
     * @return known Channels values.
     */
    public static Collection<AudioChannelType> values() {
        return values(AudioChannelType.class);
    }
}
