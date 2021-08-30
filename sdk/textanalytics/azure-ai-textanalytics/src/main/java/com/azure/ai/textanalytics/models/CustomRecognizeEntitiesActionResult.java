// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.CustomRecognizeEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.CustomRecognizeEntitiesResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link CustomRecognizeEntitiesActionResult} model.
 */
@Immutable
public final class CustomRecognizeEntitiesActionResult extends TextAnalyticsActionResult {
    private CustomRecognizeEntitiesResultCollection documentsResults;

    static {
        CustomRecognizeEntitiesActionResultPropertiesHelper.setAccessor(
            (actionResult, documentsResults) -> actionResult.setDocumentsResults(documentsResults));
    }

    /**
     * Gets the custom entities recognition action result.
     *
     * @return the custom entities recognition action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public CustomRecognizeEntitiesResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(CustomRecognizeEntitiesResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
