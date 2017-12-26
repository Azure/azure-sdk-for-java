/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.customsearch;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for TextFormat.
 */
public final class TextFormat extends ExpandableStringEnum<TextFormat> {
    /** Static value Raw for TextFormat. */
    public static final TextFormat RAW = fromString("Raw");

    /** Static value Html for TextFormat. */
    public static final TextFormat HTML = fromString("Html");

    /**
     * Creates or finds a TextFormat from its string representation.
     * @param name a name to look for
     * @return the corresponding TextFormat
     */
    @JsonCreator
    public static TextFormat fromString(String name) {
        return fromString(name, TextFormat.class);
    }

    /**
     * @return known TextFormat values
     */
    public static Collection<TextFormat> values() {
        return values(TextFormat.class);
    }
}
