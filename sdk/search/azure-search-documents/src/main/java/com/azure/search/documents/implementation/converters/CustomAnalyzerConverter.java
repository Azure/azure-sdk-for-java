// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.CustomAnalyzer;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.CustomAnalyzer} and
 * {@link CustomAnalyzer}.
 */
public final class CustomAnalyzerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.CustomAnalyzer} to {@link CustomAnalyzer}.
     */
    public static CustomAnalyzer map(com.azure.search.documents.indexes.implementation.models.CustomAnalyzer obj) {
        if (obj == null) {
            return null;
        }

        CustomAnalyzer customAnalyzer = new CustomAnalyzer(obj.getName(), obj.getTokenizer());

        if (obj.getCharFilters() != null) {
            customAnalyzer.setCharFilters(obj.getCharFilters());
        }

        if (obj.getTokenFilters() != null) {
            customAnalyzer.setTokenFilters(obj.getTokenFilters());
        }

        return customAnalyzer;
    }

    /**
     * Maps from {@link CustomAnalyzer} to
     * {@link com.azure.search.documents.indexes.implementation.models.CustomAnalyzer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.CustomAnalyzer map(CustomAnalyzer obj) {
        if (obj == null) {
            return null;
        }

        com.azure.search.documents.indexes.implementation.models.CustomAnalyzer customAnalyzer =
            new com.azure.search.documents.indexes.implementation.models.CustomAnalyzer(obj.getName(),
                obj.getTokenizer());

        if (obj.getCharFilters() != null) {
            customAnalyzer.setCharFilters(obj.getCharFilters());
        }

        if (obj.getTokenFilters() != null) {
            customAnalyzer.setTokenFilters(obj.getTokenFilters());
        }

        return customAnalyzer;
    }

    private CustomAnalyzerConverter() {
    }
}
