package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.CharFilterName;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.CharFilterName} and
 * {@link CharFilterName}.
 */
public final class CharFilterNameConverter {
    private static final ClientLogger LOGGER = new ClientLogger(CharFilterNameConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.implementation.models.CharFilterName} to enum
     * {@link CharFilterName}.
     */
    public static CharFilterName map(com.azure.search.documents.implementation.models.CharFilterName obj) {
        if (obj == null) {
            return null;
        }
        return CharFilterName.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link CharFilterName} to enum
     * {@link com.azure.search.documents.implementation.models.CharFilterName}.
     */
    public static com.azure.search.documents.implementation.models.CharFilterName map(CharFilterName obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.implementation.models.CharFilterName.fromString(obj.toString());
    }
}
