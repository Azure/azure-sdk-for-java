// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;

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
     * @param entities An {@link IterableStream} of {@link LinkedEntity}.
     * @param warnings A {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public RecognizeLinkedEntitiesResult(String id, TextDocumentStatistics textDocumentStatistics,
                                         TextAnalyticsError error, IterableStream<LinkedEntity> entities,
                                         IterableStream<TextAnalyticsWarning> warnings) {
        super(id, textDocumentStatistics, error);
        this.entities = new LinkedEntityCollection(
            entities == null ? new IterableStream<>(new ArrayList<>()) : entities, warnings);
    }

    /**
     * Get an {@link IterableStream} of {@link LinkedEntity}.
     *
     * @return An {@link IterableStream} of {@link LinkedEntity}.
     */
    public LinkedEntityCollection getEntities() {
        throwExceptionIfError();
        return entities;
    }
}
