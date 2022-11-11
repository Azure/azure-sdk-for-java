// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AbstractSummaryActionResult;
import com.azure.ai.textanalytics.util.AbstractSummaryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link AbstractSummaryActionResult} instance.
 */
public final class AbstractiveSummaryActionResultPropertiesHelper {
    private static AbstractiveSummaryActionResultAccessor accessor;

    private AbstractiveSummaryActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AbstractSummaryActionResult}
     * instance.
     */
    public interface AbstractiveSummaryActionResultAccessor {
        void setDocumentsResults(AbstractSummaryActionResult actionResult,
                                 AbstractSummaryResultCollection documentsResults);
    }

    /**
     * The method called from {@link AbstractSummaryActionResult} to set it's accessor.
     *
     * @param abstractiveSummaryActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final AbstractiveSummaryActionResultAccessor abstractiveSummaryActionResultAccessor) {
        accessor = abstractiveSummaryActionResultAccessor;
    }

    public static void setDocumentsResults(AbstractSummaryActionResult actionResult,
                                           AbstractSummaryResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
