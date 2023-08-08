// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.RecognizeEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link RecognizeEntitiesActionResult} model.
 */
@Immutable
public final class RecognizeEntitiesActionResult extends TextAnalyticsActionResult {
    private RecognizeEntitiesResultCollection documentsResults;

    static {
        RecognizeEntitiesActionResultPropertiesHelper.setAccessor(
            (actionResult, documentsResults) -> actionResult.setDocumentsResults(documentsResults));
    }

    /**
     * Gets the entities recognition action result.
     *
     * @return The entities recognition action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public RecognizeEntitiesResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(RecognizeEntitiesResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
