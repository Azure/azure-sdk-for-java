// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link ExtractKeyPhraseResult} model.
 */
@Immutable
public final class ExtractKeyPhraseResult extends DocumentResult {
    private final List<String> keyPhrases;

    /**
     * Create a {@link ExtractKeyPhraseResult} model that describes extracted key phrases result.
     *
     * @param id Unique, non-empty document identifier.
     * @param inputText The input text in request.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param keyPhrases A list of key phrases string.
     */
    public ExtractKeyPhraseResult(String id, String inputText, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, List<String> keyPhrases) {
        super(id, inputText, textDocumentStatistics, error);
        this.keyPhrases = keyPhrases == null ? new ArrayList<>() : keyPhrases;
    }

    /**
     * Get a list of key phrase string.
     *
     * @return A list of key phrase string.
     */
    public List<String> getKeyPhrases() {
        throwExceptionIfError();
        return keyPhrases;
    }
}
