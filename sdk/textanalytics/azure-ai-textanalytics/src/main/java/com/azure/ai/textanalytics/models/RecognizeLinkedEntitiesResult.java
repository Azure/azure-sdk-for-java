// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.IterableStream;

/**
 * The {@link RecognizeLinkedEntitiesResult} model.
 */
public interface RecognizeLinkedEntitiesResult extends TextAnalyticsResult {
    /**
     * Get an {@link IterableStream} of {@link LinkedEntity}.
     *
     * @return An {@link IterableStream} of {@link LinkedEntity}.
     */
    IterableStream<LinkedEntity> getEntities();
}
