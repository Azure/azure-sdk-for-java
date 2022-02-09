// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link PiiEntityCollection} model.
 */
@Immutable
public final class PiiEntityCollection extends IterableStream<PiiEntity> {
    private final String redactedText;
    private final IterableStream<TextAnalyticsWarning> warnings;

    /**
     * Creates a {@link PiiEntityCollection} model that describes a Personally Identifiable Information entities
     * collection including warnings.
     *
     * @param entities An {@link IterableStream} of {@link PiiEntity Personally Identifiable Information entities}.
     * @param redactedText The text of the input document with all of the PII information redacted out.
     * @param warnings An {@link IterableStream} of {@link TextAnalyticsWarning warnings}.
     */
    public PiiEntityCollection(IterableStream<PiiEntity> entities, String redactedText,
        IterableStream<TextAnalyticsWarning> warnings) {
        super(entities);
        this.redactedText = redactedText;
        this.warnings = warnings;
    }

    /**
     * Get the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return this.warnings;
    }


    /**
     * Get the property redactedText value. The text of the input document with all of the PII information redacted out.
     * Only returned for API version v3.1 and up.
     *
     * @return The text of the input document with all of the PII information redacted out.
     */
    public String getRedactedText() {
        return redactedText;
    }
}
