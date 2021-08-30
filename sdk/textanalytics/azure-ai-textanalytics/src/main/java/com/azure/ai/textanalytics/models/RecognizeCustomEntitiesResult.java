// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.RecognizeCustomEntitiesResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link RecognizeCustomEntitiesResult} model.
 */
@Immutable
public final class RecognizeCustomEntitiesResult extends TextAnalyticsResult {
    private CustomEntityCollection entities;

    static {
        RecognizeCustomEntitiesResultPropertiesHelper.setAccessor(
            (actionResult, entities) -> actionResult.setEntities(entities));
    }

    /**
     * Creates a {@link RecognizeCustomEntitiesResult} model that describes recognized custom entities result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public RecognizeCustomEntitiesResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
     * Gets an {@link IterableStream} of {@link CustomEntity}.
     *
     * @return An {@link IterableStream} of {@link CustomEntity}.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public CustomEntityCollection getEntities() {
        throwExceptionIfError();
        return entities;
    }

    private void setEntities(CustomEntityCollection entities) {
        this.entities = entities;
    }
}
