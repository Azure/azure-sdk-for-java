// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;

/**
 * The {@link RecognizeEntitiesResult} model.
 */
@Immutable
public final class RecognizeEntitiesResult extends TextAnalyticsResult {
    private final CategorizedEntityCollection entities;

    /**
     * Creates a {@link RecognizeEntitiesResult} model that describes recognized entities result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param entities An {@link IterableStream} of {@link CategorizedEntity}.
     * @param warnings A {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public RecognizeEntitiesResult(String id, TextDocumentStatistics textDocumentStatistics,
                                   TextAnalyticsError error, IterableStream<CategorizedEntity> entities,
                                   IterableStream<TextAnalyticsWarning> warnings) {
        super(id, textDocumentStatistics, error);
        this.entities = new CategorizedEntityCollection(
            entities == null ? new IterableStream<>(new ArrayList<>()) : entities, warnings);
    }

    /**
     * Get an {@link IterableStream} of {@link CategorizedEntity}.
     *
     * @return An {@link IterableStream} of {@link CategorizedEntity}.
     */
    public CategorizedEntityCollection getEntities() {
        throwExceptionIfError();
        return entities;
    }
}
