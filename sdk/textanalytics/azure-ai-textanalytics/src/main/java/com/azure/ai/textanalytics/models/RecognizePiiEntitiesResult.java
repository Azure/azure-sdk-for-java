// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;

/**
 * The {@link RecognizePiiEntitiesResult} model.
 */
@Immutable
public final class RecognizePiiEntitiesResult extends DocumentResult {
    private final IterableStream<PiiEntity> entities;

    /**
     * Creates a {@link RecognizePiiEntitiesResult} model that describes recognized entities result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param entities An {@link IterableStream} of {@link PiiEntity}.

     */
    public RecognizePiiEntitiesResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, IterableStream<PiiEntity> entities) {
        super(id, textDocumentStatistics, error);
        this.entities = entities == null ? new IterableStream<>(new ArrayList<>()) : entities;
    }

    /**
     * Get an {@link IterableStream} of {@link PiiEntity}.
     *
     * @return An {@link IterableStream} of {@link PiiEntity}.
     */
    public IterableStream<PiiEntity> getEntities() {
        throwExceptionIfError();
        return entities;
    }
}
