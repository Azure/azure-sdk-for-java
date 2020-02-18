// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.ArrayList;
import java.util.List;

/**
 * The RecognizeEntitiesResult model.
 */
@Immutable
public final class RecognizeEntitiesResult extends DocumentResult {
    private final List<CategorizedEntity> entities;

    /**
     * Creates a {@code RecognizeEntitiesResult} model that describes recognized entities result.
     *
     * @param id unique, non-empty document identifier
     * @param textDocumentStatistics text document statistics
     * @param error the document error
     * @param entities a list of {@link CategorizedEntity}
     */
    public RecognizeEntitiesResult(String id, TextDocumentStatistics textDocumentStatistics, TextAnalyticsError error,
        List<CategorizedEntity> entities) {
        super(id, textDocumentStatistics, error);
        this.entities = entities == null ? new ArrayList<>() : entities;
    }

    /**
     * Get a list of categorized entities string.
     *
     * @return a list of {@link CategorizedEntity}
     */
    public List<CategorizedEntity> getEntities() {
        throwExceptionIfError();
        return entities;
    }
}
