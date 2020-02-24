// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link RecognizePiiEntitiesResult} model.
 */
@Immutable
public final class RecognizePiiEntitiesResult extends DocumentResult {
    private final List<PiiEntity> entities;

    /**
     * Creates a {@link RecognizePiiEntitiesResult} model that describes recognized entities result.
     *
     * @param id Unique, non-empty document identifier.
     * @param inputText The input text in request.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param entities A list of {@link PiiEntity}.
     */
    public RecognizePiiEntitiesResult(String id, String inputText, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, List<PiiEntity> entities) {
        super(id, inputText, textDocumentStatistics, error);
        this.entities = entities == null ? new ArrayList<>() : entities;
    }

    /**
     * Get a list of Personally Identifiable Information entities.
     *
     * @return a list of {@link PiiEntity}
     */
    public List<PiiEntity> getEntities() {
        throwExceptionIfError();
        return entities;
    }
}
