// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.CustomEntityCollectionPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link CustomEntityCollection} model.
 */
@Immutable
public final class CustomEntityCollection extends IterableStream<CustomEntity> {
    private IterableStream<TextAnalyticsWarning> warnings;

    static {
        CustomEntityCollectionPropertiesHelper.setAccessor(
            (documentClassifications, warnings) -> documentClassifications.setWarnings(warnings));
    }

    /**
     * Creates a {@link CustomEntityCollection} model that describes a custom entity collection including
     * warnings.
     *
     * @param entities An {@link IterableStream} of {@link CustomEntity}.
     */
    public CustomEntityCollection(IterableStream<CustomEntity> entities) {
        super(entities);
    }

    /**
     * Get the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return this.warnings;
    }

    private void setWarnings(IterableStream<TextAnalyticsWarning> warnings) {
        this.warnings = warnings;
    }
}
