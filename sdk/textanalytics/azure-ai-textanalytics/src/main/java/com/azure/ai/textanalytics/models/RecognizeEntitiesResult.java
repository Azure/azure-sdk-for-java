// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.IterableStream;

/**
 * The {@link RecognizeEntitiesResult} model.
 */
public interface RecognizeEntitiesResult extends TextAnalyticsResult {
    /**
     * Get an {@link IterableStream} of {@link CategorizedEntity}.
     *
     * @return An {@link IterableStream} of {@link CategorizedEntity}.
     */
    IterableStream<CategorizedEntity> getEntities();
}
