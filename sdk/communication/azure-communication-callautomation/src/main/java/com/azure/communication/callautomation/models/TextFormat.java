// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Specifies the text format of transcription.
 */
public final class TextFormat extends ExpandableStringEnum<TextFormat> {
    /**
     * Display.
     */
    public static final TextFormat DISPLAY = fromString("Display");

    /**
     * Creates a new instance of TextFormat value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public TextFormat() {
    }

    /**
     * Creates or finds a TextFormat from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding TextFormat.
     */
    public static TextFormat fromString(String name) {
        return fromString(name, TextFormat.class);
    }

    /**
     * Gets known TextFormat values.
     * 
     * @return known TextFormat values.
     */
    public static Collection<TextFormat> values() {
        return values(TextFormat.class);
    }
}
