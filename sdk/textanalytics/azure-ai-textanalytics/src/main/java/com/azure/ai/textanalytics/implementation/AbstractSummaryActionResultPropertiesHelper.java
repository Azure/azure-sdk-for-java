// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AbstractSummaryActionResult;
import com.azure.ai.textanalytics.util.AbstractSummaryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link AbstractSummaryActionResult} instance.
 */
public final class AbstractSummaryActionResultPropertiesHelper {
    private static AbstractSummaryActionResultAccessor accessor;

    private AbstractSummaryActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AbstractSummaryActionResult}
     * instance.
     */
    public interface AbstractSummaryActionResultAccessor {
        void setDocumentsResults(AbstractSummaryActionResult actionResult,
            AbstractSummaryResultCollection documentsResults);
    }

    /**
     * The method called from {@link AbstractSummaryActionResult} to set it's accessor.
     *
     * @param abstractSummaryActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final AbstractSummaryActionResultAccessor abstractSummaryActionResultAccessor) {
        accessor = abstractSummaryActionResultAccessor;
    }

    public static void setDocumentsResults(AbstractSummaryActionResult actionResult,
                                           AbstractSummaryResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
