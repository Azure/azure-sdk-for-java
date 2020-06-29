// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.PatternCaptureTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.PatternCaptureTokenFilter} and
 * {@link PatternCaptureTokenFilter}.
 */
public final class PatternCaptureTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.PatternCaptureTokenFilter} to
     * {@link PatternCaptureTokenFilter}.
     */
    public static PatternCaptureTokenFilter map(com.azure.search.documents.indexes.implementation.models.PatternCaptureTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        PatternCaptureTokenFilter patternCaptureTokenFilter = new PatternCaptureTokenFilter(obj.getName(),
            obj.getPatterns());


        Boolean preserveOriginal = obj.isPreserveOriginal();
        patternCaptureTokenFilter.setPreserveOriginal(preserveOriginal);
        return patternCaptureTokenFilter;
    }

    /**
     * Maps from {@link PatternCaptureTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.PatternCaptureTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.PatternCaptureTokenFilter map(PatternCaptureTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.PatternCaptureTokenFilter patternCaptureTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.PatternCaptureTokenFilter(obj.getName(),
                obj.getPatterns());

        Boolean preserveOriginal = obj.isPreserveOriginal();
        patternCaptureTokenFilter.setPreserveOriginal(preserveOriginal);
        return patternCaptureTokenFilter;
    }

    private PatternCaptureTokenFilterConverter() {
    }
}
