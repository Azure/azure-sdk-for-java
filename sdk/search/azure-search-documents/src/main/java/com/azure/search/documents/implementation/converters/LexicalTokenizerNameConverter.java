// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.LexicalTokenizerName;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.LexicalTokenizerName} and
 * {@link LexicalTokenizerName}.
 */
public final class LexicalTokenizerNameConverter {
    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.LexicalTokenizerName} to enum
     * {@link LexicalTokenizerName}.
     */
    public static LexicalTokenizerName map(com.azure.search.documents.indexes.implementation.models.LexicalTokenizerName obj) {
        if (obj == null) {
            return null;
        }
        return LexicalTokenizerName.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link LexicalTokenizerName} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.LexicalTokenizerName}.
     */
    public static com.azure.search.documents.indexes.implementation.models.LexicalTokenizerName map(LexicalTokenizerName obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.indexes.implementation.models.LexicalTokenizerName.fromString(obj.toString());
    }

    private LexicalTokenizerNameConverter() {
    }
}
