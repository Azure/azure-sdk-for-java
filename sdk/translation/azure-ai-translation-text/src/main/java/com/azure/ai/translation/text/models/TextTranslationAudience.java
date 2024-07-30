// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for TextTranslationAudience. */
@Immutable
public final class TextTranslationAudience extends ExpandableStringEnum<TextTranslationAudience> {

    /**
     * Constructs a TextTranslationAudience object.
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public TextTranslationAudience() {
    }

    /** Static value AZURE_RESOURCE_MANAGER_CHINA for TextTranslationAudience. */
    public static final TextTranslationAudience AZURE_CHINA = fromString("https://cognitiveservices.azure.cn");

    /** Static value AZURE_RESOURCE_MANAGER_GOVERNMENT for TextTranslationAudience. */
    public static final TextTranslationAudience AZURE_GOVERNMENT = fromString("https://cognitiveservices.azure.us");

    /** Static value AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD for TextTranslationAudience. */
    public static final TextTranslationAudience AZURE_PUBLIC_CLOUD = fromString("https://cognitiveservices.azure.com");

    /**
     * Creates or finds a TextTranslationAudience from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding TextTranslationAudience.
     */
    public static TextTranslationAudience fromString(String name) {
        return fromString(name, TextTranslationAudience.class);
    }

    /**
     * Returns known TextTranslationAudience values.
     * @return known TextTranslationAudience values.
     */
    public static Collection<TextTranslationAudience> values() {
        return values(TextTranslationAudience.class);
    }
}
