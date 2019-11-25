// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.IterableStream;

/**
 * The NamedEntityResult model.
 */
@Fluent
public final class NamedEntityResult extends DocumentResult {
    private IterableStream<NamedEntity> namedEntities;

    public IterableStream<NamedEntity> getNamedEntities() {
        return namedEntities;
    }

    NamedEntityResult setNamedEntities(IterableStream<NamedEntity> namedEntities) {
        this.namedEntities = namedEntities;
        return this;
    }
}
