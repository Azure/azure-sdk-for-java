// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The NamedEntityResult model.
 */
@Immutable
public final class NamedEntityResult extends DocumentResult {
    private final List<NamedEntity> namedEntities;

    /**
     * Creates a {@code NamedEntityResult} model that describes recognized entities result
     *
     * @param id unique, non-empty document identifier
     * @param textDocumentStatistics text document statistics
     * @param error the document error
     * @param namedEntities a list of {@link NamedEntity}
     */
    public NamedEntityResult(String id, TextDocumentStatistics textDocumentStatistics, Error error,
                             List<NamedEntity> namedEntities) {
        super(id, textDocumentStatistics, error);
        this.namedEntities = namedEntities;
    }

    /**
     * Get a list of named entities string
     *
     * @return a list of {@link NamedEntity}
     */
    public List<NamedEntity> getNamedEntities() {
        return namedEntities;
    }
}
