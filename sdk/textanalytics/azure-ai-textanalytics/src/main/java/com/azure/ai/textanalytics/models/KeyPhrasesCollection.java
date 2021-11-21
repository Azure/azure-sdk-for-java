// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link KeyPhrasesCollection} model.
 */
@Immutable
public final class KeyPhrasesCollection extends IterableStream<String> {
    private final IterableStream<TextAnalyticsWarning> warnings;

    /**
     * Creates a {@link KeyPhrasesCollection} model that describes a key phrases collection including warnings.
     *
     * @param keyPhrases An {@link IterableStream} of key phrases.
     * @param warnings An {@link IterableStream} of {@link TextAnalyticsWarning warnings}.
     */
    public KeyPhrasesCollection(IterableStream<String> keyPhrases, IterableStream<TextAnalyticsWarning> warnings) {
        super(keyPhrases);
        this.warnings = warnings;
    }

    /**
     * Gets the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return this.warnings;
    }
}
