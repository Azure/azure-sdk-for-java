// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.SingleLabelClassifyActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.ClassifyDocumentResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link SingleLabelClassifyActionResult} model.
 */
@Immutable
public final class SingleLabelClassifyActionResult extends TextAnalyticsActionResult {
    private ClassifyDocumentResultCollection documentsResults;

    static {
        SingleLabelClassifyActionResultPropertiesHelper.setAccessor(
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
