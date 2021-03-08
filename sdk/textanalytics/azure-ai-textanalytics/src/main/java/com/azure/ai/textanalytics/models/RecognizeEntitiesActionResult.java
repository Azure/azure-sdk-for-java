// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.RecognizeEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;

/**
 * The {@link RecognizeEntitiesActionResult} model.
 */
public final class RecognizeEntitiesActionResult extends TextAnalyticsActionResult {
    private RecognizeEntitiesResultCollection result;

    static {
        RecognizeEntitiesActionResultPropertiesHelper.setAccessor(
            (actionsResult, result) -> actionsResult.setResult(result));
    }

    /**
     * Gets the entities recognition action result.
     *
     * @return the entities recognition action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public RecognizeEntitiesResultCollection getResult() {
        throwExceptionIfError();
        return result;
    }

    private void setResult(RecognizeEntitiesResultCollection result) {
        this.result = result;
    }
}
