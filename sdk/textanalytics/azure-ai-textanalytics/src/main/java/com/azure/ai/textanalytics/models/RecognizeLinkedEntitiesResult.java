// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link RecognizeLinkedEntitiesResult} model.
 */
@Immutable
public final class RecognizeLinkedEntitiesResult extends TextAnalyticsResult {
    private final LinkedEntityCollection entities;

    /**
     * Creates a {@link RecognizeLinkedEntitiesResult} model that describes recognized linked entities result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param entities A {@link LinkedEntityCollection} contains entities and warnings.
     */
    public RecognizeLinkedEntitiesResult(String id, TextDocumentStatistics textDocumentStatistics,
                                         TextAnalyticsError error, LinkedEntityCollection entities) {
        super(id, textDocumentStatistics, error);
        this.entities = entities;
    }

    /**
     * Gets an {@link IterableStream} of {@link LinkedEntity}.
     *
     * @return An {@link IterableStream} of {@link LinkedEntity}.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public LinkedEntityCollection getEntities() {
        throwExceptionIfError();
        return entities;
    }
}
