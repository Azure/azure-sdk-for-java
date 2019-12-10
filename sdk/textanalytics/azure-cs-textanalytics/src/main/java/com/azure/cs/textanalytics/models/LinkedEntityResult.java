// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;
import com.azure.cs.textanalytics.implementation.models.Error;

import java.util.List;


/**
 * The LinkedEntityResult model.
 */
@Fluent
public final class LinkedEntityResult extends DocumentResult {
    private List<LinkedEntity> linkedEntities;

<<<<<<< Updated upstream
    public LinkedEntityResult(String id, TextDocumentStatistics textDocumentStatistics,
                              List<LinkedEntity> linkedEntities) {
        super(id, textDocumentStatistics);
=======
    /**
     * LinkedEntityResult model constructor
     *
     * @param id document id
     * @param textDocumentStatistics text document statistics
     * @param error error model
     * @param linkedEntities a list of linked entities
     */
    public LinkedEntityResult(String id, TextDocumentStatistics textDocumentStatistics, Error error, List<LinkedEntity> linkedEntities) {
        super(id, textDocumentStatistics, error);
>>>>>>> Stashed changes
        this.linkedEntities = linkedEntities;
    }

    /**
     * Get a list of linked entities
     * @return a list of linked entities.
     */
    public List<LinkedEntity> getLinkedEntities() {
        return linkedEntities;
    }

    LinkedEntityResult setLinkedEntities(List<LinkedEntity> linkedEntities) {
        this.linkedEntities = linkedEntities;
        return this;
    }
}
