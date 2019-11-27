// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The KeyPhraseResult model.
 */
@Fluent
public final class KeyPhraseResult extends DocumentResult {
    private List<KeyPhraseResult> keyPhrases;

    public List<KeyPhraseResult> getKeyPhrases() {
        return keyPhrases;
    }

    KeyPhraseResult setKeyPhrases(List<KeyPhraseResult> keyPhrases) {
        this.keyPhrases = keyPhrases;
        return this;
    }
}
