// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ClassifyDocumentResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link ClassifyDocumentResult} model. It classify the text document one single category.
 */
@Immutable
public final class ClassifyDocumentResult extends TextAnalyticsResult {
    private DetectedLanguage detectedLanguage;
    private IterableStream<ClassificationCategory> classifications;
    private IterableStream<TextAnalyticsWarning> warnings;

    static {
        ClassifyDocumentResultPropertiesHelper.setAccessor(
            new ClassifyDocumentResultPropertiesHelper.ClassifyDocumentResultAccessor() {
                @Override
                public void setClassifications(ClassifyDocumentResult classifyDocumentResult,
                    IterableStream<ClassificationCategory> classifications) {
                    classifyDocumentResult.setClassifications(classifications);
                }

                @Override
                public void setDetectedLanguage(ClassifyDocumentResult classifyDocumentResult,
                    DetectedLanguage detectedLanguage) {
                    classifyDocumentResult.setDetectedLanguage(detectedLanguage);
                }

                @Override
                public void setWarnings(ClassifyDocumentResult classifyDocumentResult,
                    IterableStream<TextAnalyticsWarning> warnings) {
                    classifyDocumentResult.setWarnings(warnings);
                }
            });

    }

    /**
     * Creates a {@link ClassifyDocumentResult} model.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public ClassifyDocumentResult(String id, TextDocumentStatistics textDocumentStatistics, TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
     * The document classification result which contains the classification category and the confidence score on it.
     *
     * @return {@link IterableStream} of {@link ClassificationCategory}.
     */
    public IterableStream<ClassificationCategory> getClassifications() {
        throwExceptionIfError();
        return classifications;
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


    /**
     * Gets the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return this.warnings;
    }

    private void setDetectedLanguage(DetectedLanguage detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
    }

    private void setClassifications(IterableStream<ClassificationCategory> classifications) {
        this.classifications = classifications;
    }

    private void setWarnings(IterableStream<TextAnalyticsWarning> warnings) {
        this.warnings = warnings;
    }
}
