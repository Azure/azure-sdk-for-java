// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.IterableStream;
import com.azure.cs.textanalytics.implementation.models.EntitiesResult;

/**
 * The NamedEntityResult model.
 */
@Fluent
public final class NamedEntityResult extends DocumentResult {
    private IterableStream<NamedEntity> items;

    public IterableStream<NamedEntity> getItems() {
        return items;
    }
}
