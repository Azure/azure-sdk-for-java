// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.textanalytics.implementation.models;

import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link DetectLanguageResultImpl} model.
 */
@Immutable
public final class DetectLanguageResultImpl extends DocumentResultImpl implements DetectLanguageResult {
    private final DetectedLanguage primaryLanguage;

    /**
     * Create a {@link DetectLanguageResultImpl} model that describes detected languages result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param primaryLanguage The detected primary language.
     * @param warnings A {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public DetectLanguageResultImpl(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, DetectedLanguage primaryLanguage, IterableStream<TextAnalyticsWarning> warnings) {
        super(id, textDocumentStatistics, error, warnings);
        this.primaryLanguage = primaryLanguage;
    }

    /**
     * Get the detected primary language.
     *
     * @return The detected language.
     */
    public DetectedLanguage getPrimaryLanguage() {
        throwExceptionIfError();
        return primaryLanguage;
    }
}
