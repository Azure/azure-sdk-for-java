// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ExtractSummaryActionResult;
import com.azure.ai.textanalytics.util.ExtractSummaryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ExtractSummaryActionResult} instance.
 */
public final class ExtractSummaryActionResultPropertiesHelper {
    private static ExtractSummaryActionResultAccessor accessor;

    private ExtractSummaryActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ExtractSummaryActionResult}
     * instance.
     */
    public interface ExtractSummaryActionResultAccessor {
        void setDocumentsResults(ExtractSummaryActionResult actionResult,
            ExtractSummaryResultCollection documentsResults);
    }

    /**
     * The method called from {@link ExtractSummaryActionResult} to set it's accessor.
     *
     * @param extractSummaryActionResultAccessor The accessor.
     */
    public static void setAccessor(final ExtractSummaryActionResultAccessor extractSummaryActionResultAccessor) {
        accessor = extractSummaryActionResultAccessor;
    }

    public static void setDocumentsResults(ExtractSummaryActionResult actionResult,
        ExtractSummaryResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
