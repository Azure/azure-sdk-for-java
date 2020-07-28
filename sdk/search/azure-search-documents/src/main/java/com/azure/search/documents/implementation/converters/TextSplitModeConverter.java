// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.TextSplitMode;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.TextSplitMode} and {@link TextSplitMode}.
 */
public final class TextSplitModeConverter {
    private static final ClientLogger LOGGER = new ClientLogger(TextSplitModeConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.TextSplitMode} to enum
     * {@link TextSplitMode}.
     */
    public static TextSplitMode map(com.azure.search.documents.indexes.implementation.models.TextSplitMode obj) {
        if (obj == null) {
            return null;
        }
        return TextSplitMode.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link TextSplitMode} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.TextSplitMode}.
     */
    public static com.azure.search.documents.indexes.implementation.models.TextSplitMode map(TextSplitMode obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.indexes.implementation.models.TextSplitMode.fromString(obj.toString());
    }

    private TextSplitModeConverter() {
    }
}
