// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AbstractiveSummaryActionResult;
import com.azure.ai.textanalytics.util.AbstractiveSummaryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link AbstractiveSummaryActionResult} instance.
 */
public final class AbstractiveSummaryActionResultPropertiesHelper {
    private static AbstractiveSummaryActionResultAccessor accessor;

    private AbstractiveSummaryActionResultPropertiesHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link AbstractiveSummaryActionResult}
     * instance.
     */
    public interface AbstractiveSummaryActionResultAccessor {
        void setDocumentsResults(AbstractiveSummaryActionResult actionResult,
            AbstractiveSummaryResultCollection documentsResults);
    }

    /**
     * The method called from {@link AbstractiveSummaryActionResult} to set it's accessor.
     *
     * @param abstractiveSummaryActionResultAccessor The accessor.
     */
    public static void
        setAccessor(final AbstractiveSummaryActionResultAccessor abstractiveSummaryActionResultAccessor) {
        accessor = abstractiveSummaryActionResultAccessor;
    }

    public static void setDocumentsResults(AbstractiveSummaryActionResult actionResult,
        AbstractiveSummaryResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
