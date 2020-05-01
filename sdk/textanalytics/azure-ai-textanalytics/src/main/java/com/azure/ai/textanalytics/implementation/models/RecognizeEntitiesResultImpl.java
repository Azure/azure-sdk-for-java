// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;

/**
 * The {@link RecognizeEntitiesResultImpl} model.
 */
@Immutable
public final class RecognizeEntitiesResultImpl extends TextAnalyticsResultImpl implements RecognizeEntitiesResult {
    private final IterableStream<CategorizedEntity> entities;

    /**
     * Creates a {@link RecognizeEntitiesResultImpl} model that describes recognized entities result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param entities An {@link IterableStream} of {@link CategorizedEntity}.
     * @param warnings A {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public RecognizeEntitiesResultImpl(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, IterableStream<CategorizedEntity> entities,
        IterableStream<TextAnalyticsWarning> warnings) {
        super(id, textDocumentStatistics, error, warnings);
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
