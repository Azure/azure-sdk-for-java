// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ClassificationCategoryCollectionPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link ClassificationCategoryCollection} includes a list of {@link ClassificationCategory}
 * classifications and the {@link TextAnalyticsWarning} warnings if exist.
 */
@Immutable
public final class ClassificationCategoryCollection extends IterableStream<ClassificationCategory> {
    private IterableStream<TextAnalyticsWarning> warnings;

    static {
        ClassificationCategoryCollectionPropertiesHelper.setAccessor(
            (classifications, warnings) -> classifications.setWarnings(warnings));
    }

    /**
     * Creates a {@link ClassificationCategoryCollection} model that describes a document classification
     * collection including warnings.
     *
     * @param classifications An {@link IterableStream} of {@link ClassificationCategory}.
     */
    public ClassificationCategoryCollection(IterableStream<ClassificationCategory> classifications) {
        super(classifications);
    }

    /**
     * Gets the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
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
