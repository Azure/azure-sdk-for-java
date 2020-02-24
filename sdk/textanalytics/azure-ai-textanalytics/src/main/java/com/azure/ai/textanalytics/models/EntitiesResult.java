// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;

/**
 * The {@link EntitiesResult} model.
 */
@Immutable
public final class EntitiesResult<T> extends DocumentResult {
    private final IterableStream<T> entities;

    /**
     * Creates a {@link EntitiesResult} model that describes recognized entities result.
     *
     * @param id Unique, non-empty document identifier.
     * @param inputText The input text in request.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param entities A list of {@link T}.
     */
    public EntitiesResult(String id, String inputText, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, IterableStream<T> entities) {
        super(id, inputText, textDocumentStatistics, error);
        this.entities = entities == null ? new IterableStream<T>(new ArrayList<>()) : entities;
    }

    /**
     * Get a list of T.
     *
     * @return A list of {@link T}.
     */
    public IterableStream<T> getEntities() {
        throwExceptionIfError();
        return entities;
    }
}
