// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link CategorizedEntityCollection} model.
 */
@Immutable
public final class CategorizedEntityCollection extends IterableStream<CategorizedEntity> {
    private final IterableStream<TextAnalyticsWarning> warnings;

    /**
     * Creates a {@link CategorizedEntityCollection} model that describes a entities collection including warnings.
     *
     * @param entities An {@link IterableStream} of {@link CategorizedEntity categorized entities}.
     * @param warnings An {@link IterableStream} of {@link TextAnalyticsWarning warnings}.
     */
    public CategorizedEntityCollection(IterableStream<CategorizedEntity> entities,
                                       IterableStream<TextAnalyticsWarning> warnings) {
        super(entities);
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
