// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Defines values for TextContentType.
 */
public final class TextContentType extends ExpandableStringEnum<TextContentType> {
    /**
     * Static value word for TextContentType.
     */
    public static final TextContentType WORD = fromString("word");

    /**
     * Static value line for TextContentType.
     */
    public static final TextContentType LINE = fromString("line");

    /**
     * Parses a serialized value to a {@code TextContentType} instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed TextContentType object, or null if unable to parse.
     */
    public static TextContentType fromString(String value) {
        return fromString(value, TextContentType.class);
    }
}
