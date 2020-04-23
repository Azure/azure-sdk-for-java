// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;

/**
 * The {@link RecognizeCategorizedEntitiesResult} model.
 */
@Immutable
public final class RecognizeCategorizedEntitiesResult extends DocumentResult {
    private final IterableStream<CategorizedEntity> entities;

    /**
     * Creates a {@link RecognizeCategorizedEntitiesResult} model that describes recognized entities result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param entities An {@link IterableStream} of {@link CategorizedEntity}.
     */
    public RecognizeCategorizedEntitiesResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, IterableStream<CategorizedEntity> entities) {
        super(id, textDocumentStatistics, error);
        this.entities = entities == null ? new IterableStream<>(new ArrayList<>()) : entities;
    }

    /**
     * Get an {@link IterableStream} of {@link CategorizedEntity}.
     *
     * @return An {@link IterableStream} of {@link CategorizedEntity}.
     */
    public IterableStream<CategorizedEntity> getEntities() {
        throwExceptionIfError();
        return entities;
    }
}
