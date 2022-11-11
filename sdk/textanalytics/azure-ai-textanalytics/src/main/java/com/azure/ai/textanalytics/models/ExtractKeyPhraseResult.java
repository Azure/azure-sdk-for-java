// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ExtractKeyPhraseResultPropertiesHelper;
import com.azure.core.annotation.Immutable;

/**
 * The {@link ExtractKeyPhraseResult} model.
 */
@Immutable
public final class ExtractKeyPhraseResult extends TextAnalyticsResult {
    private final KeyPhrasesCollection keyPhrases;

    private DetectedLanguage detectedLanguage;

    static {
        ExtractKeyPhraseResultPropertiesHelper.setAccessor(
            (documentResult, detectedLanguage) -> documentResult.setDetectedLanguage(detectedLanguage));
    }

    /**
     * Creates a {@link ExtractKeyPhraseResult} model that describes extracted key phrases result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param keyPhrases A {@link KeyPhrasesCollection} contains a list of key phrases and warnings.
     */
    public ExtractKeyPhraseResult(String id, TextDocumentStatistics textDocumentStatistics,
                                  TextAnalyticsError error, KeyPhrasesCollection keyPhrases) {
        super(id, textDocumentStatistics, error);
        this.keyPhrases = keyPhrases;
    }

    /**
     * Gets a {@link KeyPhrasesCollection} contains a list of key phrases and warnings.
     *
     * @return A {@link KeyPhrasesCollection} contains a list of key phrases and warnings.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public KeyPhrasesCollection getKeyPhrases() {
        throwExceptionIfError();
        return keyPhrases;
    }


    /**
     * Get the detectedLanguage property: If 'language' is set to 'auto' for the document in the request this field will
     * contain an object of the language detected for this document.
     *
     * @return the detectedLanguage value.
     */
    public DetectedLanguage getDetectedLanguage() {
        return this.detectedLanguage;
    }

    private void setDetectedLanguage(DetectedLanguage detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
    }
}
