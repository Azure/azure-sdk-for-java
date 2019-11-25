// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.IterableStream;

/**
 * The LinkedEntityResult model.
 */
@Fluent
public final class LinkedEntityResult extends DocumentResult {
    private IterableStream<LinkedEntity> items;

    public IterableStream<LinkedEntity> getItems() {
        return items;
    }

    LinkedEntityResult setItems(IterableStream<LinkedEntity> items) {
        this.items = items;
        return this;
    }
}
