// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.MultiLabelClassificationActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.LabelClassificationResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link MultiLabelClassificationActionResult} is the result for multi-label classification action analysis.
 */
@Immutable
public final class MultiLabelClassificationActionResult extends TextAnalyticsActionResult {
    private LabelClassificationResultCollection documentsResults;

    static {
        MultiLabelClassificationActionResultPropertiesHelper.setAccessor(
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
    public LabelClassificationResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(LabelClassificationResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
