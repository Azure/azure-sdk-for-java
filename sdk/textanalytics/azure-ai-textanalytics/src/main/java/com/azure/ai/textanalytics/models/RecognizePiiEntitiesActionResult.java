// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.RecognizePiiEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;

/**
 * The {@link RecognizePiiEntitiesActionResult} model.
 */
public final class RecognizePiiEntitiesActionResult extends TextAnalyticsActionResult {
    private RecognizePiiEntitiesResultCollection result;

    static {
        RecognizePiiEntitiesActionResultPropertiesHelper.setAccessor(
            (actionsResult, result) -> actionsResult.setResult(result));
    }

    /**
     * Gets the PII entities recognition action result.
     *
     * @return the PII entities recognition action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public RecognizePiiEntitiesResultCollection getResult() {
        throwExceptionIfError();
        return result;
    }

    private void setResult(RecognizePiiEntitiesResultCollection result) {
        this.result = result;
    }
}
