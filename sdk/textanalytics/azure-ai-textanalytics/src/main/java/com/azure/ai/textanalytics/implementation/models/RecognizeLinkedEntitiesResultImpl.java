// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;

/**
 * The {@link RecognizeLinkedEntitiesResultImpl} model.
 */
@Immutable
public final class RecognizeLinkedEntitiesResultImpl extends DocumentResultImpl
    implements RecognizeLinkedEntitiesResult {
    private final IterableStream<LinkedEntity> entities;

    /**
     * Creates a {@link RecognizeLinkedEntitiesResultImpl} model that describes recognized linked entities result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param entities An {@link IterableStream} of {@link LinkedEntity}.
     */
    public RecognizeLinkedEntitiesResultImpl(String id, TextDocumentStatistics textDocumentStatistics,
                                             TextAnalyticsError error, IterableStream<LinkedEntity> entities) {
        super(id, textDocumentStatistics, error);
        this.entities = entities == null ? new IterableStream<>(new ArrayList<>()) : entities;
    }

    /**
     * Get an {@link IterableStream} of {@link LinkedEntity}.
     *
     * @return An {@link IterableStream} of {@link LinkedEntity}.
     */
    public IterableStream<LinkedEntity> getEntities() {
        throwExceptionIfError();
        return entities;
    }
}
