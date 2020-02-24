// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link RecognizeEntitiesResult} model.
 */
@Immutable
public final class RecognizeEntitiesResult extends DocumentResult {
    private final List<CategorizedEntity> entities;

    /**
     * Creates a {@link RecognizeEntitiesResult} model that describes recognized entities result.
     *
     * @param id Unique, non-empty document identifier.
     * @param inputText The input text in request.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param entities A list of {@link CategorizedEntity}.
     */
    public RecognizeEntitiesResult(String id, String inputText, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, List<CategorizedEntity> entities) {
        super(id, inputText, textDocumentStatistics, error);
        this.entities = entities == null ? new ArrayList<>() : entities;
    }

    /**
     * Get a list of categorized entities string.
     *
     * @return A list of {@link CategorizedEntity}.
     */
    public List<CategorizedEntity> getEntities() {
        throwExceptionIfError();
        return entities;
    }
}
