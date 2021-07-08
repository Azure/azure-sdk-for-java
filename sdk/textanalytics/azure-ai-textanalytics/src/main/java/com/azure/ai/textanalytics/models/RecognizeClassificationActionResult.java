// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

@Immutable
public final class RecognizeClassificationActionResult extends TextAnalyticsActionResult {

    private String projectName;
    private String modelName;
    private RecognizeClassificationResultCollection documentsResults;

    /**
     *
     * @return
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     *
     * @return
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Gets the custom entities recognition action result.
     *
     * @return the custom entities recognition action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public RecognizeClassificationResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }
}
