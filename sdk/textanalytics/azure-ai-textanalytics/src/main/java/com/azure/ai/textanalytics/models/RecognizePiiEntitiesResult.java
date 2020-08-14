// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link RecognizePiiEntitiesResult} model.
 */
@Immutable
public final class RecognizePiiEntitiesResult extends TextAnalyticsResult {
    private final PiiEntityCollection entities;

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
     * Get an {@link IterableStream} of {@link PiiEntity}.
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
}
