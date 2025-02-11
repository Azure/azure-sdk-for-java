// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ExtractiveSummaryActionResult;
import com.azure.ai.textanalytics.util.ExtractiveSummaryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ExtractiveSummaryActionResult} instance.
 */
public final class ExtractiveSummaryActionResultPropertiesHelper {
    private static ExtractiveSummaryActionResultAccessor accessor;

    private ExtractiveSummaryActionResultPropertiesHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link ExtractiveSummaryActionResult}
     * instance.
     */
    public interface ExtractiveSummaryActionResultAccessor {
        void setDocumentsResults(ExtractiveSummaryActionResult actionResult,
            ExtractiveSummaryResultCollection documentsResults);
    }

    /**
     * The method called from {@link ExtractiveSummaryActionResult} to set it's accessor.
     *
     * @param extractiveSummaryActionResultAccessor The accessor.
     */
    public static void setAccessor(final ExtractiveSummaryActionResultAccessor extractiveSummaryActionResultAccessor) {
        accessor = extractiveSummaryActionResultAccessor;
    }

    public static void setDocumentsResults(ExtractiveSummaryActionResult actionResult,
        ExtractiveSummaryResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
