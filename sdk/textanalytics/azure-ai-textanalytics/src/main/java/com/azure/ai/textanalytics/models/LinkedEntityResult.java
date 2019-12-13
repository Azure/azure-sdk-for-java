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
    private List<LinkedEntity> linkedEntities;

    // TODO(shawn): not public modifier
    public LinkedEntityResult(String id, Error error, boolean isError) {
        super(id, error, isError);
    }

    // TODO(shawn): not public modifier

    /**
     * LinkedEntityResult model constructor
     *
     * @param id document id
     * @param textDocumentStatistics text document statistics
     * @param linkedEntities a list of linked entities
     */
    public LinkedEntityResult(String id, TextDocumentStatistics textDocumentStatistics,
                              List<LinkedEntity> linkedEntities) {
        super(id, textDocumentStatistics);
        this.linkedEntities = linkedEntities;
    }

    /**
     * Get a list of linked entities
     * @return a list of linked entities.
     */
    public List<LinkedEntity> getLinkedEntities() {
        return linkedEntities;
    }
}
