// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.RecognizePiiEntitiesResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link RecognizePiiEntitiesResult} model.
 */
@Immutable
public final class RecognizePiiEntitiesResult extends TextAnalyticsResult {
    private final PiiEntityCollection entities;

    private DetectedLanguage detectedLanguage;

    static {
        RecognizePiiEntitiesResultPropertiesHelper.setAccessor(
            (documentResult, detectedLanguage) -> documentResult.setDetectedLanguage(detectedLanguage));
    }

    /**
     * Creates a {@link RecognizePiiEntitiesResult} model that describes recognized PII entities result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param entities A {@link PiiEntityCollection} contains entities and warnings.
     */
    public RecognizePiiEntitiesResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, PiiEntityCollection entities) {
        super(id, textDocumentStatistics, error);
        this.entities = entities;
    }

    /**
     * Gets an {@link IterableStream} of {@link PiiEntity}.
     *
     * @return An {@link IterableStream} of {@link PiiEntity}.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public PiiEntityCollection getEntities() {
        throwExceptionIfError();
        return entities;
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
