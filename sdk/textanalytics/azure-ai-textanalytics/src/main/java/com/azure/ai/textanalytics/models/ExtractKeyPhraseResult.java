// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.IterableStream;

/**
 * The {@link ExtractKeyPhraseResult} model.
 */
public interface ExtractKeyPhraseResult extends DocumentResult {
    /**
     * Get a list of key phrase string.
     *
     * @return A list of key phrase string.
     */
    IterableStream<String> getKeyPhrases();
}
