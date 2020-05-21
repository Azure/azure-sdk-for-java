// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.PatternCaptureTokenFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.PatternCaptureTokenFilter} and
 * {@link PatternCaptureTokenFilter}.
 */
public final class PatternCaptureTokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(PatternCaptureTokenFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.PatternCaptureTokenFilter} to
     * {@link PatternCaptureTokenFilter}.
     */
    public static PatternCaptureTokenFilter map(com.azure.search.documents.implementation.models.PatternCaptureTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        PatternCaptureTokenFilter patternCaptureTokenFilter = new PatternCaptureTokenFilter();

        String _name = obj.getName();
        patternCaptureTokenFilter.setName(_name);

        if (obj.getPatterns() != null) {
            List<String> _patterns = new ArrayList<>(obj.getPatterns());
            patternCaptureTokenFilter.setPatterns(_patterns);
        }

        Boolean _preserveOriginal = obj.isPreserveOriginal();
        patternCaptureTokenFilter.setPreserveOriginal(_preserveOriginal);
        return patternCaptureTokenFilter;
    }

    /**
     * Maps from {@link PatternCaptureTokenFilter} to
     * {@link com.azure.search.documents.implementation.models.PatternCaptureTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.PatternCaptureTokenFilter map(PatternCaptureTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.PatternCaptureTokenFilter patternCaptureTokenFilter =
            new com.azure.search.documents.implementation.models.PatternCaptureTokenFilter();

        String _name = obj.getName();
        patternCaptureTokenFilter.setName(_name);

        if (obj.getPatterns() != null) {
            List<String> _patterns = new ArrayList<>(obj.getPatterns());
            patternCaptureTokenFilter.setPatterns(_patterns);
        }

        Boolean _preserveOriginal = obj.isPreserveOriginal();
        patternCaptureTokenFilter.setPreserveOriginal(_preserveOriginal);
        return patternCaptureTokenFilter;
    }
}
