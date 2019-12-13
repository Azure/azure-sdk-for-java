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

    // TODO(shawn): not public modifier
    public NamedEntityResult(String id, Error error, boolean isError) {
        super(id, error, isError);
        namedEntities = null;
    }

    // TODO(shawn): not public modifier
    public NamedEntityResult(String id, TextDocumentStatistics textDocumentStatistics,
                             List<NamedEntity> namedEntities) {
        super(id, textDocumentStatistics);
        this.namedEntities = namedEntities;
    }

    public List<NamedEntity> getNamedEntities() {
        return namedEntities;
    }
}
