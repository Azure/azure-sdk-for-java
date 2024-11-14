// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Specifies the media kind for out streaming data.
 */
public final class MediaKind extends ExpandableStringEnum<MediaKind> {
    /**
     * AudioData.
     */
    public static final MediaKind AUDIO_DATA = fromString("AudioData");

    /**
     * StopAudio.
     */
    public static final MediaKind STOP_AUDIO = fromString("StopAudio");

    /**
     * Creates a new instance of MediaKind value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public MediaKind() {
    }

    /**
     * Creates or finds a MediaKind from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding MediaKind.
     */
    public static MediaKind fromString(String name) {
        return fromString(name, MediaKind.class);
    }

    /**
     * Gets known MediaKind values.
     * 
     * @return known MediaKind values.
     */
    public static Collection<MediaKind> values() {
        return values(MediaKind.class);
    }
}
