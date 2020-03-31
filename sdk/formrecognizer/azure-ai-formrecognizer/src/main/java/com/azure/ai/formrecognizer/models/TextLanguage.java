// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Defines values for Language.
 */
public final class TextLanguage extends ExpandableStringEnum<TextLanguage> {
    /**
     * Static value en for Language.
     */
    public static final TextLanguage EN = fromString("en");

    /**
     * Static value es for Language.
     */
    public static final TextLanguage ES = fromString("es");

    /**
     * Creates or finds a Language from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding Language.
     */
    @JsonCreator
    public static TextLanguage fromString(String name) {
        return fromString(name, TextLanguage.class);
    }

    /**
     * @return known Language values.
     */
    public static Collection<TextLanguage> values() {
        return values(TextLanguage.class);
    }
}
