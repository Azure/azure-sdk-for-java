// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.KeepTokenFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.KeepTokenFilter} and
 * {@link KeepTokenFilter}.
 */
public final class KeepTokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(KeepTokenFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.KeepTokenFilter} to {@link KeepTokenFilter}.
     */
    public static KeepTokenFilter map(com.azure.search.documents.implementation.models.KeepTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        KeepTokenFilter keepTokenFilter = new KeepTokenFilter();

        String _name = obj.getName();
        keepTokenFilter.setName(_name);

        if (obj.getKeepWords() != null) {
            List<String> _keepWords = new ArrayList<>(obj.getKeepWords());
            keepTokenFilter.setKeepWords(_keepWords);
        }

        Boolean _lowerCaseKeepWords = obj.isLowerCaseKeepWords();
        keepTokenFilter.setLowerCaseKeepWords(_lowerCaseKeepWords);
        return keepTokenFilter;
    }

    /**
     * Maps from {@link KeepTokenFilter} to {@link com.azure.search.documents.implementation.models.KeepTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.KeepTokenFilter map(KeepTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.KeepTokenFilter keepTokenFilter =
            new com.azure.search.documents.implementation.models.KeepTokenFilter();

        String _name = obj.getName();
        keepTokenFilter.setName(_name);

        if (obj.getKeepWords() != null) {
            List<String> _keepWords = new ArrayList<>(obj.getKeepWords());
            keepTokenFilter.setKeepWords(_keepWords);
        }

        Boolean _lowerCaseKeepWords = obj.isLowerCaseKeepWords();
        keepTokenFilter.setLowerCaseKeepWords(_lowerCaseKeepWords);
        return keepTokenFilter;
    }
}
