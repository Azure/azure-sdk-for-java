// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.StopAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.StopAnalyzer} and {@link StopAnalyzer}.
 */
public final class StopAnalyzerConverter {
    private static final ClientLogger LOGGER = new ClientLogger(StopAnalyzerConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.StopAnalyzer} to {@link StopAnalyzer}.
     */
    public static StopAnalyzer map(com.azure.search.documents.implementation.models.StopAnalyzer obj) {
        if (obj == null) {
            return null;
        }
        StopAnalyzer stopAnalyzer = new StopAnalyzer();

        String _name = obj.getName();
        stopAnalyzer.setName(_name);

        if (obj.getStopwords() != null) {
            List<String> _stopwords = new ArrayList<>(obj.getStopwords());
            stopAnalyzer.setStopwords(_stopwords);
        }
        return stopAnalyzer;
    }

    /**
     * Maps from {@link StopAnalyzer} to {@link com.azure.search.documents.implementation.models.StopAnalyzer}.
     */
    public static com.azure.search.documents.implementation.models.StopAnalyzer map(StopAnalyzer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.StopAnalyzer stopAnalyzer =
            new com.azure.search.documents.implementation.models.StopAnalyzer();

        String _name = obj.getName();
        stopAnalyzer.setName(_name);

        if (obj.getStopwords() != null) {
            List<String> _stopwords = new ArrayList<>(obj.getStopwords());
            stopAnalyzer.setStopwords(_stopwords);
        }
        return stopAnalyzer;
    }
}
