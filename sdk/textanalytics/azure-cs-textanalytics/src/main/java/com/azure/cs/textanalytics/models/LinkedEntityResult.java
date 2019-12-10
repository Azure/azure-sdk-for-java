// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.List;


/**
 * The LinkedEntityResult model.
 */
@Fluent
public final class LinkedEntityResult extends DocumentResult {
    private List<LinkedEntity> linkedEntities;

    public LinkedEntityResult(String id, TextDocumentStatistics textDocumentStatistics,
                              List<LinkedEntity> linkedEntities) {
        super(id, textDocumentStatistics);
        this.linkedEntities = linkedEntities;
    }

    public List<LinkedEntity> getLinkedEntities() {
        return linkedEntities;
    }

    LinkedEntityResult setLinkedEntities(List<LinkedEntity> linkedEntities) {
        this.linkedEntities = linkedEntities;
        return this;
    }
}
