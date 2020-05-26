// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.models.CustomAnalyzer;
import com.azure.search.documents.implementation.models.LuceneStandardAnalyzer;
import com.azure.search.documents.implementation.models.PatternAnalyzer;
import com.azure.search.documents.implementation.models.StopAnalyzer;
import com.azure.search.documents.models.LexicalAnalyzer;

import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.LexicalAnalyzer} and
 * {@link LexicalAnalyzer}.
 */
public final class LexicalAnalyzerConverter {
    private static final ClientLogger LOGGER = new ClientLogger(LexicalAnalyzerConverter.class);

    /**
     * Maps abstract class from {@link com.azure.search.documents.implementation.models.LexicalAnalyzer} to
     * {@link LexicalAnalyzer}. Dedicate works to sub class converter.
     */
    public static LexicalAnalyzer map(com.azure.search.documents.implementation.models.LexicalAnalyzer obj) {
        if (obj instanceof LuceneStandardAnalyzer) {
            return LuceneStandardAnalyzerConverter.map((LuceneStandardAnalyzer) obj);
        }
        if (obj instanceof PatternAnalyzer) {
            return PatternAnalyzerConverter.map((PatternAnalyzer) obj);
        }
        if (obj instanceof CustomAnalyzer) {
            return CustomAnalyzerConverter.map((CustomAnalyzer) obj);
        }
        if (obj instanceof StopAnalyzer) {
            return StopAnalyzerConverter.map((StopAnalyzer) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_EXTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    /**
     * Maps abstract class from {@link LexicalAnalyzer} to
     * {@link com.azure.search.documents.implementation.models.LexicalAnalyzer}. Dedicate works to sub class converter.
     */
    public static com.azure.search.documents.implementation.models.LexicalAnalyzer map(LexicalAnalyzer obj) {
        if (obj instanceof com.azure.search.documents.models.CustomAnalyzer) {
            return CustomAnalyzerConverter.map((com.azure.search.documents.models.CustomAnalyzer) obj);
        }
        if (obj instanceof com.azure.search.documents.models.LuceneStandardAnalyzer) {
            return LuceneStandardAnalyzerConverter.map((com.azure.search.documents.models.LuceneStandardAnalyzer) obj);
        }
        if (obj instanceof com.azure.search.documents.models.PatternAnalyzer) {
            return PatternAnalyzerConverter.map((com.azure.search.documents.models.PatternAnalyzer) obj);
        }
        if (obj instanceof com.azure.search.documents.models.StopAnalyzer) {
            return StopAnalyzerConverter.map((com.azure.search.documents.models.StopAnalyzer) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_INTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    private LexicalAnalyzerConverter() {
    }
}
