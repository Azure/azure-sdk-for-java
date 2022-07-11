// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.MultiLabelClassifyActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.ClassifyDocumentResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link MultiLabelClassifyActionResult} is the result for multi-label classification action analysis.
 */
@Immutable
public final class MultiLabelClassifyActionResult extends TextAnalyticsActionResult {
    private ClassifyDocumentResultCollection documentsResults;

    static {
        MultiLabelClassifyActionResultPropertiesHelper.setAccessor(
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
    public ClassifyDocumentResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(ClassifyDocumentResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
