// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.TextAnalyticsOperationResultPropertiesHelper;

/**
 * The TextAnalyticsOperationResult model.
 */
public final class TextAnalyticsOperationResult {
    private String resultId;

    static {
        TextAnalyticsOperationResultPropertiesHelper.setAccessor(
            TextAnalyticsOperationResult::setResultId);
    }

    /**
     * Gets the resultId property of the TextAnalyticsOperationResult.
     *
     * @return the resultId property of the TextAnalyticsOperationResult.
     */
    public String getResultId() {
        return resultId;
    }

    private void setResultId(String resultId) {
        this.resultId = resultId;
    }
}
