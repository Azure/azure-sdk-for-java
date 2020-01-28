// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * The ExtractKeyPhraseResult model.
 */
@Immutable
public final class ExtractKeyPhraseResult extends DocumentResult {
    private final List<String> keyPhrases;
    private final ClientLogger logger = new ClientLogger(ExtractKeyPhraseResult.class);

    /**
     * Create a {@code KeyPhraseResult} model that describes extracted key phrases result
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics text document statistics
     * @param error the document error.
     * @param keyPhrases a list of key phrases string
     */
    public ExtractKeyPhraseResult(String id, TextDocumentStatistics textDocumentStatistics, TextAnalyticsError error,
        List<String> keyPhrases) {
        super(id, textDocumentStatistics, error);
        this.keyPhrases = keyPhrases == null ? new ArrayList<>() : keyPhrases;
    }

    /**
     * Get a list of key phrase string
     *
     * @return a list of key phrase string
     */
    public List<String> getKeyPhrases() {
        throwExceptionIfError(logger);
        return keyPhrases;
    }
}
