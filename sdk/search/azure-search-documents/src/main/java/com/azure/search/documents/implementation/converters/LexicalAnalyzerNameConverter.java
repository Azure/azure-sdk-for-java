// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.LexicalAnalyzerName;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.LexicalAnalyzerName} and
 * {@link LexicalAnalyzerName}.
 */
public final class LexicalAnalyzerNameConverter {
    /**
     * Maps from enum {@link com.azure.search.documents.implementation.models.LexicalAnalyzerName} to enum
     * {@link LexicalAnalyzerName}.
     */
    public static LexicalAnalyzerName map(com.azure.search.documents.implementation.models.LexicalAnalyzerName obj) {
        if (obj == null) {
            return null;
        }
        return LexicalAnalyzerName.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link LexicalAnalyzerName} to enum
     * {@link com.azure.search.documents.implementation.models.LexicalAnalyzerName}.
     */
    public static com.azure.search.documents.implementation.models.LexicalAnalyzerName map(LexicalAnalyzerName obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.implementation.models.LexicalAnalyzerName.fromString(obj.toString());
    }

    private LexicalAnalyzerNameConverter() {
    }
}
