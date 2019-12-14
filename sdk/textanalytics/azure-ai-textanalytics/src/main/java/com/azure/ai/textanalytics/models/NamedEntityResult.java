// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The NamedEntityResult model.
 */
// TODO (shawn): Should be @Immutable, but will produce spotbug/checkstyle error
@Fluent
public final class NamedEntityResult extends DocumentResult {
    private final List<NamedEntity> namedEntities;

    public NamedEntityResult(String id, TextDocumentStatistics textDocumentStatistics, Error error,
                             List<NamedEntity> namedEntities) {
        super(id, textDocumentStatistics, error);
        this.namedEntities = namedEntities;
    }

    public List<NamedEntity> getNamedEntities() {
        return namedEntities;
    }
}
