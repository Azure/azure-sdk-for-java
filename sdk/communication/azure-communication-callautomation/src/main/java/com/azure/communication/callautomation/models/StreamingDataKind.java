// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.util.Collection;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Specifies the streaming data kind
 */
public final class StreamingDataKind extends ExpandableStringEnum<StreamingDataKind> {
    /**
     * Streamind Data kind "audioData"
     */
    public static final StreamingDataKind AUDIO_DATA = fromString("audioData");

    /**
     * Streamind Data kind "audioMetadata".
     */
    public static final StreamingDataKind AUDIO_METADATA = fromString("audioMetadata");

    /**
     * Streamind Data kind "dtmfData"
     */
    public static final StreamingDataKind DTMF_DATA = fromString("dtmfData");

    /**
    * Streamind Data kind "transcriptionData".
    */
    public static final StreamingDataKind TRANSCRIPTION_DATA = fromString("transcriptionData");

    /**
    * Streamind Data kind "transcriptionMetadata".
    */
    public static final StreamingDataKind TRANSCRIPTION_METADATA = fromString("transcriptionMetadata");

    /**
     * Creates a new instance of StreamingDataKind value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public StreamingDataKind() {
    }

    /**
     * Creates or finds a StreamingDataKind from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding StreamingDataKind.
     */
    public static StreamingDataKind fromString(String name) {
        return fromString(name, StreamingDataKind.class);
    }

    /**
     * Gets known StreamingDataKind values.
     * 
     * @return known StreamingDataKind values.
     */
    public static Collection<StreamingDataKind> values() {
        return values(StreamingDataKind.class);
    }
}
