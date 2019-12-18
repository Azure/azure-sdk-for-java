// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The KeyPhraseResult model.
 */
@Immutable
public final class KeyPhraseResult extends DocumentResult {
    private final List<String> keyPhrases;

    /**
     * Create a {@code KeyPhraseResult} model that describes extracted key phrases result
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics text document statistics
     * @param error the document error.
     * @param keyPhrases a list of key phrases string
     */
    public KeyPhraseResult(String id, TextDocumentStatistics textDocumentStatistics, Error error,
        List<String> keyPhrases) {
        super(id, textDocumentStatistics, error);
        this.keyPhrases = keyPhrases;
    }

    /**
     * Get a list of key phrase string
     *
     * @return a list of key phrase string
     */
    public List<String> getKeyPhrases() {
        return keyPhrases;
    }
}
