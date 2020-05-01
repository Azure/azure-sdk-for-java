// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;

/**
 * The {@link ExtractKeyPhraseResultImpl} model.
 */
@Immutable
public final class ExtractKeyPhraseResultImpl extends DocumentResultImpl implements ExtractKeyPhraseResult {
    private final IterableStream<String> keyPhrases;

    /**
     * Create a {@link ExtractKeyPhraseResultImpl} model that describes extracted key phrases result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param keyPhrases A list of key phrases string.
     * @param warnings A {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public ExtractKeyPhraseResultImpl(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, IterableStream<String> keyPhrases, IterableStream<TextAnalyticsWarning> warnings) {
        super(id, textDocumentStatistics, error, warnings);
        this.keyPhrases = keyPhrases == null ? new IterableStream<>(new ArrayList<>()) : keyPhrases;
    }

    /**
     * Get a list of key phrase string.
     *
     * @return A list of key phrase string.
     */
    public IterableStream<String> getKeyPhrases() {
        throwExceptionIfError();
        return keyPhrases;
    }
}
