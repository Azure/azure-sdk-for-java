// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.IterableStream;

/**
 * The KeyPhraseResult model.
 */
@Fluent
public final class KeyPhraseResult extends DocumentResult {
    private IterableStream<KeyPhraseResult> items;

    public IterableStream<KeyPhraseResult> getItems() {
        return items;
    }

    KeyPhraseResult setItems(IterableStream<KeyPhraseResult> items) {
        this.items = items;
        return this;
    }
}
