// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.CustomRecognizeEntitiesResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link CustomRecognizeEntitiesResult} model.
 */
@Immutable
public final class CustomRecognizeEntitiesResult extends TextAnalyticsResult {
    private CategorizedEntityCollection entities;

    static {
        CustomRecognizeEntitiesResultPropertiesHelper.setAccessor(
            (actionResult, entities) -> actionResult.setEntities(entities));
    }

    /**
     * Creates a {@link CustomRecognizeEntitiesResult} model that describes recognized custom entities result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public CustomRecognizeEntitiesResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
     * Gets an {@link IterableStream} of {@link CategorizedEntity}.
     *
     * @return An {@link IterableStream} of {@link CategorizedEntity}.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public CategorizedEntityCollection getEntities() {
        throwExceptionIfError();
        return entities;
    }

    private void setEntities(CategorizedEntityCollection entities) {
        this.entities = entities;
    }
}
