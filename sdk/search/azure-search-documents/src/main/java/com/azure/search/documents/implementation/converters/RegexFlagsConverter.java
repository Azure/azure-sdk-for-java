package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.RegexFlags;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.RegexFlags} and
 * {@link RegexFlags} mismatch.
 */
public final class RegexFlagsConverter {
    public static RegexFlags convert(com.azure.search.documents.models.RegexFlags obj) {
        return DefaultConverter.convert(obj, RegexFlags.class);
    }

    public static com.azure.search.documents.models.RegexFlags convert(RegexFlags obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.RegexFlags.class);
    }
}
