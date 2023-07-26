// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.KeepTokenFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.KeepTokenFilter} and
 * {@link KeepTokenFilter}.
 */
public final class KeepTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.KeepTokenFilter} to {@link KeepTokenFilter}.
     */
    public static KeepTokenFilter map(com.azure.search.documents.indexes.implementation.models.KeepTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        List<String> keepWords = new ArrayList<>(obj.getKeepWords());
        KeepTokenFilter keepTokenFilter = new KeepTokenFilter(obj.getName(), keepWords);

        Boolean lowerCaseKeepWords = obj.isLowerCaseKeepWords();
        keepTokenFilter.setLowerCaseKeepWords(lowerCaseKeepWords);
        return keepTokenFilter;
    }

    /**
     * Maps from {@link KeepTokenFilter} to {@link com.azure.search.documents.indexes.implementation.models.KeepTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.KeepTokenFilter map(KeepTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        List<String> keepWords = new ArrayList<>(obj.getKeepWords());
        com.azure.search.documents.indexes.implementation.models.KeepTokenFilter keepTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.KeepTokenFilter(obj.getName(), keepWords);

        Boolean lowerCaseKeepWords = obj.areLowerCaseKeepWords();
        keepTokenFilter.setLowerCaseKeepWords(lowerCaseKeepWords);

        return keepTokenFilter;
    }

    private KeepTokenFilterConverter() {
    }
}
