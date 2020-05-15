package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.TextSplitMode;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.TextSplitMode} and
 * {@link TextSplitMode} mismatch.
 */
public final class TextSplitModeConverter {
    public static TextSplitMode convert(com.azure.search.documents.models.TextSplitMode obj) {
        return DefaultConverter.convert(obj, TextSplitMode.class);
    }

    public static com.azure.search.documents.models.TextSplitMode convert(TextSplitMode obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.TextSplitMode.class);
    }
}
