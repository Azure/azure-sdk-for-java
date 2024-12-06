// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Specifies the text format of transcription.
 */
public final class Channels extends ExpandableStringEnum<Channels> {
    /**
     * Display.
     */
    public static final Channels MONO = fromString("mono");

    /**
     * Creates a new instance of Channels value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public Channels() {
    }

    /**
     * Creates or finds a Channels from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding Channels.
     */
    public static Channels fromString(String name) {
        return fromString(name, Channels.class);
    }

    /**
     * Gets known Channels values.
     * 
     * @return known Channels values.
     */
    public static Collection<Channels> values() {
        return values(Channels.class);
    }
}
