// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.StopAnalyzer;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.StopAnalyzer} and {@link StopAnalyzer}.
 */
public final class StopAnalyzerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.StopAnalyzer} to {@link StopAnalyzer}.
     */
    public static StopAnalyzer map(com.azure.search.documents.indexes.implementation.models.StopAnalyzer obj) {
        if (obj == null) {
            return null;
        }
        StopAnalyzer stopAnalyzer = new StopAnalyzer(obj.getName());

        if (obj.getStopwords() != null) {
            stopAnalyzer.setStopwords(obj.getStopwords());
        }
        return stopAnalyzer;
    }

    /**
     * Maps from {@link StopAnalyzer} to {@link com.azure.search.documents.indexes.implementation.models.StopAnalyzer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.StopAnalyzer map(StopAnalyzer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.StopAnalyzer stopAnalyzer =
            new com.azure.search.documents.indexes.implementation.models.StopAnalyzer(obj.getName());

        if (obj.getStopwords() != null) {
            List<String> stopwords = new ArrayList<>(obj.getStopwords());
            stopAnalyzer.setStopwords(stopwords);
        }

        return stopAnalyzer;
    }

    private StopAnalyzerConverter() {
    }
}
