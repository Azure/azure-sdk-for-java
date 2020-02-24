// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link RecognizeLinkedEntitiesResult} model.
 */
@Immutable
public final class RecognizeLinkedEntitiesResult extends DocumentResult {
    private final List<LinkedEntity> entities;

    /**
     * Creates a {@link RecognizeLinkedEntitiesResult} model that describes recognized linked entities result.
     *
     * @param id Unique, non-empty document identifier.
     * @param inputText The input text in request.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param entities A list of linked entities.
     */
    public RecognizeLinkedEntitiesResult(String id, String inputText, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, List<LinkedEntity> entities) {
        super(id, inputText, textDocumentStatistics, error);
        this.entities = entities == null ? new ArrayList<>() : entities;
    }

    /**
     * Get a list of linked entities.
     *
     * @return A list of linked entities.
     */
    public List<LinkedEntity> getEntities() {
        throwExceptionIfError();
        return entities;
    }
}
