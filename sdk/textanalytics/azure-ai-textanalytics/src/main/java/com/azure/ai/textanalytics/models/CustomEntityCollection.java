// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 *
 */
@Immutable
public final class CustomEntityCollection extends IterableStream<CustomEntity> {
    private final IterableStream<TextAnalyticsWarning> warnings;

    /**
     * Creates a {@link CustomEntityCollection} model that describes a custom entity collection including
     * warnings.
     *
     * @param entities An {@link IterableStream} of {@link CustomEntity}.
     * @param warnings An {@link IterableStream} of {@link TextAnalyticsWarning warnings}.
     */
    public CustomEntityCollection(IterableStream<CustomEntity> entities,
        IterableStream<TextAnalyticsWarning> warnings) {
        super(entities);
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
}
