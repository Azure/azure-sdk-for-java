// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The KeyPhraseResult model.
 */
@Fluent
public final class KeyPhraseResult extends DocumentResult {
    private List<String> keyPhrases;

    public KeyPhraseResult(String id, TextDocumentStatistics textDocumentStatistics, List<String> keyPhrases) {
        super(id, textDocumentStatistics);
        this.keyPhrases = keyPhrases;
    }

    public List<String> getKeyPhrases() {
        return keyPhrases;
    }

    KeyPhraseResult setKeyPhrases(List<String> keyPhrases) {
        this.keyPhrases = keyPhrases;
        return this;
    }
}
