// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The NamedEntityResult model.
 */
@Fluent
public final class NamedEntityResult extends DocumentResult {
    private List<NamedEntity> namedEntities;

    public NamedEntityResult(String id, TextDocumentStatistics textDocumentStatistics, DocumentError error, List<NamedEntity> namedEntities) {
        super(id, textDocumentStatistics, error);
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
