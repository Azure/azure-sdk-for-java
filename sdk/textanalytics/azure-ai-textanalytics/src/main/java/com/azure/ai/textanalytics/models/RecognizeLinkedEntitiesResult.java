// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.ArrayList;
import java.util.List;

/**
 * The RecognizeLinkedEntitiesResult model.
 */
@Immutable
public final class RecognizeLinkedEntitiesResult extends DocumentResult {
    private final List<LinkedEntity> entities;

    /**
     * Creates a {@code RecognizeLinkedEntitiesResult} model that describes recognized linked entities result.
     *
     * @param id unique, non-empty document identifier
     * @param textDocumentStatistics text document statistics
     * @param error the document error
     * @param entities a list of linked entities
     */
    public RecognizeLinkedEntitiesResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, List<LinkedEntity> entities) {
        super(id, textDocumentStatistics, error);
        this.entities = entities == null ? new ArrayList<>() : entities;
    }

    /**
     * Get a list of linked entities.
     *
     * @return a list of linked entities.
     */
    public List<LinkedEntity> getEntities() {
        throwExceptionIfError();
        return entities;
    }
}
