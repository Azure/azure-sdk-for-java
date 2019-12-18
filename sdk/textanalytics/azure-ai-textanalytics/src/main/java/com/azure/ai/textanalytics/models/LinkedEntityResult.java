// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The LinkedEntityResult model.
 */
@Immutable
public final class LinkedEntityResult extends DocumentResult {
    private final List<LinkedEntity> linkedEntities;

    /**
     * Creates a {@code LinkedEntityResult} model that describes recognized linked entities result
     *
     * @param id unique, non-empty document identifier
     * @param textDocumentStatistics text document statistics
     * @param error the document error
     * @param linkedEntities a list of linked entities
     */
    public LinkedEntityResult(String id, TextDocumentStatistics textDocumentStatistics, Error error,
                              List<LinkedEntity> linkedEntities) {
        super(id, textDocumentStatistics, error);
        this.linkedEntities = linkedEntities;
    }

    /**
     * Get a list of linked entities
     *
     * @return a list of linked entities.
     */
    public List<LinkedEntity> getLinkedEntities() {
        return linkedEntities;
    }
}
