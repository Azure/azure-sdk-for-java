// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The NamedEntityResult model.
 */
@Fluent
public final class NamedEntityResult extends DocumentResult {
    private List<NamedEntity> namedEntities;

    public NamedEntityResult(String id, TextDocumentStatistics textDocumentStatistics,
                             List<NamedEntity> namedEntities) {
        super(id, textDocumentStatistics);
        this.namedEntities = namedEntities;
    }

    public List<NamedEntity> getNamedEntities() {
        return namedEntities;
    }

    NamedEntityResult setNamedEntities(List<NamedEntity> namedEntities) {
        this.namedEntities = namedEntities;
        return this;
    }
}
